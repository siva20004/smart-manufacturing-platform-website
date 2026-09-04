package com.sivamachineworks.platform.bom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.bom.domain.BomMapping;
import com.sivamachineworks.platform.bom.domain.EbomHeader;
import com.sivamachineworks.platform.bom.domain.EbomItem;
import com.sivamachineworks.platform.bom.domain.MbomHeader;
import com.sivamachineworks.platform.bom.domain.MbomItem;
import com.sivamachineworks.platform.bom.dto.*;
import com.sivamachineworks.platform.bom.repository.BomMappingRepository;
import com.sivamachineworks.platform.bom.repository.EbomHeaderRepository;
import com.sivamachineworks.platform.bom.repository.EbomItemRepository;
import com.sivamachineworks.platform.bom.repository.MbomHeaderRepository;
import com.sivamachineworks.platform.bom.repository.MbomItemRepository;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MbomTransformationService {

    private final EbomHeaderRepository ebomHeaderRepository;
    private final EbomItemRepository ebomItemRepository;
    private final MbomHeaderRepository mbomHeaderRepository;
    private final MbomItemRepository mbomItemRepository;
    private final BomMappingRepository bomMappingRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public MbomTransformationService(EbomHeaderRepository ebomHeaderRepository,
                                    EbomItemRepository ebomItemRepository,
                                    MbomHeaderRepository mbomHeaderRepository,
                                    MbomItemRepository mbomItemRepository,
                                    BomMappingRepository bomMappingRepository,
                                    AuditLogService auditLogService,
                                    ObjectMapper objectMapper) {
        this.ebomHeaderRepository = ebomHeaderRepository;
        this.ebomItemRepository = ebomItemRepository;
        this.mbomHeaderRepository = mbomHeaderRepository;
        this.mbomItemRepository = mbomItemRepository;
        this.bomMappingRepository = bomMappingRepository;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TransformEbomResponse transform(TransformEbomRequest req, UUID userId, String ipAddress) {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 1. Select and validate source eBOM
        EbomHeader ebomHeader = ebomHeaderRepository.findById(req.ebomHeaderId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Source eBOM not found: " + req.ebomHeaderId()));

        if (!"RELEASED".equals(ebomHeader.getStatus()) && !"APPROVED".equals(ebomHeader.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "eBOM must be APPROVED or RELEASED before transformation. Current status: " + ebomHeader.getStatus());
        }

        // 2. Validate target revision uniqueness for plant
        if (mbomHeaderRepository.existsByProductIdAndRevisionCodeAndPlantLocation(
                ebomHeader.getProduct().getId(), req.mbomRevisionCode(), req.plantLocation())) {
            throw new BaseException(ErrorCode.CONFLICT, "mBOM revision " + req.mbomRevisionCode() + " already exists for plant " + req.plantLocation());
        }

        // 3. Create independent mBOM Header
        MbomHeader mbomHeader = new MbomHeader();
        mbomHeader.setEbomHeader(ebomHeader);
        mbomHeader.setProduct(ebomHeader.getProduct());
        mbomHeader.setRevisionCode(req.mbomRevisionCode());
        mbomHeader.setDescription(req.description() != null ? req.description() : "mBOM for " + req.plantLocation() + " generated from eBOM Rev " + ebomHeader.getRevisionCode());
        mbomHeader.setPlantLocation(req.plantLocation());
        mbomHeader.setStatus("DRAFT");

        MbomHeader savedMbom = mbomHeaderRepository.save(mbomHeader);

        List<EbomItem> allEbomItems = ebomItemRepository.findByEbomHeaderId(ebomHeader.getId());
        Map<UUID, EbomItem> ebomItemMap = allEbomItems.stream().collect(Collectors.toMap(EbomItem::getId, i -> i));
        Set<UUID> mappedEbomItemIds = new HashSet<>();
        List<BomItemMappingDto> mappings = new ArrayList<>();

        // Root Manufacturing Final Assembly Node
        MbomItem finalAssemblyNode = new MbomItem();
        finalAssemblyNode.setMbomHeader(savedMbom);
        finalAssemblyNode.setItemSeq(10);
        finalAssemblyNode.setPartNumber("ASY-FINAL-" + ebomHeader.getProduct().getProductNumber());
        finalAssemblyNode.setDescription("Final Machine Assembly (" + req.plantLocation() + " Plant)");
        finalAssemblyNode.setItemType("ASSEMBLY");
        finalAssemblyNode.setQuantity(BigDecimal.ONE);
        finalAssemblyNode.setUom("EA");
        finalAssemblyNode.setWorkCenter("WC-FINAL-01");
        finalAssemblyNode.setOperationSeq(100);
        finalAssemblyNode.setOperationName("Final Integration & Power-On Test");
        finalAssemblyNode.setIsConsumedPerUnit(true);
        MbomItem savedFinalAssembly = mbomItemRepository.save(finalAssemblyNode);

        Map<String, MbomItem> assemblyNodeLookup = new HashMap<>();
        assemblyNodeLookup.put(savedFinalAssembly.getPartNumber(), savedFinalAssembly);

        // 4. Custom Assembly Mappings Workflow
        if (req.assemblyMappings() != null && !req.assemblyMappings().isEmpty()) {
            int seq = 10;
            for (AssemblyMappingDto asmDto : req.assemblyMappings()) {
                MbomItem asmNode = new MbomItem();
                asmNode.setMbomHeader(savedMbom);
                asmNode.setParentItem(savedFinalAssembly);
                asmNode.setItemSeq(seq);
                asmNode.setPartNumber(asmDto.assemblyPartNumber());
                asmNode.setDescription(asmDto.assemblyName());
                asmNode.setItemType("ASSEMBLY");
                asmNode.setQuantity(BigDecimal.ONE);
                asmNode.setUom("EA");
                asmNode.setWorkCenter(asmDto.workCenter());
                asmNode.setOperationSeq(asmDto.operationSeq());
                asmNode.setOperationName(asmDto.operationName());
                MbomItem savedAsm = mbomItemRepository.save(asmNode);
                assemblyNodeLookup.put(savedAsm.getPartNumber(), savedAsm);

                int childSeq = 1;
                if (asmDto.mappedEbomItemIds() != null) {
                    for (UUID ebomItemId : asmDto.mappedEbomItemIds()) {
                        EbomItem ebomItem = ebomItemMap.get(ebomItemId);
                        if (ebomItem == null) {
                            errors.add("Invalid eBOM item ID in mapping: " + ebomItemId);
                            continue;
                        }

                        MbomItem mItem = new MbomItem();
                        mItem.setMbomHeader(savedMbom);
                        mItem.setParentItem(savedAsm);
                        mItem.setSourceEbomItem(ebomItem);
                        mItem.setItemSeq(childSeq++);
                        mItem.setPartNumber(ebomItem.getPartNumber());
                        mItem.setDescription(ebomItem.getDescription());
                        mItem.setItemType(ebomItem.getItemType());
                        mItem.setQuantity(ebomItem.getQuantity());
                        mItem.setUom(ebomItem.getUom());
                        mItem.setWorkCenter(asmDto.workCenter());
                        mItem.setOperationSeq(asmDto.operationSeq());
                        mItem.setOperationName(asmDto.operationName());
                        mItem.setNotes(ebomItem.getNotes());

                        MbomItem savedItem = mbomItemRepository.save(mItem);
                        mappedEbomItemIds.add(ebomItem.getId());

                        mappings.add(new BomItemMappingDto(
                                ebomItem.getId(),
                                ebomItem.getPartNumber(),
                                savedItem.getId(),
                                savedItem.getPartNumber(),
                                savedItem.getQuantity(),
                                savedItem.getWorkCenter(),
                                savedItem.getOperationSeq(),
                                savedItem.getOperationName()
                        ));
                    }
                }
                seq += 10;
            }
        } else {
            // Automatic standard transformation: Map eBOM tree under Final Assembly
            Map<UUID, MbomItem> ebomToMbomItemMap = new HashMap<>();

            for (EbomItem ebomItem : allEbomItems) {
                MbomItem mItem = new MbomItem();
                mItem.setMbomHeader(savedMbom);
                mItem.setSourceEbomItem(ebomItem);
                mItem.setItemSeq(ebomItem.getItemSeq());
                mItem.setPartNumber(ebomItem.getPartNumber());
                mItem.setDescription(ebomItem.getDescription());
                mItem.setItemType(ebomItem.getItemType());
                mItem.setQuantity(ebomItem.getQuantity());
                mItem.setUom(ebomItem.getUom());
                mItem.setWorkCenter(determineWorkCenter(ebomItem.getPartNumber(), req.plantLocation()));
                mItem.setOperationSeq(determineOperationSeq(ebomItem.getPartNumber()));
                mItem.setOperationName(determineOperationName(ebomItem.getPartNumber()));
                mItem.setNotes(ebomItem.getNotes());

                MbomItem savedItem = mbomItemRepository.save(mItem);
                ebomToMbomItemMap.put(ebomItem.getId(), savedItem);
                mappedEbomItemIds.add(ebomItem.getId());

                mappings.add(new BomItemMappingDto(
                        ebomItem.getId(),
                        ebomItem.getPartNumber(),
                        savedItem.getId(),
                        savedItem.getPartNumber(),
                        savedItem.getQuantity(),
                        savedItem.getWorkCenter(),
                        savedItem.getOperationSeq(),
                        savedItem.getOperationName()
                ));
            }

            // Wire hierarchy
            for (EbomItem ebomItem : allEbomItems) {
                MbomItem mItem = ebomToMbomItemMap.get(ebomItem.getId());
                if (ebomItem.getParentItem() == null) {
                    mItem.setParentItem(savedFinalAssembly);
                } else {
                    mItem.setParentItem(ebomToMbomItemMap.get(ebomItem.getParentItem().getId()));
                }
                mbomItemRepository.save(mItem);
            }
        }

        // 5. Ingest Manufacturing-Specific Additions (Consumables, Packaging, Fasteners)
        if (req.manufacturingAdditions() != null && !req.manufacturingAdditions().isEmpty()) {
            for (ManufacturingAdditionDto addDto : req.manufacturingAdditions()) {
                MbomItem parentNode = addDto.parentAssemblyPartNumber() != null ?
                        assemblyNodeLookup.getOrDefault(addDto.parentAssemblyPartNumber(), savedFinalAssembly) :
                        savedFinalAssembly;

                MbomItem additionItem = new MbomItem();
                additionItem.setMbomHeader(savedMbom);
                additionItem.setParentItem(parentNode);
                additionItem.setSourceEbomItem(null); // Manufacturing addition has no engineering source
                additionItem.setItemSeq(addDto.itemSeq() != null ? addDto.itemSeq() : 99);
                additionItem.setPartNumber(addDto.partNumber());
                additionItem.setDescription(addDto.description());
                additionItem.setItemType(addDto.itemType() != null ? addDto.itemType() : "CONSUMABLE");
                additionItem.setQuantity(addDto.quantity() != null ? addDto.quantity() : BigDecimal.ONE);
                additionItem.setUom(addDto.uom() != null ? addDto.uom() : "EA");
                additionItem.setWorkCenter(addDto.workCenter() != null ? addDto.workCenter() : parentNode.getWorkCenter());
                additionItem.setOperationSeq(addDto.operationSeq() != null ? addDto.operationSeq() : parentNode.getOperationSeq());
                additionItem.setNotes(addDto.notes());

                mbomItemRepository.save(additionItem);
            }
        }

        // 6. Check unmapped items warning
        for (EbomItem item : allEbomItems) {
            if (!mappedEbomItemIds.contains(item.getId())) {
                warnings.add("eBOM item " + item.getPartNumber() + " (" + item.getDescription() + ") was not explicitly mapped.");
            }
        }

        // 7. Save BOM Mapping Record
        BomMapping mappingRecord = new BomMapping();
        mappingRecord.setEbomHeader(ebomHeader);
        mappingRecord.setMbomHeader(savedMbom);
        mappingRecord.setTransformationStatus(errors.isEmpty() ? "COMPLETED" : "FAILED");
        try {
            mappingRecord.setRulesApplied(objectMapper.writeValueAsString(mappings));
        } catch (Exception ignored) {}
        mappingRecord.setTransformedBy(userId);
        bomMappingRepository.save(mappingRecord);

        // 8. Audit Logging
        auditLogService.log("MbomHeader", savedMbom.getId(), "EBOM_MBOM_TRANSFORMATION",
                "Transformed eBOM " + ebomHeader.getProduct().getProductNumber() + " Rev " + ebomHeader.getRevisionCode() +
                        " to mBOM Rev " + savedMbom.getRevisionCode() + " for plant " + req.plantLocation() +
                        " (" + mappings.size() + " items mapped)", userId, ipAddress);

        SourceEbomDto sourceEbomDto = new SourceEbomDto(
                ebomHeader.getId(),
                ebomHeader.getProduct().getProductNumber(),
                ebomHeader.getProduct().getName(),
                ebomHeader.getRevisionCode(),
                ebomHeader.getStatus()
        );

        MbomResponse mbomResponse = new MbomResponse(
                savedMbom.getId(),
                ebomHeader.getId(),
                ebomHeader.getProduct().getId(),
                ebomHeader.getProduct().getProductNumber(),
                ebomHeader.getProduct().getName(),
                savedMbom.getRevisionCode(),
                savedMbom.getDescription(),
                savedMbom.getPlantLocation(),
                savedMbom.getStatus(),
                savedMbom.getApprovedBy(),
                savedMbom.getApprovedAt(),
                savedMbom.getCreatedAt(),
                savedMbom.getUpdatedAt()
        );

        return new TransformEbomResponse(
                sourceEbomDto,
                ebomHeader.getRevisionCode(),
                mbomResponse,
                mappings,
                warnings,
                errors
        );
    }

    private String determineWorkCenter(String partNumber, String plant) {
        if (partNumber.contains("HYD") || partNumber.contains("PUMP") || partNumber.contains("VLV")) return "WC-HYD-01";
        if (partNumber.contains("ELEC") || partNumber.contains("PLC") || partNumber.contains("MTR") || partNumber.contains("SEN")) return "WC-ELEC-01";
        if (partNumber.contains("FRAME") || partNumber.contains("COL")) return "WC-FAB-01";
        return "WC-FINAL-01";
    }

    private Integer determineOperationSeq(String partNumber) {
        if (partNumber.contains("FRAME") || partNumber.contains("COL")) return 10;
        if (partNumber.contains("HYD") || partNumber.contains("PUMP") || partNumber.contains("VLV")) return 20;
        if (partNumber.contains("ELEC") || partNumber.contains("PLC") || partNumber.contains("MTR") || partNumber.contains("SEN")) return 30;
        return 100;
    }

    private String determineOperationName(String partNumber) {
        if (partNumber.contains("FRAME") || partNumber.contains("COL")) return "Structure Fabrication & Alignment";
        if (partNumber.contains("HYD") || partNumber.contains("PUMP") || partNumber.contains("VLV")) return "Hydraulic Subsystem Assembly & Pressure Test";
        if (partNumber.contains("ELEC") || partNumber.contains("PLC") || partNumber.contains("MTR") || partNumber.contains("SEN")) return "Electrical Harness & Drive Setup";
        return "Final Machine Assembly & Inspection";
    }
}

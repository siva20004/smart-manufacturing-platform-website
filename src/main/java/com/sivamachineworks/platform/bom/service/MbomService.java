package com.sivamachineworks.platform.bom.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.bom.domain.MbomHeader;
import com.sivamachineworks.platform.bom.domain.MbomItem;
import com.sivamachineworks.platform.bom.dto.*;
import com.sivamachineworks.platform.bom.repository.MbomHeaderRepository;
import com.sivamachineworks.platform.bom.repository.MbomItemRepository;
import com.sivamachineworks.platform.shared.dto.PaginatedResponse;
import com.sivamachineworks.platform.shared.dto.PaginationMeta;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MbomService {

    private final MbomHeaderRepository mbomHeaderRepository;
    private final MbomItemRepository mbomItemRepository;
    private final AuditLogService auditLogService;

    public MbomService(MbomHeaderRepository mbomHeaderRepository,
                       MbomItemRepository mbomItemRepository,
                       AuditLogService auditLogService) {
        this.mbomHeaderRepository = mbomHeaderRepository;
        this.mbomItemRepository = mbomItemRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public MbomResponse getMbomById(UUID id) {
        MbomHeader header = mbomHeaderRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "mBOM not found: " + id));
        return mapToResponse(header);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<MbomResponse> listMboms(UUID productId, String plantLocation, Pageable pageable) {
        Page<MbomHeader> page;
        if (productId != null) {
            page = mbomHeaderRepository.findByProductId(productId, pageable);
        } else if (plantLocation != null) {
            page = mbomHeaderRepository.findByPlantLocation(plantLocation, pageable);
        } else {
            page = mbomHeaderRepository.findAll(pageable);
        }

        List<MbomResponse> dtos = page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        PaginationMeta meta = new PaginationMeta(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());

        return new PaginatedResponse<>(dtos, meta);
    }

    @Transactional(readOnly = true)
    public List<MbomTreeNodeDto> getMbomTree(UUID mbomId) {
        if (!mbomHeaderRepository.existsById(mbomId)) {
            throw new BaseException(ErrorCode.NOT_FOUND, "mBOM not found: " + mbomId);
        }

        List<MbomItem> allItems = mbomItemRepository.findByMbomHeaderId(mbomId);
        Map<UUID, List<MbomItem>> parentChildMap = new HashMap<>();

        for (MbomItem item : allItems) {
            UUID parentId = item.getParentItem() != null ? item.getParentItem().getId() : null;
            parentChildMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(item);
        }

        return buildTreeNodes(null, parentChildMap);
    }

    private List<MbomTreeNodeDto> buildTreeNodes(UUID parentId, Map<UUID, List<MbomItem>> map) {
        List<MbomItem> children = map.getOrDefault(parentId, Collections.emptyList());
        children.sort(Comparator.comparing(MbomItem::getItemSeq));

        return children.stream().map(item -> new MbomTreeNodeDto(
                item.getId(),
                parentId,
                item.getSourceEbomItem() != null ? item.getSourceEbomItem().getId() : null,
                item.getItemSeq(),
                item.getPartNumber(),
                item.getDescription(),
                item.getItemType(),
                item.getQuantity(),
                item.getUom(),
                item.getWorkCenter(),
                item.getOperationSeq(),
                item.getOperationName(),
                item.getIsConsumedPerUnit(),
                item.getScrapFactor(),
                item.getNotes(),
                buildTreeNodes(item.getId(), map)
        )).collect(Collectors.toList());
    }

    @Transactional
    public MbomResponse approveMbom(UUID mbomId, ApproveMbomRequest req, UUID approverId, String ipAddress) {
        MbomHeader header = mbomHeaderRepository.findById(mbomId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "mBOM not found: " + mbomId));

        header.setStatus("RELEASED");
        header.setApprovedBy(approverId);
        header.setApprovedAt(Instant.now());

        MbomHeader saved = mbomHeaderRepository.save(header);
        String notes = req != null && req.notes() != null ? req.notes() : "Approved";
        auditLogService.log("MbomHeader", saved.getId(), "MBOM_APPROVED", "Approved mBOM " + header.getId() + " (Rev " + header.getRevisionCode() + " for " + header.getPlantLocation() + "). Notes: " + notes, approverId, ipAddress);

        return mapToResponse(saved);
    }

    private MbomResponse mapToResponse(MbomHeader h) {
        return new MbomResponse(
                h.getId(),
                h.getEbomHeader().getId(),
                h.getProduct().getId(),
                h.getProduct().getProductNumber(),
                h.getProduct().getName(),
                h.getRevisionCode(),
                h.getDescription(),
                h.getPlantLocation(),
                h.getStatus(),
                h.getApprovedBy(),
                h.getApprovedAt(),
                h.getCreatedAt(),
                h.getUpdatedAt()
        );
    }
}

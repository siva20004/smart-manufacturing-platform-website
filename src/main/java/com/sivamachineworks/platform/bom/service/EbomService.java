package com.sivamachineworks.platform.bom.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.bom.domain.EbomHeader;
import com.sivamachineworks.platform.bom.domain.EbomItem;
import com.sivamachineworks.platform.bom.dto.*;
import com.sivamachineworks.platform.bom.repository.EbomHeaderRepository;
import com.sivamachineworks.platform.bom.repository.EbomItemRepository;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.pdm.repository.ProductRepository;
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
public class EbomService {

    private final EbomHeaderRepository ebomHeaderRepository;
    private final EbomItemRepository ebomItemRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    public EbomService(EbomHeaderRepository ebomHeaderRepository,
                       EbomItemRepository ebomItemRepository,
                       ProductRepository productRepository,
                       AuditLogService auditLogService) {
        this.ebomHeaderRepository = ebomHeaderRepository;
        this.ebomItemRepository = ebomItemRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public EbomResponse createEbom(CreateEbomRequest req, UUID userId, String ipAddress) {
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Product not found: " + req.productId()));

        if (ebomHeaderRepository.existsByProductIdAndRevisionCode(req.productId(), req.revisionCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "eBOM already exists for product " + product.getProductNumber() + " revision " + req.revisionCode());
        }

        EbomHeader header = new EbomHeader();
        header.setProduct(product);
        header.setRevisionCode(req.revisionCode());
        header.setDescription(req.description());
        header.setStatus("DRAFT");

        EbomHeader saved = ebomHeaderRepository.save(header);
        auditLogService.log("EbomHeader", saved.getId(), "EBOM_CREATION", "Created eBOM for product " + product.getProductNumber() + " Rev " + saved.getRevisionCode(), userId, ipAddress);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public EbomResponse getEbomById(UUID id) {
        EbomHeader header = ebomHeaderRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "eBOM not found: " + id));
        return mapToResponse(header);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<EbomResponse> listEboms(UUID productId, Pageable pageable) {
        Page<EbomHeader> page = productId != null ?
                ebomHeaderRepository.findByProductId(productId, pageable) :
                ebomHeaderRepository.findAll(pageable);

        List<EbomResponse> dtos = page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        PaginationMeta meta = new PaginationMeta(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());

        return new PaginatedResponse<>(dtos, meta);
    }

    @Transactional(readOnly = true)
    public List<EbomTreeNodeDto> getEbomTree(UUID ebomId) {
        if (!ebomHeaderRepository.existsById(ebomId)) {
            throw new BaseException(ErrorCode.NOT_FOUND, "eBOM not found: " + ebomId);
        }

        List<EbomItem> allItems = ebomItemRepository.findByEbomHeaderId(ebomId);
        Map<UUID, List<EbomItem>> parentChildMap = new HashMap<>();

        for (EbomItem item : allItems) {
            UUID parentId = item.getParentItem() != null ? item.getParentItem().getId() : null;
            parentChildMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(item);
        }

        return buildTreeNodes(null, parentChildMap);
    }

    private List<EbomTreeNodeDto> buildTreeNodes(UUID parentId, Map<UUID, List<EbomItem>> map) {
        List<EbomItem> children = map.getOrDefault(parentId, Collections.emptyList());
        children.sort(Comparator.comparing(EbomItem::getItemSeq));

        return children.stream().map(item -> new EbomTreeNodeDto(
                item.getId(),
                parentId,
                item.getItemSeq(),
                item.getPartNumber(),
                item.getDescription(),
                item.getItemType(),
                item.getQuantity(),
                item.getUom(),
                item.getDocumentId(),
                item.getEffectiveStartDate(),
                item.getEffectiveEndDate(),
                item.getNotes(),
                buildTreeNodes(item.getId(), map)
        )).collect(Collectors.toList());
    }

    @Transactional
    public EbomItemResponse addItem(UUID ebomId, CreateEbomItemRequest req, UUID userId, String ipAddress) {
        EbomHeader header = ebomHeaderRepository.findById(ebomId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "eBOM not found: " + ebomId));

        if (!"DRAFT".equals(header.getStatus()) && !"UNDER_REVIEW".equals(header.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot modify items on eBOM with status: " + header.getStatus());
        }

        EbomItem parent = null;
        if (req.parentItemId() != null) {
            parent = ebomItemRepository.findById(req.parentItemId())
                    .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Parent BOM item not found: " + req.parentItemId()));
            if (!parent.getEbomHeader().getId().equals(ebomId)) {
                throw new BaseException(ErrorCode.BAD_REQUEST, "Parent item does not belong to the same eBOM");
            }
        }

        EbomItem item = new EbomItem();
        item.setEbomHeader(header);
        item.setParentItem(parent);
        item.setItemSeq(req.itemSeq());
        item.setPartNumber(req.partNumber());
        item.setDescription(req.description());
        item.setItemType(req.itemType() != null ? req.itemType() : "COMPONENT");
        item.setQuantity(req.quantity());
        item.setUom(req.uom() != null ? req.uom() : "EA");
        item.setDocumentId(req.documentId());
        item.setEffectiveStartDate(req.effectiveStartDate());
        item.setEffectiveEndDate(req.effectiveEndDate());
        item.setNotes(req.notes());

        EbomItem saved = ebomItemRepository.save(item);
        auditLogService.log("EbomItem", saved.getId(), "EBOM_ITEM_ADDED", "Added item " + saved.getPartNumber() + " (Seq " + saved.getItemSeq() + ") to eBOM " + header.getId(), userId, ipAddress);

        return mapToItemResponse(saved);
    }

    @Transactional
    public EbomItemResponse updateItem(UUID ebomId, UUID itemId, UpdateEbomItemRequest req, UUID userId, String ipAddress) {
        EbomHeader header = ebomHeaderRepository.findById(ebomId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "eBOM not found: " + ebomId));

        if (!"DRAFT".equals(header.getStatus()) && !"UNDER_REVIEW".equals(header.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot modify items on eBOM with status: " + header.getStatus());
        }

        EbomItem item = ebomItemRepository.findById(itemId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "BOM item not found: " + itemId));

        if (!item.getEbomHeader().getId().equals(ebomId)) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Item does not belong to specified eBOM");
        }

        if (req.parentItemId() != null) {
            if (req.parentItemId().equals(itemId)) {
                throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Circular BOM error: An item cannot be its own parent");
            }
            // Cycle detection: parent cannot be one of item's descendants
            if (isDescendant(itemId, req.parentItemId())) {
                throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Circular BOM error: Target parent is a descendant of this item");
            }

            EbomItem newParent = ebomItemRepository.findById(req.parentItemId())
                    .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Target parent item not found: " + req.parentItemId()));
            item.setParentItem(newParent);
        }

        if (req.itemSeq() != null) item.setItemSeq(req.itemSeq());
        if (req.partNumber() != null) item.setPartNumber(req.partNumber());
        if (req.description() != null) item.setDescription(req.description());
        if (req.itemType() != null) item.setItemType(req.itemType());
        if (req.quantity() != null) item.setQuantity(req.quantity());
        if (req.uom() != null) item.setUom(req.uom());
        if (req.documentId() != null) item.setDocumentId(req.documentId());
        if (req.effectiveStartDate() != null) item.setEffectiveStartDate(req.effectiveStartDate());
        if (req.effectiveEndDate() != null) item.setEffectiveEndDate(req.effectiveEndDate());
        if (req.notes() != null) item.setNotes(req.notes());

        EbomItem saved = ebomItemRepository.save(item);
        auditLogService.log("EbomItem", saved.getId(), "EBOM_ITEM_UPDATED", "Updated item " + saved.getPartNumber() + " on eBOM " + header.getId(), userId, ipAddress);

        return mapToItemResponse(saved);
    }

    private boolean isDescendant(UUID ancestorId, UUID targetId) {
        List<EbomItem> children = ebomItemRepository.findByParentItemId(ancestorId);
        for (EbomItem child : children) {
            if (child.getId().equals(targetId)) return true;
            if (isDescendant(child.getId(), targetId)) return true;
        }
        return false;
    }

    @Transactional
    public void removeItem(UUID ebomId, UUID itemId, UUID userId, String ipAddress) {
        EbomHeader header = ebomHeaderRepository.findById(ebomId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "eBOM not found: " + ebomId));

        if (!"DRAFT".equals(header.getStatus()) && !"UNDER_REVIEW".equals(header.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot delete items from eBOM with status: " + header.getStatus());
        }

        EbomItem item = ebomItemRepository.findById(itemId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "BOM item not found: " + itemId));

        if (!item.getEbomHeader().getId().equals(ebomId)) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Item does not belong to specified eBOM");
        }

        ebomItemRepository.delete(item);
        auditLogService.log("EbomItem", itemId, "EBOM_ITEM_REMOVED", "Removed item " + item.getPartNumber() + " from eBOM " + header.getId(), userId, ipAddress);
    }

    @Transactional
    public EbomResponse createRevision(UUID ebomId, CreateEbomRevisionRequest req, UUID userId, String ipAddress) {
        EbomHeader sourceHeader = ebomHeaderRepository.findById(ebomId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Source eBOM not found: " + ebomId));

        if (ebomHeaderRepository.existsByProductIdAndRevisionCode(sourceHeader.getProduct().getId(), req.revisionCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "Revision " + req.revisionCode() + " already exists for product " + sourceHeader.getProduct().getProductNumber());
        }

        EbomHeader newHeader = new EbomHeader();
        newHeader.setProduct(sourceHeader.getProduct());
        newHeader.setRevisionCode(req.revisionCode());
        newHeader.setDescription(req.description() != null ? req.description() : "Revision " + req.revisionCode() + " cloned from Rev " + sourceHeader.getRevisionCode());
        newHeader.setStatus("DRAFT");

        EbomHeader savedHeader = ebomHeaderRepository.save(newHeader);

        // Clone items hierarchy
        List<EbomItem> sourceItems = ebomItemRepository.findByEbomHeaderId(ebomId);
        Map<UUID, EbomItem> oldToNewMap = new HashMap<>();

        // First pass: create all items
        for (EbomItem src : sourceItems) {
            EbomItem clone = new EbomItem();
            clone.setEbomHeader(savedHeader);
            clone.setItemSeq(src.getItemSeq());
            clone.setPartNumber(src.getPartNumber());
            clone.setDescription(src.getDescription());
            clone.setItemType(src.getItemType());
            clone.setQuantity(src.getQuantity());
            clone.setUom(src.getUom());
            clone.setDocumentId(src.getDocumentId());
            clone.setEffectiveStartDate(src.getEffectiveStartDate());
            clone.setEffectiveEndDate(src.getEffectiveEndDate());
            clone.setNotes(src.getNotes());
            EbomItem savedClone = ebomItemRepository.save(clone);
            oldToNewMap.put(src.getId(), savedClone);
        }

        // Second pass: wire parents
        for (EbomItem src : sourceItems) {
            if (src.getParentItem() != null) {
                EbomItem clonedItem = oldToNewMap.get(src.getId());
                EbomItem clonedParent = oldToNewMap.get(src.getParentItem().getId());
                clonedItem.setParentItem(clonedParent);
                ebomItemRepository.save(clonedItem);
            }
        }

        auditLogService.log("EbomHeader", savedHeader.getId(), "EBOM_REVISION_CREATION", "Created eBOM revision " + savedHeader.getRevisionCode() + " from Rev " + sourceHeader.getRevisionCode(), userId, ipAddress);

        return mapToResponse(savedHeader);
    }

    @Transactional
    public EbomResponse submitForApproval(UUID ebomId, UUID userId, String ipAddress) {
        EbomHeader header = ebomHeaderRepository.findById(ebomId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "eBOM not found: " + ebomId));

        if (!"DRAFT".equals(header.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Can only submit eBOMs in DRAFT status. Current: " + header.getStatus());
        }

        header.setStatus("UNDER_REVIEW");
        EbomHeader saved = ebomHeaderRepository.save(header);
        auditLogService.log("EbomHeader", saved.getId(), "EBOM_SUBMITTED", "Submitted eBOM " + header.getId() + " (Rev " + header.getRevisionCode() + ") for review", userId, ipAddress);

        return mapToResponse(saved);
    }

    @Transactional
    public EbomResponse approveEbom(UUID ebomId, ApproveEbomRequest req, UUID approverId, String ipAddress) {
        EbomHeader header = ebomHeaderRepository.findById(ebomId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "eBOM not found: " + ebomId));

        header.setStatus("RELEASED");
        header.setApprovedBy(approverId);
        header.setApprovedAt(Instant.now());

        EbomHeader saved = ebomHeaderRepository.save(header);
        String notes = req != null && req.notes() != null ? req.notes() : "Approved";
        auditLogService.log("EbomHeader", saved.getId(), "EBOM_APPROVED", "Approved eBOM " + header.getId() + " (Rev " + header.getRevisionCode() + "). Notes: " + notes, approverId, ipAddress);

        return mapToResponse(saved);
    }

    private EbomResponse mapToResponse(EbomHeader h) {
        return new EbomResponse(
                h.getId(),
                h.getProduct().getId(),
                h.getProduct().getProductNumber(),
                h.getProduct().getName(),
                h.getRevisionCode(),
                h.getDescription(),
                h.getStatus(),
                h.getApprovedBy(),
                h.getApprovedAt(),
                h.getCreatedAt(),
                h.getUpdatedAt()
        );
    }

    private EbomItemResponse mapToItemResponse(EbomItem i) {
        return new EbomItemResponse(
                i.getId(),
                i.getEbomHeader().getId(),
                i.getParentItem() != null ? i.getParentItem().getId() : null,
                i.getItemSeq(),
                i.getPartNumber(),
                i.getDescription(),
                i.getItemType(),
                i.getQuantity(),
                i.getUom(),
                i.getDocumentId(),
                i.getEffectiveStartDate(),
                i.getEffectiveEndDate(),
                i.getNotes(),
                i.getCreatedAt()
        );
    }
}

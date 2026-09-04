package com.sivamachineworks.platform.scm.quotation.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.identity.domain.User;
import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.inventory.domain.Warehouse;
import com.sivamachineworks.platform.inventory.repository.WarehouseRepository;
import com.sivamachineworks.platform.scm.domain.PurchaseOrder;
import com.sivamachineworks.platform.scm.domain.PurchaseOrderItem;
import com.sivamachineworks.platform.scm.domain.Supplier;
import com.sivamachineworks.platform.scm.quotation.domain.SupplierQuotationExtraction;
import com.sivamachineworks.platform.scm.quotation.dto.QuotationExtractionDto;
import com.sivamachineworks.platform.scm.quotation.dto.ReviewQuotationRequest;
import com.sivamachineworks.platform.scm.quotation.dto.UploadQuotationRequest;
import com.sivamachineworks.platform.scm.quotation.repository.SupplierQuotationExtractionRepository;
import com.sivamachineworks.platform.scm.repository.PurchaseOrderRepository;
import com.sivamachineworks.platform.scm.repository.SupplierRepository;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SupplierQuotationService {

    private final SupplierQuotationExtractionRepository quotationRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final QuotationAiExtractor aiExtractor;
    private final AuditLogService auditLogService;

    public SupplierQuotationService(
            SupplierQuotationExtractionRepository quotationRepository,
            SupplierRepository supplierRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            WarehouseRepository warehouseRepository,
            UserRepository userRepository,
            QuotationAiExtractor aiExtractor,
            AuditLogService auditLogService) {
        this.quotationRepository = quotationRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.userRepository = userRepository;
        this.aiExtractor = aiExtractor;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public QuotationExtractionDto uploadAndExtract(UploadQuotationRequest request, UUID userId, String ipAddress) {
        QuotationAiExtractor.ExtractionResult extraction = aiExtractor.extract(request.quotationText());

        SupplierQuotationExtraction record = new SupplierQuotationExtraction();
        record.setDocFileName(request.docFileName());
        record.setRawText(request.quotationText());
        record.setSupplier(extraction.matchedSupplier());
        record.setSupplierNameExtracted(extraction.supplierNameExtracted());
        record.setQuotationNumber(extraction.quotationNumber());
        record.setPartNumber(extraction.partNumber());
        record.setQuantity(extraction.quantity());
        record.setUnitPrice(extraction.unitPrice());
        record.setCurrency(extraction.currency());
        record.setDeliveryDate(extraction.deliveryDate());
        record.setPaymentTerms(extraction.paymentTerms());
        record.setConfidenceScore(extraction.confidenceScore());
        record.setStatus("PENDING_REVIEW"); // MANDATORY HUMAN REVIEW REQUIRED

        if (!extraction.warnings().isEmpty()) {
            record.setValidationWarnings(String.join("; ", extraction.warnings()));
        }

        SupplierQuotationExtraction saved = quotationRepository.save(record);

        auditLogService.log(
                "SupplierQuotation",
                saved.getId(),
                "QUOTATION_UPLOADED_AND_EXTRACTED",
                "Extracted quotation " + saved.getQuotationNumber() + " with confidence " + saved.getConfidenceScore() + ". Status: PENDING_REVIEW",
                userId,
                ipAddress
        );

        return mapToDto(saved);
    }

    @Transactional
    public QuotationExtractionDto reviewAndProcess(UUID quotationId, ReviewQuotationRequest reviewReq, UUID reviewerId, String ipAddress) {
        SupplierQuotationExtraction quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Quotation extraction record not found: " + quotationId));

        if (!"PENDING_REVIEW".equals(quotation.getStatus())) {
            throw new BaseException(ErrorCode.VALIDATION_FAILED, "Quotation extraction has already been reviewed (Current status: " + quotation.getStatus() + ")");
        }

        User reviewer = null;
        if (reviewerId != null) {
            reviewer = userRepository.findById(reviewerId).orElse(null);
        }

        quotation.setReviewer(reviewer);
        quotation.setReviewedAt(Instant.now());
        quotation.setReviewNotes(reviewReq.reviewNotes());

        if ("REJECT".equalsIgnoreCase(reviewReq.action())) {
            quotation.setStatus("REJECTED");
            SupplierQuotationExtraction saved = quotationRepository.save(quotation);

            auditLogService.log(
                    "SupplierQuotation",
                    saved.getId(),
                    "QUOTATION_REJECTED",
                    "Quotation " + saved.getQuotationNumber() + " rejected by reviewer. Notes: " + reviewReq.reviewNotes(),
                    reviewerId,
                    ipAddress
            );
            return mapToDto(saved);
        }

        if ("APPROVE".equalsIgnoreCase(reviewReq.action())) {
            // Apply human reviewer overrides / confirmations
            if (reviewReq.confirmedSupplierId() != null) {
                Supplier supplier = supplierRepository.findById(reviewReq.confirmedSupplierId())
                        .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Selected supplier not found: " + reviewReq.confirmedSupplierId()));
                quotation.setSupplier(supplier);
            }
            if (reviewReq.confirmedPartNumber() != null && !reviewReq.confirmedPartNumber().isBlank()) {
                quotation.setPartNumber(reviewReq.confirmedPartNumber());
            }
            if (reviewReq.confirmedQuantity() != null) {
                if (reviewReq.confirmedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BaseException(ErrorCode.VALIDATION_FAILED, "Confirmed quantity must be greater than zero");
                }
                quotation.setQuantity(reviewReq.confirmedQuantity());
            }
            if (reviewReq.confirmedUnitPrice() != null) {
                if (reviewReq.confirmedUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BaseException(ErrorCode.VALIDATION_FAILED, "Confirmed unit price must be greater than zero");
                }
                quotation.setUnitPrice(reviewReq.confirmedUnitPrice());
            }
            if (reviewReq.confirmedDeliveryDate() != null) {
                quotation.setDeliveryDate(reviewReq.confirmedDeliveryDate());
            }
            if (reviewReq.confirmedPaymentTerms() != null) {
                quotation.setPaymentTerms(reviewReq.confirmedPaymentTerms());
            }

            // Validation check before creating draft PO
            if (quotation.getSupplier() == null) {
                throw new BaseException(ErrorCode.VALIDATION_FAILED, "Cannot generate draft PO: A valid supplier must be selected");
            }
            if (quotation.getPartNumber() == null || quotation.getPartNumber().isBlank()) {
                throw new BaseException(ErrorCode.VALIDATION_FAILED, "Cannot generate draft PO: Part number is missing");
            }
            if (quotation.getQuantity() == null || quotation.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BaseException(ErrorCode.VALIDATION_FAILED, "Cannot generate draft PO: Quantity is invalid");
            }
            if (quotation.getUnitPrice() == null || quotation.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BaseException(ErrorCode.VALIDATION_FAILED, "Cannot generate draft PO: Unit price is invalid");
            }

            Warehouse defaultWarehouse = warehouseRepository.findAll().stream().findFirst().orElse(null);

            // Generate DRAFT Purchase Order (AI NEVER auto-approves or auto-submits PO)
            String poCode = "PO-" + LocalDate.now().getYear() + "-QUO-" + (int)(Math.random() * 9000 + 1000);
            BigDecimal totalAmount = quotation.getQuantity().multiply(quotation.getUnitPrice());

            PurchaseOrder draftPo = new PurchaseOrder();
            draftPo.setPoCode(poCode);
            draftPo.setSupplier(quotation.getSupplier());
            draftPo.setWarehouse(defaultWarehouse);
            draftPo.setStatus("DRAFT"); // Strictly DRAFT
            draftPo.setSubtotalAmount(totalAmount);
            draftPo.setTotalAmount(totalAmount);
            draftPo.setCurrency(quotation.getCurrency());
            draftPo.setExpectedDeliveryDate(quotation.getDeliveryDate());

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(draftPo);
            item.setItemSeq(1);
            item.setPartNumber(quotation.getPartNumber());
            item.setDescription("Extracted SCM Component: " + quotation.getPartNumber());
            item.setQuantityOrdered(quotation.getQuantity());
            item.setUnitPrice(quotation.getUnitPrice());
            item.setLineTotal(totalAmount);

            draftPo.getItems().add(item);
            PurchaseOrder savedPo = purchaseOrderRepository.save(draftPo);

            quotation.setStatus("APPROVED");
            quotation.setGeneratedPurchaseOrder(savedPo);
            SupplierQuotationExtraction savedQuotation = quotationRepository.save(quotation);

            auditLogService.log(
                    "SupplierQuotation",
                    savedQuotation.getId(),
                    "DRAFT_PO_GENERATED_FROM_QUOTATION",
                    "Approved quotation " + savedQuotation.getQuotationNumber() + " and created DRAFT PO " + savedPo.getPoCode() + " with total " + totalAmount,
                    reviewerId,
                    ipAddress
            );

            return mapToDto(savedQuotation);
        }

        throw new BaseException(ErrorCode.VALIDATION_FAILED, "Invalid review action: " + reviewReq.action() + " (Expected APPROVE or REJECT)");
    }

    @Transactional(readOnly = true)
    public List<QuotationExtractionDto> listQuotations(String status) {
        List<SupplierQuotationExtraction> list;
        if (status != null && !status.isBlank()) {
            list = quotationRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
        } else {
            list = quotationRepository.findAll();
        }
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuotationExtractionDto getQuotation(UUID id) {
        return quotationRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Quotation extraction record not found: " + id));
    }

    private QuotationExtractionDto mapToDto(SupplierQuotationExtraction q) {
        return new QuotationExtractionDto(
                q.getId(),
                q.getDocFileName(),
                q.getRawText(),
                q.getSupplier() != null ? q.getSupplier().getId() : null,
                q.getSupplier() != null ? q.getSupplier().getCompanyName() : q.getSupplierNameExtracted(),
                q.getQuotationNumber(),
                q.getPartNumber(),
                q.getQuantity(),
                q.getUnitPrice(),
                q.getCurrency(),
                q.getDeliveryDate(),
                q.getPaymentTerms(),
                q.getConfidenceScore(),
                q.getStatus(),
                q.getValidationWarnings(),
                q.getReviewer() != null ? q.getReviewer().getUsername() : null,
                q.getReviewedAt(),
                q.getReviewNotes(),
                q.getGeneratedPurchaseOrder() != null ? q.getGeneratedPurchaseOrder().getId() : null,
                q.getGeneratedPurchaseOrder() != null ? q.getGeneratedPurchaseOrder().getPoCode() : null,
                q.getCreatedAt()
        );
    }
}

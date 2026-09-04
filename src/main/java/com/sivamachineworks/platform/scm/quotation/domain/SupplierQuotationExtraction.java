package com.sivamachineworks.platform.scm.quotation.domain;

import com.sivamachineworks.platform.identity.domain.User;
import com.sivamachineworks.platform.scm.domain.PurchaseOrder;
import com.sivamachineworks.platform.scm.domain.Supplier;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "supplier_quotation_extractions")
public class SupplierQuotationExtraction extends AuditableEntity {

    @Column(name = "doc_file_name", nullable = false)
    private String docFileName;

    @Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "supplier_name_extracted")
    private String supplierNameExtracted;

    @Column(name = "quotation_number", length = 100)
    private String quotationNumber;

    @Column(name = "part_number", length = 100)
    private String partNumber;

    @Column(precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 10)
    private String currency = "JPY";

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal confidenceScore = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    private String status = "PENDING_REVIEW"; // PENDING_REVIEW, APPROVED, REJECTED

    @Column(name = "validation_warnings", columnDefinition = "TEXT")
    private String validationWarnings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_po_id")
    private PurchaseOrder generatedPurchaseOrder;

    public SupplierQuotationExtraction() {}

    public String getDocFileName() { return docFileName; }
    public void setDocFileName(String docFileName) { this.docFileName = docFileName; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public String getSupplierNameExtracted() { return supplierNameExtracted; }
    public void setSupplierNameExtracted(String supplierNameExtracted) { this.supplierNameExtracted = supplierNameExtracted; }
    public String getQuotationNumber() { return quotationNumber; }
    public void setQuotationNumber(String quotationNumber) { this.quotationNumber = quotationNumber; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getValidationWarnings() { return validationWarnings; }
    public void setValidationWarnings(String validationWarnings) { this.validationWarnings = validationWarnings; }
    public User getReviewer() { return reviewer; }
    public void setReviewer(User reviewer) { this.reviewer = reviewer; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public PurchaseOrder getGeneratedPurchaseOrder() { return generatedPurchaseOrder; }
    public void setGeneratedPurchaseOrder(PurchaseOrder generatedPurchaseOrder) { this.generatedPurchaseOrder = generatedPurchaseOrder; }
}

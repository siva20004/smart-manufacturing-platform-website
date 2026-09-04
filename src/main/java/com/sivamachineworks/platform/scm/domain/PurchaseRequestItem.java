package com.sivamachineworks.platform.scm.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "purchase_request_items")
public class PurchaseRequestItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id", nullable = false)
    private PurchaseRequest purchaseRequest;

    @Column(name = "item_seq", nullable = false)
    private Integer itemSeq;

    @Column(name = "part_number", nullable = false, length = 100)
    private String partNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "quantity_requested", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityRequested;

    @Column(length = 20)
    private String uom = "EA";

    @Column(name = "estimated_unit_price", precision = 15, scale = 2)
    private BigDecimal estimatedUnitPrice;

    @Column(name = "required_by_date")
    private LocalDate requiredByDate;

    @Column(name = "suggested_supplier_id")
    private UUID suggestedSupplierId;

    public PurchaseRequestItem() {}

    public PurchaseRequest getPurchaseRequest() { return purchaseRequest; }
    public void setPurchaseRequest(PurchaseRequest purchaseRequest) { this.purchaseRequest = purchaseRequest; }
    public Integer getItemSeq() { return itemSeq; }
    public void setItemSeq(Integer itemSeq) { this.itemSeq = itemSeq; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getQuantityRequested() { return quantityRequested; }
    public void setQuantityRequested(BigDecimal quantityRequested) { this.quantityRequested = quantityRequested; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public BigDecimal getEstimatedUnitPrice() { return estimatedUnitPrice; }
    public void setEstimatedUnitPrice(BigDecimal estimatedUnitPrice) { this.estimatedUnitPrice = estimatedUnitPrice; }
    public LocalDate getRequiredByDate() { return requiredByDate; }
    public void setRequiredByDate(LocalDate requiredByDate) { this.requiredByDate = requiredByDate; }
    public UUID getSuggestedSupplierId() { return suggestedSupplierId; }
    public void setSuggestedSupplierId(UUID suggestedSupplierId) { this.suggestedSupplierId = suggestedSupplierId; }
}

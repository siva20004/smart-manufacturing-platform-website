package com.sivamachineworks.platform.scm.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchase_requests")
public class PurchaseRequest extends AuditableEntity {

    @Column(name = "pr_code", unique = true, nullable = false, length = 50)
    private String prCode;

    @Column(name = "requested_by")
    private UUID requestedBy;

    @Column(name = "source_type", length = 30)
    private String sourceType = "MRP";

    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT";

    @Column(name = "total_estimated_amount", precision = 15, scale = 2)
    private BigDecimal totalEstimatedAmount = BigDecimal.ZERO;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseRequestItem> items = new ArrayList<>();

    public PurchaseRequest() {}

    public String getPrCode() { return prCode; }
    public void setPrCode(String prCode) { this.prCode = prCode; }
    public UUID getRequestedBy() { return requestedBy; }
    public void setRequestedBy(UUID requestedBy) { this.requestedBy = requestedBy; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public UUID getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(UUID salesOrderId) { this.salesOrderId = salesOrderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalEstimatedAmount() { return totalEstimatedAmount; }
    public void setTotalEstimatedAmount(BigDecimal totalEstimatedAmount) { this.totalEstimatedAmount = totalEstimatedAmount; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public List<PurchaseRequestItem> getItems() { return items; }
    public void setItems(List<PurchaseRequestItem> items) { this.items = items; }
}

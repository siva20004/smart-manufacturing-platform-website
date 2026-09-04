package com.sivamachineworks.platform.scm.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "supplier_parts")
public class SupplierPart extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "part_number", nullable = false, length = 100)
    private String partNumber;

    @Column(name = "supplier_part_number", length = 100)
    private String supplierPartNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 10)
    private String currency = "JPY";

    @Column(name = "lead_time_days")
    private Integer leadTimeDays = 14;

    @Column(name = "min_order_qty", precision = 12, scale = 4)
    private BigDecimal minOrderQty = BigDecimal.ONE;

    @Column(name = "is_preferred")
    private Boolean isPreferred = true;

    public SupplierPart() {}

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getSupplierPartNumber() { return supplierPartNumber; }
    public void setSupplierPartNumber(String supplierPartNumber) { this.supplierPartNumber = supplierPartNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public BigDecimal getMinOrderQty() { return minOrderQty; }
    public void setMinOrderQty(BigDecimal minOrderQty) { this.minOrderQty = minOrderQty; }
    public Boolean getIsPreferred() { return isPreferred; }
    public void setIsPreferred(Boolean isPreferred) { this.isPreferred = isPreferred; }
}

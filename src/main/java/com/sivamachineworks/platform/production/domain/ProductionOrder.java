package com.sivamachineworks.platform.production.domain;

import com.sivamachineworks.platform.bom.domain.MbomHeader;
import com.sivamachineworks.platform.inventory.domain.Warehouse;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.sales.domain.SalesOrder;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_orders")
public class ProductionOrder extends AuditableEntity {

    @Column(name = "order_code", unique = true, nullable = false, length = 50)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbom_header_id")
    private MbomHeader mbomHeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "plant_location", nullable = false, length = 50)
    private String plantLocation = "Osaka";

    @Column(name = "quantity_planned", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityPlanned;

    @Column(name = "quantity_completed", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityCompleted = BigDecimal.ZERO;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_completion_date")
    private LocalDate plannedCompletionDate;

    @Column(name = "actual_start_date")
    private Instant actualStartDate;

    @Column(name = "actual_completion_date")
    private Instant actualCompletionDate;

    @Column(nullable = false, length = 30)
    private String status = "PLANNED";

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionOperation> operations = new ArrayList<>();

    public ProductionOrder() {}

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public SalesOrder getSalesOrder() { return salesOrder; }
    public void setSalesOrder(SalesOrder salesOrder) { this.salesOrder = salesOrder; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public MbomHeader getMbomHeader() { return mbomHeader; }
    public void setMbomHeader(MbomHeader mbomHeader) { this.mbomHeader = mbomHeader; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public String getPlantLocation() { return plantLocation; }
    public void setPlantLocation(String plantLocation) { this.plantLocation = plantLocation; }
    public BigDecimal getQuantityPlanned() { return quantityPlanned; }
    public void setQuantityPlanned(BigDecimal quantityPlanned) { this.quantityPlanned = quantityPlanned; }
    public BigDecimal getQuantityCompleted() { return quantityCompleted; }
    public void setQuantityCompleted(BigDecimal quantityCompleted) { this.quantityCompleted = quantityCompleted; }
    public LocalDate getPlannedStartDate() { return plannedStartDate; }
    public void setPlannedStartDate(LocalDate plannedStartDate) { this.plannedStartDate = plannedStartDate; }
    public LocalDate getPlannedCompletionDate() { return plannedCompletionDate; }
    public void setPlannedCompletionDate(LocalDate plannedCompletionDate) { this.plannedCompletionDate = plannedCompletionDate; }
    public Instant getActualStartDate() { return actualStartDate; }
    public void setActualStartDate(Instant actualStartDate) { this.actualStartDate = actualStartDate; }
    public Instant getActualCompletionDate() { return actualCompletionDate; }
    public void setActualCompletionDate(Instant actualCompletionDate) { this.actualCompletionDate = actualCompletionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ProductionOperation> getOperations() { return operations; }
    public void setOperations(List<ProductionOperation> operations) { this.operations = operations; }
}

package com.sivamachineworks.platform.production.domain;

import com.sivamachineworks.platform.inventory.domain.Warehouse;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "material_consumptions")
public class MaterialConsumption {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "part_number", nullable = false, length = 100)
    private String partNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "planned_qty", nullable = false, precision = 12, scale = 4)
    private BigDecimal plannedQty;

    @Column(name = "actual_qty_consumed", nullable = false, precision = 12, scale = 4)
    private BigDecimal actualQtyConsumed;

    @Column(length = 20)
    private String uom = "EA";

    @Column(name = "consumed_at", nullable = false)
    private Instant consumedAt = Instant.now();

    @Column(name = "consumed_by")
    private UUID consumedBy;

    public MaterialConsumption() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProductionOrder getProductionOrder() { return productionOrder; }
    public void setProductionOrder(ProductionOrder productionOrder) { this.productionOrder = productionOrder; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPlannedQty() { return plannedQty; }
    public void setPlannedQty(BigDecimal plannedQty) { this.plannedQty = plannedQty; }
    public BigDecimal getActualQtyConsumed() { return actualQtyConsumed; }
    public void setActualQtyConsumed(BigDecimal actualQtyConsumed) { this.actualQtyConsumed = actualQtyConsumed; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }
    public UUID getConsumedBy() { return consumedBy; }
    public void setConsumedBy(UUID consumedBy) { this.consumedBy = consumedBy; }
}

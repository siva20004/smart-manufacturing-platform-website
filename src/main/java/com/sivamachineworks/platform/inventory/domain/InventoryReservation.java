package com.sivamachineworks.platform.inventory.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservations")
public class InventoryReservation extends AuditableEntity {

    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Column(name = "production_order_id")
    private UUID productionOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "part_number", nullable = false, length = 100)
    private String partNumber;

    @Column(name = "quantity_reserved", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityReserved;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt = Instant.now();

    @Column(name = "released_at")
    private Instant releasedAt;

    public InventoryReservation() {}

    public UUID getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(UUID salesOrderId) { this.salesOrderId = salesOrderId; }
    public UUID getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(UUID productionOrderId) { this.productionOrderId = productionOrderId; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public BigDecimal getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(BigDecimal quantityReserved) { this.quantityReserved = quantityReserved; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getReservedAt() { return reservedAt; }
    public void setReservedAt(Instant reservedAt) { this.reservedAt = reservedAt; }
    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }
}

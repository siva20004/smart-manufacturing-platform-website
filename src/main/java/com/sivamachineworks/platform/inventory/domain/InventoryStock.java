package com.sivamachineworks.platform.inventory.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_stock")
public class InventoryStock extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "part_number", nullable = false, length = 100)
    private String partNumber;

    private String description;

    @Column(name = "qty_on_hand", nullable = false, precision = 12, scale = 4)
    private BigDecimal qtyOnHand = BigDecimal.ZERO;

    @Column(name = "qty_reserved", nullable = false, precision = 12, scale = 4)
    private BigDecimal qtyReserved = BigDecimal.ZERO;

    @Column(name = "qty_available", nullable = false, precision = 12, scale = 4)
    private BigDecimal qtyAvailable = BigDecimal.ZERO;

    @Column(length = 20)
    private String uom = "EA";

    @Column(name = "unit_cost", precision = 15, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "min_stock_level", precision = 12, scale = 4)
    private BigDecimal minStockLevel = BigDecimal.ZERO;

    public InventoryStock() {}

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getQtyOnHand() { return qtyOnHand; }
    public void setQtyOnHand(BigDecimal qtyOnHand) { this.qtyOnHand = qtyOnHand; }
    public BigDecimal getQtyReserved() { return qtyReserved; }
    public void setQtyReserved(BigDecimal qtyReserved) { this.qtyReserved = qtyReserved; }
    public BigDecimal getQtyAvailable() { return qtyAvailable; }
    public void setQtyAvailable(BigDecimal qtyAvailable) { this.qtyAvailable = qtyAvailable; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(BigDecimal minStockLevel) { this.minStockLevel = minStockLevel; }
}

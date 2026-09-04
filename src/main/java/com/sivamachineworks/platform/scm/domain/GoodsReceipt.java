package com.sivamachineworks.platform.scm.domain;

import com.sivamachineworks.platform.inventory.domain.Warehouse;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "goods_receipts")
public class GoodsReceipt extends AuditableEntity {

    @Column(name = "gr_code", unique = true, nullable = false, length = 50)
    private String grCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "delivery_note_no", length = 100)
    private String deliveryNoteNo;

    @Column(nullable = false, length = 30)
    private String status = "RECEIVED";

    @Column(name = "received_by")
    private UUID receivedBy;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt = Instant.now();

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptItem> items = new ArrayList<>();

    public GoodsReceipt() {}

    public String getGrCode() { return grCode; }
    public void setGrCode(String grCode) { this.grCode = grCode; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public String getDeliveryNoteNo() { return deliveryNoteNo; }
    public void setDeliveryNoteNo(String deliveryNoteNo) { this.deliveryNoteNo = deliveryNoteNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getReceivedBy() { return receivedBy; }
    public void setReceivedBy(UUID receivedBy) { this.receivedBy = receivedBy; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public List<GoodsReceiptItem> getItems() { return items; }
    public void setItems(List<GoodsReceiptItem> items) { this.items = items; }
}

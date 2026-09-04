package com.sivamachineworks.platform.bom.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ebom_items")
public class EbomItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebom_header_id", nullable = false)
    private EbomHeader ebomHeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id")
    private EbomItem parentItem;

    @Column(name = "item_seq", nullable = false)
    private Integer itemSeq;

    @Column(name = "part_number", nullable = false, length = 100)
    private String partNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "item_type", nullable = false, length = 30)
    private String itemType = "COMPONENT";

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(length = 20)
    private String uom = "EA";

    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "effective_start_date")
    private LocalDate effectiveStartDate;

    @Column(name = "effective_end_date")
    private LocalDate effectiveEndDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "parentItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EbomItem> children = new ArrayList<>();

    public EbomItem() {}

    public EbomHeader getEbomHeader() { return ebomHeader; }
    public void setEbomHeader(EbomHeader ebomHeader) { this.ebomHeader = ebomHeader; }
    public EbomItem getParentItem() { return parentItem; }
    public void setParentItem(EbomItem parentItem) { this.parentItem = parentItem; }
    public Integer getItemSeq() { return itemSeq; }
    public void setItemSeq(Integer itemSeq) { this.itemSeq = itemSeq; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }
    public LocalDate getEffectiveStartDate() { return effectiveStartDate; }
    public void setEffectiveStartDate(LocalDate effectiveStartDate) { this.effectiveStartDate = effectiveStartDate; }
    public LocalDate getEffectiveEndDate() { return effectiveEndDate; }
    public void setEffectiveEndDate(LocalDate effectiveEndDate) { this.effectiveEndDate = effectiveEndDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<EbomItem> getChildren() { return children; }
    public void setChildren(List<EbomItem> children) { this.children = children; }
}

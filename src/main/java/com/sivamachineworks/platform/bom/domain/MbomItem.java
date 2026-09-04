package com.sivamachineworks.platform.bom.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mbom_items")
public class MbomItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbom_header_id", nullable = false)
    private MbomHeader mbomHeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id")
    private MbomItem parentItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_ebom_item_id")
    private EbomItem sourceEbomItem;

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

    @Column(name = "work_center", length = 50)
    private String workCenter;

    @Column(name = "operation_seq")
    private Integer operationSeq;

    @Column(name = "operation_name", length = 100)
    private String operationName;

    @Column(name = "is_consumed_per_unit")
    private Boolean isConsumedPerUnit = true;

    @Column(name = "scrap_factor", precision = 5, scale = 4)
    private BigDecimal scrapFactor = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "parentItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MbomItem> children = new ArrayList<>();

    public MbomItem() {}

    public MbomHeader getMbomHeader() { return mbomHeader; }
    public void setMbomHeader(MbomHeader mbomHeader) { this.mbomHeader = mbomHeader; }
    public MbomItem getParentItem() { return parentItem; }
    public void setParentItem(MbomItem parentItem) { this.parentItem = parentItem; }
    public EbomItem getSourceEbomItem() { return sourceEbomItem; }
    public void setSourceEbomItem(EbomItem sourceEbomItem) { this.sourceEbomItem = sourceEbomItem; }
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
    public String getWorkCenter() { return workCenter; }
    public void setWorkCenter(String workCenter) { this.workCenter = workCenter; }
    public Integer getOperationSeq() { return operationSeq; }
    public void setOperationSeq(Integer operationSeq) { this.operationSeq = operationSeq; }
    public String getOperationName() { return operationName; }
    public void setOperationName(String operationName) { this.operationName = operationName; }
    public Boolean getIsConsumedPerUnit() { return isConsumedPerUnit; }
    public void setIsConsumedPerUnit(Boolean isConsumedPerUnit) { this.isConsumedPerUnit = isConsumedPerUnit; }
    public BigDecimal getScrapFactor() { return scrapFactor; }
    public void setScrapFactor(BigDecimal scrapFactor) { this.scrapFactor = scrapFactor; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<MbomItem> getChildren() { return children; }
    public void setChildren(List<MbomItem> children) { this.children = children; }
}

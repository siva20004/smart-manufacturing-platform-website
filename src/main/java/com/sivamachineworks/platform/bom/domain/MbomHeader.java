package com.sivamachineworks.platform.bom.domain;

import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mbom_headers")
public class MbomHeader extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebom_header_id", nullable = false)
    private EbomHeader ebomHeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "revision_code", nullable = false, length = 20)
    private String revisionCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "plant_location", nullable = false, length = 50)
    private String plantLocation;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT";

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @OneToMany(mappedBy = "mbomHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MbomItem> items = new ArrayList<>();

    public MbomHeader() {}

    public EbomHeader getEbomHeader() { return ebomHeader; }
    public void setEbomHeader(EbomHeader ebomHeader) { this.ebomHeader = ebomHeader; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getRevisionCode() { return revisionCode; }
    public void setRevisionCode(String revisionCode) { this.revisionCode = revisionCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPlantLocation() { return plantLocation; }
    public void setPlantLocation(String plantLocation) { this.plantLocation = plantLocation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public List<MbomItem> getItems() { return items; }
    public void setItems(List<MbomItem> items) { this.items = items; }
}

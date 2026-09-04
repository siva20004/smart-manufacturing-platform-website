package com.sivamachineworks.platform.bom.domain;

import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ebom_headers")
public class EbomHeader extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "revision_code", nullable = false, length = 20)
    private String revisionCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT";

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @OneToMany(mappedBy = "ebomHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EbomItem> items = new ArrayList<>();

    public EbomHeader() {}

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getRevisionCode() { return revisionCode; }
    public void setRevisionCode(String revisionCode) { this.revisionCode = revisionCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public List<EbomItem> getItems() { return items; }
    public void setItems(List<EbomItem> items) { this.items = items; }
}

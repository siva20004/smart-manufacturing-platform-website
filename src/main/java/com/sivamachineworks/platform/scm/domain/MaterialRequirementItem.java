package com.sivamachineworks.platform.scm.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "material_requirement_items")
public class MaterialRequirementItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id", nullable = false)
    private MaterialRequirement materialRequirement;

    @Column(name = "part_number", nullable = false, length = 100)
    private String partNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_qty", nullable = false, precision = 12, scale = 4)
    private BigDecimal requiredQty;

    @Column(name = "available_qty", nullable = false, precision = 12, scale = 4)
    private BigDecimal availableQty;

    @Column(name = "shortage_qty", nullable = false, precision = 12, scale = 4)
    private BigDecimal shortageQty;

    @Column(length = 20)
    private String uom = "EA";

    @Column(nullable = false, length = 30)
    private String status = "SHORTAGE";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public MaterialRequirementItem() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public MaterialRequirement getMaterialRequirement() { return materialRequirement; }
    public void setMaterialRequirement(MaterialRequirement materialRequirement) { this.materialRequirement = materialRequirement; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getRequiredQty() { return requiredQty; }
    public void setRequiredQty(BigDecimal requiredQty) { this.requiredQty = requiredQty; }
    public BigDecimal getAvailableQty() { return availableQty; }
    public void setAvailableQty(BigDecimal availableQty) { this.availableQty = availableQty; }
    public BigDecimal getShortageQty() { return shortageQty; }
    public void setShortageQty(BigDecimal shortageQty) { this.shortageQty = shortageQty; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

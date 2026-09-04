package com.sivamachineworks.platform.bom.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bom_mappings")
public class BomMapping extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebom_header_id", nullable = false)
    private EbomHeader ebomHeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbom_header_id", nullable = false)
    private MbomHeader mbomHeader;

    @Column(name = "transformation_status", nullable = false, length = 30)
    private String transformationStatus = "COMPLETED";

    @Column(name = "rules_applied", columnDefinition = "TEXT")
    private String rulesApplied;

    @Column(name = "transformed_by")
    private UUID transformedBy;

    @Column(name = "transformed_at", nullable = false)
    private Instant transformedAt = Instant.now();

    public BomMapping() {}

    public EbomHeader getEbomHeader() { return ebomHeader; }
    public void setEbomHeader(EbomHeader ebomHeader) { this.ebomHeader = ebomHeader; }
    public MbomHeader getMbomHeader() { return mbomHeader; }
    public void setMbomHeader(MbomHeader mbomHeader) { this.mbomHeader = mbomHeader; }
    public String getTransformationStatus() { return transformationStatus; }
    public void setTransformationStatus(String transformationStatus) { this.transformationStatus = transformationStatus; }
    public String getRulesApplied() { return rulesApplied; }
    public void setRulesApplied(String rulesApplied) { this.rulesApplied = rulesApplied; }
    public UUID getTransformedBy() { return transformedBy; }
    public void setTransformedBy(UUID transformedBy) { this.transformedBy = transformedBy; }
    public Instant getTransformedAt() { return transformedAt; }
    public void setTransformedAt(Instant transformedAt) { this.transformedAt = transformedAt; }
}

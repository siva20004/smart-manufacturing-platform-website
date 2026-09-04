package com.sivamachineworks.platform.production.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quality_inspections")
public class QualityInspection extends AuditableEntity {

    @Column(name = "inspection_code", unique = true, nullable = false, length = 50)
    private String inspectionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @Column(name = "inspection_type", nullable = false, length = 30)
    private String inspectionType = "FINAL_ACCEPTANCE";

    @Column(nullable = false, length = 30)
    private String status = "PASSED";

    @Column(name = "fat_spindle_runout_mm", precision = 8, scale = 4)
    private BigDecimal fatSpindleRunoutMm;

    @Column(name = "fat_positioning_accuracy_mm", precision = 8, scale = 4)
    private BigDecimal fatPositioningAccuracyMm;

    @Column(name = "inspector_id")
    private UUID inspectorId;

    @Column(name = "inspected_at", nullable = false)
    private Instant inspectedAt = Instant.now();

    @Column(columnDefinition = "TEXT")
    private String notes;

    public QualityInspection() {}

    public String getInspectionCode() { return inspectionCode; }
    public void setInspectionCode(String inspectionCode) { this.inspectionCode = inspectionCode; }
    public ProductionOrder getProductionOrder() { return productionOrder; }
    public void setProductionOrder(ProductionOrder productionOrder) { this.productionOrder = productionOrder; }
    public String getInspectionType() { return inspectionType; }
    public void setInspectionType(String inspectionType) { this.inspectionType = inspectionType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getFatSpindleRunoutMm() { return fatSpindleRunoutMm; }
    public void setFatSpindleRunoutMm(BigDecimal fatSpindleRunoutMm) { this.fatSpindleRunoutMm = fatSpindleRunoutMm; }
    public BigDecimal getFatPositioningAccuracyMm() { return fatPositioningAccuracyMm; }
    public void setFatPositioningAccuracyMm(BigDecimal fatPositioningAccuracyMm) { this.fatPositioningAccuracyMm = fatPositioningAccuracyMm; }
    public UUID getInspectorId() { return inspectorId; }
    public void setInspectorId(UUID inspectorId) { this.inspectorId = inspectorId; }
    public Instant getInspectedAt() { return inspectedAt; }
    public void setInspectedAt(Instant inspectedAt) { this.inspectedAt = inspectedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

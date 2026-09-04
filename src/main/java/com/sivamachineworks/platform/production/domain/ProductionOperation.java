package com.sivamachineworks.platform.production.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "production_operations")
public class ProductionOperation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @Column(name = "operation_seq", nullable = false)
    private Integer operationSeq;

    @Column(name = "operation_name", nullable = false, length = 100)
    private String operationName;

    @Column(name = "work_center_code", nullable = false, length = 50)
    private String workCenterCode;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "planned_hours", precision = 8, scale = 2)
    private BigDecimal plannedHours;

    @Column(name = "actual_hours", precision = 8, scale = 2)
    private BigDecimal actualHours;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public ProductionOperation() {}

    public ProductionOrder getProductionOrder() { return productionOrder; }
    public void setProductionOrder(ProductionOrder productionOrder) { this.productionOrder = productionOrder; }
    public Integer getOperationSeq() { return operationSeq; }
    public void setOperationSeq(Integer operationSeq) { this.operationSeq = operationSeq; }
    public String getOperationName() { return operationName; }
    public void setOperationName(String operationName) { this.operationName = operationName; }
    public String getWorkCenterCode() { return workCenterCode; }
    public void setWorkCenterCode(String workCenterCode) { this.workCenterCode = workCenterCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getPlannedHours() { return plannedHours; }
    public void setPlannedHours(BigDecimal plannedHours) { this.plannedHours = plannedHours; }
    public BigDecimal getActualHours() { return actualHours; }
    public void setActualHours(BigDecimal actualHours) { this.actualHours = actualHours; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public UUID getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UUID assignedTo) { this.assignedTo = assignedTo; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

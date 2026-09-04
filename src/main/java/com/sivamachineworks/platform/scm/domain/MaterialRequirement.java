package com.sivamachineworks.platform.scm.domain;

import com.sivamachineworks.platform.sales.domain.SalesOrder;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "material_requirements")
public class MaterialRequirement extends AuditableEntity {

    @Column(name = "run_number", unique = true, nullable = false, length = 50)
    private String runNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @Column(name = "triggered_by")
    private UUID triggeredBy;

    @Column(name = "run_status", nullable = false, length = 30)
    private String runStatus = "COMPLETED";

    @Column(name = "run_date", nullable = false)
    private Instant runDate = Instant.now();

    @OneToMany(mappedBy = "materialRequirement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialRequirementItem> items = new ArrayList<>();

    public MaterialRequirement() {}

    public String getRunNumber() { return runNumber; }
    public void setRunNumber(String runNumber) { this.runNumber = runNumber; }
    public SalesOrder getSalesOrder() { return salesOrder; }
    public void setSalesOrder(SalesOrder salesOrder) { this.salesOrder = salesOrder; }
    public UUID getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(UUID triggeredBy) { this.triggeredBy = triggeredBy; }
    public String getRunStatus() { return runStatus; }
    public void setRunStatus(String runStatus) { this.runStatus = runStatus; }
    public Instant getRunDate() { return runDate; }
    public void setRunDate(Instant runDate) { this.runDate = runDate; }
    public List<MaterialRequirementItem> getItems() { return items; }
    public void setItems(List<MaterialRequirementItem> items) { this.items = items; }
}

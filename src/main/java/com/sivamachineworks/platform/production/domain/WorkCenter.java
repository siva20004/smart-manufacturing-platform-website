package com.sivamachineworks.platform.production.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.math.BigDecimal;

@Entity
@Table(name = "work_centers")
public class WorkCenter extends AuditableEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "plant_location", nullable = false, length = 50)
    private String plantLocation;

    @Column(name = "capacity_hours_per_day", precision = 5, scale = 2)
    private BigDecimal capacityHoursPerDay = new BigDecimal("16.0");

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public WorkCenter() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPlantLocation() { return plantLocation; }
    public void setPlantLocation(String plantLocation) { this.plantLocation = plantLocation; }
    public BigDecimal getCapacityHoursPerDay() { return capacityHoursPerDay; }
    public void setCapacityHoursPerDay(BigDecimal capacityHoursPerDay) { this.capacityHoursPerDay = capacityHoursPerDay; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

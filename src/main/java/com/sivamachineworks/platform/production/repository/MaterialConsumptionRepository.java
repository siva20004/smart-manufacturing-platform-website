package com.sivamachineworks.platform.production.repository;

import com.sivamachineworks.platform.production.domain.MaterialConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MaterialConsumptionRepository extends JpaRepository<MaterialConsumption, UUID> {
    List<MaterialConsumption> findByProductionOrderId(UUID productionOrderId);
}

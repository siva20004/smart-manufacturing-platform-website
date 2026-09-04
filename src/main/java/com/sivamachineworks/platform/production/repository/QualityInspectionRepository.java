package com.sivamachineworks.platform.production.repository;

import com.sivamachineworks.platform.production.domain.QualityInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QualityInspectionRepository extends JpaRepository<QualityInspection, UUID> {
    Optional<QualityInspection> findByInspectionCode(String inspectionCode);
    List<QualityInspection> findByProductionOrderId(UUID productionOrderId);
}

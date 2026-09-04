package com.sivamachineworks.platform.scm.repository;

import com.sivamachineworks.platform.scm.domain.MaterialRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MaterialRequirementRepository extends JpaRepository<MaterialRequirement, UUID> {
    List<MaterialRequirement> findBySalesOrderId(UUID salesOrderId);
}

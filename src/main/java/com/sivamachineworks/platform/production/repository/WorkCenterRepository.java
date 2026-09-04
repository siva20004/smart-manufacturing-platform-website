package com.sivamachineworks.platform.production.repository;

import com.sivamachineworks.platform.production.domain.WorkCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WorkCenterRepository extends JpaRepository<WorkCenter, UUID> {
    Optional<WorkCenter> findByCode(String code);
}

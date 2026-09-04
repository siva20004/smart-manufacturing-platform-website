package com.sivamachineworks.platform.scm.repository;

import com.sivamachineworks.platform.scm.domain.SupplierPart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierPartRepository extends JpaRepository<SupplierPart, UUID> {
    List<SupplierPart> findBySupplierId(UUID supplierId);
    Optional<SupplierPart> findBySupplierIdAndPartNumber(UUID supplierId, String partNumber);
    List<SupplierPart> findByPartNumber(String partNumber);
}

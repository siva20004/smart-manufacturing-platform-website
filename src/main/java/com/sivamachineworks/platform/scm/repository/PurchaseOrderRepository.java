package com.sivamachineworks.platform.scm.repository;

import com.sivamachineworks.platform.scm.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    Optional<PurchaseOrder> findByPoCode(String poCode);
    boolean existsByPoCode(String poCode);
}

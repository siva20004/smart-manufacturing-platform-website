package com.sivamachineworks.platform.scm.repository;

import com.sivamachineworks.platform.scm.domain.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, UUID> {
    Optional<PurchaseRequest> findByPrCode(String prCode);
    boolean existsByPrCode(String prCode);
}

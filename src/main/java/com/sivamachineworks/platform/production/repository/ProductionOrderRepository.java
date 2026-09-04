package com.sivamachineworks.platform.production.repository;

import com.sivamachineworks.platform.production.domain.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, UUID> {
    Optional<ProductionOrder> findByOrderCode(String orderCode);
    boolean existsByOrderCode(String orderCode);
}

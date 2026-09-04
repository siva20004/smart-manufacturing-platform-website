package com.sivamachineworks.platform.sales.repository;

import com.sivamachineworks.platform.sales.domain.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {
    Optional<SalesOrder> findBySoCode(String soCode);
    boolean existsBySoCode(String soCode);
}

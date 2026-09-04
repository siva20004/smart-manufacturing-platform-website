package com.sivamachineworks.platform.inventory.repository;

import com.sivamachineworks.platform.inventory.domain.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {
    List<InventoryReservation> findBySalesOrderId(UUID salesOrderId);
    List<InventoryReservation> findBySalesOrderIdAndStatus(UUID salesOrderId, String status);
    List<InventoryReservation> findByProductionOrderIdAndStatus(UUID productionOrderId, String status);
}

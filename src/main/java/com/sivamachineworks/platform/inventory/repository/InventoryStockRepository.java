package com.sivamachineworks.platform.inventory.repository;

import com.sivamachineworks.platform.inventory.domain.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, UUID> {
    Optional<InventoryStock> findByWarehouseIdAndPartNumber(UUID warehouseId, String partNumber);
    List<InventoryStock> findByWarehouseId(UUID warehouseId);
}

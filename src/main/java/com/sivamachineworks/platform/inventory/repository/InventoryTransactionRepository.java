package com.sivamachineworks.platform.inventory.repository;

import com.sivamachineworks.platform.inventory.domain.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    List<InventoryTransaction> findByWarehouseIdAndPartNumber(UUID warehouseId, String partNumber);
    List<InventoryTransaction> findByReferenceId(UUID referenceId);
}

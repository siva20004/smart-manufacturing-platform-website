package com.sivamachineworks.platform.inventory.repository;

import com.sivamachineworks.platform.inventory.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Optional<Warehouse> findByCode(String code);
    Optional<Warehouse> findFirstByPlantLocationAndIsActiveTrue(String plantLocation);
}

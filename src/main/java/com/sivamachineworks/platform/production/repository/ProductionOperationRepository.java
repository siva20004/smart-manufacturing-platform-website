package com.sivamachineworks.platform.production.repository;

import com.sivamachineworks.platform.production.domain.ProductionOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionOperationRepository extends JpaRepository<ProductionOperation, UUID> {
    List<ProductionOperation> findByProductionOrderIdOrderByOperationSeqAsc(UUID productionOrderId);
    Optional<ProductionOperation> findByProductionOrderIdAndOperationSeq(UUID productionOrderId, Integer operationSeq);
}

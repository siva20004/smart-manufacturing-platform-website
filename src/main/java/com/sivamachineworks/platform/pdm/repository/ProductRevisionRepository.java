package com.sivamachineworks.platform.pdm.repository;

import com.sivamachineworks.platform.pdm.domain.ProductRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRevisionRepository extends JpaRepository<ProductRevision, UUID> {
    List<ProductRevision> findByProductId(UUID productId);
    Optional<ProductRevision> findByProductIdAndRevisionNumber(UUID productId, String revisionNumber);
    boolean existsByProductIdAndRevisionNumber(UUID productId, String revisionNumber);
}

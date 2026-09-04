package com.sivamachineworks.platform.bom.repository;

import com.sivamachineworks.platform.bom.domain.EbomHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EbomHeaderRepository extends JpaRepository<EbomHeader, UUID> {
    List<EbomHeader> findByProductId(UUID productId);
    Optional<EbomHeader> findByProductIdAndRevisionCode(UUID productId, String revisionCode);
    boolean existsByProductIdAndRevisionCode(UUID productId, String revisionCode);
    Page<EbomHeader> findByProductId(UUID productId, Pageable pageable);
}

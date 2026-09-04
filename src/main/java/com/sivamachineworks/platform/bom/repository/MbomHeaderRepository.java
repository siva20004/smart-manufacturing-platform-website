package com.sivamachineworks.platform.bom.repository;

import com.sivamachineworks.platform.bom.domain.MbomHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MbomHeaderRepository extends JpaRepository<MbomHeader, UUID> {
    List<MbomHeader> findByProductId(UUID productId);
    List<MbomHeader> findByEbomHeaderId(UUID ebomHeaderId);
    Optional<MbomHeader> findByProductIdAndRevisionCodeAndPlantLocation(UUID productId, String revisionCode, String plantLocation);
    boolean existsByProductIdAndRevisionCodeAndPlantLocation(UUID productId, String revisionCode, String plantLocation);
    Page<MbomHeader> findByProductId(UUID productId, Pageable pageable);
    Page<MbomHeader> findByPlantLocation(String plantLocation, Pageable pageable);
}

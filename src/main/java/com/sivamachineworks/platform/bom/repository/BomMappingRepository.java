package com.sivamachineworks.platform.bom.repository;

import com.sivamachineworks.platform.bom.domain.BomMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BomMappingRepository extends JpaRepository<BomMapping, UUID> {
    List<BomMapping> findByEbomHeaderId(UUID ebomHeaderId);
    List<BomMapping> findByMbomHeaderId(UUID mbomHeaderId);
}

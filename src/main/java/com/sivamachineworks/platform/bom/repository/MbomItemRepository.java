package com.sivamachineworks.platform.bom.repository;

import com.sivamachineworks.platform.bom.domain.MbomItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MbomItemRepository extends JpaRepository<MbomItem, UUID> {
    List<MbomItem> findByMbomHeaderId(UUID mbomHeaderId);
    List<MbomItem> findByMbomHeaderIdAndParentItemIsNull(UUID mbomHeaderId);
    List<MbomItem> findByParentItemId(UUID parentItemId);
}

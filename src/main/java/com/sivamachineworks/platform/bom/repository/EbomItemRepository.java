package com.sivamachineworks.platform.bom.repository;

import com.sivamachineworks.platform.bom.domain.EbomItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EbomItemRepository extends JpaRepository<EbomItem, UUID> {
    List<EbomItem> findByEbomHeaderId(UUID ebomHeaderId);
    List<EbomItem> findByEbomHeaderIdAndParentItemIsNull(UUID ebomHeaderId);
    List<EbomItem> findByParentItemId(UUID parentItemId);
    List<EbomItem> findByEbomHeaderIdAndItemType(UUID ebomHeaderId, String itemType);
}

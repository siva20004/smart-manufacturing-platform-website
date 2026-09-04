package com.sivamachineworks.platform.scm.repository;

import com.sivamachineworks.platform.scm.domain.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, UUID> {
    Optional<GoodsReceipt> findByGrCode(String grCode);
    boolean existsByGrCode(String grCode);
}

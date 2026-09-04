package com.sivamachineworks.platform.scm.quotation.repository;

import com.sivamachineworks.platform.scm.quotation.domain.SupplierQuotationExtraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SupplierQuotationExtractionRepository extends JpaRepository<SupplierQuotationExtraction, UUID> {
    List<SupplierQuotationExtraction> findByStatusOrderByCreatedAtDesc(String status);
}

package com.sivamachineworks.platform.sales.repository;

import com.sivamachineworks.platform.sales.domain.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface QuotationRepository extends JpaRepository<Quotation, UUID> {
    Optional<Quotation> findByQuotationCodeAndRevisionLetter(String quotationCode, String revisionLetter);
    boolean existsByQuotationCodeAndRevisionLetter(String quotationCode, String revisionLetter);
}

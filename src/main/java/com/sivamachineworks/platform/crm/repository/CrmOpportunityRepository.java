package com.sivamachineworks.platform.crm.repository;

import com.sivamachineworks.platform.crm.domain.CrmOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrmOpportunityRepository extends JpaRepository<CrmOpportunity, UUID> {
    Optional<CrmOpportunity> findByOpportunityCode(String opportunityCode);
    boolean existsByOpportunityCode(String opportunityCode);
    List<CrmOpportunity> findByCustomerId(UUID customerId);
    List<CrmOpportunity> findByStage(String stage);
}

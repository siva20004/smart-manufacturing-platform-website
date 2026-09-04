package com.sivamachineworks.platform.crm.repository;

import com.sivamachineworks.platform.crm.domain.CustomerContact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CustomerContactRepository extends JpaRepository<CustomerContact, UUID> {
    List<CustomerContact> findByCustomerId(UUID customerId);
}

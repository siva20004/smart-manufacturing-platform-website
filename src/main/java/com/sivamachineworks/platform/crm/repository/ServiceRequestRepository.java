package com.sivamachineworks.platform.crm.repository;

import com.sivamachineworks.platform.crm.domain.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
    Optional<ServiceRequest> findByTicketNumber(String ticketNumber);
    boolean existsByTicketNumber(String ticketNumber);
    List<ServiceRequest> findByCustomerId(UUID customerId);
    List<ServiceRequest> findByStatus(String status);
}

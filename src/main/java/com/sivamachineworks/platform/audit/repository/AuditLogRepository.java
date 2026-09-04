package com.sivamachineworks.platform.audit.repository;

import com.sivamachineworks.platform.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    java.util.List<AuditLog> findByEntityNameAndEntityId(String entityName, UUID entityId);
}

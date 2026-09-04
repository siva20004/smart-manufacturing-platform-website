package com.sivamachineworks.platform.audit.service;

import com.sivamachineworks.platform.audit.domain.AuditLog;
import com.sivamachineworks.platform.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void log(String entityName, UUID entityId, String action, String changedData, UUID userId, String ipAddress) {
        AuditLog auditLog = new AuditLog(
                UUID.randomUUID(),
                entityName,
                entityId,
                action,
                changedData,
                userId,
                ipAddress,
                Instant.now()
        );
        auditLogRepository.saveAndFlush(auditLog);
    }
}

package com.sivamachineworks.platform.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
    UUID id,
    String entityName,
    UUID entityId,
    String action,
    String changedData,
    UUID userId,
    String ipAddress,
    Instant createdAt
) {}

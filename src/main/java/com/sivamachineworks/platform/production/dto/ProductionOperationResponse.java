package com.sivamachineworks.platform.production.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductionOperationResponse(
    UUID id,
    Integer operationSeq,
    String operationName,
    String workCenterCode,
    String status,
    BigDecimal plannedHours,
    BigDecimal actualHours,
    Instant startedAt,
    Instant completedAt,
    UUID assignedTo,
    String notes
) {}

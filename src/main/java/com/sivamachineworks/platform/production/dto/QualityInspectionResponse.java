package com.sivamachineworks.platform.production.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QualityInspectionResponse(
    UUID id,
    String inspectionCode,
    UUID productionOrderId,
    String inspectionType,
    String status,
    BigDecimal fatSpindleRunoutMm,
    BigDecimal fatPositioningAccuracyMm,
    UUID inspectorId,
    Instant inspectedAt,
    String notes
) {}

package com.sivamachineworks.platform.production.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record SubmitQualityInspectionRequest(
    @NotBlank String inspectionCode,
    String inspectionType,
    @NotBlank String status,
    BigDecimal fatSpindleRunoutMm,
    BigDecimal fatPositioningAccuracyMm,
    String notes
) {}

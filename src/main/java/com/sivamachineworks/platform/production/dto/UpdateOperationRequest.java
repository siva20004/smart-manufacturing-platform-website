package com.sivamachineworks.platform.production.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record UpdateOperationRequest(
    @NotBlank String status,
    BigDecimal actualHours,
    String notes
) {}

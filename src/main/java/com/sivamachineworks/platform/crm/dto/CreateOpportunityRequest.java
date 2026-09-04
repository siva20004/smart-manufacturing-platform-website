package com.sivamachineworks.platform.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateOpportunityRequest(
    @NotBlank(message = "Opportunity code is required")
    String opportunityCode,

    @NotNull(message = "Customer ID is required")
    UUID customerId,

    UUID primaryContactId,
    UUID productId,

    @NotBlank(message = "Name is required")
    String name,

    String stage,

    @NotNull(message = "Estimated value is required")
    BigDecimal estimatedValue,

    BigDecimal probabilityPct,
    LocalDate expectedCloseDate
) {}

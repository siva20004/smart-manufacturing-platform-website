package com.sivamachineworks.platform.crm.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record OpportunityResponse(
    UUID id,
    String opportunityCode,
    UUID customerId,
    String customerName,
    UUID primaryContactId,
    String contactName,
    UUID productId,
    String productName,
    String name,
    String stage,
    BigDecimal estimatedValue,
    String currency,
    BigDecimal probabilityPct,
    LocalDate expectedCloseDate,
    Instant createdAt
) {}

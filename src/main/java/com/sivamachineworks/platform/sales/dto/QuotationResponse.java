package com.sivamachineworks.platform.sales.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuotationResponse(
    UUID id,
    String quotationCode,
    String revisionLetter,
    UUID customerId,
    String customerName,
    String status,
    BigDecimal subtotalAmount,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    String currency,
    LocalDate validUntil,
    List<QuotationItemResponse> items,
    Instant createdAt
) {}

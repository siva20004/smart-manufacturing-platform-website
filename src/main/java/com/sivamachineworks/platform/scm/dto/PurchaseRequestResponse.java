package com.sivamachineworks.platform.scm.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PurchaseRequestResponse(
    UUID id,
    String prCode,
    UUID requestedBy,
    String sourceType,
    UUID salesOrderId,
    String status,
    BigDecimal totalEstimatedAmount,
    UUID approvedBy,
    Instant approvedAt,
    List<PurchaseRequestItemResponse> items,
    Instant createdAt
) {}

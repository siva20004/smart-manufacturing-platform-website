package com.sivamachineworks.platform.production.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProductionOrderResponse(
    UUID id,
    String orderCode,
    UUID salesOrderId,
    UUID productId,
    String productNumber,
    String productName,
    UUID mbomHeaderId,
    String mbomRevision,
    UUID warehouseId,
    String warehouseCode,
    String plantLocation,
    BigDecimal quantityPlanned,
    BigDecimal quantityCompleted,
    LocalDate plannedStartDate,
    LocalDate plannedCompletionDate,
    Instant actualStartDate,
    Instant actualCompletionDate,
    String status,
    List<ProductionOperationResponse> operations,
    Instant createdAt
) {}

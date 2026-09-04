package com.sivamachineworks.platform.sales.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SalesOrderResponse(
    UUID id,
    String soCode,
    UUID quotationId,
    UUID customerId,
    String customerName,
    String customerPoNumber,
    LocalDate orderDate,
    LocalDate requestedDeliveryDate,
    String plantLocation,
    String status,
    BigDecimal subtotalAmount,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    String currency,
    UUID confirmedBy,
    Instant confirmedAt,
    List<SalesOrderItemResponse> items,
    Instant createdAt
) {}

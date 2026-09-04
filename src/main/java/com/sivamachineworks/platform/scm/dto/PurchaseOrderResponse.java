package com.sivamachineworks.platform.scm.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResponse(
    UUID id,
    String poCode,
    UUID supplierId,
    String supplierName,
    UUID purchaseRequestId,
    UUID warehouseId,
    String warehouseCode,
    LocalDate orderDate,
    LocalDate expectedDeliveryDate,
    String status,
    BigDecimal subtotalAmount,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    String currency,
    List<PurchaseOrderItemResponse> items,
    Instant createdAt
) {}

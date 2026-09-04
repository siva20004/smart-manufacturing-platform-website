package com.sivamachineworks.platform.scm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreatePurchaseOrderRequest(
    @NotBlank(message = "PO code is required")
    String poCode,

    @NotNull(message = "Supplier ID is required")
    UUID supplierId,

    UUID purchaseRequestId,

    @NotNull(message = "Warehouse ID is required")
    UUID warehouseId,

    LocalDate expectedDeliveryDate,

    @NotEmpty(message = "Items are required")
    List<PurchaseOrderItemDto> items
) {}

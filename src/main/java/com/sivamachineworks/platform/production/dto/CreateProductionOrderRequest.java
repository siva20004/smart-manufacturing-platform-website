package com.sivamachineworks.platform.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateProductionOrderRequest(
    @NotBlank(message = "Order code is required")
    String orderCode,

    UUID salesOrderId,

    @NotNull(message = "Product ID is required")
    UUID productId,

    UUID warehouseId,
    String plantLocation,

    @NotNull(message = "Quantity planned is required")
    BigDecimal quantityPlanned,

    LocalDate plannedStartDate,
    LocalDate plannedCompletionDate
) {}

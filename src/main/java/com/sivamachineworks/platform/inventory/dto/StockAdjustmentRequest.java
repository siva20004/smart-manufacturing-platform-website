package com.sivamachineworks.platform.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record StockAdjustmentRequest(
    @NotNull(message = "Warehouse ID is required")
    UUID warehouseId,

    @NotBlank(message = "Part number is required")
    String partNumber,

    @NotNull(message = "Quantity is required")
    BigDecimal quantity,

    String notes
) {}

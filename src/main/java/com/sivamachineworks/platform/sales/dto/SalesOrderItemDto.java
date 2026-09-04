package com.sivamachineworks.platform.sales.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderItemDto(
    @NotNull UUID productId,
    @NotNull BigDecimal quantity,
    @NotNull BigDecimal unitPrice
) {}

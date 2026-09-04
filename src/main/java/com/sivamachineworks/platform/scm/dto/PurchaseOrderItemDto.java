package com.sivamachineworks.platform.scm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PurchaseOrderItemDto(
    @NotBlank String partNumber,
    String description,
    @NotNull BigDecimal quantityOrdered,
    String uom,
    @NotNull BigDecimal unitPrice
) {}

package com.sivamachineworks.platform.scm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PurchaseRequestItemDto(
    @NotBlank String partNumber,
    String description,
    @NotNull BigDecimal quantityRequested,
    String uom,
    BigDecimal estimatedUnitPrice,
    LocalDate requiredByDate,
    UUID suggestedSupplierId
) {}

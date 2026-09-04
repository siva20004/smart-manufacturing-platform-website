package com.sivamachineworks.platform.scm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record CreatePurchaseRequestDto(
    @NotBlank(message = "PR code is required")
    String prCode,

    UUID salesOrderId,
    String sourceType,

    @NotEmpty(message = "Items are required")
    List<PurchaseRequestItemDto> items
) {}

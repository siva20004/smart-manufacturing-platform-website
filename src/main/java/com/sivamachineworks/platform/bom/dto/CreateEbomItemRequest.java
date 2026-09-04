package com.sivamachineworks.platform.bom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateEbomItemRequest(
    UUID parentItemId,

    @NotNull(message = "Item sequence number is required")
    Integer itemSeq,

    @NotBlank(message = "Part number is required")
    String partNumber,

    String description,
    String itemType,

    @NotNull(message = "Quantity is required")
    BigDecimal quantity,

    String uom,
    UUID documentId,
    LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    String notes
) {}

package com.sivamachineworks.platform.bom.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EbomItemResponse(
    UUID id,
    UUID ebomHeaderId,
    UUID parentItemId,
    Integer itemSeq,
    String partNumber,
    String description,
    String itemType,
    BigDecimal quantity,
    String uom,
    UUID documentId,
    LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    String notes,
    Instant createdAt
) {}

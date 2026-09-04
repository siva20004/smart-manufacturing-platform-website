package com.sivamachineworks.platform.bom.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EbomTreeNodeDto(
    UUID id,
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
    List<EbomTreeNodeDto> children
) {}

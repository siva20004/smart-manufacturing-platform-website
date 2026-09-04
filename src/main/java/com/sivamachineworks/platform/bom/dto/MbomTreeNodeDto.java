package com.sivamachineworks.platform.bom.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MbomTreeNodeDto(
    UUID id,
    UUID parentItemId,
    UUID sourceEbomItemId,
    Integer itemSeq,
    String partNumber,
    String description,
    String itemType,
    BigDecimal quantity,
    String uom,
    String workCenter,
    Integer operationSeq,
    String operationName,
    Boolean isConsumedPerUnit,
    BigDecimal scrapFactor,
    String notes,
    List<MbomTreeNodeDto> children
) {}

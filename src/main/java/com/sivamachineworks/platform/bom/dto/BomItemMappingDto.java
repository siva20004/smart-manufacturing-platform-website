package com.sivamachineworks.platform.bom.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BomItemMappingDto(
    UUID ebomItemId,
    String ebomPartNumber,
    UUID mbomItemId,
    String mbomPartNumber,
    BigDecimal quantity,
    String workCenter,
    Integer operationSeq,
    String operationName
) {}

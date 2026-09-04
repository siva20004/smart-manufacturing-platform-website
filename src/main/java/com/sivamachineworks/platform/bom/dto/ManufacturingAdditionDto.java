package com.sivamachineworks.platform.bom.dto;

import java.math.BigDecimal;

public record ManufacturingAdditionDto(
    String parentAssemblyPartNumber,
    Integer itemSeq,
    String partNumber,
    String description,
    String itemType,
    BigDecimal quantity,
    String uom,
    String workCenter,
    Integer operationSeq,
    String notes
) {}

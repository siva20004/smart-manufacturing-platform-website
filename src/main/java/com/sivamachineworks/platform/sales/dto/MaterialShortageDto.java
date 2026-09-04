package com.sivamachineworks.platform.sales.dto;

import java.math.BigDecimal;

public record MaterialShortageDto(
    String partNumber,
    String description,
    BigDecimal requiredQty,
    BigDecimal availableQty,
    BigDecimal shortageQty,
    String uom
) {}

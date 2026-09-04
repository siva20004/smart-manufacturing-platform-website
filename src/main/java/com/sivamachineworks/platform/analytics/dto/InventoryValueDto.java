package com.sivamachineworks.platform.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryValueDto(
    UUID warehouseId,
    String warehouseCode,
    String plantLocation,
    Long uniquePartsCount,
    BigDecimal totalInventoryValue
) {}

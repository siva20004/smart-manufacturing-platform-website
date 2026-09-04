package com.sivamachineworks.platform.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockResponse(
    UUID id,
    UUID warehouseId,
    String warehouseCode,
    String partNumber,
    String description,
    BigDecimal qtyOnHand,
    BigDecimal qtyReserved,
    BigDecimal qtyAvailable,
    String uom,
    BigDecimal unitCost
) {}

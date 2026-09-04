package com.sivamachineworks.platform.sales.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MaterialReservationDto(
    String partNumber,
    String description,
    BigDecimal quantityReserved,
    String uom,
    UUID warehouseId,
    String warehouseCode
) {}

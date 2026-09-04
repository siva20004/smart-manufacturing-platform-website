package com.sivamachineworks.platform.sales.dto;

import java.util.List;
import java.util.UUID;

public record OrderConfirmationResponse(
    SalesOrderResponse order,
    String applicableBomType,
    String applicableBomRevision,
    List<MaterialReservationDto> reservedMaterials,
    List<MaterialShortageDto> shortages,
    UUID materialRequirementRunId
) {}

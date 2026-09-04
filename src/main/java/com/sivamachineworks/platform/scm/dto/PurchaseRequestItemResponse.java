package com.sivamachineworks.platform.scm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PurchaseRequestItemResponse(
    UUID id,
    Integer itemSeq,
    String partNumber,
    String description,
    BigDecimal quantityRequested,
    String uom,
    BigDecimal estimatedUnitPrice,
    LocalDate requiredByDate,
    UUID suggestedSupplierId
) {}

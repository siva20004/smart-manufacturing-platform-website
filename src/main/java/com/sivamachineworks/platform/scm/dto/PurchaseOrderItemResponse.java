package com.sivamachineworks.platform.scm.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemResponse(
    UUID id,
    Integer itemSeq,
    String partNumber,
    String description,
    BigDecimal quantityOrdered,
    BigDecimal quantityReceived,
    String uom,
    BigDecimal unitPrice,
    BigDecimal lineTotal,
    String status
) {}

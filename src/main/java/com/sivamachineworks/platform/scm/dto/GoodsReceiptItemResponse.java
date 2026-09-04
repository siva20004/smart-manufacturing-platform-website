package com.sivamachineworks.platform.scm.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GoodsReceiptItemResponse(
    UUID id,
    UUID purchaseOrderItemId,
    Integer itemSeq,
    String partNumber,
    BigDecimal quantityReceived,
    String uom,
    BigDecimal unitCost,
    String notes
) {}

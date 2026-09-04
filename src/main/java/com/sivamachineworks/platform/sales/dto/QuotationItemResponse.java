package com.sivamachineworks.platform.sales.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record QuotationItemResponse(
    UUID id,
    Integer itemSeq,
    UUID productId,
    String productNumber,
    String productName,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {}

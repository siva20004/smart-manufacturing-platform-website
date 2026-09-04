package com.sivamachineworks.platform.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesByProductDto(
    UUID productId,
    String productNumber,
    String productName,
    BigDecimal totalQuantitySold,
    BigDecimal totalRevenue
) {}

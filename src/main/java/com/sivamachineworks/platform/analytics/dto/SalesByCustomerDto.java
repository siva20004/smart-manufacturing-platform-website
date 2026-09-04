package com.sivamachineworks.platform.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesByCustomerDto(
    UUID customerId,
    String customerCode,
    String companyName,
    Long totalOrders,
    BigDecimal totalRevenue
) {}

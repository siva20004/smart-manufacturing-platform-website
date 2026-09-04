package com.sivamachineworks.platform.analytics.dto;

import java.math.BigDecimal;

public record OrderVolumeDto(
    String orderStatus,
    Long orderCount,
    BigDecimal totalAmount
) {}

package com.sivamachineworks.platform.analytics.dto;

import java.math.BigDecimal;

public record ProductionAnalyticsDto(
    Long totalOrdersPlanned,
    Long totalOrdersCompleted,
    Long activeOrdersInProgress,
    BigDecimal totalUnitsProduced,
    Double onTimeCompletionRatePct
) {}

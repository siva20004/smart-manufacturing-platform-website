package com.sivamachineworks.platform.analytics.dto;

public record ServiceMetricsDto(
    Long totalTickets,
    Long openTickets,
    Long inProgressTickets,
    Long resolvedTickets,
    Long criticalTickets
) {}

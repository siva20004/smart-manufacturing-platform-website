package com.sivamachineworks.platform.health.dto;

public record HealthResponse(
    String status,
    String version,
    java.time.Instant timestamp
) {}

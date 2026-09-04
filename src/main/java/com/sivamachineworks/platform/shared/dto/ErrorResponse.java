package com.sivamachineworks.platform.shared.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    boolean success,
    Instant timestamp,
    Error error,
    String correlationId
) {
    public record Error(
        String code,
        String message,
        List<ErrorDetail> details
    ) {}
}

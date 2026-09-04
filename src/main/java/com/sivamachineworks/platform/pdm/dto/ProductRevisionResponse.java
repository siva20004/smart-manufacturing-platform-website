package com.sivamachineworks.platform.pdm.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProductRevisionResponse(
    UUID id,
    UUID productId,
    String revisionNumber,
    String status,
    String changeDescription,
    LocalDate effectiveDate,
    UUID approvedBy,
    Instant approvedAt,
    Instant createdAt
) {}

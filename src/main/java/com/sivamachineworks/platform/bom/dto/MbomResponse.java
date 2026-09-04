package com.sivamachineworks.platform.bom.dto;

import java.time.Instant;
import java.util.UUID;

public record MbomResponse(
    UUID id,
    UUID ebomHeaderId,
    UUID productId,
    String productNumber,
    String productName,
    String revisionCode,
    String description,
    String plantLocation,
    String status,
    UUID approvedBy,
    Instant approvedAt,
    Instant createdAt,
    Instant updatedAt
) {}

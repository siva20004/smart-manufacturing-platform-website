package com.sivamachineworks.platform.pdm.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String productNumber,
    String name,
    String description,
    String category,
    String uom,
    BigDecimal listPrice,
    BigDecimal standardCost,
    Integer leadTimeWeeks,
    String status,
    List<ProductRevisionResponse> revisions,
    Instant createdAt,
    Instant updatedAt
) {}

package com.sivamachineworks.platform.pdm.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DocumentResponse(
    UUID id,
    String documentNumber,
    String title,
    String docType,
    UUID productId,
    Boolean isRestricted,
    List<DocumentVersionResponse> versions,
    Instant createdAt
) {}

package com.sivamachineworks.platform.pdm.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionResponse(
    UUID id,
    UUID documentId,
    Integer versionNumber,
    String revisionCode,
    String fileName,
    Long fileSizeBytes,
    String mimeType,
    String fileHashSha256,
    String cadMetadata,
    String status,
    UUID approvedBy,
    Instant approvedAt,
    Instant createdAt
) {}

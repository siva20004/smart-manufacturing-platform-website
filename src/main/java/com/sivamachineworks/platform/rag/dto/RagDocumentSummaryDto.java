package com.sivamachineworks.platform.rag.dto;

import java.time.Instant;
import java.util.UUID;

public record RagDocumentSummaryDto(
    UUID id,
    String docCode,
    String title,
    String docType,
    int versionNumber,
    String allowedRoles,
    int chunkCount,
    Instant createdAt
) {}

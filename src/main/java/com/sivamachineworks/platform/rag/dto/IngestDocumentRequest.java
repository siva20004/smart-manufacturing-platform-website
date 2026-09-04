package com.sivamachineworks.platform.rag.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record IngestDocumentRequest(
    @NotBlank(message = "Document code is required")
    String docCode,

    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Document type is required")
    String docType,

    UUID productId,

    @NotBlank(message = "Document content is required")
    String rawContent,

    @NotBlank(message = "Allowed roles are required")
    String allowedRoles // Comma-separated: "ENGINEERING,PRODUCTION,ADMIN"
) {}

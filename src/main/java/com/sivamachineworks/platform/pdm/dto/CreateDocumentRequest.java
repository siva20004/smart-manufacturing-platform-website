package com.sivamachineworks.platform.pdm.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateDocumentRequest(
    @NotBlank(message = "Document number is required")
    String documentNumber,

    @NotBlank(message = "Document title is required")
    String title,

    @NotBlank(message = "Document type is required")
    String docType,

    UUID productId,
    Boolean isRestricted
) {}

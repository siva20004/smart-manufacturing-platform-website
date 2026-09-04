package com.sivamachineworks.platform.rag.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentRagQueryRequest(
    @NotBlank(message = "Question query is required")
    String question,

    String targetDocType
) {}

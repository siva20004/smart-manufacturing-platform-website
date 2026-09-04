package com.sivamachineworks.platform.bom.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEbomRevisionRequest(
    @NotBlank(message = "Revision code is required")
    String revisionCode,

    String description
) {}

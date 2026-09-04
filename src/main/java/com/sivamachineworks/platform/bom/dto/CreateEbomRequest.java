package com.sivamachineworks.platform.bom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateEbomRequest(
    @NotNull(message = "Product ID is required")
    UUID productId,

    @NotBlank(message = "Revision code is required")
    String revisionCode,

    String description
) {}

package com.sivamachineworks.platform.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AiQueryRequest(
    @NotBlank(message = "Question query cannot be blank")
    String question,

    String focusDomain
) {}

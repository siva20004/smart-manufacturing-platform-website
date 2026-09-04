package com.sivamachineworks.platform.pdm.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateRevisionRequest(
    @NotBlank(message = "Revision number is required")
    String revisionNumber,

    String changeDescription,
    LocalDate effectiveDate
) {}

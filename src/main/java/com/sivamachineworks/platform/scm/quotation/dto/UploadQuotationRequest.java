package com.sivamachineworks.platform.scm.quotation.dto;

import jakarta.validation.constraints.NotBlank;

public record UploadQuotationRequest(
    @NotBlank(message = "File name is required")
    String docFileName,

    @NotBlank(message = "Quotation document content/text is required")
    String quotationText
) {}

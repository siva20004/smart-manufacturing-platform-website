package com.sivamachineworks.platform.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateQuotationRequest(
    @NotBlank(message = "Quotation code is required")
    String quotationCode,

    @NotNull(message = "Customer ID is required")
    UUID customerId,

    LocalDate validUntil,
    String paymentTerms,
    String deliveryIncoterms,

    @NotEmpty(message = "At least one item is required")
    List<QuotationItemDto> items
) {}

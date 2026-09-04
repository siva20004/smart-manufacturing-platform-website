package com.sivamachineworks.platform.scm.quotation.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReviewQuotationRequest(
    @NotBlank(message = "Action is required (APPROVE or REJECT)")
    String action, // APPROVE, REJECT

    UUID confirmedSupplierId,
    String confirmedPartNumber,
    BigDecimal confirmedQuantity,
    BigDecimal confirmedUnitPrice,
    LocalDate confirmedDeliveryDate,
    String confirmedPaymentTerms,
    String reviewNotes
) {}

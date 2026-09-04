package com.sivamachineworks.platform.scm.quotation.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record QuotationExtractionDto(
    UUID id,
    String docFileName,
    String rawText,
    UUID supplierId,
    String supplierName,
    String quotationNumber,
    String partNumber,
    BigDecimal quantity,
    BigDecimal unitPrice,
    String currency,
    LocalDate deliveryDate,
    String paymentTerms,
    BigDecimal confidenceScore,
    String status,
    String validationWarnings,
    String reviewerUsername,
    Instant reviewedAt,
    String reviewNotes,
    UUID generatedPoId,
    String generatedPoCode,
    Instant createdAt
) {}

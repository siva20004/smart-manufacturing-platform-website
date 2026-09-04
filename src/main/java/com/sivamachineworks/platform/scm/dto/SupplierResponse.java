package com.sivamachineworks.platform.scm.dto;

import java.time.Instant;
import java.util.UUID;

public record SupplierResponse(
    UUID id,
    String supplierCode,
    String companyName,
    String taxId,
    String country,
    String contactEmail,
    String phone,
    String address,
    String paymentTerms,
    String status,
    Instant createdAt
) {}

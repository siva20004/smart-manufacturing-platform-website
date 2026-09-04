package com.sivamachineworks.platform.scm.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSupplierRequest(
    @NotBlank(message = "Supplier code is required")
    String supplierCode,

    @NotBlank(message = "Company name is required")
    String companyName,

    String taxId,
    String country,
    String contactEmail,
    String phone,
    String address,
    String paymentTerms
) {}

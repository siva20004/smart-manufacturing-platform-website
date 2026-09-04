package com.sivamachineworks.platform.crm.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateCustomerRequest(
    @NotBlank(message = "Customer code is required")
    String customerCode,

    @NotBlank(message = "Company name is required")
    String companyName,

    String industry,
    String country,
    BigDecimal creditLimit,
    String paymentTerms,
    String contactName,
    String contactEmail,
    String phone,
    String address
) {}

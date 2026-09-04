package com.sivamachineworks.platform.crm.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
    UUID id,
    String customerCode,
    String companyName,
    String industry,
    String country,
    String status,
    BigDecimal creditLimit,
    String paymentTerms,
    String contactName,
    String contactEmail,
    String phone,
    String address,
    Instant createdAt
) {}

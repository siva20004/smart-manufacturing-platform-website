package com.sivamachineworks.platform.crm.dto;

import java.time.Instant;
import java.util.UUID;

public record ContactResponse(
    UUID id,
    UUID customerId,
    String firstName,
    String lastName,
    String email,
    String phone,
    String jobTitle,
    String department,
    Boolean isPrimary,
    Boolean isActive,
    Instant createdAt
) {}

package com.sivamachineworks.platform.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateContactRequest(
    @NotNull(message = "Customer ID is required")
    UUID customerId,

    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    String email,
    String phone,
    String jobTitle,
    String department,
    Boolean isPrimary
) {}

package com.sivamachineworks.platform.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateServiceRequestDto(
    @NotBlank(message = "Ticket number is required")
    String ticketNumber,

    @NotNull(message = "Customer ID is required")
    UUID customerId,

    UUID productId,
    String machineSerialNo,

    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Reported issue is required")
    String reportedIssue,

    String priority
) {}

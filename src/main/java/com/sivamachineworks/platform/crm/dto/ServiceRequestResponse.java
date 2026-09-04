package com.sivamachineworks.platform.crm.dto;

import java.time.Instant;
import java.util.UUID;

public record ServiceRequestResponse(
    UUID id,
    String ticketNumber,
    UUID customerId,
    String customerName,
    UUID productId,
    String productName,
    String machineSerialNo,
    String title,
    String reportedIssue,
    String priority,
    String status,
    UUID assignedTechnicianId,
    String resolutionNotes,
    Instant resolvedAt,
    Instant createdAt
) {}

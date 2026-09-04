package com.sivamachineworks.platform.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateSalesOrderRequest(
    @NotBlank(message = "Sales order code is required")
    String soCode,

    UUID quotationId,

    @NotNull(message = "Customer ID is required")
    UUID customerId,

    String customerPoNumber,
    LocalDate requestedDeliveryDate,
    String plantLocation,
    String shippingAddress,
    String specialInstructions,

    @NotEmpty(message = "At least one item is required")
    List<SalesOrderItemDto> items
) {}

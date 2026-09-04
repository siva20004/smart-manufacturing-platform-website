package com.sivamachineworks.platform.scm.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record GoodsReceiptItemDto(
    @NotNull UUID purchaseOrderItemId,
    @NotNull BigDecimal quantityReceived,
    String notes
) {}

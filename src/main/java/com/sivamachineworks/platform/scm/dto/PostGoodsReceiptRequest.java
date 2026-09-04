package com.sivamachineworks.platform.scm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record PostGoodsReceiptRequest(
    @NotBlank(message = "Goods Receipt code is required")
    String grCode,

    @NotNull(message = "Purchase Order ID is required")
    UUID purchaseOrderId,

    String deliveryNoteNo,

    @NotEmpty(message = "Items are required")
    List<GoodsReceiptItemDto> items
) {}

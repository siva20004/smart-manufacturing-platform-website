package com.sivamachineworks.platform.scm.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GoodsReceiptResponse(
    UUID id,
    String grCode,
    UUID purchaseOrderId,
    String poCode,
    UUID supplierId,
    String supplierName,
    UUID warehouseId,
    String warehouseCode,
    String deliveryNoteNo,
    String status,
    UUID receivedBy,
    Instant receivedAt,
    List<GoodsReceiptItemResponse> items
) {}

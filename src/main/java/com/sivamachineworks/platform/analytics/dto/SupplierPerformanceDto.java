package com.sivamachineworks.platform.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SupplierPerformanceDto(
    UUID supplierId,
    String supplierCode,
    String companyName,
    Long totalPurchaseOrders,
    BigDecimal totalSpend,
    BigDecimal totalUnitsOrdered,
    BigDecimal totalUnitsReceived
) {}

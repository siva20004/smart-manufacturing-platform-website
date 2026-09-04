package com.sivamachineworks.platform.pdm.dto;

import java.math.BigDecimal;

public record UpdateProductRequest(
    String name,
    String description,
    String category,
    String uom,
    BigDecimal listPrice,
    BigDecimal standardCost,
    Integer leadTimeWeeks,
    String status
) {}

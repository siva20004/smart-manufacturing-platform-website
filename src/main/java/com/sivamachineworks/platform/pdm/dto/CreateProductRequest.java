package com.sivamachineworks.platform.pdm.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank(message = "Product number is required")
    String productNumber,

    @NotBlank(message = "Product name is required")
    String name,

    String description,

    @NotBlank(message = "Category is required")
    String category,

    String uom,
    BigDecimal listPrice,
    BigDecimal standardCost,
    Integer leadTimeWeeks,
    String status
) {}

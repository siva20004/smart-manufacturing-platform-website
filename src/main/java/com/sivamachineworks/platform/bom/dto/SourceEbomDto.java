package com.sivamachineworks.platform.bom.dto;

import java.util.UUID;

public record SourceEbomDto(
    UUID id,
    String productNumber,
    String productName,
    String revisionCode,
    String status
) {}

package com.sivamachineworks.platform.bom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record TransformEbomRequest(
    @NotNull(message = "Source eBOM Header ID is required")
    UUID ebomHeaderId,

    @NotBlank(message = "Plant location is required")
    String plantLocation,

    @NotBlank(message = "mBOM revision code is required")
    String mbomRevisionCode,

    String description,

    List<AssemblyMappingDto> assemblyMappings,
    List<ManufacturingAdditionDto> manufacturingAdditions
) {}

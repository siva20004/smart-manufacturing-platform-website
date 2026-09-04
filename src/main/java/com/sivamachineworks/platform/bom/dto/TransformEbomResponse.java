package com.sivamachineworks.platform.bom.dto;

import java.util.List;

public record TransformEbomResponse(
    SourceEbomDto sourceEbom,
    String sourceRevision,
    MbomResponse generatedMbom,
    List<BomItemMappingDto> mappings,
    List<String> warnings,
    List<String> errors
) {}

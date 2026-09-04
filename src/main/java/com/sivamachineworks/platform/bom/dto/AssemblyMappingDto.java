package com.sivamachineworks.platform.bom.dto;

import java.util.List;
import java.util.UUID;

public record AssemblyMappingDto(
    String assemblyName,
    String assemblyPartNumber,
    String workCenter,
    Integer operationSeq,
    String operationName,
    List<UUID> mappedEbomItemIds
) {}

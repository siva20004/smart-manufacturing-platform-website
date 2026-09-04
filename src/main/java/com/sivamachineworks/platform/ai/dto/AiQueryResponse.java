package com.sivamachineworks.platform.ai.dto;

import java.util.List;

public record AiQueryResponse(
    String question,
    String answer,
    Double confidenceScore,
    boolean hasSufficientData,
    List<AiSourceCitation> citations,
    int retrievedEntitiesCount
) {}

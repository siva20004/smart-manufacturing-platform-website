package com.sivamachineworks.platform.rag.dto;

import java.util.List;

public record DocumentRagQueryResponse(
    String question,
    String answer,
    boolean foundAuthorizedMatches,
    List<RagChunkCitation> citations,
    int totalAuthorizedChunksSearched
) {}

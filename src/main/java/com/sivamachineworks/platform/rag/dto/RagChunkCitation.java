package com.sivamachineworks.platform.rag.dto;

import java.util.UUID;

public record RagChunkCitation(
    UUID documentId,
    String docCode,
    String docTitle,
    String docType,
    int chunkIndex,
    String sectionTitle,
    double similarityScore,
    String excerpt
) {}

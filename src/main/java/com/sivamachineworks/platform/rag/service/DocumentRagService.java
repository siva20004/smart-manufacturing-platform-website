package com.sivamachineworks.platform.rag.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.rag.domain.RagDocument;
import com.sivamachineworks.platform.rag.domain.RagDocumentChunk;
import com.sivamachineworks.platform.rag.dto.DocumentRagQueryRequest;
import com.sivamachineworks.platform.rag.dto.DocumentRagQueryResponse;
import com.sivamachineworks.platform.rag.dto.RagChunkCitation;
import com.sivamachineworks.platform.rag.dto.RagDocumentSummaryDto;
import com.sivamachineworks.platform.rag.repository.RagDocumentChunkRepository;
import com.sivamachineworks.platform.rag.repository.RagDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentRagService {

    private final RagDocumentRepository documentRepository;
    private final RagDocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final AuditLogService auditLogService;

    public DocumentRagService(
            RagDocumentRepository documentRepository,
            RagDocumentChunkRepository chunkRepository,
            EmbeddingService embeddingService,
            AuditLogService auditLogService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<RagDocumentSummaryDto> listAuthorizedDocuments(Set<String> userRoles) {
        return documentRepository.findAll().stream()
                .filter(doc -> isRoleAuthorized(doc.getAllowedRoles(), userRoles))
                .map(doc -> new RagDocumentSummaryDto(
                        doc.getId(),
                        doc.getDocCode(),
                        doc.getTitle(),
                        doc.getDocType(),
                        doc.getVersionNumber(),
                        doc.getAllowedRoles(),
                        doc.getChunks().size(),
                        doc.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentRagQueryResponse queryKnowledgeBase(
            DocumentRagQueryRequest req,
            Set<String> userRoles,
            UUID userId,
            String ipAddress) {

        // Step 1: Compute query embedding vector
        double[] queryVector = embeddingService.computeEmbedding(req.question());

        // Step 2: Retrieve all chunks from database
        List<RagDocumentChunk> allChunks = chunkRepository.findAll();
        List<RagChunkMatch> scoredMatches = new ArrayList<>();
        int authorizedChunksCount = 0;

        // Step 3: Strict RBAC Permission Filtering
        for (RagDocumentChunk chunk : allChunks) {
            // Check if user has at least one permitted role for this document chunk
            if (!isRoleAuthorized(chunk.getAllowedRoles(), userRoles)) {
                continue; // Strictly skip unauthorized document chunks (Zero Data Leakage)
            }

            if (req.targetDocType() != null && !req.targetDocType().isBlank()) {
                if (!req.targetDocType().equalsIgnoreCase(chunk.getDocument().getDocType())) {
                    continue;
                }
            }

            authorizedChunksCount++;

            // Step 4: Calculate Vector Cosine Similarity
            double[] chunkVector;
            if (chunk.getEmbeddingVector() != null && !chunk.getEmbeddingVector().isBlank()) {
                chunkVector = embeddingService.stringToVector(chunk.getEmbeddingVector());
            } else {
                chunkVector = embeddingService.computeEmbedding(chunk.getContent());
            }

            double similarity = embeddingService.calculateCosineSimilarity(queryVector, chunkVector);

            // Word-level token overlap score boost
            String[] queryWords = req.question().toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
            int matchWords = 0;
            String lowerContent = chunk.getContent().toLowerCase();
            for (String w : queryWords) {
                if (w.length() > 2 && lowerContent.contains(w)) {
                    matchWords++;
                }
            }
            if (matchWords > 0) {
                similarity = Math.max(similarity, 0.4 + (0.1 * Math.min(5, matchWords)));
            }

            if (similarity >= 0.15) { // Minimum threshold for relevance
                scoredMatches.add(new RagChunkMatch(chunk, similarity));
            }
        }

        // Sort by similarity descending
        scoredMatches.sort((a, b) -> Double.compare(b.similarity, a.similarity));

        // Step 5: Insufficient / No Authorized Data Handling
        if (scoredMatches.isEmpty()) {
            auditLogService.log(
                    "RagQuery",
                    UUID.randomUUID(),
                    "RAG_QUERY_NO_MATCH",
                    "Query: " + req.question() + " | UserRoles: " + userRoles + " | Matches: 0",
                    userId,
                    ipAddress
            );

            return new DocumentRagQueryResponse(
                    req.question(),
                    "I could not find any verified information matching your query in the internal company documents authorized for your role profile (" + userRoles + ").",
                    false,
                    List.of(),
                    authorizedChunksCount
            );
        }

        // Step 6: Build Grounded Response and Citations
        List<RagChunkMatch> topMatches = scoredMatches.subList(0, Math.min(3, scoredMatches.size()));
        List<RagChunkCitation> citations = new ArrayList<>();
        StringBuilder answerBuilder = new StringBuilder();

        answerBuilder.append("According to verified internal documentation (");
        for (int i = 0; i < topMatches.size(); i++) {
            RagChunkMatch match = topMatches.get(i);
            RagDocument doc = match.chunk.getDocument();
            if (i > 0) answerBuilder.append(", ");
            answerBuilder.append(doc.getDocCode());

            String excerpt = match.chunk.getContent();
            if (excerpt.length() > 180) {
                excerpt = excerpt.substring(0, 180) + "...";
            }

            citations.add(new RagChunkCitation(
                    doc.getId(),
                    doc.getDocCode(),
                    doc.getTitle(),
                    doc.getDocType(),
                    match.chunk.getChunkIndex(),
                    match.chunk.getSectionTitle(),
                    match.similarity,
                    excerpt
            ));
        }
        answerBuilder.append("):\n\n");

        for (RagChunkMatch match : topMatches) {
            answerBuilder.append("• ").append(match.chunk.getContent()).append("\n\n");
        }

        answerBuilder.append("Source references: ");
        for (RagChunkCitation c : citations) {
            answerBuilder.append("[").append(c.docCode()).append(" - ").append(c.sectionTitle()).append("] ");
        }

        // Step 7: Audit Log
        auditLogService.log(
                "RagQuery",
                UUID.randomUUID(),
                "RAG_QUERY_SUCCESS",
                "Query: " + req.question() + " | TopDoc: " + citations.get(0).docCode() + " | UserRoles: " + userRoles,
                userId,
                ipAddress
        );

        return new DocumentRagQueryResponse(
                req.question(),
                answerBuilder.toString().trim(),
                true,
                citations,
                authorizedChunksCount
        );
    }

    private boolean isRoleAuthorized(String allowedRolesStr, Set<String> userRoles) {
        if (allowedRolesStr == null || allowedRolesStr.isBlank()) return false;
        if (userRoles.contains("ADMIN") || userRoles.contains("ROLE_ADMIN")) return true;

        String[] allowed = allowedRolesStr.split(",");
        for (String a : allowed) {
            String roleName = a.trim().replace("ROLE_", "");
            if (userRoles.contains(roleName) || userRoles.contains("ROLE_" + roleName)) {
                return true;
            }
        }
        return false;
    }

    private static class RagChunkMatch {
        final RagDocumentChunk chunk;
        final double similarity;

        RagChunkMatch(RagDocumentChunk chunk, double similarity) {
            this.chunk = chunk;
            this.similarity = similarity;
        }
    }
}

package com.sivamachineworks.platform.rag.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.pdm.repository.ProductRepository;
import com.sivamachineworks.platform.rag.domain.RagDocument;
import com.sivamachineworks.platform.rag.domain.RagDocumentChunk;
import com.sivamachineworks.platform.rag.dto.IngestDocumentRequest;
import com.sivamachineworks.platform.rag.repository.RagDocumentRepository;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentIngestionService {

    private final RagDocumentRepository documentRepository;
    private final ProductRepository productRepository;
    private final EmbeddingService embeddingService;
    private final AuditLogService auditLogService;

    public DocumentIngestionService(
            RagDocumentRepository documentRepository,
            ProductRepository productRepository,
            EmbeddingService embeddingService,
            AuditLogService auditLogService) {
        this.documentRepository = documentRepository;
        this.productRepository = productRepository;
        this.embeddingService = embeddingService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public RagDocument ingestDocument(IngestDocumentRequest req, UUID userId, String ipAddress) {
        if (documentRepository.existsByDocCode(req.docCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "Document code already exists in RAG index: " + req.docCode());
        }

        Product product = null;
        if (req.productId() != null) {
            product = productRepository.findById(req.productId()).orElse(null);
        }

        RagDocument doc = new RagDocument();
        doc.setDocCode(req.docCode());
        doc.setTitle(req.title());
        doc.setDocType(req.docType());
        doc.setProduct(product);
        doc.setAllowedRoles(req.allowedRoles());
        doc.setVersionNumber(1);

        // Perform semantic text chunking (500 chars with overlap)
        List<String> rawChunks = chunkText(req.rawContent(), 400, 50);
        List<RagDocumentChunk> chunks = new ArrayList<>();

        for (int i = 0; i < rawChunks.size(); i++) {
            String chunkText = rawChunks.get(i);
            RagDocumentChunk chunk = new RagDocumentChunk();
            chunk.setDocument(doc);
            chunk.setChunkIndex(i + 1);
            chunk.setContent(chunkText);
            chunk.setTokenCount(chunkText.split("\\s+").length);
            chunk.setAllowedRoles(req.allowedRoles());
            chunk.setSectionTitle("Section " + (i + 1) + " of " + doc.getTitle());

            // Compute vector embedding
            double[] embedding = embeddingService.computeEmbedding(chunkText);
            chunk.setEmbeddingVector(embeddingService.vectorToString(embedding));

            chunks.add(chunk);
        }

        doc.setChunks(chunks);
        RagDocument saved = documentRepository.save(doc);

        auditLogService.log(
                "RagDocument",
                saved.getId(),
                "DOCUMENT_INGESTED",
                "Ingested document " + saved.getDocCode() + " with " + chunks.size() + " semantic chunks. Allowed roles: " + saved.getAllowedRoles(),
                userId,
                ipAddress
        );

        return saved;
    }

    private List<String> chunkText(String text, int targetChunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        String[] paragraphs = text.split("\\n+");
        StringBuilder current = new StringBuilder();

        for (String p : paragraphs) {
            if (current.length() + p.length() > targetChunkSize && current.length() > 0) {
                chunks.add(current.toString().trim());
                current = new StringBuilder();
            }
            if (current.length() > 0) current.append(" ");
            current.append(p);
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}

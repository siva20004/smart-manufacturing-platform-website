package com.sivamachineworks.platform.rag.repository;

import com.sivamachineworks.platform.rag.domain.RagDocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RagDocumentChunkRepository extends JpaRepository<RagDocumentChunk, UUID> {
    List<RagDocumentChunk> findByDocumentId(UUID documentId);
}

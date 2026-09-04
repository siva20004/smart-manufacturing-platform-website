package com.sivamachineworks.platform.rag.repository;

import com.sivamachineworks.platform.rag.domain.RagDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RagDocumentRepository extends JpaRepository<RagDocument, UUID> {
    Optional<RagDocument> findByDocCode(String docCode);
    boolean existsByDocCode(String docCode);
    List<RagDocument> findByDocType(String docType);
}

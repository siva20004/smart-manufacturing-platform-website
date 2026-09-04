package com.sivamachineworks.platform.pdm.repository;

import com.sivamachineworks.platform.pdm.domain.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByDocumentNumber(String documentNumber);
    boolean existsByDocumentNumber(String documentNumber);
    List<Document> findByProductId(UUID productId);
    Page<Document> findByProductId(UUID productId, Pageable pageable);
}

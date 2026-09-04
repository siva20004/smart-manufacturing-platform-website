package com.sivamachineworks.platform.pdm.repository;

import com.sivamachineworks.platform.pdm.domain.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
    List<DocumentVersion> findByDocumentId(UUID documentId);
    Optional<DocumentVersion> findByDocumentIdAndVersionNumber(UUID documentId, Integer versionNumber);
    boolean existsByDocumentIdAndVersionNumber(UUID documentId, Integer versionNumber);
}

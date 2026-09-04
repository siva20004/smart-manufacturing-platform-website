package com.sivamachineworks.platform.pdm.service;
import com.sivamachineworks.platform.shared.service.StorageService;
import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.pdm.domain.Document;
import com.sivamachineworks.platform.pdm.domain.DocumentVersion;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.pdm.dto.CreateDocumentRequest;
import com.sivamachineworks.platform.pdm.dto.DocumentResponse;
import com.sivamachineworks.platform.pdm.dto.DocumentVersionResponse;
import com.sivamachineworks.platform.pdm.repository.DocumentRepository;
import com.sivamachineworks.platform.pdm.repository.DocumentVersionRepository;
import com.sivamachineworks.platform.pdm.repository.ProductRepository;
import com.sivamachineworks.platform.shared.dto.PaginatedResponse;
import com.sivamachineworks.platform.shared.dto.PaginationMeta;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import com.sivamachineworks.platform.shared.storage.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
package com.sivamachineworks.platform.pdm.service;

import com.sivamachineworks.platform.shared.service.StorageService;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "dxf", "dwg", "step", "stp", "iges", "igs", "sldprt", "sldasm", "png", "jpg", "jpeg", "txt", "csv", "zip", "json"
    );

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final ProductRepository productRepository;
    private final StorageService storageService;
    private final AuditLogService auditLogService;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentVersionRepository versionRepository,
                           ProductRepository productRepository,
                           StorageService storageService,
                           AuditLogService auditLogService) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.productRepository = productRepository;
        this.storageService = storageService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest req, UUID userId, String ipAddress) {
        if (documentRepository.existsByDocumentNumber(req.documentNumber())) {
            throw new BaseException(ErrorCode.CONFLICT, "Document number already exists: " + req.documentNumber());
        }

        Product product = null;
        if (req.productId() != null) {
            product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Product not found: " + req.productId()));
        }

        Document doc = new Document();
        doc.setDocumentNumber(req.documentNumber());
        doc.setTitle(req.title());
        doc.setDocType(req.docType());
        doc.setProduct(product);
        doc.setIsRestricted(req.isRestricted() != null ? req.isRestricted() : false);

        Document saved = documentRepository.save(doc);
        auditLogService.log("Document", saved.getId(), "DOCUMENT_CREATE", "Created document " + saved.getDocumentNumber(), userId, ipAddress);

        return mapToDocumentResponse(saved);
    }

    @Transactional
    public DocumentVersionResponse uploadVersion(UUID documentId, String revisionCode, String cadMetadata, MultipartFile file, UUID userId, String ipAddress) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Document not found: " + documentId));

        if (file.isEmpty()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "File must not be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unnamed_document.bin";
        }

        // Security check: filename sanitization & extension validation
        String sanitizedFilename = sanitizeFilename(originalFilename);
        validateExtension(sanitizedFilename);

        try {
            byte[] bytes = file.getBytes();
            String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));

            List<DocumentVersion> existingVersions = versionRepository.findByDocumentId(documentId);
            int nextVersionNumber = existingVersions.size() + 1;

            String storageKey = "documents/" + document.getDocumentNumber() + "/v" + nextVersionNumber + "_" + sanitizedFilename;
            String location = storageService.storeFile(storageKey, bytes, file.getContentType());

            DocumentVersion version = new DocumentVersion();
            version.setDocument(document);
            version.setVersionNumber(nextVersionNumber);
            version.setRevisionCode(revisionCode != null ? revisionCode : "A");
            version.setStorageLocation(location);
            version.setStorageKey(storageKey);
            version.setFileName(sanitizedFilename);
            version.setFileSizeBytes(file.getSize());
            version.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            version.setFileHashSha256(sha256);
            version.setCadMetadata(cadMetadata);
            version.setStatus("DRAFT");

            DocumentVersion saved = versionRepository.save(version);
            auditLogService.log("DocumentVersion", saved.getId(), "DOCUMENT_UPLOAD", "Uploaded document version " + saved.getVersionNumber() + " for " + document.getDocumentNumber() + " (" + sanitizedFilename + ")", userId, ipAddress);

            return mapToVersionResponse(saved);
        } catch (Exception e) {
            if (e instanceof BaseException be) throw be;
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to upload document version: " + e.getMessage());
        }
    }

    @Transactional
    public byte[] downloadVersion(UUID documentId, Integer versionNumber, UUID userId, String ipAddress) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Document not found: " + documentId));

        DocumentVersion version = versionRepository.findByDocumentIdAndVersionNumber(documentId, versionNumber)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Document version not found: " + versionNumber));

        byte[] data = storageService.loadFile(version.getStorageKey());
        auditLogService.log("DocumentVersion", version.getId(), "DOCUMENT_DOWNLOAD", "Downloaded document " + document.getDocumentNumber() + " version " + version.getVersionNumber(), userId, ipAddress);

        return data;
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Document not found: " + id));
        return mapToDocumentResponse(document);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<DocumentResponse> listDocuments(Pageable pageable) {
        Page<Document> page = documentRepository.findAll(pageable);
        List<DocumentResponse> dtos = page.getContent().stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());

        PaginationMeta meta = new PaginationMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );

        return new PaginatedResponse<>(dtos, meta);
    }

    @Transactional
    public DocumentVersionResponse approveVersion(UUID documentId, Integer versionNumber, UUID approverId, String ipAddress) {
        DocumentVersion version = versionRepository.findByDocumentIdAndVersionNumber(documentId, versionNumber)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Document version not found: " + versionNumber));

        version.setStatus("APPROVED");
        version.setApprovedBy(approverId);
        version.setApprovedAt(Instant.now());
        DocumentVersion saved = versionRepository.save(version);
        auditLogService.log("DocumentVersion", saved.getId(), "DOCUMENT_APPROVE", "Approved document version " + versionNumber, approverId, ipAddress);

        return mapToVersionResponse(saved);
    }

    private String sanitizeFilename(String filename) {
        String clean = filename.replaceAll("[/\\\\]+", "_").replaceAll("\\.\\.+", "_");
        return clean.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void validateExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) return;
        String ext = filename.substring(dotIndex + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BaseException(ErrorCode.FORBIDDEN, "Security Exception: Disallowed file extension '." + ext + "'. Permitted extensions: " + ALLOWED_EXTENSIONS);
        }
    }

    private DocumentResponse mapToDocumentResponse(Document doc) {
        List<DocumentVersionResponse> vers = doc.getVersions() != null
                ? doc.getVersions().stream().map(this::mapToVersionResponse).collect(Collectors.toList())
                : List.of();

        return new DocumentResponse(
                doc.getId(),
                doc.getDocumentNumber(),
                doc.getTitle(),
                doc.getDocType(),
                doc.getProduct() != null ? doc.getProduct().getId() : null,
                doc.getIsRestricted(),
                vers,
                doc.getCreatedAt()
        );
    }

    private DocumentVersionResponse mapToVersionResponse(DocumentVersion ver) {
        return new DocumentVersionResponse(
                ver.getId(),
                ver.getDocument().getId(),
                ver.getVersionNumber(),
                ver.getRevisionCode(),
                ver.getFileName(),
                ver.getFileSizeBytes(),
                ver.getMimeType(),
                ver.getFileHashSha256(),
                ver.getCadMetadata(),
                ver.getStatus(),
                ver.getApprovedBy(),
                ver.getApprovedAt(),
                ver.getCreatedAt()
        );
    }
}

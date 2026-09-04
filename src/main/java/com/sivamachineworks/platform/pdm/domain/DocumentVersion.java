package com.sivamachineworks.platform.pdm.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_versions")
public class DocumentVersion extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "revision_code", length = 20)
    private String revisionCode;

    @Column(name = "storage_location", nullable = false, length = 500)
    private String storageLocation;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_hash_sha256", length = 64)
    private String fileHashSha256;

    @Column(name = "cad_metadata", columnDefinition = "TEXT")
    private String cadMetadata;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT";

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    public DocumentVersion() {}

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public String getRevisionCode() { return revisionCode; }
    public void setRevisionCode(String revisionCode) { this.revisionCode = revisionCode; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getFileHashSha256() { return fileHashSha256; }
    public void setFileHashSha256(String fileHashSha256) { this.fileHashSha256 = fileHashSha256; }
    public String getCadMetadata() { return cadMetadata; }
    public void setCadMetadata(String cadMetadata) { this.cadMetadata = cadMetadata; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
}

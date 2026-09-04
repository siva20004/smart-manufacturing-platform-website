import os

java_code = {
    "src/main/java/com/sivamachineworks/platform/pdm/domain/ProductRevision.java": """package com.sivamachineworks.platform.pdm.domain;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_revisions")
public class ProductRevision extends AuditableEntity {
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    @Column(name = "revision_number", nullable = false)
    private String revisionNumber;
    @Column(nullable = false)
    private String status = "DRAFT";
    @Column(name = "change_description")
    private String changeDescription;
    @Column(name = "effective_date")
    private LocalDate effectiveDate;
    @Column(name = "approved_by")
    private UUID approvedBy;
    @Column(name = "approved_at")
    private ZonedDateTime approvedAt;
    
    public ProductRevision() {}
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(String revisionNumber) { this.revisionNumber = revisionNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public ZonedDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(ZonedDateTime approvedAt) { this.approvedAt = approvedAt; }
}
""",
    "src/main/java/com/sivamachineworks/platform/pdm/domain/Document.java": """package com.sivamachineworks.platform.pdm.domain;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document extends AuditableEntity {
    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;
    @Column(nullable = false)
    private String title;
    @Column(name = "doc_type", nullable = false)
    private String docType;
    @Column(name = "product_id")
    private UUID productId;
    @Column(name = "is_restricted")
    private Boolean isRestricted = false;

    public Document() {}
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public Boolean getIsRestricted() { return isRestricted; }
    public void setIsRestricted(Boolean isRestricted) { this.isRestricted = isRestricted; }
}
""",
    "src/main/java/com/sivamachineworks/platform/pdm/domain/DocumentVersion.java": """package com.sivamachineworks.platform.pdm.domain;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_versions")
public class DocumentVersion extends AuditableEntity {
    @Column(name = "document_id", nullable = false)
    private UUID documentId;
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;
    @Column(name = "revision_code")
    private String revisionCode;
    @Column(name = "storage_location", nullable = false)
    private String storageLocation;
    @Column(name = "storage_key", nullable = false)
    private String storageKey;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;
    @Column(name = "mime_type", nullable = false)
    private String mimeType;
    @Column(name = "file_hash_sha256")
    private String fileHashSha256;
    @Column(name = "cad_metadata")
    private String cadMetadata;
    @Column(nullable = false)
    private String status = "DRAFT";
    @Column(name = "approved_by")
    private UUID approvedBy;
    @Column(name = "approved_at")
    private ZonedDateTime approvedAt;
    
    public DocumentVersion() {}
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }
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
    public ZonedDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(ZonedDateTime approvedAt) { this.approvedAt = approvedAt; }
}
"""
}

base_path = r"c:\Users\dell\Desktop\nihon project"
for rel_path, content in java_code.items():
    full_path = os.path.join(base_path, rel_path.replace("/", "\\"))
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content)


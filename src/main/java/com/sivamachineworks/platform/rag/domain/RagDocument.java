package com.sivamachineworks.platform.rag.domain;

import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rag_documents")
public class RagDocument extends AuditableEntity {

    @Column(name = "doc_code", unique = true, nullable = false, length = 50)
    private String docCode;

    @Column(nullable = false)
    private String title;

    @Column(name = "doc_type", nullable = false, length = 50)
    private String docType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 1;

    @Column(name = "mime_type", length = 100)
    private String mimeType = "application/pdf";

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes = 1024000L;

    @Column(name = "allowed_roles", nullable = false)
    private String allowedRoles; // Comma-separated: "ENGINEERING,PRODUCTION,ADMIN"

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RagDocumentChunk> chunks = new ArrayList<>();

    public RagDocument() {}

    public String getDocCode() { return docCode; }
    public void setDocCode(String docCode) { this.docCode = docCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public String getAllowedRoles() { return allowedRoles; }
    public void setAllowedRoles(String allowedRoles) { this.allowedRoles = allowedRoles; }
    public List<RagDocumentChunk> getChunks() { return chunks; }
    public void setChunks(List<RagDocumentChunk> chunks) { this.chunks = chunks; }
}

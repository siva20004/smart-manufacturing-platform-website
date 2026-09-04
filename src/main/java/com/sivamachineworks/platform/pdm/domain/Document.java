package com.sivamachineworks.platform.pdm.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
public class Document extends AuditableEntity {

    @Column(name = "document_number", unique = true, nullable = false, length = 100)
    private String documentNumber;

    @Column(nullable = false)
    private String title;

    @Column(name = "doc_type", nullable = false, length = 50)
    private String docType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "is_restricted")
    private Boolean isRestricted = false;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentVersion> versions = new ArrayList<>();

    public Document() {}

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Boolean getIsRestricted() { return isRestricted; }
    public void setIsRestricted(Boolean isRestricted) { this.isRestricted = isRestricted; }
    public List<DocumentVersion> getVersions() { return versions; }
    public void setVersions(List<DocumentVersion> versions) { this.versions = versions; }
}

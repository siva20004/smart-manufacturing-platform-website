package com.sivamachineworks.platform.pdm.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.pdm.domain.ProductRevision;
import com.sivamachineworks.platform.pdm.dto.ApproveRevisionRequest;
import com.sivamachineworks.platform.pdm.dto.CreateRevisionRequest;
import com.sivamachineworks.platform.pdm.dto.ProductRevisionResponse;
import com.sivamachineworks.platform.pdm.repository.ProductRepository;
import com.sivamachineworks.platform.pdm.repository.ProductRevisionRepository;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductRevisionService {

    private final ProductRepository productRepository;
    private final ProductRevisionRepository revisionRepository;
    private final AuditLogService auditLogService;

    public ProductRevisionService(ProductRepository productRepository, ProductRevisionRepository revisionRepository, AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.revisionRepository = revisionRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ProductRevisionResponse createRevision(UUID productId, CreateRevisionRequest req, UUID userId, String ipAddress) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Product not found: " + productId));

        if (revisionRepository.existsByProductIdAndRevisionNumber(productId, req.revisionNumber())) {
            throw new BaseException(ErrorCode.CONFLICT, "Revision " + req.revisionNumber() + " already exists for product " + product.getProductNumber());
        }

        ProductRevision revision = new ProductRevision();
        revision.setProduct(product);
        revision.setRevisionNumber(req.revisionNumber());
        revision.setChangeDescription(req.changeDescription());
        revision.setEffectiveDate(req.effectiveDate());
        revision.setStatus("DRAFT");

        ProductRevision saved = revisionRepository.save(revision);
        auditLogService.log("ProductRevision", saved.getId(), "REVISION_CREATION", "Created revision " + saved.getRevisionNumber() + " for product " + product.getProductNumber(), userId, ipAddress);

        return mapToRevisionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductRevisionResponse> getRevisions(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new BaseException(ErrorCode.NOT_FOUND, "Product not found: " + productId);
        }
        return revisionRepository.findByProductId(productId).stream()
                .map(this::mapToRevisionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductRevisionResponse approveRevision(UUID productId, UUID revisionId, ApproveRevisionRequest req, UUID approverId, String ipAddress) {
        ProductRevision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Revision not found: " + revisionId));

        if (!revision.getProduct().getId().equals(productId)) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Revision does not belong to the specified product");
        }

        revision.setStatus("RELEASED");
        revision.setApprovedBy(approverId);
        revision.setApprovedAt(Instant.now());

        ProductRevision saved = revisionRepository.save(revision);
        auditLogService.log("ProductRevision", saved.getId(), "REVISION_APPROVAL", "Approved revision " + saved.getRevisionNumber() + " for product " + revision.getProduct().getProductNumber() + ". Notes: " + req.notes(), approverId, ipAddress);

        return mapToRevisionResponse(saved);
    }

    private ProductRevisionResponse mapToRevisionResponse(ProductRevision r) {
        return new ProductRevisionResponse(
                r.getId(),
                r.getProduct().getId(),
                r.getRevisionNumber(),
                r.getStatus(),
                r.getChangeDescription(),
                r.getEffectiveDate(),
                r.getApprovedBy(),
                r.getApprovedAt(),
                r.getCreatedAt()
        );
    }
}

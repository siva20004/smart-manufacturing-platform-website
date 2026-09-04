package com.sivamachineworks.platform.pdm.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.pdm.domain.ProductRevision;
import com.sivamachineworks.platform.pdm.dto.CreateProductRequest;
import com.sivamachineworks.platform.pdm.dto.ProductResponse;
import com.sivamachineworks.platform.pdm.dto.ProductRevisionResponse;
import com.sivamachineworks.platform.pdm.dto.UpdateProductRequest;
import com.sivamachineworks.platform.pdm.repository.ProductRepository;
import com.sivamachineworks.platform.pdm.repository.ProductRevisionRepository;
import com.sivamachineworks.platform.shared.dto.PaginatedResponse;
import com.sivamachineworks.platform.shared.dto.PaginationMeta;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductRevisionRepository revisionRepository;
    private final AuditLogService auditLogService;

    public ProductService(ProductRepository productRepository, ProductRevisionRepository revisionRepository, AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.revisionRepository = revisionRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest req, UUID userId, String ipAddress) {
        if (productRepository.existsByProductNumber(req.productNumber())) {
            throw new BaseException(ErrorCode.CONFLICT, "Product number already exists: " + req.productNumber());
        }

        Product product = new Product();
        product.setProductNumber(req.productNumber());
        product.setName(req.name());
        product.setDescription(req.description());
        product.setCategory(req.category());
        product.setUom(req.uom() != null ? req.uom() : "EA");
        product.setListPrice(req.listPrice());
        product.setStandardCost(req.standardCost());
        product.setLeadTimeWeeks(req.leadTimeWeeks());
        product.setStatus(req.status() != null ? req.status() : "ACTIVE");

        Product saved = productRepository.save(product);

        // Initial revision 'A' (Draft)
        ProductRevision initialRev = new ProductRevision();
        initialRev.setProduct(saved);
        initialRev.setRevisionNumber("A");
        initialRev.setStatus("DRAFT");
        initialRev.setChangeDescription("Initial product release baseline");
        revisionRepository.save(initialRev);

        auditLogService.log("Product", saved.getId(), "PRODUCT_CREATION", "Created product: " + saved.getProductNumber(), userId, ipAddress);

        return mapToProductResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Product not found: " + id));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByNumber(String productNumber) {
        Product product = productRepository.findByProductNumber(productNumber)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Product not found: " + productNumber));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> listProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        List<ProductResponse> dtos = page.getContent().stream()
                .map(this::mapToProductResponse)
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
    public ProductResponse updateProduct(UUID id, UpdateProductRequest req, UUID userId, String ipAddress) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Product not found: " + id));

        if (req.name() != null) product.setName(req.name());
        if (req.description() != null) product.setDescription(req.description());
        if (req.category() != null) product.setCategory(req.category());
        if (req.uom() != null) product.setUom(req.uom());
        if (req.listPrice() != null) product.setListPrice(req.listPrice());
        if (req.standardCost() != null) product.setStandardCost(req.standardCost());
        if (req.leadTimeWeeks() != null) product.setLeadTimeWeeks(req.leadTimeWeeks());
        if (req.status() != null) product.setStatus(req.status());

        Product saved = productRepository.save(product);
        auditLogService.log("Product", saved.getId(), "PRODUCT_UPDATE", "Updated product: " + saved.getProductNumber(), userId, ipAddress);

        return mapToProductResponse(saved);
    }

    public ProductResponse mapToProductResponse(Product p) {
        List<ProductRevision> revs = revisionRepository.findByProductId(p.getId());
        List<ProductRevisionResponse> revResponses = revs.stream()
                .map(r -> new ProductRevisionResponse(
                        r.getId(),
                        p.getId(),
                        r.getRevisionNumber(),
                        r.getStatus(),
                        r.getChangeDescription(),
                        r.getEffectiveDate(),
                        r.getApprovedBy(),
                        r.getApprovedAt(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new ProductResponse(
                p.getId(),
                p.getProductNumber(),
                p.getName(),
                p.getDescription(),
                p.getCategory(),
                p.getUom(),
                p.getListPrice(),
                p.getStandardCost(),
                p.getLeadTimeWeeks(),
                p.getStatus(),
                revResponses,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

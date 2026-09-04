package com.sivamachineworks.platform.pdm.controller;

import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.pdm.dto.*;
import com.sivamachineworks.platform.pdm.service.ProductRevisionService;
import com.sivamachineworks.platform.pdm.service.ProductService;
import com.sivamachineworks.platform.shared.dto.ApiResponse;
import com.sivamachineworks.platform.shared.dto.PaginatedResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductRevisionService revisionService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, ProductRevisionService revisionService, UserRepository userRepository) {
        this.productService = productService;
        this.revisionService = revisionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ProductResponse response = productService.createProduct(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductResponse>>> listProducts(Pageable pageable) {
        PaginatedResponse<ProductResponse> response = productService.listProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ProductResponse response = productService.updateProduct(id, request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/revisions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ProductRevisionResponse>> createRevision(
            @PathVariable UUID id,
            @Valid @RequestBody CreateRevisionRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ProductRevisionResponse response = revisionService.createRevision(id, request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<ApiResponse<List<ProductRevisionResponse>>> getRevisions(@PathVariable UUID id) {
        List<ProductRevisionResponse> response = revisionService.getRevisions(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/revisions/{revId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<ProductRevisionResponse>> approveRevision(
            @PathVariable UUID id,
            @PathVariable UUID revId,
            @RequestBody(required = false) ApproveRevisionRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        String notes = request != null ? request.notes() : "Approved";
        ProductRevisionResponse response = revisionService.approveRevision(id, revId, new ApproveRevisionRequest(notes), userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

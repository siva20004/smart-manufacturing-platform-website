package com.sivamachineworks.platform.production.controller;

import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.production.dto.*;
import com.sivamachineworks.platform.production.service.ProductionService;
import com.sivamachineworks.platform.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/production")
public class ProductionController {

    private final ProductionService productionService;
    private final UserRepository userRepository;

    public ProductionController(ProductionService productionService, UserRepository userRepository) {
        this.productionService = productionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> createProductionOrder(
            @Valid @RequestBody CreateProductionOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ProductionOrderResponse response = productionService.createProductionOrder(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<java.util.List<ProductionOrderResponse>>> listProductionOrders() {
        java.util.List<ProductionOrderResponse> response = productionService.listProductionOrders();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> getProductionOrder(@PathVariable UUID id) {
        ProductionOrderResponse response = productionService.getProductionOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/orders/{id}/reserve-materials")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> reserveMaterials(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ProductionOrderResponse response = productionService.reserveMaterials(id, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/orders/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> startProduction(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ProductionOrderResponse response = productionService.startProduction(id, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/orders/{id}/operations/{seq}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ProductionOperationResponse>> updateOperation(
            @PathVariable UUID id,
            @PathVariable Integer seq,
            @Valid @RequestBody UpdateOperationRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ProductionOperationResponse response = productionService.updateOperation(id, seq, request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/orders/{id}/quality-check")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<QualityInspectionResponse>> submitQualityInspection(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitQualityInspectionRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        QualityInspectionResponse response = productionService.submitQualityInspection(id, request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/orders/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<ProductionOrderResponse>> completeProduction(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ProductionOrderResponse response = productionService.completeProduction(id, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

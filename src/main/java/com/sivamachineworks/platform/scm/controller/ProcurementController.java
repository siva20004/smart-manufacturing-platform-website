package com.sivamachineworks.platform.scm.controller;

import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.scm.dto.*;
import com.sivamachineworks.platform.scm.service.ProcurementService;
import com.sivamachineworks.platform.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/procurement")
public class ProcurementController {

    private final ProcurementService procurementService;
    private final UserRepository userRepository;

    public ProcurementController(ProcurementService procurementService, UserRepository userRepository) {
        this.procurementService = procurementService;
        this.userRepository = userRepository;
    }

    @PostMapping("/suppliers")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        SupplierResponse response = procurementService.createSupplier(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/suppliers")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> listSuppliers() {
        List<SupplierResponse> response = procurementService.listSuppliers();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/suppliers/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplier(@PathVariable UUID id) {
        SupplierResponse response = procurementService.getSupplierById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT', 'PRODUCTION', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> createPurchaseRequest(
            @Valid @RequestBody CreatePurchaseRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        PurchaseRequestResponse response = procurementService.createPurchaseRequest(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/requests/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> approvePurchaseRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        PurchaseRequestResponse response = procurementService.approvePurchaseRequest(id, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<PurchaseRequestResponse>>> listPurchaseRequests() {
        List<PurchaseRequestResponse> response = procurementService.listPurchaseRequests();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> listPurchaseOrders() {
        List<PurchaseOrderResponse> response = procurementService.listPurchaseOrders();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        PurchaseOrderResponse response = procurementService.createPurchaseOrder(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/orders/{id}/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> issuePurchaseOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        PurchaseOrderResponse response = procurementService.issuePurchaseOrder(id, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPurchaseOrder(@PathVariable UUID id) {
        PurchaseOrderResponse response = procurementService.getPurchaseOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/receipts")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT', 'PRODUCTION', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> postGoodsReceipt(
            @Valid @RequestBody PostGoodsReceiptRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        GoodsReceiptResponse response = procurementService.postGoodsReceipt(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

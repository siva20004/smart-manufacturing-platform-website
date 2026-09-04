package com.sivamachineworks.platform.sales.controller;

import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.sales.dto.CreateSalesOrderRequest;
import com.sivamachineworks.platform.sales.dto.OrderConfirmationResponse;
import com.sivamachineworks.platform.sales.dto.SalesOrderResponse;
import com.sivamachineworks.platform.sales.service.SalesOrderService;
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
@RequestMapping("/api/v1/sales/orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final UserRepository userRepository;

    public SalesOrderController(SalesOrderService salesOrderService, UserRepository userRepository) {
        this.salesOrderService = salesOrderService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> createSalesOrder(
            @Valid @RequestBody CreateSalesOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        SalesOrderResponse response = salesOrderService.createSalesOrder(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SalesOrderResponse>>> listSalesOrders() {
        List<SalesOrderResponse> response = salesOrderService.listSalesOrders();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> getSalesOrder(@PathVariable UUID id) {
        SalesOrderResponse response = salesOrderService.getSalesOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<OrderConfirmationResponse>> confirmSalesOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        OrderConfirmationResponse response = salesOrderService.confirmSalesOrder(id, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> cancelSalesOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        SalesOrderResponse response = salesOrderService.cancelSalesOrder(id, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

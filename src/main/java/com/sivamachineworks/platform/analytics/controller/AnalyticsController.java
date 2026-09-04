package com.sivamachineworks.platform.analytics.controller;

import com.sivamachineworks.platform.analytics.dto.*;
import com.sivamachineworks.platform.analytics.service.AnalyticsService;
import com.sivamachineworks.platform.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/sales-by-customer")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'MANAGEMENT', 'FINANCE', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<List<SalesByCustomerDto>>> getSalesByCustomer() {
        List<SalesByCustomerDto> response = analyticsService.getSalesByCustomer();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sales-by-product")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'MANAGEMENT', 'FINANCE', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<List<SalesByProductDto>>> getSalesByProduct() {
        List<SalesByProductDto> response = analyticsService.getSalesByProduct();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/order-volume")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'MANAGEMENT', 'FINANCE', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<List<OrderVolumeDto>>> getOrderVolume() {
        List<OrderVolumeDto> response = analyticsService.getOrderVolume();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/inventory-value")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT', 'FINANCE', 'PROCUREMENT', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<List<InventoryValueDto>>> getInventoryValue() {
        List<InventoryValueDto> response = analyticsService.getInventoryValue();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/production-metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'MANAGEMENT', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ProductionAnalyticsDto>> getProductionMetrics() {
        ProductionAnalyticsDto response = analyticsService.getProductionMetrics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/supplier-performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT', 'MANAGEMENT', 'FINANCE', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<List<SupplierPerformanceDto>>> getSupplierPerformance() {
        List<SupplierPerformanceDto> response = analyticsService.getSupplierPerformance();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/service-metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PRODUCTION', 'MANAGEMENT', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ServiceMetricsDto>> getServiceMetrics() {
        ServiceMetricsDto response = analyticsService.getServiceMetrics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

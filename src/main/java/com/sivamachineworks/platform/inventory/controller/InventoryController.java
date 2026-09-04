package com.sivamachineworks.platform.inventory.controller;

import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.inventory.dto.StockAdjustmentRequest;
import com.sivamachineworks.platform.inventory.dto.StockResponse;
import com.sivamachineworks.platform.inventory.service.InventoryService;
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
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    public InventoryController(InventoryService inventoryService, UserRepository userRepository) {
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/warehouses/{warehouseId}/stock")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getStockByWarehouse(@PathVariable UUID warehouseId) {
        List<StockResponse> response = inventoryService.getStockByWarehouse(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'PROCUREMENT', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<StockResponse>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        StockResponse response = inventoryService.adjustStock(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

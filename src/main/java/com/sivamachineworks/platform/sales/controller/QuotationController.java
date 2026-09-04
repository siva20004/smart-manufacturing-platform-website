package com.sivamachineworks.platform.sales.controller;

import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.sales.dto.CreateQuotationRequest;
import com.sivamachineworks.platform.sales.dto.QuotationResponse;
import com.sivamachineworks.platform.sales.service.QuotationService;
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
@RequestMapping("/api/v1/sales/quotations")
public class QuotationController {

    private final QuotationService quotationService;
    private final UserRepository userRepository;

    public QuotationController(QuotationService quotationService, UserRepository userRepository) {
        this.quotationService = quotationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<QuotationResponse>> createQuotation(
            @Valid @RequestBody CreateQuotationRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        QuotationResponse response = quotationService.createQuotation(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationResponse>> getQuotation(@PathVariable UUID id) {
        QuotationResponse response = quotationService.getQuotationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

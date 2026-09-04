package com.sivamachineworks.platform.scm.quotation.controller;

import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.scm.quotation.dto.QuotationExtractionDto;
import com.sivamachineworks.platform.scm.quotation.dto.ReviewQuotationRequest;
import com.sivamachineworks.platform.scm.quotation.dto.UploadQuotationRequest;
import com.sivamachineworks.platform.scm.quotation.service.SupplierQuotationService;
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
@RequestMapping("/api/v1/procurement/quotations")
public class SupplierQuotationController {

    private final SupplierQuotationService quotationService;
    private final UserRepository userRepository;

    public SupplierQuotationController(SupplierQuotationService quotationService, UserRepository userRepository) {
        this.quotationService = quotationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<QuotationExtractionDto>> uploadAndExtract(
            @Valid @RequestBody UploadQuotationRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        QuotationExtractionDto response = quotationService.uploadAndExtract(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<QuotationExtractionDto>> reviewQuotation(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewQuotationRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        QuotationExtractionDto response = quotationService.reviewAndProcess(id, request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<QuotationExtractionDto>>> listQuotations(
            @RequestParam(required = false) String status) {
        List<QuotationExtractionDto> list = quotationService.listQuotations(status);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<QuotationExtractionDto>> getQuotation(@PathVariable UUID id) {
        QuotationExtractionDto dto = quotationService.getQuotation(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

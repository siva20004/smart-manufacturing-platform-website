package com.sivamachineworks.platform.bom.controller;

import com.sivamachineworks.platform.bom.dto.*;
import com.sivamachineworks.platform.bom.service.EbomService;
import com.sivamachineworks.platform.identity.repository.UserRepository;
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
@RequestMapping("/api/v1/bom")
public class EbomController {

    private final EbomService ebomService;
    private final UserRepository userRepository;

    public EbomController(EbomService ebomService, UserRepository userRepository) {
        this.ebomService = ebomService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<EbomResponse>> createEbom(
            @Valid @RequestBody CreateEbomRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        EbomResponse response = ebomService.createEbom(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EbomResponse>> getEbom(@PathVariable UUID id) {
        EbomResponse response = ebomService.getEbomById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<EbomResponse>>> listEboms(
            @RequestParam(required = false) UUID productId,
            Pageable pageable) {
        PaginatedResponse<EbomResponse> response = ebomService.listEboms(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<ApiResponse<List<EbomTreeNodeDto>>> getEbomTree(@PathVariable UUID id) {
        List<EbomTreeNodeDto> response = ebomService.getEbomTree(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<EbomItemResponse>> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody CreateEbomItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        EbomItemResponse response = ebomService.addItem(id, request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<EbomItemResponse>> updateItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateEbomItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        EbomItemResponse response = ebomService.updateItem(id, itemId, request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ebomService.removeItem(id, itemId, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/revisions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<EbomResponse>> createRevision(
            @PathVariable UUID id,
            @Valid @RequestBody CreateEbomRevisionRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        EbomResponse response = ebomService.createRevision(id, request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<EbomResponse>> submitForApproval(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        EbomResponse response = ebomService.submitForApproval(id, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<EbomResponse>> approveEbom(
            @PathVariable UUID id,
            @RequestBody(required = false) ApproveEbomRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        EbomResponse response = ebomService.approveEbom(id, request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

package com.sivamachineworks.platform.bom.controller;

import com.sivamachineworks.platform.bom.dto.*;
import com.sivamachineworks.platform.bom.service.MbomService;
import com.sivamachineworks.platform.bom.service.MbomTransformationService;
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
public class MbomController {

    private final MbomTransformationService transformationService;
    private final MbomService mbomService;
    private final UserRepository userRepository;

    public MbomController(MbomTransformationService transformationService,
                          MbomService mbomService,
                          UserRepository userRepository) {
        this.transformationService = transformationService;
        this.mbomService = mbomService;
        this.userRepository = userRepository;
    }

    @PostMapping("/transform")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_PLANNER', 'PRODUCTION', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<TransformEbomResponse>> transformEbom(
            @Valid @RequestBody TransformEbomRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        TransformEbomResponse response = transformationService.transform(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mbom/{id}")
    public ResponseEntity<ApiResponse<MbomResponse>> getMbom(@PathVariable UUID id) {
        MbomResponse response = mbomService.getMbomById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mbom/{id}/tree")
    public ResponseEntity<ApiResponse<List<MbomTreeNodeDto>>> getMbomTree(@PathVariable UUID id) {
        List<MbomTreeNodeDto> response = mbomService.getMbomTree(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mbom")
    public ResponseEntity<ApiResponse<PaginatedResponse<MbomResponse>>> listMboms(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String plantLocation,
            Pageable pageable) {
        PaginatedResponse<MbomResponse> response = mbomService.listMboms(productId, plantLocation, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/mbom/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_PLANNER', 'PRODUCTION', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<MbomResponse>> approveMbom(
            @PathVariable UUID id,
            @RequestBody(required = false) ApproveMbomRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        MbomResponse response = mbomService.approveMbom(id, request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

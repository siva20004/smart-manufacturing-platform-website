package com.sivamachineworks.platform.pdm.controller;

import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.pdm.dto.CreateDocumentRequest;
import com.sivamachineworks.platform.pdm.dto.DocumentResponse;
import com.sivamachineworks.platform.pdm.dto.DocumentVersionResponse;
import com.sivamachineworks.platform.pdm.service.DocumentService;
import com.sivamachineworks.platform.shared.dto.ApiResponse;
import com.sivamachineworks.platform.shared.dto.PaginatedResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pdm/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    public DocumentController(DocumentService documentService, UserRepository userRepository) {
        this.documentService = documentService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        DocumentResponse response = documentService.createDocument(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<DocumentResponse>>> listDocuments(Pageable pageable) {
        PaginatedResponse<DocumentResponse> response = documentService.listDocuments(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(@PathVariable UUID id) {
        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(value = "/{id}/versions/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<DocumentVersionResponse>> uploadVersion(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "revisionCode", required = false, defaultValue = "A") String revisionCode,
            @RequestParam(value = "cadMetadata", required = false) String cadMetadata,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        DocumentVersionResponse response = documentService.uploadVersion(id, revisionCode, cadMetadata, file, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/versions/{versionNumber}/download")
    public ResponseEntity<byte[]> downloadVersion(
            @PathVariable UUID id,
            @PathVariable Integer versionNumber,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        byte[] fileData = documentService.downloadVersion(id, versionNumber, userId, req.getRemoteAddr());
        DocumentResponse doc = documentService.getDocumentById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.documentNumber() + "_v" + versionNumber + ".bin\"")
                .body(fileData);
    }

    @PutMapping("/{id}/versions/{versionNumber}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<DocumentVersionResponse>> approveVersion(
            @PathVariable UUID id,
            @PathVariable Integer versionNumber,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        DocumentVersionResponse response = documentService.approveVersion(id, versionNumber, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

package com.sivamachineworks.platform.rag.controller;

import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.rag.domain.RagDocument;
import com.sivamachineworks.platform.rag.dto.DocumentRagQueryRequest;
import com.sivamachineworks.platform.rag.dto.DocumentRagQueryResponse;
import com.sivamachineworks.platform.rag.dto.IngestDocumentRequest;
import com.sivamachineworks.platform.rag.dto.RagDocumentSummaryDto;
import com.sivamachineworks.platform.rag.service.DocumentIngestionService;
import com.sivamachineworks.platform.rag.service.DocumentRagService;
import com.sivamachineworks.platform.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rag")
public class DocumentRagController {

    private final DocumentIngestionService ingestionService;
    private final DocumentRagService ragService;
    private final UserRepository userRepository;

    public DocumentRagController(
            DocumentIngestionService ingestionService,
            DocumentRagService ragService,
            UserRepository userRepository) {
        this.ingestionService = ingestionService;
        this.ragService = ragService;
        this.userRepository = userRepository;
    }

    @PostMapping("/documents/ingest")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEERING', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<RagDocumentSummaryDto>> ingestDocument(
            @Valid @RequestBody IngestDocumentRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        RagDocument doc = ingestionService.ingestDocument(request, userId, req.getRemoteAddr());
        RagDocumentSummaryDto response = new RagDocumentSummaryDto(
                doc.getId(),
                doc.getDocCode(),
                doc.getTitle(),
                doc.getDocType(),
                doc.getVersionNumber(),
                doc.getAllowedRoles(),
                doc.getChunks().size(),
                doc.getCreatedAt()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/documents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RagDocumentSummaryDto>>> listDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {
        Set<String> roles = extractRoles(userDetails);
        List<RagDocumentSummaryDto> docs = ragService.listAuthorizedDocuments(roles);
        return ResponseEntity.ok(ApiResponse.success(docs));
    }

    @PostMapping("/query")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentRagQueryResponse>> queryKnowledgeBase(
            @Valid @RequestBody DocumentRagQueryRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        Set<String> roles = extractRoles(userDetails);
        DocumentRagQueryResponse response = ragService.queryKnowledgeBase(request, roles, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private Set<String> extractRoles(UserDetails userDetails) {
        if (userDetails == null) return Set.of();
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

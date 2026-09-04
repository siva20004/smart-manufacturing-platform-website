package com.sivamachineworks.platform.audit.controller;

import com.sivamachineworks.platform.audit.domain.AuditLog;
import com.sivamachineworks.platform.audit.dto.AuditLogResponse;
import com.sivamachineworks.platform.audit.repository.AuditLogRepository;
import com.sivamachineworks.platform.shared.dto.ApiResponse;
import com.sivamachineworks.platform.shared.dto.PaginatedResponse;
import com.sivamachineworks.platform.shared.dto.PaginationMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_ENGINEER', 'MANAGEMENT')")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLogResponse>>> getAuditLogs(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAll(pageable);
        List<AuditLogResponse> dtoList = page.getContent().stream()
                .map(a -> new AuditLogResponse(
                        a.getId(),
                        a.getEntityName(),
                        a.getEntityId(),
                        a.getAction(),
                        a.getChangedData(),
                        a.getUserId(),
                        a.getIpAddress(),
                        a.getCreatedAt()
                ))
                .toList();

        PaginationMeta meta = new PaginationMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
        PaginatedResponse<AuditLogResponse> paginated = new PaginatedResponse<>(dtoList, meta);
        return ResponseEntity.ok(ApiResponse.success(paginated));
    }
}

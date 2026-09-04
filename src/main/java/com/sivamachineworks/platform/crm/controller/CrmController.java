package com.sivamachineworks.platform.crm.controller;

import com.sivamachineworks.platform.crm.dto.*;
import com.sivamachineworks.platform.crm.service.CrmService;
import com.sivamachineworks.platform.identity.repository.UserRepository;
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
@RequestMapping("/api/v1/crm")
public class CrmController {

    private final CrmService crmService;
    private final UserRepository userRepository;

    public CrmController(CrmService crmService, UserRepository userRepository) {
        this.crmService = crmService;
        this.userRepository = userRepository;
    }

    @PostMapping("/contacts")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ContactResponse>> createContact(
            @Valid @RequestBody CreateContactRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ContactResponse response = crmService.createContact(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customers/{customerId}/contacts")
    public ResponseEntity<ApiResponse<List<ContactResponse>>> getContactsByCustomer(@PathVariable UUID customerId) {
        List<ContactResponse> response = crmService.getContactsByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/opportunities")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<OpportunityResponse>> createOpportunity(
            @Valid @RequestBody CreateOpportunityRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        OpportunityResponse response = crmService.createOpportunity(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customers/{customerId}/opportunities")
    public ResponseEntity<ApiResponse<List<OpportunityResponse>>> getOpportunitiesByCustomer(@PathVariable UUID customerId) {
        List<OpportunityResponse> response = crmService.getOpportunitiesByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/service-requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PRODUCTION', 'IT_ENGINEER')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> createServiceRequest(
            @Valid @RequestBody CreateServiceRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ServiceRequestResponse response = crmService.createServiceRequest(request, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/service-requests/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'IT_ENGINEER', 'ENGINEERING')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> resolveServiceRequest(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "Resolved field service ticket") String resolutionNotes,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) {
        UUID userId = getUserId(userDetails);
        ServiceRequestResponse response = crmService.resolveServiceRequest(id, resolutionNotes, userId, req.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customers/{customerId}/history")
    public ResponseEntity<ApiResponse<CustomerHistoryResponse>> getCustomerHistory(@PathVariable UUID customerId) {
        CustomerHistoryResponse response = crmService.getCustomerHistory(customerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private UUID getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}

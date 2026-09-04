package com.sivamachineworks.platform.crm.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.crm.domain.CrmOpportunity;
import com.sivamachineworks.platform.crm.domain.Customer;
import com.sivamachineworks.platform.crm.domain.CustomerContact;
import com.sivamachineworks.platform.crm.domain.ServiceRequest;
import com.sivamachineworks.platform.crm.dto.*;
import com.sivamachineworks.platform.crm.repository.CrmOpportunityRepository;
import com.sivamachineworks.platform.crm.repository.CustomerContactRepository;
import com.sivamachineworks.platform.crm.repository.CustomerRepository;
import com.sivamachineworks.platform.crm.repository.ServiceRequestRepository;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.pdm.repository.ProductRepository;
import com.sivamachineworks.platform.sales.dto.QuotationResponse;
import com.sivamachineworks.platform.sales.dto.SalesOrderResponse;
import com.sivamachineworks.platform.sales.repository.QuotationRepository;
import com.sivamachineworks.platform.sales.repository.SalesOrderRepository;
import com.sivamachineworks.platform.sales.service.QuotationService;
import com.sivamachineworks.platform.sales.service.SalesOrderService;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CrmService {

    private final CustomerRepository customerRepository;
    private final CustomerContactRepository contactRepository;
    private final CrmOpportunityRepository opportunityRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ProductRepository productRepository;
    private final QuotationRepository quotationRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final QuotationService quotationService;
    private final SalesOrderService salesOrderService;
    private final AuditLogService auditLogService;

    public CrmService(CustomerRepository customerRepository,
                      CustomerContactRepository contactRepository,
                      CrmOpportunityRepository opportunityRepository,
                      ServiceRequestRepository serviceRequestRepository,
                      ProductRepository productRepository,
                      QuotationRepository quotationRepository,
                      SalesOrderRepository salesOrderRepository,
                      QuotationService quotationService,
                      SalesOrderService salesOrderService,
                      AuditLogService auditLogService) {
        this.customerRepository = customerRepository;
        this.contactRepository = contactRepository;
        this.opportunityRepository = opportunityRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.productRepository = productRepository;
        this.quotationRepository = quotationRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.quotationService = quotationService;
        this.salesOrderService = salesOrderService;
        this.auditLogService = auditLogService;
    }

    // --- Contacts ---
    @Transactional
    public ContactResponse createContact(CreateContactRequest req, UUID userId, String ipAddress) {
        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Customer not found: " + req.customerId()));

        CustomerContact contact = new CustomerContact();
        contact.setCustomer(customer);
        contact.setFirstName(req.firstName());
        contact.setLastName(req.lastName());
        contact.setEmail(req.email());
        contact.setPhone(req.phone());
        contact.setJobTitle(req.jobTitle());
        contact.setDepartment(req.department());
        contact.setIsPrimary(req.isPrimary() != null ? req.isPrimary() : false);
        contact.setIsActive(true);

        CustomerContact saved = contactRepository.save(contact);
        auditLogService.log("CustomerContact", saved.getId(), "CONTACT_CREATED", "Added contact " + saved.getFirstName() + " " + saved.getLastName() + " to customer " + customer.getCustomerCode(), userId, ipAddress);

        return mapToContactResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getContactsByCustomer(UUID customerId) {
        return contactRepository.findByCustomerId(customerId).stream().map(this::mapToContactResponse).collect(Collectors.toList());
    }

    // --- Opportunities ---
    @Transactional
    public OpportunityResponse createOpportunity(CreateOpportunityRequest req, UUID userId, String ipAddress) {
        if (opportunityRepository.existsByOpportunityCode(req.opportunityCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "Opportunity code already exists: " + req.opportunityCode());
        }

        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Customer not found: " + req.customerId()));

        CustomerContact contact = req.primaryContactId() != null
                ? contactRepository.findById(req.primaryContactId()).orElse(null)
                : null;

        Product product = req.productId() != null
                ? productRepository.findById(req.productId()).orElse(null)
                : null;

        CrmOpportunity opp = new CrmOpportunity();
        opp.setOpportunityCode(req.opportunityCode());
        opp.setCustomer(customer);
        opp.setPrimaryContact(contact);
        opp.setProduct(product);
        opp.setName(req.name());
        opp.setStage(req.stage() != null ? req.stage() : "PROSPECTING");
        opp.setEstimatedValue(req.estimatedValue());
        opp.setProbabilityPct(req.probabilityPct() != null ? req.probabilityPct() : new java.math.BigDecimal("20.0"));
        opp.setExpectedCloseDate(req.expectedCloseDate());
        opp.setAssignedRepId(userId);

        CrmOpportunity saved = opportunityRepository.save(opp);
        auditLogService.log("CrmOpportunity", saved.getId(), "OPPORTUNITY_CREATED", "Created opportunity " + saved.getOpportunityCode(), userId, ipAddress);

        return mapToOppResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> getOpportunitiesByCustomer(UUID customerId) {
        return opportunityRepository.findByCustomerId(customerId).stream().map(this::mapToOppResponse).collect(Collectors.toList());
    }

    // --- Service Requests ---
    @Transactional
    public ServiceRequestResponse createServiceRequest(CreateServiceRequestDto req, UUID userId, String ipAddress) {
        if (serviceRequestRepository.existsByTicketNumber(req.ticketNumber())) {
            throw new BaseException(ErrorCode.CONFLICT, "Ticket number already exists: " + req.ticketNumber());
        }

        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Customer not found: " + req.customerId()));

        Product product = req.productId() != null
                ? productRepository.findById(req.productId()).orElse(null)
                : null;

        ServiceRequest sr = new ServiceRequest();
        sr.setTicketNumber(req.ticketNumber());
        sr.setCustomer(customer);
        sr.setProduct(product);
        sr.setMachineSerialNo(req.machineSerialNo());
        sr.setTitle(req.title());
        sr.setReportedIssue(req.reportedIssue());
        sr.setPriority(req.priority() != null ? req.priority() : "MEDIUM");
        sr.setStatus("OPEN");

        ServiceRequest saved = serviceRequestRepository.save(sr);
        auditLogService.log("ServiceRequest", saved.getId(), "SERVICE_REQUEST_CREATED", "Logged service ticket " + saved.getTicketNumber(), userId, ipAddress);

        return mapToSrResponse(saved);
    }

    @Transactional
    public ServiceRequestResponse resolveServiceRequest(UUID id, String resolutionNotes, UUID technicianId, String ipAddress) {
        ServiceRequest sr = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Service Request not found: " + id));

        sr.setStatus("RESOLVED");
        sr.setResolutionNotes(resolutionNotes);
        sr.setResolvedAt(Instant.now());
        sr.setAssignedTechnicianId(technicianId);

        ServiceRequest saved = serviceRequestRepository.save(sr);
        auditLogService.log("ServiceRequest", saved.getId(), "SERVICE_REQUEST_RESOLVED", "Resolved ticket " + saved.getTicketNumber(), technicianId, ipAddress);

        return mapToSrResponse(saved);
    }

    // --- Customer 360 History ---
    @Transactional(readOnly = true)
    public CustomerHistoryResponse getCustomerHistory(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Customer not found: " + customerId));

        List<ContactResponse> contacts = contactRepository.findByCustomerId(customerId).stream()
                .map(this::mapToContactResponse)
                .collect(Collectors.toList());

        List<OpportunityResponse> opps = opportunityRepository.findByCustomerId(customerId).stream()
                .map(this::mapToOppResponse)
                .collect(Collectors.toList());

        List<QuotationResponse> quotations = quotationRepository.findAll().stream()
                .filter(q -> q.getCustomer().getId().equals(customerId))
                .map(quotationService::mapToResponse)
                .collect(Collectors.toList());

        List<SalesOrderResponse> salesOrders = salesOrderRepository.findAll().stream()
                .filter(o -> o.getCustomer().getId().equals(customerId))
                .map(salesOrderService::mapToResponse)
                .collect(Collectors.toList());

        List<ServiceRequestResponse> serviceRequests = serviceRequestRepository.findByCustomerId(customerId).stream()
                .map(this::mapToSrResponse)
                .collect(Collectors.toList());

        return new CustomerHistoryResponse(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getCompanyName(),
                contacts,
                opps,
                quotations,
                salesOrders,
                serviceRequests
        );
    }

    private ContactResponse mapToContactResponse(CustomerContact c) {
        return new ContactResponse(
                c.getId(),
                c.getCustomer().getId(),
                c.getFirstName(),
                c.getLastName(),
                c.getEmail(),
                c.getPhone(),
                c.getJobTitle(),
                c.getDepartment(),
                c.getIsPrimary(),
                c.getIsActive(),
                c.getCreatedAt()
        );
    }

    private OpportunityResponse mapToOppResponse(CrmOpportunity o) {
        return new OpportunityResponse(
                o.getId(),
                o.getOpportunityCode(),
                o.getCustomer().getId(),
                o.getCustomer().getCompanyName(),
                o.getPrimaryContact() != null ? o.getPrimaryContact().getId() : null,
                o.getPrimaryContact() != null ? (o.getPrimaryContact().getFirstName() + " " + o.getPrimaryContact().getLastName()) : null,
                o.getProduct() != null ? o.getProduct().getId() : null,
                o.getProduct() != null ? o.getProduct().getName() : null,
                o.getName(),
                o.getStage(),
                o.getEstimatedValue(),
                o.getCurrency(),
                o.getProbabilityPct(),
                o.getExpectedCloseDate(),
                o.getCreatedAt()
        );
    }

    private ServiceRequestResponse mapToSrResponse(ServiceRequest sr) {
        return new ServiceRequestResponse(
                sr.getId(),
                sr.getTicketNumber(),
                sr.getCustomer().getId(),
                sr.getCustomer().getCompanyName(),
                sr.getProduct() != null ? sr.getProduct().getId() : null,
                sr.getProduct() != null ? sr.getProduct().getName() : null,
                sr.getMachineSerialNo(),
                sr.getTitle(),
                sr.getReportedIssue(),
                sr.getPriority(),
                sr.getStatus(),
                sr.getAssignedTechnicianId(),
                sr.getResolutionNotes(),
                sr.getResolvedAt(),
                sr.getCreatedAt()
        );
    }
}

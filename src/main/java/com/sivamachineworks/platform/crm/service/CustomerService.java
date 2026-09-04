package com.sivamachineworks.platform.crm.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.crm.domain.Customer;
import com.sivamachineworks.platform.crm.dto.CreateCustomerRequest;
import com.sivamachineworks.platform.crm.dto.CustomerResponse;
import com.sivamachineworks.platform.crm.repository.CustomerRepository;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;

    public CustomerService(CustomerRepository customerRepository, AuditLogService auditLogService) {
        this.customerRepository = customerRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest req, UUID userId, String ipAddress) {
        if (customerRepository.existsByCustomerCode(req.customerCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "Customer code already exists: " + req.customerCode());
        }

        Customer customer = new Customer();
        customer.setCustomerCode(req.customerCode());
        customer.setCompanyName(req.companyName());
        customer.setIndustry(req.industry());
        customer.setCountry(req.country() != null ? req.country() : "Japan");
        customer.setCreditLimit(req.creditLimit());
        customer.setPaymentTerms(req.paymentTerms() != null ? req.paymentTerms() : "Net 30");
        customer.setContactName(req.contactName());
        customer.setContactEmail(req.contactEmail());
        customer.setPhone(req.phone());
        customer.setAddress(req.address());

        Customer saved = customerRepository.save(customer);
        auditLogService.log("Customer", saved.getId(), "CUSTOMER_CREATION", "Created customer: " + saved.getCustomerCode(), userId, ipAddress);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Customer not found: " + id));
        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> listCustomers() {
        return customerRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public CustomerResponse mapToResponse(Customer c) {
        return new CustomerResponse(
                c.getId(),
                c.getCustomerCode(),
                c.getCompanyName(),
                c.getIndustry(),
                c.getCountry(),
                c.getStatus(),
                c.getCreditLimit(),
                c.getPaymentTerms(),
                c.getContactName(),
                c.getContactEmail(),
                c.getPhone(),
                c.getAddress(),
                c.getCreatedAt()
        );
    }
}

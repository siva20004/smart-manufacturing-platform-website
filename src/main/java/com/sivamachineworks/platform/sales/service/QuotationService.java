package com.sivamachineworks.platform.sales.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.crm.domain.Customer;
import com.sivamachineworks.platform.crm.repository.CustomerRepository;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.pdm.repository.ProductRepository;
import com.sivamachineworks.platform.sales.domain.Quotation;
import com.sivamachineworks.platform.sales.domain.QuotationItem;
import com.sivamachineworks.platform.sales.dto.*;
import com.sivamachineworks.platform.sales.repository.QuotationRepository;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    public QuotationService(QuotationRepository quotationRepository,
                            CustomerRepository customerRepository,
                            ProductRepository productRepository,
                            AuditLogService auditLogService) {
        this.quotationRepository = quotationRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public QuotationResponse createQuotation(CreateQuotationRequest req, UUID userId, String ipAddress) {
        if (quotationRepository.existsByQuotationCodeAndRevisionLetter(req.quotationCode(), "A")) {
            throw new BaseException(ErrorCode.CONFLICT, "Quotation " + req.quotationCode() + " Rev A already exists");
        }

        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Customer not found: " + req.customerId()));

        Quotation quotation = new Quotation();
        quotation.setQuotationCode(req.quotationCode());
        quotation.setRevisionLetter("A");
        quotation.setCustomer(customer);
        quotation.setStatus("DRAFT");
        quotation.setValidUntil(req.validUntil());
        quotation.setPaymentTerms(req.paymentTerms() != null ? req.paymentTerms() : "Net 30");
        quotation.setDeliveryIncoterms(req.deliveryIncoterms() != null ? req.deliveryIncoterms() : "EXW");

        BigDecimal subtotal = BigDecimal.ZERO;
        List<QuotationItem> items = new ArrayList<>();
        int seq = 1;

        for (QuotationItemDto itemDto : req.items()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Product not found: " + itemDto.productId()));

            BigDecimal lineTotal = itemDto.quantity().multiply(itemDto.unitPrice());
            subtotal = subtotal.add(lineTotal);

            QuotationItem item = new QuotationItem();
            item.setQuotation(quotation);
            item.setItemSeq(seq++);
            item.setProduct(product);
            item.setQuantity(itemDto.quantity());
            item.setUnitPrice(itemDto.unitPrice());
            item.setLineTotal(lineTotal);
            items.add(item);
        }

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10")); // 10% JPY consumption tax
        quotation.setSubtotalAmount(subtotal);
        quotation.setTaxAmount(tax);
        quotation.setTotalAmount(subtotal.add(tax));
        quotation.setItems(items);

        Quotation saved = quotationRepository.save(quotation);
        auditLogService.log("Quotation", saved.getId(), "QUOTATION_CREATION", "Created quotation " + saved.getQuotationCode() + " for customer " + customer.getCustomerCode(), userId, ipAddress);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public QuotationResponse getQuotationById(UUID id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Quotation not found: " + id));
        return mapToResponse(quotation);
    }

    public QuotationResponse mapToResponse(Quotation q) {
        List<QuotationItemResponse> itemResponses = q.getItems().stream()
                .map(i -> new QuotationItemResponse(
                        i.getId(),
                        i.getItemSeq(),
                        i.getProduct().getId(),
                        i.getProduct().getProductNumber(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getLineTotal()
                ))
                .collect(Collectors.toList());

        return new QuotationResponse(
                q.getId(),
                q.getQuotationCode(),
                q.getRevisionLetter(),
                q.getCustomer().getId(),
                q.getCustomer().getCompanyName(),
                q.getStatus(),
                q.getSubtotalAmount(),
                q.getTaxAmount(),
                q.getTotalAmount(),
                q.getCurrency(),
                q.getValidUntil(),
                itemResponses,
                q.getCreatedAt()
        );
    }
}

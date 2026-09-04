package com.sivamachineworks.platform.sales.domain;

import com.sivamachineworks.platform.crm.domain.Customer;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotations")
public class Quotation extends AuditableEntity {

    @Column(name = "quotation_code", nullable = false, length = 50)
    private String quotationCode;

    @Column(name = "revision_letter", nullable = false, length = 5)
    private String revisionLetter = "A";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT";

    @Column(name = "subtotal_amount", precision = 15, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 10)
    private String currency = "JPY";

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms = "Net 30";

    @Column(name = "delivery_incoterms", length = 50)
    private String deliveryIncoterms = "EXW";

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationItem> items = new ArrayList<>();

    public Quotation() {}

    public String getQuotationCode() { return quotationCode; }
    public void setQuotationCode(String quotationCode) { this.quotationCode = quotationCode; }
    public String getRevisionLetter() { return revisionLetter; }
    public void setRevisionLetter(String revisionLetter) { this.revisionLetter = revisionLetter; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getSubtotalAmount() { return subtotalAmount; }
    public void setSubtotalAmount(BigDecimal subtotalAmount) { this.subtotalAmount = subtotalAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public String getDeliveryIncoterms() { return deliveryIncoterms; }
    public void setDeliveryIncoterms(String deliveryIncoterms) { this.deliveryIncoterms = deliveryIncoterms; }
    public List<QuotationItem> getItems() { return items; }
    public void setItems(List<QuotationItem> items) { this.items = items; }
}

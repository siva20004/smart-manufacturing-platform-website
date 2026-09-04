package com.sivamachineworks.platform.crm.domain;

import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "crm_opportunities")
public class CrmOpportunity extends AuditableEntity {

    @Column(name = "opportunity_code", unique = true, nullable = false, length = 50)
    private String opportunityCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_contact_id")
    private CustomerContact primaryContact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 30)
    private String stage = "PROSPECTING";

    @Column(name = "estimated_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedValue = BigDecimal.ZERO;

    @Column(length = 10)
    private String currency = "JPY";

    @Column(name = "probability_pct", precision = 5, scale = 2)
    private BigDecimal probabilityPct = new BigDecimal("20.0");

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(name = "actual_close_date")
    private LocalDate actualCloseDate;

    @Column(name = "lost_reason", columnDefinition = "TEXT")
    private String lostReason;

    @Column(name = "assigned_rep_id")
    private UUID assignedRepId;

    public CrmOpportunity() {}

    public String getOpportunityCode() { return opportunityCode; }
    public void setOpportunityCode(String opportunityCode) { this.opportunityCode = opportunityCode; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public CustomerContact getPrimaryContact() { return primaryContact; }
    public void setPrimaryContact(CustomerContact primaryContact) { this.primaryContact = primaryContact; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(BigDecimal estimatedValue) { this.estimatedValue = estimatedValue; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getProbabilityPct() { return probabilityPct; }
    public void setProbabilityPct(BigDecimal probabilityPct) { this.probabilityPct = probabilityPct; }
    public LocalDate getExpectedCloseDate() { return expectedCloseDate; }
    public void setExpectedCloseDate(LocalDate expectedCloseDate) { this.expectedCloseDate = expectedCloseDate; }
    public LocalDate getActualCloseDate() { return actualCloseDate; }
    public void setActualCloseDate(LocalDate actualCloseDate) { this.actualCloseDate = actualCloseDate; }
    public String getLostReason() { return lostReason; }
    public void setLostReason(String lostReason) { this.lostReason = lostReason; }
    public UUID getAssignedRepId() { return assignedRepId; }
    public void setAssignedRepId(UUID assignedRepId) { this.assignedRepId = assignedRepId; }
}

package com.sivamachineworks.platform.scm.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "suppliers")
public class Supplier extends AuditableEntity {

    @Column(name = "supplier_code", unique = true, nullable = false, length = 50)
    private String supplierCode;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Column(length = 100)
    private String country = "Japan";

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(length = 50)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms = "Net 30";

    @Column(length = 10)
    private String currency = "JPY";

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    public Supplier() {}

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

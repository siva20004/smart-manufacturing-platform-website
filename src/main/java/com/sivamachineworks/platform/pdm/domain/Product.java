package com.sivamachineworks.platform.pdm.domain;

import com.sivamachineworks.platform.shared.domain.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product extends AuditableEntity {

    @Column(name = "product_number", unique = true, nullable = false)
    private String productNumber;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(length = 20)
    private String uom = "EA";

    @Column(name = "list_price", precision = 15, scale = 2)
    private BigDecimal listPrice;

    @Column(name = "standard_cost", precision = 15, scale = 2)
    private BigDecimal standardCost;

    @Column(name = "lead_time_weeks")
    private Integer leadTimeWeeks;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductRevision> revisions = new ArrayList<>();

    public Product() {}

    public String getProductNumber() { return productNumber; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public BigDecimal getListPrice() { return listPrice; }
    public void setListPrice(BigDecimal listPrice) { this.listPrice = listPrice; }
    public BigDecimal getStandardCost() { return standardCost; }
    public void setStandardCost(BigDecimal standardCost) { this.standardCost = standardCost; }
    public Integer getLeadTimeWeeks() { return leadTimeWeeks; }
    public void setLeadTimeWeeks(Integer leadTimeWeeks) { this.leadTimeWeeks = leadTimeWeeks; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ProductRevision> getRevisions() { return revisions; }
    public void setRevisions(List<ProductRevision> revisions) { this.revisions = revisions; }
}

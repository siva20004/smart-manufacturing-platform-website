package com.sivamachineworks.platform.ai.retrieval;

import com.sivamachineworks.platform.ai.dto.AiSourceCitation;
import com.sivamachineworks.platform.bom.domain.EbomHeader;
import com.sivamachineworks.platform.bom.domain.EbomItem;
import com.sivamachineworks.platform.inventory.domain.InventoryStock;
import com.sivamachineworks.platform.production.domain.ProductionOrder;
import com.sivamachineworks.platform.sales.domain.SalesOrder;
import com.sivamachineworks.platform.scm.domain.PurchaseOrder;
import com.sivamachineworks.platform.scm.domain.Supplier;

import java.util.ArrayList;
import java.util.List;

public class RetrievedBusinessContext {
    private final List<ProductionOrder> productionOrders = new ArrayList<>();
    private final List<InventoryStock> inventoryStocks = new ArrayList<>();
    private final List<EbomItem> bomComponents = new ArrayList<>();
    private final List<PurchaseOrder> purchaseOrders = new ArrayList<>();
    private final List<Supplier> suppliers = new ArrayList<>();
    private final List<SalesOrder> salesOrders = new ArrayList<>();
    private final List<AiSourceCitation> citations = new ArrayList<>();
    private final List<String> contextSummaries = new ArrayList<>();
    private boolean foundMatchingData = false;

    public RetrievedBusinessContext() {}

    public List<ProductionOrder> getProductionOrders() { return productionOrders; }
    public List<InventoryStock> getInventoryStocks() { return inventoryStocks; }
    public List<EbomItem> getBomComponents() { return bomComponents; }
    public List<PurchaseOrder> getPurchaseOrders() { return purchaseOrders; }
    public List<Supplier> getSuppliers() { return suppliers; }
    public List<SalesOrder> getSalesOrders() { return salesOrders; }
    public List<AiSourceCitation> getCitations() { return citations; }
    public List<String> getContextSummaries() { return contextSummaries; }
    public boolean hasFoundMatchingData() { return foundMatchingData; }
    public void setFoundMatchingData(boolean foundMatchingData) { this.foundMatchingData = foundMatchingData; }

    public void addCitation(String entityType, String identifier, String desc) {
        citations.add(new AiSourceCitation(entityType, identifier, desc));
    }

    public void addSummary(String summary) {
        contextSummaries.add(summary);
    }
}

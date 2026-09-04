package com.sivamachineworks.platform.ai.retrieval;

import com.sivamachineworks.platform.bom.domain.EbomHeader;
import com.sivamachineworks.platform.bom.domain.EbomItem;
import com.sivamachineworks.platform.bom.repository.EbomHeaderRepository;
import com.sivamachineworks.platform.bom.repository.EbomItemRepository;
import com.sivamachineworks.platform.inventory.domain.InventoryStock;
import com.sivamachineworks.platform.inventory.repository.InventoryStockRepository;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.pdm.repository.ProductRepository;
import com.sivamachineworks.platform.production.repository.ProductionOrderRepository;
import com.sivamachineworks.platform.sales.domain.SalesOrder;
import com.sivamachineworks.platform.sales.repository.SalesOrderRepository;
import com.sivamachineworks.platform.scm.domain.PurchaseOrder;
import com.sivamachineworks.platform.scm.domain.PurchaseOrderItem;
import com.sivamachineworks.platform.scm.domain.Supplier;
import com.sivamachineworks.platform.scm.repository.PurchaseOrderRepository;
import com.sivamachineworks.platform.scm.repository.SupplierRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ManufacturingContextRetriever {

    private final ProductionOrderRepository productionOrderRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final EbomHeaderRepository ebomHeaderRepository;
    private final EbomItemRepository ebomItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;

    public ManufacturingContextRetriever(
            ProductionOrderRepository productionOrderRepository,
            InventoryStockRepository inventoryStockRepository,
            EbomHeaderRepository ebomHeaderRepository,
            EbomItemRepository ebomItemRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            SalesOrderRepository salesOrderRepository,
            ProductRepository productRepository) {
        this.productionOrderRepository = productionOrderRepository;
        this.inventoryStockRepository = inventoryStockRepository;
        this.ebomHeaderRepository = ebomHeaderRepository;
        this.ebomItemRepository = ebomItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public RetrievedBusinessContext retrieveContext(String question) {
        RetrievedBusinessContext ctx = new RetrievedBusinessContext();
        String lowerQuery = question.toLowerCase();

        // 1. Detect Production Order specific queries
        Pattern poPattern = Pattern.compile("(?i)(PRD-[a-zA-Z0-9-]+|PO-[a-zA-Z0-9-]+)");
        Matcher poMatcher = poPattern.matcher(question);
        if (poMatcher.find()) {
            String orderCode = poMatcher.group(1);
            productionOrderRepository.findByOrderCode(orderCode).ifPresent(order -> {
                ctx.getProductionOrders().add(order);
                ctx.setFoundMatchingData(true);
                ctx.addCitation("ProductionOrder", order.getOrderCode(), "Status: " + order.getStatus() + ", Product: " + order.getProduct().getProductNumber());

                StringBuilder summary = new StringBuilder("Production Order " + order.getOrderCode() + ": Status is " + order.getStatus() + ". ");
                if ("PLANNED".equals(order.getStatus())) {
                    summary.append("Waiting for material reservation in warehouse before shop routing can begin. ");
                } else if ("IN_PROGRESS".equals(order.getStatus())) {
                    long pendingOps = order.getOperations().stream().filter(op -> !"COMPLETED".equals(op.getStatus())).count();
                    summary.append("Currently in progress with " + pendingOps + " pending operation sequences. ");
                }
                ctx.addSummary(summary.toString());
            });
        }

        // 2. Material Shortages / BOM queries (e.g., "shortage", "materials", "HM-500", "piston pump")
        if (lowerQuery.contains("shortage") || lowerQuery.contains("material") || lowerQuery.contains("hm-500") || lowerQuery.contains("hm500") || lowerQuery.contains("delay")) {
            List<InventoryStock> allStocks = inventoryStockRepository.findAll();
            ctx.getInventoryStocks().addAll(allStocks);

            List<Product> products = productRepository.findAll();
            for (Product p : products) {
                if (lowerQuery.contains("hm-500") || lowerQuery.contains("hm500") || lowerQuery.contains("material") || lowerQuery.contains("shortage")) {
                    List<EbomHeader> eboms = ebomHeaderRepository.findByProductId(p.getId());
                    for (EbomHeader ebom : eboms) {
                        List<EbomItem> items = ebomItemRepository.findByEbomHeaderIdAndItemType(ebom.getId(), "COMPONENT");
                        ctx.getBomComponents().addAll(items);
                        ctx.setFoundMatchingData(true);

                        for (EbomItem item : items) {
                            allStocks.stream()
                                    .filter(s -> s.getPartNumber().equals(item.getPartNumber()))
                                    .findFirst()
                                    .ifPresent(s -> {
                                        ctx.addCitation("InventoryStock", s.getPartNumber(), "Warehouse: " + s.getWarehouse().getCode() + " | On-Hand: " + s.getQtyOnHand() + " | Avail: " + s.getQtyAvailable());
                                        if (s.getQtyAvailable().compareTo(BigDecimal.ZERO) <= 0) {
                                            ctx.addSummary("CRITICAL SHORTAGE: Part " + item.getPartNumber() + " (" + item.getDescription() + ") has ZERO available stock in warehouse " + s.getWarehouse().getCode());
                                        }
                                    });
                        }
                    }
                }
            }
        }

        // 3. Supplier Performance & Delivery Risks
        if (lowerQuery.contains("supplier") || lowerQuery.contains("delivery risk") || lowerQuery.contains("vendor") || lowerQuery.contains("po")) {
            List<PurchaseOrder> pos = purchaseOrderRepository.findAll();
            List<Supplier> suppliers = supplierRepository.findAll();
            ctx.getPurchaseOrders().addAll(pos);
            ctx.getSuppliers().addAll(suppliers);
            ctx.setFoundMatchingData(true);

            for (PurchaseOrder po : pos) {
                ctx.addCitation("PurchaseOrder", po.getPoCode(), "Supplier: " + po.getSupplier().getCompanyName() + " | Status: " + po.getStatus());
                if (!"RECEIVED".equals(po.getStatus())) {
                    for (PurchaseOrderItem item : po.getItems()) {
                        BigDecimal pending = item.getQuantityOrdered().subtract(item.getQuantityReceived());
                        if (pending.compareTo(BigDecimal.ZERO) > 0) {
                            ctx.addSummary("PENDING SCM DELIVERY: PO " + po.getPoCode() + " from " + po.getSupplier().getCompanyName() + " has " + pending + " units of " + item.getPartNumber() + " pending receipt.");
                        }
                    }
                }
            }
        }

        // 4. Customer Orders at Risk
        if (lowerQuery.contains("customer") || lowerQuery.contains("at risk") || lowerQuery.contains("sales order") || lowerQuery.contains("order")) {
            List<SalesOrder> sos = salesOrderRepository.findAll();
            ctx.getSalesOrders().addAll(sos);
            if (!sos.isEmpty()) {
                ctx.setFoundMatchingData(true);
                for (SalesOrder so : sos) {
                    ctx.addCitation("SalesOrder", so.getSoCode(), "Customer: " + so.getCustomer().getCompanyName() + " | Status: " + so.getStatus());
                    if ("DRAFT".equals(so.getStatus())) {
                        ctx.addSummary("Sales Order " + so.getSoCode() + " for " + so.getCustomer().getCompanyName() + " is in DRAFT status awaiting inventory confirmation.");
                    }
                }
            }
        }

        return ctx;
    }
}

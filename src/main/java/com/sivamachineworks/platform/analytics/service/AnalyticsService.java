package com.sivamachineworks.platform.analytics.service;

import com.sivamachineworks.platform.analytics.dto.InventoryValueDto;
import com.sivamachineworks.platform.analytics.dto.OrderVolumeDto;
import com.sivamachineworks.platform.analytics.dto.ProductionAnalyticsDto;
import com.sivamachineworks.platform.analytics.dto.SalesByCustomerDto;
import com.sivamachineworks.platform.analytics.dto.SalesByProductDto;
import com.sivamachineworks.platform.analytics.dto.ServiceMetricsDto;
import com.sivamachineworks.platform.analytics.dto.SupplierPerformanceDto;
import com.sivamachineworks.platform.crm.domain.ServiceRequest;
import com.sivamachineworks.platform.crm.repository.ServiceRequestRepository;
import com.sivamachineworks.platform.inventory.domain.InventoryStock;
import com.sivamachineworks.platform.inventory.repository.InventoryStockRepository;
import com.sivamachineworks.platform.production.domain.ProductionOrder;
import com.sivamachineworks.platform.production.repository.ProductionOrderRepository;
import com.sivamachineworks.platform.sales.domain.SalesOrder;
import com.sivamachineworks.platform.sales.domain.SalesOrderItem;
import com.sivamachineworks.platform.sales.repository.SalesOrderRepository;
import com.sivamachineworks.platform.scm.domain.PurchaseOrder;
import com.sivamachineworks.platform.scm.domain.PurchaseOrderItem;
import com.sivamachineworks.platform.scm.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final SalesOrderRepository salesOrderRepository;
    private final InventoryStockRepository stockRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public AnalyticsService(SalesOrderRepository salesOrderRepository,
                            InventoryStockRepository stockRepository,
                            ProductionOrderRepository productionOrderRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            ServiceRequestRepository serviceRequestRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.stockRepository = stockRepository;
        this.productionOrderRepository = productionOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Transactional(readOnly = true)
    public List<SalesByCustomerDto> getSalesByCustomer() {
        List<SalesOrder> orders = salesOrderRepository.findAll();
        Map<UUID, List<SalesOrder>> byCustomer = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCustomer().getId()));

        List<SalesByCustomerDto> results = new ArrayList<>();
        for (var entry : byCustomer.entrySet()) {
            List<SalesOrder> custOrders = entry.getValue();
            SalesOrder first = custOrders.get(0);
            BigDecimal totalRev = custOrders.stream()
                    .map(SalesOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            results.add(new SalesByCustomerDto(
                    first.getCustomer().getId(),
                    first.getCustomer().getCustomerCode(),
                    first.getCustomer().getCompanyName(),
                    (long) custOrders.size(),
                    totalRev
            ));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<SalesByProductDto> getSalesByProduct() {
        List<SalesOrder> orders = salesOrderRepository.findAll();
        Map<UUID, List<SalesOrderItem>> byProduct = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(i -> i.getProduct().getId()));

        List<SalesByProductDto> results = new ArrayList<>();
        for (var entry : byProduct.entrySet()) {
            List<SalesOrderItem> pItems = entry.getValue();
            SalesOrderItem first = pItems.get(0);
            BigDecimal totalQty = pItems.stream().map(SalesOrderItem::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRev = pItems.stream().map(SalesOrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

            results.add(new SalesByProductDto(
                    first.getProduct().getId(),
                    first.getProduct().getProductNumber(),
                    first.getProduct().getName(),
                    totalQty,
                    totalRev
            ));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<OrderVolumeDto> getOrderVolume() {
        List<SalesOrder> orders = salesOrderRepository.findAll();
        Map<String, List<SalesOrder>> byStatus = orders.stream()
                .collect(Collectors.groupingBy(SalesOrder::getStatus));

        List<OrderVolumeDto> results = new ArrayList<>();
        for (var entry : byStatus.entrySet()) {
            BigDecimal totalAmt = entry.getValue().stream().map(SalesOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            results.add(new OrderVolumeDto(entry.getKey(), (long) entry.getValue().size(), totalAmt));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<InventoryValueDto> getInventoryValue() {
        List<InventoryStock> stocks = stockRepository.findAll();
        Map<UUID, List<InventoryStock>> byWarehouse = stocks.stream()
                .collect(Collectors.groupingBy(s -> s.getWarehouse().getId()));

        List<InventoryValueDto> results = new ArrayList<>();
        for (var entry : byWarehouse.entrySet()) {
            List<InventoryStock> whStocks = entry.getValue();
            InventoryStock first = whStocks.get(0);
            BigDecimal totalVal = whStocks.stream()
                    .map(s -> s.getQtyOnHand().multiply(s.getUnitCost() != null ? s.getUnitCost() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            results.add(new InventoryValueDto(
                    first.getWarehouse().getId(),
                    first.getWarehouse().getCode(),
                    first.getWarehouse().getPlantLocation(),
                    (long) whStocks.size(),
                    totalVal
            ));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public ProductionAnalyticsDto getProductionMetrics() {
        List<ProductionOrder> prdOrders = productionOrderRepository.findAll();
        long planned = prdOrders.stream().filter(o -> "PLANNED".equals(o.getStatus()) || "MATERIAL_RESERVED".equals(o.getStatus())).count();
        long inProgress = prdOrders.stream().filter(o -> "IN_PROGRESS".equals(o.getStatus()) || "QUALITY_CHECK".equals(o.getStatus())).count();
        long completed = prdOrders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();

        BigDecimal unitsProduced = prdOrders.stream()
                .map(ProductionOrder::getQuantityCompleted)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProductionAnalyticsDto(
                planned,
                completed,
                inProgress,
                unitsProduced,
                100.0 // On-time rate
        );
    }

    @Transactional(readOnly = true)
    public List<SupplierPerformanceDto> getSupplierPerformance() {
        List<PurchaseOrder> pos = purchaseOrderRepository.findAll();
        Map<UUID, List<PurchaseOrder>> bySupplier = pos.stream()
                .collect(Collectors.groupingBy(po -> po.getSupplier().getId()));

        List<SupplierPerformanceDto> results = new ArrayList<>();
        for (var entry : bySupplier.entrySet()) {
            List<PurchaseOrder> sPos = entry.getValue();
            PurchaseOrder first = sPos.get(0);
            BigDecimal totalSpend = sPos.stream().map(PurchaseOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal ordered = sPos.stream()
                    .flatMap(po -> po.getItems().stream())
                    .map(PurchaseOrderItem::getQuantityOrdered)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal received = sPos.stream()
                    .flatMap(po -> po.getItems().stream())
                    .map(PurchaseOrderItem::getQuantityReceived)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            results.add(new SupplierPerformanceDto(
                    first.getSupplier().getId(),
                    first.getSupplier().getSupplierCode(),
                    first.getSupplier().getCompanyName(),
                    (long) sPos.size(),
                    totalSpend,
                    ordered,
                    received
            ));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public ServiceMetricsDto getServiceMetrics() {
        List<ServiceRequest> tickets = serviceRequestRepository.findAll();
        long total = tickets.size();
        long open = tickets.stream().filter(t -> "OPEN".equals(t.getStatus())).count();
        long inProg = tickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus()) || "ASSIGNED".equals(t.getStatus())).count();
        long resolved = tickets.stream().filter(t -> "RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus())).count();
        long critical = tickets.stream().filter(t -> "CRITICAL".equals(t.getPriority())).count();

        return new ServiceMetricsDto(total, open, inProg, resolved, critical);
    }
}

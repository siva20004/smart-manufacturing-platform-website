package com.sivamachineworks.platform.production.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.bom.domain.EbomHeader;
import com.sivamachineworks.platform.bom.domain.EbomItem;
import com.sivamachineworks.platform.bom.domain.MbomHeader;
import com.sivamachineworks.platform.bom.domain.MbomItem;
import com.sivamachineworks.platform.bom.repository.EbomHeaderRepository;
import com.sivamachineworks.platform.bom.repository.EbomItemRepository;
import com.sivamachineworks.platform.bom.repository.MbomHeaderRepository;
import com.sivamachineworks.platform.bom.repository.MbomItemRepository;
import com.sivamachineworks.platform.inventory.domain.InventoryReservation;
import com.sivamachineworks.platform.inventory.domain.InventoryStock;
import com.sivamachineworks.platform.inventory.domain.InventoryTransaction;
import com.sivamachineworks.platform.inventory.domain.Warehouse;
import com.sivamachineworks.platform.inventory.repository.InventoryReservationRepository;
import com.sivamachineworks.platform.inventory.repository.InventoryStockRepository;
import com.sivamachineworks.platform.inventory.repository.InventoryTransactionRepository;
import com.sivamachineworks.platform.inventory.repository.WarehouseRepository;
import com.sivamachineworks.platform.inventory.service.InventoryService;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.pdm.repository.ProductRepository;
import com.sivamachineworks.platform.production.domain.*;
import com.sivamachineworks.platform.production.dto.*;
import com.sivamachineworks.platform.production.repository.*;
import com.sivamachineworks.platform.sales.domain.SalesOrder;
import com.sivamachineworks.platform.sales.repository.SalesOrderRepository;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductionService {

    private final ProductionOrderRepository orderRepository;
    private final ProductionOperationRepository operationRepository;
    private final MaterialConsumptionRepository consumptionRepository;
    private final QualityInspectionRepository inspectionRepository;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryStockRepository stockRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryService inventoryService;
    private final MbomHeaderRepository mbomHeaderRepository;
    private final MbomItemRepository mbomItemRepository;
    private final EbomHeaderRepository ebomHeaderRepository;
    private final EbomItemRepository ebomItemRepository;
    private final AuditLogService auditLogService;

    public ProductionService(ProductionOrderRepository orderRepository,
                             ProductionOperationRepository operationRepository,
                             MaterialConsumptionRepository consumptionRepository,
                             QualityInspectionRepository inspectionRepository,
                             ProductRepository productRepository,
                             SalesOrderRepository salesOrderRepository,
                             WarehouseRepository warehouseRepository,
                             InventoryStockRepository stockRepository,
                             InventoryReservationRepository reservationRepository,
                             InventoryTransactionRepository transactionRepository,
                             InventoryService inventoryService,
                             MbomHeaderRepository mbomHeaderRepository,
                             MbomItemRepository mbomItemRepository,
                             EbomHeaderRepository ebomHeaderRepository,
                             EbomItemRepository ebomItemRepository,
                             AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.operationRepository = operationRepository;
        this.consumptionRepository = consumptionRepository;
        this.inspectionRepository = inspectionRepository;
        this.productRepository = productRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockRepository = stockRepository;
        this.reservationRepository = reservationRepository;
        this.transactionRepository = transactionRepository;
        this.inventoryService = inventoryService;
        this.mbomHeaderRepository = mbomHeaderRepository;
        this.mbomItemRepository = mbomItemRepository;
        this.ebomHeaderRepository = ebomHeaderRepository;
        this.ebomItemRepository = ebomItemRepository;
        this.auditLogService = auditLogService;
    }

    // --- 1. Create Production Order (PLANNED) ---
    @Transactional
    public ProductionOrderResponse createProductionOrder(CreateProductionOrderRequest req, UUID userId, String ipAddress) {
        if (orderRepository.existsByOrderCode(req.orderCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "Production order code already exists: " + req.orderCode());
        }

        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Product not found: " + req.productId()));

        if (req.quantityPlanned().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Quantity planned must be greater than zero");
        }

        String plant = req.plantLocation() != null ? req.plantLocation() : "Osaka";
        Warehouse warehouse = req.warehouseId() != null
                ? warehouseRepository.findById(req.warehouseId()).orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Warehouse not found"))
                : warehouseRepository.findFirstByPlantLocationAndIsActiveTrue(plant).orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "No warehouse for plant " + plant));

        SalesOrder salesOrder = null;
        if (req.salesOrderId() != null) {
            salesOrder = salesOrderRepository.findById(req.salesOrderId())
                    .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Sales Order not found: " + req.salesOrderId()));
        }

        // Find released mBOM
        List<MbomHeader> mboms = mbomHeaderRepository.findByProductId(product.getId());
        MbomHeader mbom = mboms.stream()
                .filter(m -> "RELEASED".equals(m.getStatus()) && plant.equalsIgnoreCase(m.getPlantLocation()))
                .findFirst()
                .orElse(null);

        ProductionOrder order = new ProductionOrder();
        order.setOrderCode(req.orderCode());
        order.setSalesOrder(salesOrder);
        order.setProduct(product);
        order.setMbomHeader(mbom);
        order.setWarehouse(warehouse);
        order.setPlantLocation(plant);
        order.setQuantityPlanned(req.quantityPlanned());
        order.setQuantityCompleted(BigDecimal.ZERO);
        order.setPlannedStartDate(req.plannedStartDate());
        order.setPlannedCompletionDate(req.plannedCompletionDate());
        order.setStatus("PLANNED");

        // Generate standard operations
        List<ProductionOperation> ops = new ArrayList<>();
        ops.add(createOp(order, 10, "Heavy Structural Fabrication", "WC-FAB-01", new BigDecimal("12.0")));
        ops.add(createOp(order, 20, "Hydraulic Power Unit Assembly", "WC-HYD-01", new BigDecimal("8.0")));
        ops.add(createOp(order, 30, "CNC Control & Servo Wiring", "WC-ELEC-01", new BigDecimal("10.0")));
        ops.add(createOp(order, 40, "Final Integration & Alignment", "WC-FIN-01", new BigDecimal("16.0")));
        ops.add(createOp(order, 50, "Precision Metrology & Laser Calibration", "WC-QC-01", new BigDecimal("4.0")));
        order.setOperations(ops);

        ProductionOrder saved = orderRepository.save(order);
        auditLogService.log("ProductionOrder", saved.getId(), "PRODUCTION_ORDER_CREATED", "Created Production Order " + saved.getOrderCode() + " for product " + product.getProductNumber(), userId, ipAddress);

        return mapToOrderResponse(saved);
    }

    private ProductionOperation createOp(ProductionOrder order, int seq, String name, String wc, BigDecimal plannedHours) {
        ProductionOperation op = new ProductionOperation();
        op.setProductionOrder(order);
        op.setOperationSeq(seq);
        op.setOperationName(name);
        op.setWorkCenterCode(wc);
        op.setStatus("PENDING");
        op.setPlannedHours(plannedHours);
        return op;
    }

    // --- 2. State Transition: PLANNED -> MATERIAL_RESERVED ---
    @Transactional
    public ProductionOrderResponse reserveMaterials(UUID orderId, UUID userId, String ipAddress) {
        ProductionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Production Order not found: " + orderId));

        if (!"PLANNED".equals(order.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot reserve materials. Invalid status transition from: " + order.getStatus());
        }

        // Reserve components in warehouse
        List<EbomItem> bomItems = getBomComponents(order.getProduct().getId(), order.getMbomHeader());
        for (EbomItem comp : bomItems) {
            BigDecimal totalReq = comp.getQuantity().multiply(order.getQuantityPlanned());
            if (order.getSalesOrder() != null) {
                inventoryService.reserveInventory(
                        order.getSalesOrder().getId(),
                        order.getWarehouse().getId(),
                        comp.getPartNumber(),
                        totalReq,
                        userId,
                        ipAddress
                );
            } else {
                inventoryService.reserveInventoryForProduction(
                        order.getId(),
                        order.getWarehouse().getId(),
                        comp.getPartNumber(),
                        totalReq,
                        userId,
                        ipAddress
                );
            }
        }

        order.setStatus("MATERIAL_RESERVED");
        ProductionOrder saved = orderRepository.save(order);
        auditLogService.log("ProductionOrder", saved.getId(), "PRODUCTION_MATERIALS_RESERVED", "Materials reserved for " + saved.getOrderCode(), userId, ipAddress);

        return mapToOrderResponse(saved);
    }

    // --- 3. State Transition: MATERIAL_RESERVED -> IN_PROGRESS ---
    @Transactional
    public ProductionOrderResponse startProduction(UUID orderId, UUID userId, String ipAddress) {
        ProductionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Production Order not found: " + orderId));

        if (!"MATERIAL_RESERVED".equals(order.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot start production. Must be in MATERIAL_RESERVED status. Current: " + order.getStatus());
        }

        order.setStatus("IN_PROGRESS");
        order.setActualStartDate(Instant.now());

        if (!order.getOperations().isEmpty()) {
            ProductionOperation firstOp = order.getOperations().get(0);
            firstOp.setStatus("IN_PROGRESS");
            firstOp.setStartedAt(Instant.now());
            firstOp.setAssignedTo(userId);
        }

        ProductionOrder saved = orderRepository.save(order);
        auditLogService.log("ProductionOrder", saved.getId(), "PRODUCTION_STARTED", "Started production for " + saved.getOrderCode(), userId, ipAddress);

        return mapToOrderResponse(saved);
    }

    // --- 4. Operation Status Updates ---
    @Transactional
    public ProductionOperationResponse updateOperation(UUID orderId, Integer seq, UpdateOperationRequest req, UUID userId, String ipAddress) {
        ProductionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Production Order not found: " + orderId));

        if (!"IN_PROGRESS".equals(order.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot update operations when order status is: " + order.getStatus());
        }

        ProductionOperation op = operationRepository.findByProductionOrderIdAndOperationSeq(orderId, seq)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Operation seq " + seq + " not found on order: " + orderId));

        op.setStatus(req.status());
        if (req.actualHours() != null) op.setActualHours(req.actualHours());
        if (req.notes() != null) op.setNotes(req.notes());

        if ("COMPLETED".equals(req.status())) {
            op.setCompletedAt(Instant.now());
            // Automatically start next pending operation
            operationRepository.findByProductionOrderIdAndOperationSeq(orderId, seq + 10).ifPresent(nextOp -> {
                if ("PENDING".equals(nextOp.getStatus())) {
                    nextOp.setStatus("IN_PROGRESS");
                    nextOp.setStartedAt(Instant.now());
                }
            });
        }

        ProductionOperation saved = operationRepository.save(op);
        return mapToOpResponse(saved);
    }

    // --- 5. State Transition: IN_PROGRESS -> QUALITY_CHECK ---
    @Transactional
    public QualityInspectionResponse submitQualityInspection(UUID orderId, SubmitQualityInspectionRequest req, UUID inspectorId, String ipAddress) {
        ProductionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Production Order not found: " + orderId));

        if (!"IN_PROGRESS".equals(order.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Quality check can only be submitted for IN_PROGRESS orders. Current: " + order.getStatus());
        }

        // Verify all operations are completed
        boolean allOpsCompleted = order.getOperations().stream().allMatch(o -> "COMPLETED".equals(o.getStatus()));
        if (!allOpsCompleted) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot perform quality check: Not all production operations are COMPLETED");
        }

        if (!"PASSED".equalsIgnoreCase(req.status())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Quality inspection did not pass. Status: " + req.status());
        }

        QualityInspection qi = new QualityInspection();
        qi.setInspectionCode(req.inspectionCode());
        qi.setProductionOrder(order);
        qi.setInspectionType(req.inspectionType() != null ? req.inspectionType() : "FINAL_ACCEPTANCE");
        qi.setStatus("PASSED");
        qi.setFatSpindleRunoutMm(req.fatSpindleRunoutMm());
        qi.setFatPositioningAccuracyMm(req.fatPositioningAccuracyMm());
        qi.setInspectorId(inspectorId);
        qi.setInspectedAt(Instant.now());
        qi.setNotes(req.notes());

        QualityInspection savedQi = inspectionRepository.save(qi);

        order.setStatus("QUALITY_CHECK");
        orderRepository.save(order);

        auditLogService.log("QualityInspection", savedQi.getId(), "QUALITY_INSPECTION_PASSED", "FAT Inspection passed for " + order.getOrderCode(), inspectorId, ipAddress);

        return new QualityInspectionResponse(
                savedQi.getId(),
                savedQi.getInspectionCode(),
                order.getId(),
                savedQi.getInspectionType(),
                savedQi.getStatus(),
                savedQi.getFatSpindleRunoutMm(),
                savedQi.getFatPositioningAccuracyMm(),
                savedQi.getInspectorId(),
                savedQi.getInspectedAt(),
                savedQi.getNotes()
        );
    }

    // --- 6. State Transition: QUALITY_CHECK -> COMPLETED (Consumes Materials, Produces Finished Goods) ---
    @Transactional
    public ProductionOrderResponse completeProduction(UUID orderId, UUID userId, String ipAddress) {
        ProductionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Production Order not found: " + orderId));

        if (!"QUALITY_CHECK".equals(order.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Invalid transition to COMPLETED. Production order must be in QUALITY_CHECK status. Current: " + order.getStatus());
        }

        Warehouse wh = order.getWarehouse();
        BigDecimal plannedQty = order.getQuantityPlanned();

        // 1. Consume raw materials/components from Inventory
        List<EbomItem> components = getBomComponents(order.getProduct().getId(), order.getMbomHeader());
        for (EbomItem comp : components) {
            BigDecimal consumedQty = comp.getQuantity().multiply(plannedQty);

            // Deduct on_hand and reserved stock
            stockRepository.findByWarehouseIdAndPartNumber(wh.getId(), comp.getPartNumber()).ifPresent(stock -> {
                stock.setQtyOnHand(stock.getQtyOnHand().subtract(consumedQty).max(BigDecimal.ZERO));
                stock.setQtyReserved(stock.getQtyReserved().subtract(consumedQty).max(BigDecimal.ZERO));
                stock.setQtyAvailable(stock.getQtyOnHand().subtract(stock.getQtyReserved()));
                stockRepository.save(stock);
            });

            // Record Material Consumption
            MaterialConsumption mc = new MaterialConsumption();
            mc.setId(UUID.randomUUID());
            mc.setProductionOrder(order);
            mc.setWarehouse(wh);
            mc.setPartNumber(comp.getPartNumber());
            mc.setDescription(comp.getDescription());
            mc.setPlannedQty(consumedQty);
            mc.setActualQtyConsumed(consumedQty);
            mc.setUom(comp.getUom());
            mc.setConsumedAt(Instant.now());
            mc.setConsumedBy(userId);
            consumptionRepository.save(mc);

            // Record Ledger Transaction (GOODS_ISSUE)
            InventoryTransaction txIssue = new InventoryTransaction();
            txIssue.setId(UUID.randomUUID());
            txIssue.setTransactionNumber("TXN-PRD-ISSUE-" + System.currentTimeMillis() + "-" + comp.getPartNumber());
            txIssue.setPartNumber(comp.getPartNumber());
            txIssue.setWarehouse(wh);
            txIssue.setTransactionType("GOODS_ISSUE");
            txIssue.setQuantity(consumedQty);
            txIssue.setReferenceType("PRODUCTION_ORDER");
            txIssue.setReferenceId(order.getId());
            txIssue.setNotes("Consumed in Production Order: " + order.getOrderCode());
            txIssue.setPerformedBy(userId);
            transactionRepository.save(txIssue);
        }

        // Release/Consume active reservations
        List<InventoryReservation> reservations = order.getSalesOrder() != null
                ? reservationRepository.findBySalesOrderIdAndStatus(order.getSalesOrder().getId(), "ACTIVE")
                : reservationRepository.findByProductionOrderIdAndStatus(order.getId(), "ACTIVE");
        for (InventoryReservation res : reservations) {
            res.setStatus("CONSUMED");
            res.setReleasedAt(Instant.now());
            reservationRepository.save(res);
        }

        // 2. Increase Finished Goods Inventory (e.g. SMW-HM-500)
        String finishedProductNumber = order.getProduct().getProductNumber();
        InventoryStock fgStock = stockRepository.findByWarehouseIdAndPartNumber(wh.getId(), finishedProductNumber)
                .orElseGet(() -> {
                    InventoryStock s = new InventoryStock();
                    s.setWarehouse(wh);
                    s.setPartNumber(finishedProductNumber);
                    s.setDescription(order.getProduct().getName());
                    s.setQtyOnHand(BigDecimal.ZERO);
                    s.setQtyReserved(BigDecimal.ZERO);
                    s.setQtyAvailable(BigDecimal.ZERO);
                    s.setUom("EA");
                    s.setUnitCost(new BigDecimal("12500000.00"));
                    return s;
                });

        fgStock.setQtyOnHand(fgStock.getQtyOnHand().add(plannedQty));
        fgStock.setQtyAvailable(fgStock.getQtyOnHand().subtract(fgStock.getQtyReserved()));
        stockRepository.save(fgStock);

        // Record Finished Goods Receipt Transaction
        InventoryTransaction txReceipt = new InventoryTransaction();
        txReceipt.setId(UUID.randomUUID());
        txReceipt.setTransactionNumber("TXN-PRD-FG-" + System.currentTimeMillis());
        txReceipt.setPartNumber(finishedProductNumber);
        txReceipt.setWarehouse(wh);
        txReceipt.setTransactionType("GOODS_RECEIPT");
        txReceipt.setQuantity(plannedQty);
        txReceipt.setReferenceType("PRODUCTION_ORDER");
        txReceipt.setReferenceId(order.getId());
        txReceipt.setNotes("Finished Goods Output from Production Order: " + order.getOrderCode());
        txReceipt.setPerformedBy(userId);
        transactionRepository.save(txReceipt);

        // 3. Update Order status
        order.setStatus("COMPLETED");
        order.setQuantityCompleted(plannedQty);
        order.setActualCompletionDate(Instant.now());

        if (order.getSalesOrder() != null) {
            order.getSalesOrder().setStatus("READY_TO_SHIP");
            salesOrderRepository.save(order.getSalesOrder());
        }

        ProductionOrder saved = orderRepository.save(order);
        auditLogService.log("ProductionOrder", saved.getId(), "PRODUCTION_ORDER_COMPLETED",
                "Completed Production Order " + saved.getOrderCode() + " (" + plannedQty + " units of " + finishedProductNumber + " added to FG stock in " + wh.getCode() + ")", userId, ipAddress);

        return mapToOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductionOrderResponse getProductionOrderById(UUID id) {
        ProductionOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Production Order not found: " + id));
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<ProductionOrderResponse> listProductionOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private List<EbomItem> getBomComponents(UUID productId, MbomHeader mbom) {
        if (mbom != null) {
            List<MbomItem> mItems = mbomItemRepository.findByMbomHeaderId(mbom.getId());
            List<EbomItem> items = new ArrayList<>();
            for (MbomItem mi : mItems) {
                if ("ASSEMBLY".equals(mi.getItemType())) continue;
                EbomItem ei = new EbomItem();
                ei.setPartNumber(mi.getPartNumber());
                ei.setDescription(mi.getDescription());
                ei.setQuantity(mi.getQuantity());
                ei.setUom(mi.getUom());
                items.add(ei);
            }
            return items;
        }

        // Fallback eBOM
        List<EbomHeader> eboms = ebomHeaderRepository.findByProductId(productId);
        Optional<EbomHeader> releasedEbom = eboms.stream().filter(e -> "RELEASED".equals(e.getStatus()) || "APPROVED".equals(e.getStatus())).findFirst();
        if (releasedEbom.isPresent()) {
            return ebomItemRepository.findByEbomHeaderId(releasedEbom.get().getId()).stream()
                    .filter(i -> !"ASSEMBLY".equals(i.getItemType()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private ProductionOrderResponse mapToOrderResponse(ProductionOrder o) {
        List<ProductionOperationResponse> opResponses = o.getOperations().stream()
                .map(this::mapToOpResponse)
                .collect(Collectors.toList());

        return new ProductionOrderResponse(
                o.getId(),
                o.getOrderCode(),
                o.getSalesOrder() != null ? o.getSalesOrder().getId() : null,
                o.getProduct().getId(),
                o.getProduct().getProductNumber(),
                o.getProduct().getName(),
                o.getMbomHeader() != null ? o.getMbomHeader().getId() : null,
                o.getMbomHeader() != null ? o.getMbomHeader().getRevisionCode() : "A",
                o.getWarehouse().getId(),
                o.getWarehouse().getCode(),
                o.getPlantLocation(),
                o.getQuantityPlanned(),
                o.getQuantityCompleted(),
                o.getPlannedStartDate(),
                o.getPlannedCompletionDate(),
                o.getActualStartDate(),
                o.getActualCompletionDate(),
                o.getStatus(),
                opResponses,
                o.getCreatedAt()
        );
    }

    private ProductionOperationResponse mapToOpResponse(ProductionOperation op) {
        return new ProductionOperationResponse(
                op.getId(),
                op.getOperationSeq(),
                op.getOperationName(),
                op.getWorkCenterCode(),
                op.getStatus(),
                op.getPlannedHours(),
                op.getActualHours(),
                op.getStartedAt(),
                op.getCompletedAt(),
                op.getAssignedTo(),
                op.getNotes()
        );
    }
}

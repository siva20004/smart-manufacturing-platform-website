package com.sivamachineworks.platform.scm.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.inventory.domain.InventoryStock;
import com.sivamachineworks.platform.inventory.domain.InventoryTransaction;
import com.sivamachineworks.platform.inventory.domain.Warehouse;
import com.sivamachineworks.platform.inventory.repository.InventoryStockRepository;
import com.sivamachineworks.platform.inventory.repository.InventoryTransactionRepository;
import com.sivamachineworks.platform.inventory.repository.WarehouseRepository;
import com.sivamachineworks.platform.scm.domain.*;
import com.sivamachineworks.platform.scm.dto.*;
import com.sivamachineworks.platform.scm.repository.*;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProcurementService {

    private final SupplierRepository supplierRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryStockRepository stockRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final AuditLogService auditLogService;

    public ProcurementService(SupplierRepository supplierRepository,
                              PurchaseRequestRepository purchaseRequestRepository,
                              PurchaseOrderRepository purchaseOrderRepository,
                              GoodsReceiptRepository goodsReceiptRepository,
                              WarehouseRepository warehouseRepository,
                              InventoryStockRepository stockRepository,
                              InventoryTransactionRepository transactionRepository,
                              AuditLogService auditLogService) {
        this.supplierRepository = supplierRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockRepository = stockRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogService = auditLogService;
    }

    // --- Supplier Operations ---
    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest req, UUID userId, String ipAddress) {
        if (supplierRepository.existsBySupplierCode(req.supplierCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "Supplier code already exists: " + req.supplierCode());
        }

        Supplier supplier = new Supplier();
        supplier.setSupplierCode(req.supplierCode());
        supplier.setCompanyName(req.companyName());
        supplier.setTaxId(req.taxId());
        supplier.setCountry(req.country() != null ? req.country() : "Japan");
        supplier.setContactEmail(req.contactEmail());
        supplier.setPhone(req.phone());
        supplier.setAddress(req.address());
        supplier.setPaymentTerms(req.paymentTerms() != null ? req.paymentTerms() : "Net 30");
        supplier.setStatus("ACTIVE");

        Supplier saved = supplierRepository.save(supplier);
        auditLogService.log("Supplier", saved.getId(), "SUPPLIER_CREATION", "Created supplier: " + saved.getSupplierCode(), userId, ipAddress);

        return mapToSupplierResponse(saved);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Supplier not found: " + id));
        return mapToSupplierResponse(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> listSuppliers() {
        return supplierRepository.findAll().stream().map(this::mapToSupplierResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PurchaseRequestResponse> listPurchaseRequests() {
        return purchaseRequestRepository.findAll().stream().map(this::mapToPrResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> listPurchaseOrders() {
        return purchaseOrderRepository.findAll().stream().map(this::mapToPoResponse).collect(Collectors.toList());
    }

    // --- Purchase Request Operations ---
    @Transactional
    public PurchaseRequestResponse createPurchaseRequest(CreatePurchaseRequestDto req, UUID userId, String ipAddress) {
        if (purchaseRequestRepository.existsByPrCode(req.prCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "Purchase request code already exists: " + req.prCode());
        }

        PurchaseRequest pr = new PurchaseRequest();
        pr.setPrCode(req.prCode());
        pr.setRequestedBy(userId);
        pr.setSourceType(req.sourceType() != null ? req.sourceType() : "MRP");
        pr.setSalesOrderId(req.salesOrderId());
        pr.setStatus("DRAFT");

        BigDecimal totalEstimated = BigDecimal.ZERO;
        List<PurchaseRequestItem> items = new ArrayList<>();
        int seq = 1;

        for (PurchaseRequestItemDto itemDto : req.items()) {
            if (itemDto.quantityRequested().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BaseException(ErrorCode.BAD_REQUEST, "Quantity requested must be greater than zero");
            }

            PurchaseRequestItem item = new PurchaseRequestItem();
            item.setPurchaseRequest(pr);
            item.setItemSeq(seq++);
            item.setPartNumber(itemDto.partNumber());
            item.setDescription(itemDto.description());
            item.setQuantityRequested(itemDto.quantityRequested());
            item.setUom(itemDto.uom() != null ? itemDto.uom() : "EA");
            item.setEstimatedUnitPrice(itemDto.estimatedUnitPrice());
            item.setRequiredByDate(itemDto.requiredByDate());
            item.setSuggestedSupplierId(itemDto.suggestedSupplierId());

            if (itemDto.estimatedUnitPrice() != null) {
                totalEstimated = totalEstimated.add(itemDto.estimatedUnitPrice().multiply(itemDto.quantityRequested()));
            }
            items.add(item);
        }

        pr.setTotalEstimatedAmount(totalEstimated);
        pr.setItems(items);

        PurchaseRequest saved = purchaseRequestRepository.save(pr);
        auditLogService.log("PurchaseRequest", saved.getId(), "PURCHASE_REQUEST_CREATION", "Created Purchase Request " + saved.getPrCode(), userId, ipAddress);

        return mapToPrResponse(saved);
    }

    @Transactional
    public PurchaseRequestResponse approvePurchaseRequest(UUID id, UUID approverId, String ipAddress) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Purchase Request not found: " + id));

        if (!"DRAFT".equals(pr.getStatus()) && !"SUBMITTED".equals(pr.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot approve PR with status: " + pr.getStatus());
        }

        pr.setStatus("APPROVED");
        pr.setApprovedBy(approverId);
        pr.setApprovedAt(Instant.now());

        PurchaseRequest saved = purchaseRequestRepository.save(pr);
        auditLogService.log("PurchaseRequest", saved.getId(), "PURCHASE_REQUEST_APPROVED", "Approved Purchase Request " + saved.getPrCode(), approverId, ipAddress);

        return mapToPrResponse(saved);
    }

    // --- Purchase Order Operations ---
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest req, UUID userId, String ipAddress) {
        if (purchaseOrderRepository.existsByPoCode(req.poCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "Purchase Order code already exists: " + req.poCode());
        }

        Supplier supplier = supplierRepository.findById(req.supplierId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Supplier not found: " + req.supplierId()));

        if (!"ACTIVE".equalsIgnoreCase(supplier.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot create PO: Supplier status is " + supplier.getStatus());
        }

        Warehouse warehouse = warehouseRepository.findById(req.warehouseId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Warehouse not found: " + req.warehouseId()));

        PurchaseRequest pr = null;
        if (req.purchaseRequestId() != null) {
            pr = purchaseRequestRepository.findById(req.purchaseRequestId())
                    .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Purchase Request not found: " + req.purchaseRequestId()));
            if (!"APPROVED".equals(pr.getStatus())) {
                throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Referenced Purchase Request must be APPROVED. Current status: " + pr.getStatus());
            }
            pr.setStatus("CONVERTED_TO_PO");
            purchaseRequestRepository.save(pr);
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setPoCode(req.poCode());
        po.setSupplier(supplier);
        po.setPurchaseRequest(pr);
        po.setWarehouse(warehouse);
        po.setExpectedDeliveryDate(req.expectedDeliveryDate());
        po.setStatus("DRAFT");

        BigDecimal subtotal = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = new ArrayList<>();
        int seq = 1;

        for (PurchaseOrderItemDto itemDto : req.items()) {
            if (itemDto.quantityOrdered().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BaseException(ErrorCode.BAD_REQUEST, "Quantity ordered must be greater than zero");
            }

            BigDecimal lineTotal = itemDto.quantityOrdered().multiply(itemDto.unitPrice());
            subtotal = subtotal.add(lineTotal);

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setItemSeq(seq++);
            item.setPartNumber(itemDto.partNumber());
            item.setDescription(itemDto.description());
            item.setQuantityOrdered(itemDto.quantityOrdered());
            item.setQuantityReceived(BigDecimal.ZERO);
            item.setUom(itemDto.uom() != null ? itemDto.uom() : "EA");
            item.setUnitPrice(itemDto.unitPrice());
            item.setLineTotal(lineTotal);
            item.setStatus("ORDERED");
            items.add(item);
        }

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10"));
        po.setSubtotalAmount(subtotal);
        po.setTaxAmount(tax);
        po.setTotalAmount(subtotal.add(tax));
        po.setItems(items);

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogService.log("PurchaseOrder", saved.getId(), "PURCHASE_ORDER_CREATION", "Created Purchase Order " + saved.getPoCode() + " for supplier " + supplier.getSupplierCode(), userId, ipAddress);

        return mapToPoResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse issuePurchaseOrder(UUID id, UUID userId, String ipAddress) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Purchase Order not found: " + id));

        if (!"DRAFT".equals(po.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Can only issue PO in DRAFT status. Current: " + po.getStatus());
        }

        po.setStatus("ISSUED");
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogService.log("PurchaseOrder", saved.getId(), "PURCHASE_ORDER_ISSUED", "Issued Purchase Order " + saved.getPoCode() + " to supplier " + saved.getSupplier().getSupplierCode(), userId, ipAddress);

        return mapToPoResponse(saved);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(UUID id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Purchase Order not found: " + id));
        return mapToPoResponse(po);
    }

    // --- Goods Receipt Operations ---
    @Transactional
    public GoodsReceiptResponse postGoodsReceipt(PostGoodsReceiptRequest req, UUID userId, String ipAddress) {
        if (goodsReceiptRepository.existsByGrCode(req.grCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "Goods receipt code already exists: " + req.grCode());
        }

        PurchaseOrder po = purchaseOrderRepository.findById(req.purchaseOrderId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Purchase Order not found: " + req.purchaseOrderId()));

        if (!"ISSUED".equals(po.getStatus()) && !"PARTIALLY_RECEIVED".equals(po.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot receive goods against PO with status: " + po.getStatus());
        }

        GoodsReceipt gr = new GoodsReceipt();
        gr.setGrCode(req.grCode());
        gr.setPurchaseOrder(po);
        gr.setSupplier(po.getSupplier());
        gr.setWarehouse(po.getWarehouse());
        gr.setDeliveryNoteNo(req.deliveryNoteNo());
        gr.setStatus("RECEIVED");
        gr.setReceivedBy(userId);
        gr.setReceivedAt(Instant.now());

        Map<UUID, PurchaseOrderItem> poItemMap = po.getItems().stream()
                .collect(Collectors.toMap(PurchaseOrderItem::getId, i -> i));

        List<GoodsReceiptItem> grItems = new ArrayList<>();
        int seq = 1;

        for (GoodsReceiptItemDto itemDto : req.items()) {
            PurchaseOrderItem poItem = poItemMap.get(itemDto.purchaseOrderItemId());
            if (poItem == null) {
                throw new BaseException(ErrorCode.BAD_REQUEST, "Invalid Purchase Order item ID: " + itemDto.purchaseOrderItemId());
            }

            BigDecimal remaining = poItem.getQuantityOrdered().subtract(poItem.getQuantityReceived());
            if (itemDto.quantityReceived().compareTo(remaining) > 0) {
                throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Received quantity (" + itemDto.quantityReceived() + ") exceeds remaining allowed ordered quantity (" + remaining + ") for part " + poItem.getPartNumber());
            }

            // Update PO Item quantity received
            poItem.setQuantityReceived(poItem.getQuantityReceived().add(itemDto.quantityReceived()));
            if (poItem.getQuantityReceived().compareTo(poItem.getQuantityOrdered()) >= 0) {
                poItem.setStatus("RECEIVED");
            } else {
                poItem.setStatus("PARTIALLY_RECEIVED");
            }

            GoodsReceiptItem grItem = new GoodsReceiptItem();
            grItem.setGoodsReceipt(gr);
            grItem.setPurchaseOrderItem(poItem);
            grItem.setItemSeq(seq++);
            grItem.setPartNumber(poItem.getPartNumber());
            grItem.setQuantityReceived(itemDto.quantityReceived());
            grItem.setUom(poItem.getUom());
            grItem.setUnitCost(poItem.getUnitPrice());
            grItem.setNotes(itemDto.notes());
            grItems.add(grItem);

            // Increment Inventory in destination warehouse
            Warehouse wh = po.getWarehouse();
            InventoryStock stock = stockRepository.findByWarehouseIdAndPartNumber(wh.getId(), poItem.getPartNumber())
                    .orElseGet(() -> {
                        InventoryStock s = new InventoryStock();
                        s.setWarehouse(wh);
                        s.setPartNumber(poItem.getPartNumber());
                        s.setDescription(poItem.getDescription());
                        s.setQtyOnHand(BigDecimal.ZERO);
                        s.setQtyReserved(BigDecimal.ZERO);
                        s.setQtyAvailable(BigDecimal.ZERO);
                        s.setUom(poItem.getUom());
                        s.setUnitCost(poItem.getUnitPrice());
                        return s;
                    });

            stock.setQtyOnHand(stock.getQtyOnHand().add(itemDto.quantityReceived()));
            stock.setQtyAvailable(stock.getQtyOnHand().subtract(stock.getQtyReserved()));
            stockRepository.save(stock);

            // Record Inventory Transaction
            InventoryTransaction tx = new InventoryTransaction();
            tx.setId(UUID.randomUUID());
            tx.setTransactionNumber("TXN-GR-" + System.currentTimeMillis() + "-" + seq);
            tx.setPartNumber(poItem.getPartNumber());
            tx.setWarehouse(wh);
            tx.setTransactionType("GOODS_RECEIPT");
            tx.setQuantity(itemDto.quantityReceived());
            tx.setUnitCost(poItem.getUnitPrice());
            tx.setReferenceType("PURCHASE_ORDER");
            tx.setReferenceId(po.getId());
            tx.setNotes("Goods Receipt: " + req.grCode() + " (DN: " + req.deliveryNoteNo() + ")");
            tx.setPerformedBy(userId);
            transactionRepository.save(tx);
        }

        // Update PO Header status
        boolean allReceived = po.getItems().stream().allMatch(i -> "RECEIVED".equals(i.getStatus()));
        po.setStatus(allReceived ? "RECEIVED" : "PARTIALLY_RECEIVED");
        purchaseOrderRepository.save(po);

        gr.setItems(grItems);
        GoodsReceipt savedGr = goodsReceiptRepository.save(gr);

        auditLogService.log("GoodsReceipt", savedGr.getId(), "GOODS_RECEIPT_POSTED",
                "Posted Goods Receipt " + savedGr.getGrCode() + " for PO " + po.getPoCode() + " (" + grItems.size() + " items updated in " + po.getWarehouse().getCode() + ")", userId, ipAddress);

        return mapToGrResponse(savedGr);
    }

    private SupplierResponse mapToSupplierResponse(Supplier s) {
        return new SupplierResponse(
                s.getId(),
                s.getSupplierCode(),
                s.getCompanyName(),
                s.getTaxId(),
                s.getCountry(),
                s.getContactEmail(),
                s.getPhone(),
                s.getAddress(),
                s.getPaymentTerms(),
                s.getStatus(),
                s.getCreatedAt()
        );
    }

    private PurchaseRequestResponse mapToPrResponse(PurchaseRequest pr) {
        List<PurchaseRequestItemResponse> itemResponses = pr.getItems().stream()
                .map(i -> new PurchaseRequestItemResponse(
                        i.getId(),
                        i.getItemSeq(),
                        i.getPartNumber(),
                        i.getDescription(),
                        i.getQuantityRequested(),
                        i.getUom(),
                        i.getEstimatedUnitPrice(),
                        i.getRequiredByDate(),
                        i.getSuggestedSupplierId()
                ))
                .collect(Collectors.toList());

        return new PurchaseRequestResponse(
                pr.getId(),
                pr.getPrCode(),
                pr.getRequestedBy(),
                pr.getSourceType(),
                pr.getSalesOrderId(),
                pr.getStatus(),
                pr.getTotalEstimatedAmount(),
                pr.getApprovedBy(),
                pr.getApprovedAt(),
                itemResponses,
                pr.getCreatedAt()
        );
    }

    private PurchaseOrderResponse mapToPoResponse(PurchaseOrder po) {
        List<PurchaseOrderItemResponse> itemResponses = po.getItems().stream()
                .map(i -> new PurchaseOrderItemResponse(
                        i.getId(),
                        i.getItemSeq(),
                        i.getPartNumber(),
                        i.getDescription(),
                        i.getQuantityOrdered(),
                        i.getQuantityReceived(),
                        i.getUom(),
                        i.getUnitPrice(),
                        i.getLineTotal(),
                        i.getStatus()
                ))
                .collect(Collectors.toList());

        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoCode(),
                po.getSupplier().getId(),
                po.getSupplier().getCompanyName(),
                po.getPurchaseRequest() != null ? po.getPurchaseRequest().getId() : null,
                po.getWarehouse().getId(),
                po.getWarehouse().getCode(),
                po.getOrderDate(),
                po.getExpectedDeliveryDate(),
                po.getStatus(),
                po.getSubtotalAmount(),
                po.getTaxAmount(),
                po.getTotalAmount(),
                po.getCurrency(),
                itemResponses,
                po.getCreatedAt()
        );
    }

    private GoodsReceiptResponse mapToGrResponse(GoodsReceipt gr) {
        List<GoodsReceiptItemResponse> itemResponses = gr.getItems().stream()
                .map(i -> new GoodsReceiptItemResponse(
                        i.getId(),
                        i.getPurchaseOrderItem().getId(),
                        i.getItemSeq(),
                        i.getPartNumber(),
                        i.getQuantityReceived(),
                        i.getUom(),
                        i.getUnitCost(),
                        i.getNotes()
                ))
                .collect(Collectors.toList());

        return new GoodsReceiptResponse(
                gr.getId(),
                gr.getGrCode(),
                gr.getPurchaseOrder().getId(),
                gr.getPurchaseOrder().getPoCode(),
                gr.getSupplier().getId(),
                gr.getSupplier().getCompanyName(),
                gr.getWarehouse().getId(),
                gr.getWarehouse().getCode(),
                gr.getDeliveryNoteNo(),
                gr.getStatus(),
                gr.getReceivedBy(),
                gr.getReceivedAt(),
                itemResponses
        );
    }
}

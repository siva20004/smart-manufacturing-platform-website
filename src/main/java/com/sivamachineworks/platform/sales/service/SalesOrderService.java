package com.sivamachineworks.platform.sales.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.bom.domain.EbomHeader;
import com.sivamachineworks.platform.bom.domain.EbomItem;
import com.sivamachineworks.platform.bom.domain.MbomHeader;
import com.sivamachineworks.platform.bom.domain.MbomItem;
import com.sivamachineworks.platform.bom.repository.EbomHeaderRepository;
import com.sivamachineworks.platform.bom.repository.EbomItemRepository;
import com.sivamachineworks.platform.bom.repository.MbomHeaderRepository;
import com.sivamachineworks.platform.bom.repository.MbomItemRepository;
import com.sivamachineworks.platform.crm.domain.Customer;
import com.sivamachineworks.platform.crm.repository.CustomerRepository;
import com.sivamachineworks.platform.inventory.domain.Warehouse;
import com.sivamachineworks.platform.inventory.repository.InventoryStockRepository;
import com.sivamachineworks.platform.inventory.repository.WarehouseRepository;
import com.sivamachineworks.platform.inventory.service.InventoryService;
import com.sivamachineworks.platform.pdm.domain.Product;
import com.sivamachineworks.platform.pdm.repository.ProductRepository;
import com.sivamachineworks.platform.sales.domain.Quotation;
import com.sivamachineworks.platform.sales.domain.SalesOrder;
import com.sivamachineworks.platform.sales.domain.SalesOrderItem;
import com.sivamachineworks.platform.sales.dto.*;
import com.sivamachineworks.platform.sales.repository.QuotationRepository;
import com.sivamachineworks.platform.sales.repository.SalesOrderRepository;
import com.sivamachineworks.platform.scm.domain.MaterialRequirement;
import com.sivamachineworks.platform.scm.domain.MaterialRequirementItem;
import com.sivamachineworks.platform.scm.repository.MaterialRequirementRepository;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;
    private final MbomHeaderRepository mbomHeaderRepository;
    private final MbomItemRepository mbomItemRepository;
    private final EbomHeaderRepository ebomHeaderRepository;
    private final EbomItemRepository ebomItemRepository;
    private final MaterialRequirementRepository materialRequirementRepository;
    private final AuditLogService auditLogService;

    public SalesOrderService(SalesOrderRepository salesOrderRepository,
                             QuotationRepository quotationRepository,
                             CustomerRepository customerRepository,
                             ProductRepository productRepository,
                             WarehouseRepository warehouseRepository,
                             InventoryService inventoryService,
                             MbomHeaderRepository mbomHeaderRepository,
                             MbomItemRepository mbomItemRepository,
                             EbomHeaderRepository ebomHeaderRepository,
                             EbomItemRepository ebomItemRepository,
                             MaterialRequirementRepository materialRequirementRepository,
                             AuditLogService auditLogService) {
        this.salesOrderRepository = salesOrderRepository;
        this.quotationRepository = quotationRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryService = inventoryService;
        this.mbomHeaderRepository = mbomHeaderRepository;
        this.mbomItemRepository = mbomItemRepository;
        this.ebomHeaderRepository = ebomHeaderRepository;
        this.ebomItemRepository = ebomItemRepository;
        this.materialRequirementRepository = materialRequirementRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public SalesOrderResponse createSalesOrder(CreateSalesOrderRequest req, UUID userId, String ipAddress) {
        if (salesOrderRepository.existsBySoCode(req.soCode())) {
            throw new BaseException(ErrorCode.CONFLICT, "Sales order code already exists: " + req.soCode());
        }

        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Customer not found: " + req.customerId()));

        Quotation quotation = null;
        if (req.quotationId() != null) {
            quotation = quotationRepository.findById(req.quotationId())
                    .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Quotation not found: " + req.quotationId()));
        }

        SalesOrder order = new SalesOrder();
        order.setSoCode(req.soCode());
        order.setQuotation(quotation);
        order.setCustomer(customer);
        order.setCustomerPoNumber(req.customerPoNumber());
        order.setOrderDate(LocalDate.now());
        order.setRequestedDeliveryDate(req.requestedDeliveryDate());
        order.setPlantLocation(req.plantLocation() != null ? req.plantLocation() : "Osaka");
        order.setStatus("DRAFT");
        order.setShippingAddress(req.shippingAddress());
        order.setSpecialInstructions(req.specialInstructions());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<SalesOrderItem> items = new ArrayList<>();
        int seq = 1;

        for (SalesOrderItemDto itemDto : req.items()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Product not found: " + itemDto.productId()));

            if (itemDto.quantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BaseException(ErrorCode.BAD_REQUEST, "Quantity must be greater than zero");
            }

            BigDecimal lineTotal = itemDto.quantity().multiply(itemDto.unitPrice());
            subtotal = subtotal.add(lineTotal);

            SalesOrderItem item = new SalesOrderItem();
            item.setSalesOrder(order);
            item.setItemSeq(seq++);
            item.setProduct(product);
            item.setQuantity(itemDto.quantity());
            item.setUnitPrice(itemDto.unitPrice());
            item.setLineTotal(lineTotal);
            items.add(item);
        }

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10"));
        order.setSubtotalAmount(subtotal);
        order.setTaxAmount(tax);
        order.setTotalAmount(subtotal.add(tax));
        order.setItems(items);

        SalesOrder saved = salesOrderRepository.save(order);
        auditLogService.log("SalesOrder", saved.getId(), "SALES_ORDER_CREATION", "Created Sales Order " + saved.getSoCode() + " for customer " + customer.getCustomerCode(), userId, ipAddress);

        return mapToResponse(saved);
    }

    @Transactional
    public OrderConfirmationResponse confirmSalesOrder(UUID orderId, UUID userId, String ipAddress) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Sales Order not found: " + orderId));

        if (!"DRAFT".equals(order.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Can only confirm Sales Order in DRAFT status. Current status: " + order.getStatus());
        }

        // 1. Validate customer status
        Customer customer = order.getCustomer();
        if ("CREDIT_HOLD".equalsIgnoreCase(customer.getStatus()) || "INACTIVE".equalsIgnoreCase(customer.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot confirm order: Customer is on " + customer.getStatus());
        }

        // 2. Identify default warehouse for plant location
        Warehouse warehouse = warehouseRepository.findFirstByPlantLocationAndIsActiveTrue(order.getPlantLocation())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "No active warehouse found for plant " + order.getPlantLocation()));

        List<MaterialReservationDto> reservations = new ArrayList<>();
        List<MaterialShortageDto> shortages = new ArrayList<>();
        String applicableBomType = "NONE";
        String applicableBomRevision = "NONE";

        // 3. For each sales order line item, explode BOM and check/reserve materials
        for (SalesOrderItem item : order.getItems()) {
            Product product = item.getProduct();
            BigDecimal orderQty = item.getQuantity();

            // First check released mBOM
            List<MbomHeader> mboms = mbomHeaderRepository.findByProductId(product.getId());
            Optional<MbomHeader> releasedMbom = mboms.stream()
                    .filter(m -> "RELEASED".equals(m.getStatus()) && order.getPlantLocation().equalsIgnoreCase(m.getPlantLocation()))
                    .findFirst();

            if (releasedMbom.isPresent()) {
                applicableBomType = "mBOM";
                applicableBomRevision = releasedMbom.get().getRevisionCode();
                List<MbomItem> mItems = mbomItemRepository.findByMbomHeaderId(releasedMbom.get().getId());

                for (MbomItem comp : mItems) {
                    if ("ASSEMBLY".equals(comp.getItemType())) continue; // Skip abstract assembly groupings

                    BigDecimal requiredQty = comp.getQuantity().multiply(orderQty);
                    BigDecimal reserved = inventoryService.reserveInventory(order.getId(), warehouse.getId(), comp.getPartNumber(), requiredQty, userId, ipAddress);

                    if (reserved.compareTo(BigDecimal.ZERO) > 0) {
                        reservations.add(new MaterialReservationDto(
                                comp.getPartNumber(),
                                comp.getDescription(),
                                reserved,
                                comp.getUom(),
                                warehouse.getId(),
                                warehouse.getCode()
                        ));
                    }

                    BigDecimal shortage = requiredQty.subtract(reserved);
                    if (shortage.compareTo(BigDecimal.ZERO) > 0) {
                        shortages.add(new MaterialShortageDto(
                                comp.getPartNumber(),
                                comp.getDescription(),
                                requiredQty,
                                reserved,
                                shortage,
                                comp.getUom()
                        ));
                    }
                }
            } else {
                // Fallback to released eBOM
                List<EbomHeader> eboms = ebomHeaderRepository.findByProductId(product.getId());
                Optional<EbomHeader> releasedEbom = eboms.stream()
                        .filter(e -> "RELEASED".equals(e.getStatus()) || "APPROVED".equals(e.getStatus()))
                        .findFirst();

                if (releasedEbom.isPresent()) {
                    applicableBomType = "eBOM";
                    applicableBomRevision = releasedEbom.get().getRevisionCode();
                    List<EbomItem> eItems = ebomItemRepository.findByEbomHeaderId(releasedEbom.get().getId());

                    for (EbomItem comp : eItems) {
                        if ("ASSEMBLY".equals(comp.getItemType())) continue;

                        BigDecimal requiredQty = comp.getQuantity().multiply(orderQty);
                        BigDecimal reserved = inventoryService.reserveInventory(order.getId(), warehouse.getId(), comp.getPartNumber(), requiredQty, userId, ipAddress);

                        if (reserved.compareTo(BigDecimal.ZERO) > 0) {
                            reservations.add(new MaterialReservationDto(
                                    comp.getPartNumber(),
                                    comp.getDescription(),
                                    reserved,
                                    comp.getUom(),
                                    warehouse.getId(),
                                    warehouse.getCode()
                            ));
                        }

                        BigDecimal shortage = requiredQty.subtract(reserved);
                        if (shortage.compareTo(BigDecimal.ZERO) > 0) {
                            shortages.add(new MaterialShortageDto(
                                    comp.getPartNumber(),
                                    comp.getDescription(),
                                    requiredQty,
                                    reserved,
                                    shortage,
                                    comp.getUom()
                            ));
                        }
                    }
                }
            }
        }

        // 4. If shortages identified, generate Material Requirements (MRP run)
        UUID matReqId = null;
        if (!shortages.isEmpty()) {
            MaterialRequirement matReq = new MaterialRequirement();
            matReq.setRunNumber("MRP-" + order.getSoCode() + "-" + System.currentTimeMillis());
            matReq.setSalesOrder(order);
            matReq.setTriggeredBy(userId);
            matReq.setRunStatus("COMPLETED");

            List<MaterialRequirementItem> reqItems = new ArrayList<>();
            for (MaterialShortageDto shortage : shortages) {
                MaterialRequirementItem rItem = new MaterialRequirementItem();
                rItem.setId(UUID.randomUUID());
                rItem.setMaterialRequirement(matReq);
                rItem.setPartNumber(shortage.partNumber());
                rItem.setDescription(shortage.description());
                rItem.setRequiredQty(shortage.requiredQty());
                rItem.setAvailableQty(shortage.availableQty());
                rItem.setShortageQty(shortage.shortageQty());
                rItem.setUom(shortage.uom());
                rItem.setStatus("SHORTAGE");
                reqItems.add(rItem);
            }
            matReq.setItems(reqItems);
            MaterialRequirement savedMatReq = materialRequirementRepository.save(matReq);
            matReqId = savedMatReq.getId();
        }

        // 5. Update Order Status
        order.setStatus("CONFIRMED");
        order.setConfirmedBy(userId);
        order.setConfirmedAt(Instant.now());
        for (SalesOrderItem i : order.getItems()) {
            i.setStatus("CONFIRMED");
        }

        SalesOrder savedOrder = salesOrderRepository.save(order);
        auditLogService.log("SalesOrder", savedOrder.getId(), "SALES_ORDER_CONFIRMED",
                "Confirmed order " + savedOrder.getSoCode() + " (" + reservations.size() + " materials reserved, " + shortages.size() + " shortages triggered MRP)", userId, ipAddress);

        return new OrderConfirmationResponse(
                mapToResponse(savedOrder),
                applicableBomType,
                applicableBomRevision,
                reservations,
                shortages,
                matReqId
        );
    }

    @Transactional
    public SalesOrderResponse cancelSalesOrder(UUID orderId, UUID userId, String ipAddress) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Sales Order not found: " + orderId));

        if ("CANCELLED".equals(order.getStatus()) || "SHIPPED".equals(order.getStatus())) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot cancel order with status: " + order.getStatus());
        }

        // Release inventory reservations
        inventoryService.releaseReservations(order.getId(), userId, ipAddress);

        order.setStatus("CANCELLED");
        for (SalesOrderItem item : order.getItems()) {
            item.setStatus("CANCELLED");
        }

        SalesOrder saved = salesOrderRepository.save(order);
        auditLogService.log("SalesOrder", saved.getId(), "SALES_ORDER_CANCELLED", "Cancelled Sales Order " + saved.getSoCode() + " and released all reservations", userId, ipAddress);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse getSalesOrderById(UUID id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Sales Order not found: " + id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<SalesOrderResponse> listSalesOrders() {
        return salesOrderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SalesOrderResponse mapToResponse(SalesOrder o) {
        List<SalesOrderItemResponse> itemResponses = o.getItems().stream()
                .map(i -> new SalesOrderItemResponse(
                        i.getId(),
                        i.getItemSeq(),
                        i.getProduct().getId(),
                        i.getProduct().getProductNumber(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getLineTotal(),
                        i.getStatus()
                ))
                .collect(Collectors.toList());

        return new SalesOrderResponse(
                o.getId(),
                o.getSoCode(),
                o.getQuotation() != null ? o.getQuotation().getId() : null,
                o.getCustomer().getId(),
                o.getCustomer().getCompanyName(),
                o.getCustomerPoNumber(),
                o.getOrderDate(),
                o.getRequestedDeliveryDate(),
                o.getPlantLocation(),
                o.getStatus(),
                o.getSubtotalAmount(),
                o.getTaxAmount(),
                o.getTotalAmount(),
                o.getCurrency(),
                o.getConfirmedBy(),
                o.getConfirmedAt(),
                itemResponses,
                o.getCreatedAt()
        );
    }
}

package com.sivamachineworks.platform.inventory.service;

import com.sivamachineworks.platform.audit.service.AuditLogService;
import com.sivamachineworks.platform.inventory.domain.InventoryReservation;
import com.sivamachineworks.platform.inventory.domain.InventoryStock;
import com.sivamachineworks.platform.inventory.domain.InventoryTransaction;
import com.sivamachineworks.platform.inventory.domain.Warehouse;
import com.sivamachineworks.platform.inventory.dto.StockAdjustmentRequest;
import com.sivamachineworks.platform.inventory.dto.StockResponse;
import com.sivamachineworks.platform.inventory.repository.InventoryReservationRepository;
import com.sivamachineworks.platform.inventory.repository.InventoryStockRepository;
import com.sivamachineworks.platform.inventory.repository.InventoryTransactionRepository;
import com.sivamachineworks.platform.inventory.repository.WarehouseRepository;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryStockRepository stockRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryReservationRepository reservationRepository;
    private final AuditLogService auditLogService;

    public InventoryService(WarehouseRepository warehouseRepository,
                            InventoryStockRepository stockRepository,
                            InventoryTransactionRepository transactionRepository,
                            InventoryReservationRepository reservationRepository,
                            AuditLogService auditLogService) {
        this.warehouseRepository = warehouseRepository;
        this.stockRepository = stockRepository;
        this.transactionRepository = transactionRepository;
        this.reservationRepository = reservationRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByWarehouse(UUID warehouseId) {
        return stockRepository.findByWarehouseId(warehouseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockResponse adjustStock(StockAdjustmentRequest req, UUID userId, String ipAddress) {
        Warehouse warehouse = warehouseRepository.findById(req.warehouseId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "Warehouse not found: " + req.warehouseId()));

        InventoryStock stock = stockRepository.findByWarehouseIdAndPartNumber(req.warehouseId(), req.partNumber())
                .orElseGet(() -> {
                    InventoryStock s = new InventoryStock();
                    s.setWarehouse(warehouse);
                    s.setPartNumber(req.partNumber());
                    s.setQtyOnHand(BigDecimal.ZERO);
                    s.setQtyReserved(BigDecimal.ZERO);
                    s.setQtyAvailable(BigDecimal.ZERO);
                    return s;
                });

        BigDecimal newOnHand = stock.getQtyOnHand().add(req.quantity());
        if (newOnHand.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(ErrorCode.BUSINESS_RULE_VIOLATION, "Negative inventory prohibited. Available on-hand: " + stock.getQtyOnHand());
        }

        stock.setQtyOnHand(newOnHand);
        stock.setQtyAvailable(newOnHand.subtract(stock.getQtyReserved()));
        InventoryStock saved = stockRepository.save(stock);

        // Record transaction
        InventoryTransaction tx = new InventoryTransaction();
        tx.setId(UUID.randomUUID());
        tx.setTransactionNumber("TXN-" + System.currentTimeMillis());
        tx.setPartNumber(req.partNumber());
        tx.setWarehouse(warehouse);
        tx.setTransactionType(req.quantity().compareTo(BigDecimal.ZERO) >= 0 ? "RECEIPT" : "ISSUE");
        tx.setQuantity(req.quantity().abs());
        tx.setReferenceType("MANUAL_ADJUSTMENT");
        tx.setNotes(req.notes());
        tx.setPerformedBy(userId);
        tx.setCreatedAt(Instant.now());
        transactionRepository.save(tx);

        auditLogService.log("InventoryStock", saved.getId(), "STOCK_ADJUSTMENT", "Adjusted " + req.partNumber() + " by " + req.quantity() + " at " + warehouse.getCode(), userId, ipAddress);

        return mapToResponse(saved);
    }

    @Transactional
    public BigDecimal reserveInventory(UUID salesOrderId, UUID warehouseId, String partNumber, BigDecimal requiredQty, UUID userId, String ipAddress) {
        InventoryStock stock = stockRepository.findByWarehouseIdAndPartNumber(warehouseId, partNumber)
                .orElse(null);

        if (stock == null || stock.getQtyAvailable().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal available = stock.getQtyAvailable();
        BigDecimal toReserve = available.min(requiredQty);

        stock.setQtyReserved(stock.getQtyReserved().add(toReserve));
        stock.setQtyAvailable(stock.getQtyOnHand().subtract(stock.getQtyReserved()));
        stockRepository.save(stock);

        InventoryReservation reservation = new InventoryReservation();
        reservation.setSalesOrderId(salesOrderId);
        reservation.setWarehouse(stock.getWarehouse());
        reservation.setPartNumber(partNumber);
        reservation.setQuantityReserved(toReserve);
        reservation.setStatus("ACTIVE");
        reservationRepository.save(reservation);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setId(UUID.randomUUID());
        tx.setTransactionNumber("TXN-RES-" + System.currentTimeMillis());
        tx.setPartNumber(partNumber);
        tx.setWarehouse(stock.getWarehouse());
        tx.setTransactionType("RESERVATION");
        tx.setQuantity(toReserve);
        tx.setReferenceType("SALES_ORDER");
        tx.setReferenceId(salesOrderId);
        tx.setPerformedBy(userId);
        transactionRepository.save(tx);

        return toReserve;
    }

    @Transactional
    public BigDecimal reserveInventoryForProduction(UUID productionOrderId, UUID warehouseId, String partNumber, BigDecimal requiredQty, UUID userId, String ipAddress) {
        InventoryStock stock = stockRepository.findByWarehouseIdAndPartNumber(warehouseId, partNumber)
                .orElse(null);

        if (stock == null || stock.getQtyAvailable().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal available = stock.getQtyAvailable();
        BigDecimal toReserve = available.min(requiredQty);

        stock.setQtyReserved(stock.getQtyReserved().add(toReserve));
        stock.setQtyAvailable(stock.getQtyOnHand().subtract(stock.getQtyReserved()));
        stockRepository.save(stock);

        InventoryReservation reservation = new InventoryReservation();
        reservation.setProductionOrderId(productionOrderId);
        reservation.setWarehouse(stock.getWarehouse());
        reservation.setPartNumber(partNumber);
        reservation.setQuantityReserved(toReserve);
        reservation.setStatus("ACTIVE");
        reservationRepository.save(reservation);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setId(UUID.randomUUID());
        tx.setTransactionNumber("TXN-RES-PRD-" + System.currentTimeMillis());
        tx.setPartNumber(partNumber);
        tx.setWarehouse(stock.getWarehouse());
        tx.setTransactionType("RESERVATION");
        tx.setQuantity(toReserve);
        tx.setReferenceType("PRODUCTION_ORDER");
        tx.setReferenceId(productionOrderId);
        tx.setPerformedBy(userId);
        transactionRepository.save(tx);

        return toReserve;
    }

    @Transactional
    public void releaseReservations(UUID salesOrderId, UUID userId, String ipAddress) {
        List<InventoryReservation> activeReservations = reservationRepository.findBySalesOrderIdAndStatus(salesOrderId, "ACTIVE");

        for (InventoryReservation res : activeReservations) {
            InventoryStock stock = stockRepository.findByWarehouseIdAndPartNumber(res.getWarehouse().getId(), res.getPartNumber()).orElse(null);
            if (stock != null) {
                stock.setQtyReserved(stock.getQtyReserved().subtract(res.getQuantityReserved()).max(BigDecimal.ZERO));
                stock.setQtyAvailable(stock.getQtyOnHand().subtract(stock.getQtyReserved()));
                stockRepository.save(stock);
            }

            res.setStatus("RELEASED");
            res.setReleasedAt(Instant.now());
            reservationRepository.save(res);

            InventoryTransaction tx = new InventoryTransaction();
            tx.setId(UUID.randomUUID());
            tx.setTransactionNumber("TXN-REL-" + System.currentTimeMillis());
            tx.setPartNumber(res.getPartNumber());
            tx.setWarehouse(res.getWarehouse());
            tx.setTransactionType("RESERVATION_RELEASE");
            tx.setQuantity(res.getQuantityReserved());
            tx.setReferenceType("SALES_ORDER");
            tx.setReferenceId(salesOrderId);
            tx.setPerformedBy(userId);
            transactionRepository.save(tx);
        }
    }

    public StockResponse mapToResponse(InventoryStock s) {
        return new StockResponse(
                s.getId(),
                s.getWarehouse().getId(),
                s.getWarehouse().getCode(),
                s.getPartNumber(),
                s.getDescription(),
                s.getQtyOnHand(),
                s.getQtyReserved(),
                s.getQtyAvailable(),
                s.getUom(),
                s.getUnitCost()
        );
    }
}

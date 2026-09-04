package com.sivamachineworks.platform.sales;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.audit.domain.AuditLog;
import com.sivamachineworks.platform.audit.repository.AuditLogRepository;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.inventory.dto.StockAdjustmentRequest;
import com.sivamachineworks.platform.inventory.repository.InventoryStockRepository;
import com.sivamachineworks.platform.sales.dto.CreateQuotationRequest;
import com.sivamachineworks.platform.sales.dto.CreateSalesOrderRequest;
import com.sivamachineworks.platform.sales.dto.QuotationItemDto;
import com.sivamachineworks.platform.sales.dto.SalesOrderItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SalesAndInventoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InventoryStockRepository stockRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String salesToken;
    private String prodToken;

    @BeforeEach
    public void setup() throws Exception {
        userRepository.findAll().forEach(user -> {
            user.setPasswordHash(passwordEncoder.encode("Password@123"));
            userRepository.save(user);
        });

        salesToken = obtainToken("sales_user", "Password@123");
        prodToken = obtainToken("prod_user", "Password@123");
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }

    @Test
    public void test1_fullSalesWorkflow_withInventoryReservation() throws Exception {
        UUID customerId = UUID.fromString("c1111111-1111-1111-1111-111111111111"); // Toyota
        UUID hm500Id = UUID.fromString("a1111111-1111-1111-1111-111111111111");     // HM-500

        // Step 1: Create Quotation
        CreateQuotationRequest quotationReq = new CreateQuotationRequest(
                "QT-2026-TOYOTA-01",
                customerId,
                LocalDate.now().plusDays(30),
                "Net 60",
                "FOB Osaka",
                List.of(new QuotationItemDto(hm500Id, new BigDecimal("2.0"), new BigDecimal("12500000.00")))
        );

        MvcResult quoteResult = mockMvc.perform(post("/api/v1/sales/quotations")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(quotationReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quotationCode").value("QT-2026-TOYOTA-01"))
                .andReturn();

        UUID quoteId = UUID.fromString(objectMapper.readTree(quoteResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Step 2: Create Sales Order from Quotation
        CreateSalesOrderRequest soReq = new CreateSalesOrderRequest(
                "SO-2026-TOYOTA-01",
                quoteId,
                customerId,
                "PO-TOYOTA-78901",
                LocalDate.now().plusDays(45),
                "Osaka",
                "Toyota Plant #3, Motomachi, Toyota City",
                "Ship via Precision Logistics Carrier",
                List.of(new SalesOrderItemDto(hm500Id, new BigDecimal("2.0"), new BigDecimal("12500000.00")))
        );

        MvcResult soResult = mockMvc.perform(post("/api/v1/sales/orders")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(soReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.soCode").value("SO-2026-TOYOTA-01"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        UUID soId = UUID.fromString(objectMapper.readTree(soResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Pre-confirmation check: Capture current reserved stock
        UUID osakaWhId = UUID.fromString("d1111111-1111-1111-1111-111111111111");
        BigDecimal initialReserved = stockRepository.findByWarehouseIdAndPartNumber(osakaWhId, "PUMP-HP-75").get().getQtyReserved();

        // Step 3: Confirm Sales Order (triggers BOM explosion & inventory reservation)
        MvcResult confirmResult = mockMvc.perform(put("/api/v1/sales/orders/" + soId + "/confirm")
                .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.order.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.reservedMaterials").isArray())
                .andReturn();

        var confirmData = objectMapper.readTree(confirmResult.getResponse().getContentAsString()).get("data");
        assertThat(confirmData.get("reservedMaterials").size()).isGreaterThan(0);

        // Verify stock reservation state in Osaka Warehouse (increased by 2.0)
        var pumpStock = stockRepository.findByWarehouseIdAndPartNumber(osakaWhId, "PUMP-HP-75").orElseThrow();
        // 2 machines * 1 pump = 2 pumps reserved
        assertThat(pumpStock.getQtyReserved()).isEqualByComparingTo(initialReserved.add(new BigDecimal("2.0")));

        // Verify audit log
        List<AuditLog> soLogs = auditLogRepository.findByEntityNameAndEntityId("SalesOrder", soId);
        assertThat(soLogs.stream().anyMatch(l -> "SALES_ORDER_CONFIRMED".equals(l.getAction()))).isTrue();
    }

    @Test
    public void test2_insufficientInventory_triggersMaterialRequirements() throws Exception {
        UUID customerId = UUID.fromString("c2222222-2222-2222-2222-222222222222"); // Komatsu
        UUID hm500Id = UUID.fromString("a1111111-1111-1111-1111-111111111111");

        // Order 20 units of HM-500 (Each requires 1 PLC-CNC-840D -> Total 20 needed, but stock is only 5!)
        CreateSalesOrderRequest largeOrderReq = new CreateSalesOrderRequest(
                "SO-2026-KOMATSU-01",
                null,
                customerId,
                "PO-KOMATSU-4455",
                LocalDate.now().plusDays(60),
                "Osaka",
                "Komatsu Osaka Plant",
                "Heavy duty assembly",
                List.of(new SalesOrderItemDto(hm500Id, new BigDecimal("20.0"), new BigDecimal("12000000.00")))
        );

        MvcResult soResult = mockMvc.perform(post("/api/v1/sales/orders")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeOrderReq)))
                .andExpect(status().isOk())
                .andReturn();

        UUID soId = UUID.fromString(objectMapper.readTree(soResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Confirm order -> will reserve available stock and trigger MRP shortage
        MvcResult confirmResult = mockMvc.perform(put("/api/v1/sales/orders/" + soId + "/confirm")
                .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.order.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.shortages").isArray())
                .andExpect(jsonPath("$.data.materialRequirementRunId").exists())
                .andReturn();

        var confirmData = objectMapper.readTree(confirmResult.getResponse().getContentAsString()).get("data");
        assertThat(confirmData.get("shortages").size()).isGreaterThan(0);
    }

    @Test
    public void test3_orderCancellation_releasesReservedInventory() throws Exception {
        UUID customerId = UUID.fromString("c1111111-1111-1111-1111-111111111111");
        UUID hm500Id = UUID.fromString("a1111111-1111-1111-1111-111111111111");
        UUID osakaWhId = UUID.fromString("d1111111-1111-1111-1111-111111111111");

        BigDecimal initialReserved = stockRepository.findByWarehouseIdAndPartNumber(osakaWhId, "PUMP-HP-75").get().getQtyReserved();

        CreateSalesOrderRequest orderReq = new CreateSalesOrderRequest(
                "SO-2026-CANCEL-01",
                null,
                customerId,
                "PO-TEST-CANCEL",
                LocalDate.now().plusDays(30),
                "Osaka",
                "Test Address",
                "Cancel test",
                List.of(new SalesOrderItemDto(hm500Id, new BigDecimal("1.0"), new BigDecimal("12500000.00")))
        );

        MvcResult soResult = mockMvc.perform(post("/api/v1/sales/orders")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isOk())
                .andReturn();

        UUID soId = UUID.fromString(objectMapper.readTree(soResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Confirm order
        mockMvc.perform(put("/api/v1/sales/orders/" + soId + "/confirm")
                .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isOk());

        // Cancel order -> releases reservations
        mockMvc.perform(put("/api/v1/sales/orders/" + soId + "/cancel")
                .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Verify reservations released
        BigDecimal finalReserved = stockRepository.findByWarehouseIdAndPartNumber(osakaWhId, "PUMP-HP-75").get().getQtyReserved();
        assertThat(finalReserved).isEqualByComparingTo(initialReserved);
    }

    @Test
    public void test4_negativeInventoryPrevention() throws Exception {
        UUID osakaWhId = UUID.fromString("d1111111-1111-1111-1111-111111111111");

        // Attempt to adjust stock down by -1000 when stock is only 10 -> Must be rejected with 400 Bad Request
        StockAdjustmentRequest negAdj = new StockAdjustmentRequest(
                osakaWhId,
                "PUMP-HP-75",
                new BigDecimal("-1000.0"),
                "Illegal negative stock attempt"
        );

        mockMvc.perform(post("/api/v1/inventory/adjust")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negAdj)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

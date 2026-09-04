package com.sivamachineworks.platform.scm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.audit.domain.AuditLog;
import com.sivamachineworks.platform.audit.repository.AuditLogRepository;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.inventory.repository.InventoryStockRepository;
import com.sivamachineworks.platform.scm.dto.*;
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
public class ProcurementTest {

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

    private String procToken;
    private String mgmtToken;
    private String salesToken;

    @BeforeEach
    public void setup() throws Exception {
        userRepository.findAll().forEach(user -> {
            user.setPasswordHash(passwordEncoder.encode("Password@123"));
            userRepository.save(user);
        });

        procToken = obtainToken("proc_user", "Password@123");
        mgmtToken = obtainToken("mgmt_user", "Password@123");
        salesToken = obtainToken("sales_user", "Password@123");
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
    public void test1_completeProcurementWorkflow_fromPrToGoodsReceipt() throws Exception {
        UUID yukenSupplierId = UUID.fromString("b1111111-1111-1111-1111-111111111111");
        UUID osakaWhId = UUID.fromString("d1111111-1111-1111-1111-111111111111");

        // Step 1: Create Purchase Request (PR) for 15 units of Hydraulic Pumps
        CreatePurchaseRequestDto prDto = new CreatePurchaseRequestDto(
                "PR-2026-YUKEN-01",
                null,
                "MRP",
                List.of(new PurchaseRequestItemDto(
                        "PUMP-HP-75",
                        "Variable Displacement Piston Pump 75cc",
                        new BigDecimal("15.0"),
                        "EA",
                        new BigDecimal("120000.00"),
                        LocalDate.now().plusDays(20),
                        yukenSupplierId
                ))
        );

        MvcResult prResult = mockMvc.perform(post("/api/v1/procurement/requests")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prCode").value("PR-2026-YUKEN-01"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        UUID prId = UUID.fromString(objectMapper.readTree(prResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Step 2: Approve Purchase Request as MANAGEMENT
        mockMvc.perform(put("/api/v1/procurement/requests/" + prId + "/approve")
                .header("Authorization", "Bearer " + mgmtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // Step 3: Create Purchase Order (PO) converting approved PR
        CreatePurchaseOrderRequest poDto = new CreatePurchaseOrderRequest(
                "PO-2026-YUKEN-01",
                yukenSupplierId,
                prId,
                osakaWhId,
                LocalDate.now().plusDays(21),
                List.of(new PurchaseOrderItemDto(
                        "PUMP-HP-75",
                        "Variable Displacement Piston Pump 75cc",
                        new BigDecimal("15.0"),
                        "EA",
                        new BigDecimal("120000.00")
                ))
        );

        MvcResult poResult = mockMvc.perform(post("/api/v1/procurement/orders")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(poDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.poCode").value("PO-2026-YUKEN-01"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        var poData = objectMapper.readTree(poResult.getResponse().getContentAsString()).get("data");
        UUID poId = UUID.fromString(poData.get("id").asText());
        UUID poItemId = UUID.fromString(poData.get("items").get(0).get("id").asText());

        // Step 4: Issue PO to Supplier
        mockMvc.perform(put("/api/v1/procurement/orders/" + poId + "/issue")
                .header("Authorization", "Bearer " + procToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ISSUED"));

        // Pre-receipt check: Get current on-hand stock of PUMP-HP-75
        BigDecimal initialOnHand = stockRepository.findByWarehouseIdAndPartNumber(osakaWhId, "PUMP-HP-75").get().getQtyOnHand();

        // Step 5: Post Goods Receipt for 15 units received at Osaka warehouse
        PostGoodsReceiptRequest grDto = new PostGoodsReceiptRequest(
                "GR-2026-YUKEN-01",
                poId,
                "DN-YK-88990",
                List.of(new GoodsReceiptItemDto(
                        poItemId,
                        new BigDecimal("15.0"),
                        "Delivered in sealed wooden crate, QC passed"
                ))
        );

        MvcResult grResult = mockMvc.perform(post("/api/v1/procurement/receipts")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(grDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grCode").value("GR-2026-YUKEN-01"))
                .andExpect(jsonPath("$.data.status").value("RECEIVED"))
                .andReturn();

        UUID grId = UUID.fromString(objectMapper.readTree(grResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Verify that inventory on-hand was increased by +15
        BigDecimal updatedOnHand = stockRepository.findByWarehouseIdAndPartNumber(osakaWhId, "PUMP-HP-75").get().getQtyOnHand();
        assertThat(updatedOnHand).isEqualByComparingTo(initialOnHand.add(new BigDecimal("15.0")));

        // Verify PO status transitioned to RECEIVED
        mockMvc.perform(get("/api/v1/procurement/orders/" + poId)
                .header("Authorization", "Bearer " + procToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.items[0].quantityReceived").value(15.0));

        // Verify Audit Logs
        List<AuditLog> grLogs = auditLogRepository.findByEntityNameAndEntityId("GoodsReceipt", grId);
        assertThat(grLogs).isNotEmpty();
        assertThat(grLogs.get(0).getAction()).isEqualTo("GOODS_RECEIPT_POSTED");
    }

    @Test
    public void test2_overReceiptQuantity_rejected() throws Exception {
        UUID yukenSupplierId = UUID.fromString("b1111111-1111-1111-1111-111111111111");
        UUID osakaWhId = UUID.fromString("d1111111-1111-1111-1111-111111111111");

        // Create PO for 5 units
        CreatePurchaseOrderRequest poDto = new CreatePurchaseOrderRequest(
                "PO-2026-OVERRECEIPT-01",
                yukenSupplierId,
                null,
                osakaWhId,
                LocalDate.now().plusDays(10),
                List.of(new PurchaseOrderItemDto(
                        "VLV-PROP-350",
                        "Valves",
                        new BigDecimal("5.0"),
                        "EA",
                        new BigDecimal("45000.00")
                ))
        );

        MvcResult poResult = mockMvc.perform(post("/api/v1/procurement/orders")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(poDto)))
                .andExpect(status().isOk())
                .andReturn();

        var poData = objectMapper.readTree(poResult.getResponse().getContentAsString()).get("data");
        UUID poId = UUID.fromString(poData.get("id").asText());
        UUID poItemId = UUID.fromString(poData.get("items").get(0).get("id").asText());

        // Issue PO
        mockMvc.perform(put("/api/v1/procurement/orders/" + poId + "/issue")
                .header("Authorization", "Bearer " + procToken))
                .andExpect(status().isOk());

        // Attempt to receive 10 units when only 5 ordered -> Must fail with Business Rule Violation (400 Bad Request)
        PostGoodsReceiptRequest excessGrDto = new PostGoodsReceiptRequest(
                "GR-2026-OVER-01",
                poId,
                "DN-EXCESS-01",
                List.of(new GoodsReceiptItemDto(
                        poItemId,
                        new BigDecimal("10.0"), // Ordered 5, receiving 10
                        "Excess receipt attempt"
                ))
        );

        mockMvc.perform(post("/api/v1/procurement/receipts")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(excessGrDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void test3_unauthorizedRoleCannotApprovePurchaseRequest() throws Exception {
        UUID yukenSupplierId = UUID.fromString("b1111111-1111-1111-1111-111111111111");

        // Create PR
        CreatePurchaseRequestDto prDto = new CreatePurchaseRequestDto(
                "PR-2026-UNAUTH-01",
                null,
                "MANUAL",
                List.of(new PurchaseRequestItemDto(
                        "SEN-LIN-ENC",
                        "Sensor",
                        new BigDecimal("2.0"),
                        "EA",
                        new BigDecimal("25000.00"),
                        LocalDate.now().plusDays(10),
                        yukenSupplierId
                ))
        );

        MvcResult prResult = mockMvc.perform(post("/api/v1/procurement/requests")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prDto)))
                .andExpect(status().isOk())
                .andReturn();

        UUID prId = UUID.fromString(objectMapper.readTree(prResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // SALES role cannot approve purchase requests (requires ADMIN, PROCUREMENT, or MANAGEMENT) -> 403 Forbidden
        mockMvc.perform(put("/api/v1/procurement/requests/" + prId + "/approve")
                .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}

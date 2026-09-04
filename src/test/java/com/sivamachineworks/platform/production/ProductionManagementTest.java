package com.sivamachineworks.platform.production;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.audit.domain.AuditLog;
import com.sivamachineworks.platform.audit.repository.AuditLogRepository;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.inventory.repository.InventoryStockRepository;
import com.sivamachineworks.platform.production.dto.CreateProductionOrderRequest;
import com.sivamachineworks.platform.production.dto.SubmitQualityInspectionRequest;
import com.sivamachineworks.platform.production.dto.UpdateOperationRequest;
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
public class ProductionManagementTest {

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

    private String prodToken;
    private String engToken;

    @BeforeEach
    public void setup() throws Exception {
        userRepository.findAll().forEach(user -> {
            user.setPasswordHash(passwordEncoder.encode("Password@123"));
            userRepository.save(user);
        });

        prodToken = obtainToken("prod_user", "Password@123");
        engToken = obtainToken("eng_user", "Password@123");
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
    public void test1_fullProductionLifecycle_controlledTransitionsAndCompletion() throws Exception {
        UUID hm500Id = UUID.fromString("a1111111-1111-1111-1111-111111111111");
        UUID osakaWhId = UUID.fromString("d1111111-1111-1111-1111-111111111111");

        // 1. Create Production Order (PLANNED)
        CreateProductionOrderRequest req = new CreateProductionOrderRequest(
                "PRD-2026-HM500-01",
                null,
                hm500Id,
                osakaWhId,
                "Osaka",
                new BigDecimal("1.0"),
                LocalDate.now(),
                LocalDate.now().plusDays(15)
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/production/orders")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderCode").value("PRD-2026-HM500-01"))
                .andExpect(jsonPath("$.data.status").value("PLANNED"))
                .andExpect(jsonPath("$.data.operations.length()").value(5))
                .andReturn();

        UUID orderId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // 2. Reserve Materials: PLANNED -> MATERIAL_RESERVED
        mockMvc.perform(put("/api/v1/production/orders/" + orderId + "/reserve-materials")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MATERIAL_RESERVED"));

        // 3. Start Production: MATERIAL_RESERVED -> IN_PROGRESS
        mockMvc.perform(put("/api/v1/production/orders/" + orderId + "/start")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.actualStartDate").exists());

        // 4. Complete all operations sequentially (OP 10 -> 20 -> 30 -> 40 -> 50)
        int[] opSeqs = {10, 20, 30, 40, 50};
        for (int seq : opSeqs) {
            UpdateOperationRequest opReq = new UpdateOperationRequest("COMPLETED", new BigDecimal("8.0"), "Operation finished successfully");
            mockMvc.perform(put("/api/v1/production/orders/" + orderId + "/operations/" + seq)
                    .header("Authorization", "Bearer " + prodToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(opReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        // 5. Submit Quality Inspection: IN_PROGRESS -> QUALITY_CHECK
        SubmitQualityInspectionRequest qcReq = new SubmitQualityInspectionRequest(
                "FAT-2026-HM500-01",
                "FINAL_ACCEPTANCE",
                "PASSED",
                new BigDecimal("0.0015"),
                new BigDecimal("0.0020"),
                "Spindle runout < 2um, positioning precision compliant with JIS B 6338"
        );

        mockMvc.perform(post("/api/v1/production/orders/" + orderId + "/quality-check")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qcReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PASSED"));

        // Pre-completion check: Record current inventory of finished product SMW-HM-500
        BigDecimal initialFgStock = stockRepository.findByWarehouseIdAndPartNumber(osakaWhId, "SMW-HM-500")
                .map(s -> s.getQtyOnHand())
                .orElse(BigDecimal.ZERO);

        // 6. Complete Production: QUALITY_CHECK -> COMPLETED (Consumes raw materials, increases FG inventory)
        mockMvc.perform(put("/api/v1/production/orders/" + orderId + "/complete")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.quantityCompleted").value(1.0))
                .andExpect(jsonPath("$.data.actualCompletionDate").exists())
                .andReturn();

        // Verify Finished Goods stock in Osaka Warehouse increased by +1.0
        BigDecimal finalFgStock = stockRepository.findByWarehouseIdAndPartNumber(osakaWhId, "SMW-HM-500").get().getQtyOnHand();
        assertThat(finalFgStock).isEqualByComparingTo(initialFgStock.add(new BigDecimal("1.0")));

        // Verify Audit Logs
        List<AuditLog> prdLogs = auditLogRepository.findByEntityNameAndEntityId("ProductionOrder", orderId);
        assertThat(prdLogs.stream().anyMatch(l -> "PRODUCTION_ORDER_COMPLETED".equals(l.getAction()))).isTrue();
    }

    @Test
    public void test2_invalidStateTransitions_rejected() throws Exception {
        UUID hm500Id = UUID.fromString("a1111111-1111-1111-1111-111111111111");
        UUID osakaWhId = UUID.fromString("d1111111-1111-1111-1111-111111111111");

        // 1. Create Order (PLANNED)
        CreateProductionOrderRequest req = new CreateProductionOrderRequest(
                "PRD-2026-INVALID-TRANS",
                null,
                hm500Id,
                osakaWhId,
                "Osaka",
                new BigDecimal("1.0"),
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/production/orders")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        UUID orderId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Attempt 1: Skip directly from PLANNED to IN_PROGRESS (Must fail with 400 Bad Request)
        mockMvc.perform(put("/api/v1/production/orders/" + orderId + "/start")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Attempt 2: Skip directly from PLANNED to COMPLETED (Must fail with 400 Bad Request)
        mockMvc.perform(put("/api/v1/production/orders/" + orderId + "/complete")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void test3_completionBlocked_withoutQualityCheck() throws Exception {
        UUID hm500Id = UUID.fromString("a1111111-1111-1111-1111-111111111111");
        UUID osakaWhId = UUID.fromString("d1111111-1111-1111-1111-111111111111");

        CreateProductionOrderRequest req = new CreateProductionOrderRequest(
                "PRD-2026-NOQC-TEST",
                null,
                hm500Id,
                osakaWhId,
                "Osaka",
                new BigDecimal("1.0"),
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/production/orders")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        UUID orderId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Reserve & start
        mockMvc.perform(put("/api/v1/production/orders/" + orderId + "/reserve-materials")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/production/orders/" + orderId + "/start")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk());

        // Try to complete immediately while IN_PROGRESS (without QC) -> Must fail with 400 Bad Request
        mockMvc.perform(put("/api/v1/production/orders/" + orderId + "/complete")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

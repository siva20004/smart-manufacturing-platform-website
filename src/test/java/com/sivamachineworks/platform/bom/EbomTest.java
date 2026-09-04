package com.sivamachineworks.platform.bom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.audit.domain.AuditLog;
import com.sivamachineworks.platform.audit.repository.AuditLogRepository;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.bom.dto.*;
import com.sivamachineworks.platform.identity.repository.UserRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EbomTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String engToken;
    private String mgmtToken;
    private String salesToken;

    @BeforeEach
    public void setup() throws Exception {
        userRepository.findAll().forEach(user -> {
            user.setPasswordHash(passwordEncoder.encode("Password@123"));
            userRepository.save(user);
        });

        engToken = obtainToken("eng_user", "Password@123");
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
    public void test1_retrieveSeededEbomTree_andVerifyHierarchy() throws Exception {
        // Retrieve seeded eBOM for HM-500
        MvcResult result = mockMvc.perform(get("/api/v1/bom/e1111111-1111-1111-1111-111111111111/tree")
                .header("Authorization", "Bearer " + engToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        var treeNode = objectMapper.readTree(responseStr).get("data");

        // Top level has 2 subsystems (Hydraulic System and Electrical System)
        assertThat(treeNode.size()).isEqualTo(2);

        // Subsystem 1: Hydraulic System -> has 2 children (Pump, Valve)
        var hydSys = treeNode.get(0);
        assertThat(hydSys.get("partNumber").asText()).isEqualTo("SYS-HYD-500");
        assertThat(hydSys.get("children").size()).isEqualTo(2);
        assertThat(hydSys.get("children").get(0).get("partNumber").asText()).isEqualTo("PUMP-HP-75");
        assertThat(hydSys.get("children").get(1).get("partNumber").asText()).isEqualTo("VLV-PROP-350");

        // Subsystem 2: Electrical System -> has 3 children (Controller, Motor, Sensor)
        var elecSys = treeNode.get(1);
        assertThat(elecSys.get("partNumber").asText()).isEqualTo("SYS-ELEC-500");
        assertThat(elecSys.get("children").size()).isEqualTo(3);
        assertThat(elecSys.get("children").get(0).get("partNumber").asText()).isEqualTo("PLC-CNC-840D");
        assertThat(elecSys.get("children").get(1).get("partNumber").asText()).isEqualTo("MTR-SRV-22KW");
        assertThat(elecSys.get("children").get(2).get("partNumber").asText()).isEqualTo("SEN-LIN-ENC");
    }

    @Test
    public void test2_createEbom_addComponents_andVerifyAuditLogs() throws Exception {
        // Create new eBOM for HM-700
        CreateEbomRequest createReq = new CreateEbomRequest(
                UUID.fromString("a2222222-2222-2222-2222-222222222222"),
                "A",
                "Initial eBOM for HM-700 Heavy-Duty Press"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/bom")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revisionCode").value("A"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        UUID ebomId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Verify EBOM_CREATION audit
        List<AuditLog> createLogs = auditLogRepository.findByEntityNameAndEntityId("EbomHeader", ebomId);
        assertThat(createLogs).isNotEmpty();
        assertThat(createLogs.get(0).getAction()).isEqualTo("EBOM_CREATION");

        // Add top-level Assembly: Press Frame Structure
        CreateEbomItemRequest frameReq = new CreateEbomItemRequest(
                null,
                10,
                "SYS-FRAME-700",
                "Reinforced Cast Iron Frame Assembly",
                "ASSEMBLY",
                new BigDecimal("1.0"),
                "EA",
                null,
                null,
                null,
                "Grade 45 Cast Steel"
        );

        MvcResult frameResult = mockMvc.perform(post("/api/v1/bom/" + ebomId + "/items")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(frameReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.partNumber").value("SYS-FRAME-700"))
                .andReturn();

        UUID frameItemId = UUID.fromString(objectMapper.readTree(frameResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Add child item to Frame Assembly: Guide Column
        CreateEbomItemRequest colReq = new CreateEbomItemRequest(
                frameItemId,
                1,
                "COL-HD-120",
                "Hardened Chrome Guide Column 120mm",
                "COMPONENT",
                new BigDecimal("4.0"),
                "EA",
                null,
                null,
                null,
                "Hardness HRC 58-62"
        );

        MvcResult colResult = mockMvc.perform(post("/api/v1/bom/" + ebomId + "/items")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(colReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.parentItemId").value(frameItemId.toString()))
                .andExpect(jsonPath("$.data.partNumber").value("COL-HD-120"))
                .andReturn();

        UUID colItemId = UUID.fromString(objectMapper.readTree(colResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Verify EBOM_ITEM_ADDED audit log
        List<AuditLog> colLogs = auditLogRepository.findByEntityNameAndEntityId("EbomItem", colItemId);
        assertThat(colLogs).isNotEmpty();
        assertThat(colLogs.get(0).getAction()).isEqualTo("EBOM_ITEM_ADDED");
    }

    @Test
    public void test3_preventCircularBomRelationships() throws Exception {
        // Create an eBOM
        CreateEbomRequest createReq = new CreateEbomRequest(
                UUID.fromString("a2222222-2222-2222-2222-222222222222"),
                "B",
                "eBOM for cycle detection test"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/bom")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        UUID ebomId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Add Item A (top-level)
        CreateEbomItemRequest itemAReq = new CreateEbomItemRequest(null, 10, "ITEM-A", "Assembly A", "ASSEMBLY", BigDecimal.ONE, "EA", null, null, null, null);
        MvcResult itemAResult = mockMvc.perform(post("/api/v1/bom/" + ebomId + "/items")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemAReq)))
                .andExpect(status().isOk())
                .andReturn();
        UUID itemAId = UUID.fromString(objectMapper.readTree(itemAResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Add Item B (child of A)
        CreateEbomItemRequest itemBReq = new CreateEbomItemRequest(itemAId, 1, "ITEM-B", "Subassembly B", "ASSEMBLY", BigDecimal.ONE, "EA", null, null, null, null);
        MvcResult itemBResult = mockMvc.perform(post("/api/v1/bom/" + ebomId + "/items")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemBReq)))
                .andExpect(status().isOk())
                .andReturn();
        UUID itemBId = UUID.fromString(objectMapper.readTree(itemBResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Case 1: Try setting Item A's parent to Item A (self-cycle) -> 400 Bad Request
        UpdateEbomItemRequest selfCycleReq = new UpdateEbomItemRequest(itemAId, 10, "ITEM-A", "Desc", "ASSEMBLY", BigDecimal.ONE, "EA", null, null, null, null);
        mockMvc.perform(put("/api/v1/bom/" + ebomId + "/items/" + itemAId)
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selfCycleReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Case 2: Try setting Item A's parent to Item B (descendant cycle: A -> B -> A) -> 400 Bad Request
        UpdateEbomItemRequest descendantCycleReq = new UpdateEbomItemRequest(itemBId, 10, "ITEM-A", "Desc", "ASSEMBLY", BigDecimal.ONE, "EA", null, null, null, null);
        mockMvc.perform(put("/api/v1/bom/" + ebomId + "/items/" + itemAId)
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(descendantCycleReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void test4_submitAndApproveEbom_andCreateRevision() throws Exception {
        // Create an eBOM
        CreateEbomRequest createReq = new CreateEbomRequest(
                UUID.fromString("a2222222-2222-2222-2222-222222222222"),
                "C",
                "eBOM for approval workflow test"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/bom")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        UUID ebomId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Add an item
        CreateEbomItemRequest itemReq = new CreateEbomItemRequest(null, 10, "PART-TEST-01", "Component", "COMPONENT", BigDecimal.ONE, "EA", null, null, null, null);
        mockMvc.perform(post("/api/v1/bom/" + ebomId + "/items")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isOk());

        // Submit for approval as ENGINEERING
        mockMvc.perform(put("/api/v1/bom/" + ebomId + "/submit")
                .header("Authorization", "Bearer " + engToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        // Approve eBOM as MANAGEMENT
        ApproveEbomRequest approveReq = new ApproveEbomRequest("Engineering review passed and signed off");
        mockMvc.perform(put("/api/v1/bom/" + ebomId + "/approve")
                .header("Authorization", "Bearer " + mgmtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RELEASED"))
                .andExpect(jsonPath("$.data.approvedBy").exists())
                .andExpect(jsonPath("$.data.approvedAt").exists());

        // Check EBOM_APPROVED audit log
        List<AuditLog> approveLogs = auditLogRepository.findByEntityNameAndEntityId("EbomHeader", ebomId);
        assertThat(approveLogs.stream().anyMatch(l -> "EBOM_APPROVED".equals(l.getAction()))).isTrue();

        // Create Revision D from Rev C (cloning items)
        CreateEbomRevisionRequest revReq = new CreateEbomRevisionRequest("D", "ECO Design update for Part-01");
        MvcResult revResult = mockMvc.perform(post("/api/v1/bom/" + ebomId + "/revisions")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(revReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revisionCode").value("D"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        UUID revEbomId = UUID.fromString(objectMapper.readTree(revResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Verify cloned items exist in new revision tree
        mockMvc.perform(get("/api/v1/bom/" + revEbomId + "/tree")
                .header("Authorization", "Bearer " + engToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].partNumber").value("PART-TEST-01"));
    }

    @Test
    public void test5_unauthorizedRoleCannotApproveEbom() throws Exception {
        // SALES user cannot approve eBOM (requires ADMIN, ENGINEERING, or MANAGEMENT)
        mockMvc.perform(put("/api/v1/bom/e1111111-1111-1111-1111-111111111111/approve")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ApproveEbomRequest("Illegal Approval"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}

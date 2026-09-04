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
public class MbomTransformationTest {

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
    private String prodToken;
    private String mgmtToken;
    private String salesToken;

    @BeforeEach
    public void setup() throws Exception {
        userRepository.findAll().forEach(user -> {
            user.setPasswordHash(passwordEncoder.encode("Password@123"));
            userRepository.save(user);
        });

        engToken = obtainToken("eng_user", "Password@123");
        prodToken = obtainToken("prod_user", "Password@123");
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
    public void test1_validTransformation_andVerifyManufacturingStructureAndTraceability() throws Exception {
        UUID ebomId = UUID.fromString("e1111111-1111-1111-1111-111111111111"); // Seeded released eBOM for HM-500

        // Define manufacturing additions (Hydraulic oil, solder wire, packaging crate)
        ManufacturingAdditionDto oil = new ManufacturingAdditionDto(
                "ASY-FINAL-SMW-HM-500",
                30,
                "OIL-HYD-ISO46",
                "Hydraulic Fluid ISO VG 46 (200L Drum)",
                "CONSUMABLE",
                new BigDecimal("200.0"),
                "L",
                "WC-HYD-01",
                20,
                "Filled during final hydraulic testing"
        );

        ManufacturingAdditionDto crate = new ManufacturingAdditionDto(
                "ASY-FINAL-SMW-HM-500",
                40,
                "PKG-CRATE-HM500",
                "Reinforced Export Timber Crate HM-500",
                "PACKAGING",
                BigDecimal.ONE,
                "EA",
                "WC-FINAL-01",
                100,
                "Complies with ISPM-15 export standard"
        );

        TransformEbomRequest transformReq = new TransformEbomRequest(
                ebomId,
                "Osaka",
                "M-A",
                "Initial Manufacturing BOM for Osaka Plant",
                null, // Use auto-mapping of eBOM tree
                List.of(oil, crate)
        );

        MvcResult result = mockMvc.perform(post("/api/v1/bom/transform")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transformReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceEbom.productNumber").value("SMW-HM-500"))
                .andExpect(jsonPath("$.data.sourceRevision").value("A"))
                .andExpect(jsonPath("$.data.generatedMbom.plantLocation").value("Osaka"))
                .andExpect(jsonPath("$.data.generatedMbom.revisionCode").value("M-A"))
                .andExpect(jsonPath("$.data.mappings").isArray())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        var rootNode = objectMapper.readTree(responseStr).get("data");
        UUID mbomId = UUID.fromString(rootNode.get("generatedMbom").get("id").asText());

        // Verify EBOM_MBOM_TRANSFORMATION audit log
        List<AuditLog> auditLogs = auditLogRepository.findByEntityNameAndEntityId("MbomHeader", mbomId);
        assertThat(auditLogs).isNotEmpty();
        assertThat(auditLogs.get(0).getAction()).isEqualTo("EBOM_MBOM_TRANSFORMATION");

        // Retrieve and inspect hierarchical mBOM tree
        MvcResult treeResult = mockMvc.perform(get("/api/v1/bom/mbom/" + mbomId + "/tree")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk())
                .andReturn();

        var tree = objectMapper.readTree(treeResult.getResponse().getContentAsString()).get("data");
        assertThat(tree.size()).isEqualTo(1); // Root Final Assembly node
        var finalAsm = tree.get(0);
        assertThat(finalAsm.get("partNumber").asText()).isEqualTo("ASY-FINAL-SMW-HM-500");
        assertThat(finalAsm.get("workCenter").asText()).isEqualTo("WC-FINAL-01");

        // Children under Final Assembly: Hydraulic system, Electrical system, Oil consumable, Packaging crate
        var children = finalAsm.get("children");
        assertThat(children.size()).isGreaterThanOrEqualTo(4);

        // Verify traceability: source eBOM item IDs are preserved
        boolean hasSourceEbomLink = false;
        for (int i = 0; i < children.size(); i++) {
            if (!children.get(i).get("sourceEbomItemId").isNull()) {
                hasSourceEbomLink = true;
                break;
            }
        }
        assertThat(hasSourceEbomLink).isTrue();
    }

    @Test
    public void test2_unapprovedEbom_transformationRejected() throws Exception {
        // Create an eBOM in DRAFT status
        CreateEbomRequest draftEbomReq = new CreateEbomRequest(
                UUID.fromString("a2222222-2222-2222-2222-222222222222"),
                "DRAFT-REV",
                "Unapproved eBOM"
        );

        MvcResult draftResult = mockMvc.perform(post("/api/v1/bom")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draftEbomReq)))
                .andExpect(status().isOk())
                .andReturn();

        UUID draftEbomId = UUID.fromString(objectMapper.readTree(draftResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Attempting to transform DRAFT eBOM should fail with Business Rule Violation (400 Bad Request)
        TransformEbomRequest transformReq = new TransformEbomRequest(
                draftEbomId,
                "Nagoya",
                "M-1.0",
                "Attempt on Draft eBOM",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/bom/transform")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transformReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void test3_customAssemblyMapping_andMissingEbomItemWarnings() throws Exception {
        UUID ebomId = UUID.fromString("e1111111-1111-1111-1111-111111111111");

        // Custom mapping: map only the hydraulic pump to a dedicated Work Cell
        AssemblyMappingDto hydCellMapping = new AssemblyMappingDto(
                "Hydraulic Power Unit Assembly Cell",
                "ASY-HPU-OSK",
                "WC-HYD-CELL",
                10,
                "HPU Sub-Assembly & Calibration",
                List.of(UUID.fromString("e1111111-2222-1111-1111-222222222222")) // Pump item
        );

        TransformEbomRequest req = new TransformEbomRequest(
                ebomId,
                "Nagoya",
                "M-CUSTOM",
                "Custom assembly grouping",
                List.of(hydCellMapping),
                null
        );

        MvcResult result = mockMvc.perform(post("/api/v1/bom/transform")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.warnings").isArray())
                .andReturn();

        var responseData = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        // Verify that unmapped eBOM items are reported in warnings
        assertThat(responseData.get("warnings").size()).isGreaterThan(0);
    }

    @Test
    public void test4_unauthorizedRoleCannotTransform() throws Exception {
        TransformEbomRequest transformReq = new TransformEbomRequest(
                UUID.fromString("e1111111-1111-1111-1111-111111111111"),
                "Penang",
                "M-A",
                "Unauthorized attempt",
                null,
                null
        );

        // SALES role is not authorized to trigger manufacturing transformation
        mockMvc.perform(post("/api/v1/bom/transform")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transformReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void test5_revisionIndependence_andMbomApproval() throws Exception {
        UUID ebomId = UUID.fromString("e1111111-1111-1111-1111-111111111111");

        TransformEbomRequest transformReq = new TransformEbomRequest(
                ebomId,
                "Penang",
                "M-PEN-01",
                "Penang sub-assembly BOM",
                null,
                null
        );

        MvcResult transformResult = mockMvc.perform(post("/api/v1/bom/transform")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transformReq)))
                .andExpect(status().isOk())
                .andReturn();

        UUID mbomId = UUID.fromString(objectMapper.readTree(transformResult.getResponse().getContentAsString()).get("data").get("generatedMbom").get("id").asText());

        // Approve mBOM as MANAGEMENT
        ApproveMbomRequest approveReq = new ApproveMbomRequest("Production line ready for Penang plant rollout");
        mockMvc.perform(put("/api/v1/bom/mbom/" + mbomId + "/approve")
                .header("Authorization", "Bearer " + mgmtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RELEASED"))
                .andExpect(jsonPath("$.data.approvedBy").exists())
                .andExpect(jsonPath("$.data.approvedAt").exists());

        // Create a new eBOM revision on engineering side
        CreateEbomRevisionRequest revReq = new CreateEbomRevisionRequest("REV-E-IND", "Engineering ECO design update");
        mockMvc.perform(post("/api/v1/bom/" + ebomId + "/revisions")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(revReq)))
                .andExpect(status().isOk());

        // Verify that the approved mBOM is untouched and remains RELEASED with original source link
        MvcResult verifyResult = mockMvc.perform(get("/api/v1/bom/mbom/" + mbomId)
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RELEASED"))
                .andExpect(jsonPath("$.data.revisionCode").value("M-PEN-01"))
                .andExpect(jsonPath("$.data.ebomHeaderId").value(ebomId.toString()))
                .andReturn();
    }
}

package com.sivamachineworks.platform.pdm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.audit.domain.AuditLog;
import com.sivamachineworks.platform.audit.repository.AuditLogRepository;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.pdm.dto.ApproveRevisionRequest;
import com.sivamachineworks.platform.pdm.dto.CreateDocumentRequest;
import com.sivamachineworks.platform.pdm.dto.CreateProductRequest;
import com.sivamachineworks.platform.pdm.dto.CreateRevisionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
public class ProductAndPdmTest {

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
    public void test1_createProduct_andVerifyAuditLog() throws Exception {
        CreateProductRequest req = new CreateProductRequest(
                "SMW-HM-1000",
                "High Precision Hydraulic Machining Center 1000",
                "Flagship heavy-duty 5-axis hydraulic machining center",
                "MACHINERY",
                "EA",
                new BigDecimal("65000000"),
                new BigDecimal("42000000"),
                20,
                "ACTIVE"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productNumber").value("SMW-HM-1000"))
                .andExpect(jsonPath("$.data.revisions[0].revisionNumber").value("A"))
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        String productIdStr = objectMapper.readTree(responseStr).get("data").get("id").asText();
        UUID productId = UUID.fromString(productIdStr);

        List<AuditLog> logs = auditLogRepository.findByEntityNameAndEntityId("Product", productId);
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getAction()).isEqualTo("PRODUCT_CREATION");
    }

    @Test
    public void test2_createProduct_duplicateNumberFails() throws Exception {
        CreateProductRequest req = new CreateProductRequest(
                "SMW-HM-500", // already seeded
                "Duplicate Machine",
                "Desc",
                "MACHINERY",
                "EA",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                10,
                "ACTIVE"
        );

        mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void test3_createRevision_andApproveRevision() throws Exception {
        // Fetch existing HM-500
        MvcResult prodResult = mockMvc.perform(get("/api/v1/products/a1111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer " + engToken))
                .andExpect(status().isOk())
                .andReturn();

        UUID productId = UUID.fromString(objectMapper.readTree(prodResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Create revision B
        CreateRevisionRequest revReq = new CreateRevisionRequest("B", "Spindle hydraulic pressure sensor upgrade", LocalDate.now().plusMonths(1));
        MvcResult revResult = mockMvc.perform(post("/api/v1/products/" + productId + "/revisions")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(revReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revisionNumber").value("B"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        UUID revId = UUID.fromString(objectMapper.readTree(revResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Check REVISION_CREATION audit
        List<AuditLog> createLogs = auditLogRepository.findByEntityNameAndEntityId("ProductRevision", revId);
        assertThat(createLogs).isNotEmpty();
        assertThat(createLogs.get(0).getAction()).isEqualTo("REVISION_CREATION");

        // Approve revision B as MANAGEMENT
        ApproveRevisionRequest approveReq = new ApproveRevisionRequest("Approved for manufacturing rollout");
        mockMvc.perform(put("/api/v1/products/" + productId + "/revisions/" + revId + "/approve")
                .header("Authorization", "Bearer " + mgmtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RELEASED"))
                .andExpect(jsonPath("$.data.approvedBy").exists());

        // Check REVISION_APPROVAL audit
        List<AuditLog> allLogs = auditLogRepository.findByEntityNameAndEntityId("ProductRevision", revId);
        assertThat(allLogs.stream().anyMatch(l -> "REVISION_APPROVAL".equals(l.getAction()))).isTrue();
    }

    @Test
    public void test4_createDocument_uploadVersion_download_andVerifyAuditLogs() throws Exception {
        CreateDocumentRequest docReq = new CreateDocumentRequest(
                "CAD-HM-500-MAIN",
                "3D Master CAD Assembly SMW-HM-500",
                "CAD_MODEL",
                UUID.fromString("a1111111-1111-1111-1111-111111111111"),
                false
        );

        MvcResult docResult = mockMvc.perform(post("/api/v1/pdm/documents")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(docReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentNumber").value("CAD-HM-500-MAIN"))
                .andReturn();

        UUID docId = UUID.fromString(objectMapper.readTree(docResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Upload version 1 file with CAD metadata
        String cadMetadata = """
                {"format":"STEP AP242","software":"SolidWorks 2024","units":"mm","massKg":12500.5,"volumeCm3":1620000.0,"boundingBox":{"x":4200,"y":3100,"z":2800}}
                """;

        MockMultipartFile file = new MockMultipartFile("file", "HM500_Assembly.step", "application/step", "DUMMY STEP CAD DATA BINARY 123456789".getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/pdm/documents/" + docId + "/versions/upload")
                .file(file)
                .param("revisionCode", "A")
                .param("cadMetadata", cadMetadata)
                .header("Authorization", "Bearer " + engToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.versionNumber").value(1))
                .andExpect(jsonPath("$.data.fileName").value("HM500_Assembly.step"))
                .andReturn();

        UUID versionId = UUID.fromString(objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // Verify DOCUMENT_UPLOAD audit log
        List<AuditLog> uploadLogs = auditLogRepository.findByEntityNameAndEntityId("DocumentVersion", versionId);
        assertThat(uploadLogs).isNotEmpty();
        assertThat(uploadLogs.get(0).getAction()).isEqualTo("DOCUMENT_UPLOAD");

        // Download document version
        MvcResult downloadResult = mockMvc.perform(get("/api/v1/pdm/documents/" + docId + "/versions/1/download")
                .header("Authorization", "Bearer " + engToken))
                .andExpect(status().isOk())
                .andReturn();

        byte[] downloadedData = downloadResult.getResponse().getContentAsByteArray();
        assertThat(new String(downloadedData)).isEqualTo("DUMMY STEP CAD DATA BINARY 123456789");

        // Verify DOCUMENT_DOWNLOAD audit log
        List<AuditLog> allVersionLogs = auditLogRepository.findByEntityNameAndEntityId("DocumentVersion", versionId);
        assertThat(allVersionLogs.stream().anyMatch(l -> "DOCUMENT_DOWNLOAD".equals(l.getAction()))).isTrue();
    }

    @Test
    public void test5_forbiddenRoleAccess() throws Exception {
        CreateProductRequest req = new CreateProductRequest(
                "SMW-FAIL-01",
                "Fail Machine",
                "Desc",
                "MACHINERY",
                "EA",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                5,
                "ACTIVE"
        );

        // SALES user cannot create product (requires ADMIN, ENGINEERING, or IT_ENGINEER)
        mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}

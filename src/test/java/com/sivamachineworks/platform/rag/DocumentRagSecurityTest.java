package com.sivamachineworks.platform.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.rag.dto.DocumentRagQueryRequest;
import com.sivamachineworks.platform.rag.dto.DocumentRagQueryResponse;
import com.sivamachineworks.platform.rag.dto.IngestDocumentRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DocumentRagSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String engToken;
    private String salesToken;
    private String itToken;
    private String procToken;

    @BeforeEach
    public void setup() throws Exception {
        userRepository.findAll().forEach(user -> {
            user.setPasswordHash(passwordEncoder.encode("Password@123"));
            userRepository.save(user);
        });

        engToken = obtainToken("eng_user");
        salesToken = obtainToken("sales_user");
        itToken = obtainToken("it_engineer");
        procToken = obtainToken("proc_user");
    }

    private String obtainToken(String username) throws Exception {
        LoginRequest login = new LoginRequest(username, "Password@123");
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }

    @Test
    public void test1_ingestNewDocument_createsChunksAndEmbeddings() throws Exception {
        IngestDocumentRequest req = new IngestDocumentRequest(
                "DOC-SOP-WELD-01",
                "Robotic Laser Welding Standard Operating Procedure",
                "MANUFACTURING_PROCEDURE",
                null,
                "Robotic welding of the machine bed casting requires high-purity argon shielding gas at 25 L/min. Beam spot diameter is set to 0.6 mm for structural weld seams.",
                "PRODUCTION,ENGINEERING,ADMIN"
        );

        mockMvc.perform(post("/api/v1/rag/documents/ingest")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.docCode").value("DOC-SOP-WELD-01"))
                .andExpect(jsonPath("$.data.chunkCount").isNumber());
    }

    @Test
    public void test2_authorizedEngineeringQuery_retrievesSpindleCalibrationWithCitations() throws Exception {
        DocumentRagQueryRequest req = new DocumentRagQueryRequest(
                "What is the maximum allowable spindle runout for HM-500?",
                "ENGINEERING_MANUAL"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/rag/query")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.foundAuthorizedMatches").value(true))
                .andReturn();

        DocumentRagQueryResponse response = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                DocumentRagQueryResponse.class
        );

        assertThat(response.citations()).isNotEmpty();
        assertThat(response.citations().get(0).docCode()).isEqualTo("DOC-ENG-HM500-01");
        assertThat(response.answer()).contains("0.0020 mm");
    }

    @Test
    public void test3_securityRbacIsolation_salesUserCannotAccessItRunbookOrEngineeringManual() throws Exception {
        // Sales user queries IT disaster recovery and key rotation
        DocumentRagQueryRequest req = new DocumentRagQueryRequest(
                "How is database key rotation and disaster recovery performed?",
                "IT_DOCUMENTATION"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/rag/query")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.foundAuthorizedMatches").value(false))
                .andReturn();

        DocumentRagQueryResponse response = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                DocumentRagQueryResponse.class
        );

        // Verify zero citations and explicit unauthorized / insufficient data message
        assertThat(response.citations()).isEmpty();
        assertThat(response.answer()).contains("could not find any verified information matching your query in the internal company documents authorized for your role");
    }

    @Test
    public void test4_authorizedItEngineerQuery_retrievesKeyManagementRunbook() throws Exception {
        DocumentRagQueryRequest req = new DocumentRagQueryRequest(
                "Explain database key rotation and disaster recovery procedures.",
                "IT_DOCUMENTATION"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/rag/query")
                .header("Authorization", "Bearer " + itToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.foundAuthorizedMatches").value(true))
                .andReturn();

        DocumentRagQueryResponse response = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                DocumentRagQueryResponse.class
        );

        assertThat(response.citations()).isNotEmpty();
        assertThat(response.citations().get(0).docCode()).isEqualTo("DOC-IT-INFRA-01");
        assertThat(response.answer()).contains("AWS KMS");
    }

    @Test
    public void test5_supplierProcurementDocument_accessibleToProcurementUser() throws Exception {
        DocumentRagQueryRequest req = new DocumentRagQueryRequest(
                "What is the warranty and lead time for Yuken hydraulic pumps?",
                "SUPPLIER_DOCUMENT"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/rag/query")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.foundAuthorizedMatches").value(true))
                .andReturn();

        DocumentRagQueryResponse response = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                DocumentRagQueryResponse.class
        );

        assertThat(response.citations()).isNotEmpty();
        assertThat(response.citations().get(0).docCode()).isEqualTo("DOC-SCM-YUKEN-01");
        assertThat(response.answer()).contains("36-month");
    }
}

package com.sivamachineworks.platform.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.ai.dto.AiQueryRequest;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.bom.dto.*;
import com.sivamachineworks.platform.crm.dto.CreateCustomerRequest;
import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.pdm.dto.CreateDocumentRequest;
import com.sivamachineworks.platform.pdm.dto.CreateProductRequest;
import com.sivamachineworks.platform.pdm.dto.DocumentResponse;
import com.sivamachineworks.platform.pdm.dto.ProductResponse;
import com.sivamachineworks.platform.production.dto.CreateProductionOrderRequest;
import com.sivamachineworks.platform.production.dto.ProductionOperationResponse;
import com.sivamachineworks.platform.production.dto.ProductionOrderResponse;
import com.sivamachineworks.platform.rag.dto.DocumentRagQueryRequest;
import com.sivamachineworks.platform.rag.dto.IngestDocumentRequest;
import com.sivamachineworks.platform.sales.dto.CreateSalesOrderRequest;
import com.sivamachineworks.platform.sales.dto.SalesOrderItemDto;
import com.sivamachineworks.platform.sales.dto.SalesOrderResponse;
import com.sivamachineworks.platform.scm.quotation.dto.ReviewQuotationRequest;
import com.sivamachineworks.platform.scm.quotation.dto.UploadQuotationRequest;
import com.sivamachineworks.platform.scm.repository.SupplierRepository;
import com.sivamachineworks.platform.shared.security.RateLimitingFilter;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EndToEndManufacturingLifecycleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    private String adminToken;
    private String engToken;
    private String salesToken;
    private String procToken;
    private String prodToken;

    @BeforeEach
    public void setup() throws Exception {
        rateLimitingFilter.resetLimitsForTesting();

        userRepository.findAll().forEach(u -> {
            u.setPasswordHash(passwordEncoder.encode("Password@123"));
            userRepository.save(u);
        });

        adminToken = login("admin");
        engToken = login("eng_user");
        salesToken = login("sales_user");
        procToken = login("proc_user");
        prodToken = login("prod_user");
    }

    private String login(String username) throws Exception {
        LoginRequest req = new LoginRequest(username, "Password@123");
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }

    @Test
    public void testCompleteEnterpriseManufacturingLifecycle() throws Exception {
        // ==========================================
        // 1. PRODUCT CREATION (PDM)
        // ==========================================
        CreateProductRequest prodReq = new CreateProductRequest(
                "SMW-HM-800",
                "Ultra-Precision Horizontal Machining Center HM-800",
                "5-Axis High Torque Machining Center",
                "MACHINING_CENTER",
                "EA",
                new BigDecimal("45000000.00"),
                new BigDecimal("32000000.00"),
                8,
                "ACTIVE"
        );

        MvcResult prodRes = mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prodReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productNumber").value("SMW-HM-800"))
                .andReturn();

        ProductResponse product = objectMapper.readValue(
                objectMapper.readTree(prodRes.getResponse().getContentAsString()).get("data").toString(),
                ProductResponse.class
        );

        // ==========================================
        // 2. PDM DOCUMENT UPLOAD & VERSIONING
        // ==========================================
        CreateDocumentRequest docReq = new CreateDocumentRequest(
                "DOC-HM800-DWG-01",
                "HM-800 Assembly Drawing",
                "DRAWING",
                product.id(),
                false
        );

        MvcResult docRes = mockMvc.perform(post("/api/v1/pdm/documents")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(docReq)))
                .andExpect(status().isOk())
                .andReturn();

        DocumentResponse doc = objectMapper.readValue(
                objectMapper.readTree(docRes.getResponse().getContentAsString()).get("data").toString(),
                DocumentResponse.class
        );

        MockMultipartFile cadFile = new MockMultipartFile("file", "HM800_Layout.step", "application/step", "ISO-10303-21 STEP CAD MODEL DATA".getBytes());
        mockMvc.perform(multipart("/api/v1/pdm/documents/" + doc.id() + "/versions/upload")
                .file(cadFile)
                .param("revisionCode", "A")
                .header("Authorization", "Bearer " + engToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.versionNumber").value(1));

        // ==========================================
        // 3. ENGINEERING BOM (eBOM) CREATION & APPROVAL
        // ==========================================
        CreateEbomRequest ebomReq = new CreateEbomRequest(
                product.id(),
                "A",
                "Initial HM-800 eBOM Release"
        );

        MvcResult ebomRes = mockMvc.perform(post("/api/v1/bom")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ebomReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        EbomResponse ebom = objectMapper.readValue(
                objectMapper.readTree(ebomRes.getResponse().getContentAsString()).get("data").toString(),
                EbomResponse.class
        );

        // Add eBOM items
        CreateEbomItemRequest item1 = new CreateEbomItemRequest(
                null, 1, "PUMP-HP-75", "High Pressure Axial Piston Pump", "COMPONENT",
                new BigDecimal("2.0"), "EA", null, null, null, "Main Hydraulic Supply"
        );
        mockMvc.perform(post("/api/v1/bom/" + ebom.id() + "/items")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item1)))
                .andExpect(status().isOk());

        // Approve eBOM
        mockMvc.perform(put("/api/v1/bom/" + ebom.id() + "/approve")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ApproveEbomRequest("Approved by Chief Engineer"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RELEASED"));

        // ==========================================
        // 4. eBOM -> mBOM TRANSFORMATION
        // ==========================================
        TransformEbomRequest transformReq = new TransformEbomRequest(
                ebom.id(),
                "Osaka",
                "A",
                "HM-800 Manufacturing BOM Initial Routing",
                List.of(),
                List.of(new ManufacturingAdditionDto(null, 99, "OIL-ISO-VG46", "Hydraulic Oil ISO VG 46", "CONSUMABLE", new BigDecimal("120.0"), "L", "WC-FINAL-01", 30, "Initial Hydraulic Tank Fill"))
        );

        MvcResult mbomRes = mockMvc.perform(post("/api/v1/bom/transform")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transformReq)))
                .andExpect(status().isOk())
                .andReturn();

        TransformEbomResponse transformResp = objectMapper.readValue(
                objectMapper.readTree(mbomRes.getResponse().getContentAsString()).get("data").toString(),
                TransformEbomResponse.class
        );
        MbomResponse mbom = transformResp.generatedMbom();

        // Approve mBOM
        mockMvc.perform(put("/api/v1/bom/mbom/" + mbom.id() + "/approve")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ApproveMbomRequest("Approved for Factory Production"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RELEASED"));

        // ==========================================
        // 5. CRM CUSTOMER & SALES ORDER
        // ==========================================
        CreateCustomerRequest custReq = new CreateCustomerRequest(
                "CUST-TOYOTA-01",
                "Toyota Motor Corporation",
                "AUTOMOTIVE",
                "JAPAN",
                new BigDecimal("500000000.00"),
                "NET_60",
                "Takeshi Sato",
                "procurement@toyota.co.jp",
                "+81-565-28-2121",
                "TMC Nagoya Plant"
        );

        MvcResult custRes = mockMvc.perform(post("/api/v1/customers")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(custReq)))
                .andExpect(status().isOk())
                .andReturn();

        UUID customerId = UUID.fromString(objectMapper.readTree(custRes.getResponse().getContentAsString()).get("data").get("id").asText());

        CreateSalesOrderRequest soReq = new CreateSalesOrderRequest(
                "SO-2026-E2E-01",
                null,
                customerId,
                "PO-TOYOTA-9988",
                LocalDate.now().plusDays(45),
                "Osaka",
                "Nagoya Plant",
                "Handle with care",
                List.of(new SalesOrderItemDto(product.id(), new BigDecimal("1.0"), new BigDecimal("45000000.00")))
        );

        MvcResult soRes = mockMvc.perform(post("/api/v1/sales/orders")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(soReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        SalesOrderResponse salesOrder = objectMapper.readValue(
                objectMapper.readTree(soRes.getResponse().getContentAsString()).get("data").toString(),
                SalesOrderResponse.class
        );

        // Confirm Sales Order
        mockMvc.perform(put("/api/v1/sales/orders/" + salesOrder.id() + "/confirm")
                .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.order.status").value("CONFIRMED"));

        // ==========================================
        // 6. AI SUPPLIER QUOTATION EXTRACTION & HUMAN REVIEW
        // ==========================================
        String yukenQuoteText = """
                OFFICIAL SUPPLIER QUOTATION
                Supplier: Yuken Kogyo Co., Ltd.
                Quotation No: QUO-YK-2026-E2E
                Part No: PUMP-HP-75
                Quantity: 10
                Unit Price: ¥185,000
                Delivery Date: 2026-10-30
                Payment Terms: Net 30 Days
                """;

        UploadQuotationRequest quoteUploadReq = new UploadQuotationRequest("Yuken_E2E_Quote.pdf", yukenQuoteText);
        MvcResult quoteRes = mockMvc.perform(post("/api/v1/procurement/quotations/upload")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(quoteUploadReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_REVIEW"))
                .andReturn();

        UUID quoteId = UUID.fromString(objectMapper.readTree(quoteRes.getResponse().getContentAsString()).get("data").get("id").asText());
        UUID yukenSupplierId = supplierRepository.findBySupplierCode("SUP-YUKEN").orElseThrow().getId();

        ReviewQuotationRequest reviewReq = new ReviewQuotationRequest(
                "APPROVE",
                yukenSupplierId,
                "PUMP-HP-75",
                new BigDecimal("10.0"),
                new BigDecimal("185000.00"),
                LocalDate.now().plusDays(30),
                "Net 30 Days",
                "Quotation verified against master agreement."
        );

        MvcResult reviewRes = mockMvc.perform(post("/api/v1/procurement/quotations/" + quoteId + "/review")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.generatedPoCode").exists())
                .andReturn();


        // ==========================================
        // 7. PRODUCTION ORDER & EXECUTION
        // ==========================================
        CreateProductionOrderRequest prodOrderReq = new CreateProductionOrderRequest(
                "WO-2026-E2E-800",
                salesOrder.id(),
                product.id(),
                null,
                "Osaka",
                new BigDecimal("1.0"),
                LocalDate.now(),
                LocalDate.now().plusDays(15)
        );

        MvcResult prodOrderRes = mockMvc.perform(post("/api/v1/production/orders")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prodOrderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PLANNED"))
                .andReturn();

        ProductionOrderResponse workOrder = objectMapper.readValue(
                objectMapper.readTree(prodOrderRes.getResponse().getContentAsString()).get("data").toString(),
                ProductionOrderResponse.class
        );

        // Reserve Materials
        mockMvc.perform(put("/api/v1/production/orders/" + workOrder.id() + "/reserve-materials")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MATERIAL_RESERVED"));

        // Start Production Order
        mockMvc.perform(put("/api/v1/production/orders/" + workOrder.id() + "/start")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // Complete All Production Operations
        for (ProductionOperationResponse op : workOrder.operations()) {
            mockMvc.perform(put("/api/v1/production/orders/" + workOrder.id() + "/operations/" + op.operationSeq())
                    .header("Authorization", "Bearer " + prodToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new com.sivamachineworks.platform.production.dto.UpdateOperationRequest(
                            "COMPLETED", op.plannedHours(), "Operation sequence " + op.operationSeq() + " completed"
                    ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        // Submit Quality Inspection (Transitions Order to QUALITY_CHECK)
        mockMvc.perform(post("/api/v1/production/orders/" + workOrder.id() + "/quality-check")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.sivamachineworks.platform.production.dto.SubmitQualityInspectionRequest(
                        "INSP-E2E-800-01", "FAT", "PASSED", new BigDecimal("0.0012"), new BigDecimal("0.0015"), "Laser interferometry passed with zero deviation."
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PASSED"));

        // Complete Production Order
        mockMvc.perform(put("/api/v1/production/orders/" + workOrder.id() + "/complete")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // ==========================================
        // 8. RAG DOCUMENT INGESTION & AI ASSISTANT QUERY
        // ==========================================
        IngestDocumentRequest ragDocReq = new IngestDocumentRequest(
                "MAN-HM800-OP-01",
                "SMW-HM-800 Factory Acceptance Testing (FAT) Protocol",
                "MANUFACTURING_PROCEDURE",
                product.id(),
                """
                FACTORY ACCEPTANCE TESTING PROCEDURE FOR HM-800
                1. Laser Interferometer Volumetric Accuracy Verification (< 1.5 microns).
                2. Spindle Thermal Growth Dynamic Compensation Test at 15,000 RPM.
                3. Hydrostatic Guideway Pressure Calibration: Nominal pressure 14.5 MPa.
                """,
                "ENGINEERING,PRODUCTION,ADMIN"
        );

        mockMvc.perform(post("/api/v1/rag/documents/ingest")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ragDocReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.docCode").value("MAN-HM800-OP-01"));

        // RAG Query
        DocumentRagQueryRequest ragQuery = new DocumentRagQueryRequest(
                "What is the hydrostatic guideway nominal pressure for HM-800?",
                "MANUFACTURING_PROCEDURE"
        );

        mockMvc.perform(post("/api/v1/rag/query")
                .header("Authorization", "Bearer " + engToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ragQuery)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.foundAuthorizedMatches").value(true));

        // AI Operations Query
        AiQueryRequest aiReq = new AiQueryRequest(
                "What is the status of production order WO-2026-E2E-800?",
                "OPERATIONS"
        );

        mockMvc.perform(post("/api/v1/ai/query")
                .header("Authorization", "Bearer " + prodToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aiReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasSufficientData").value(true));

        // ==========================================
        // 9. AUDIT LOG VERIFICATION
        // ==========================================
        mockMvc.perform(get("/api/v1/audit/logs")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}

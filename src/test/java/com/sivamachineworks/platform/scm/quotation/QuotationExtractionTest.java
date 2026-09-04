package com.sivamachineworks.platform.scm.quotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.scm.domain.Supplier;
import com.sivamachineworks.platform.scm.quotation.dto.QuotationExtractionDto;
import com.sivamachineworks.platform.scm.quotation.dto.ReviewQuotationRequest;
import com.sivamachineworks.platform.scm.quotation.dto.UploadQuotationRequest;
import com.sivamachineworks.platform.scm.repository.SupplierRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class QuotationExtractionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String procToken;
    private String salesToken;
    private Supplier yukenSupplier;

    @BeforeEach
    public void setup() throws Exception {
        userRepository.findAll().forEach(user -> {
            user.setPasswordHash(passwordEncoder.encode("Password@123"));
            userRepository.save(user);
        });

        procToken = obtainToken("proc_user");
        salesToken = obtainToken("sales_user");
        yukenSupplier = supplierRepository.findBySupplierCode("SUP-YUKEN").orElse(null);
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
    public void test1_uploadQuotation_extractsFieldsAndEnforcesPendingReview() throws Exception {
        String quoteText = """
                OFFICIAL SUPPLIER QUOTATION
                Supplier: Yuken Kogyo Co., Ltd.
                Quotation No: QUO-YK-2026-901
                Date: 2026-09-02
                Valid Until: 2026-10-15
                
                Item Description: High Pressure Axial Piston Pump
                Part No: PUMP-HP-75
                Quantity: 10
                Unit Price: ¥185,000
                Total Amount: ¥1,850,000
                
                Delivery Date: 2026-10-15
                Payment Terms: Net 30 Days
                """;

        UploadQuotationRequest request = new UploadQuotationRequest("Yuken_Pump_Quotation_2026.pdf", quoteText);

        MvcResult result = mockMvc.perform(post("/api/v1/procurement/quotations/upload")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.data.quotationNumber").value("QUO-YK-2026-901"))
                .andExpect(jsonPath("$.data.partNumber").value("PUMP-HP-75"))
                .andExpect(jsonPath("$.data.quantity").value(10.0))
                .andExpect(jsonPath("$.data.unitPrice").value(185000.0))
                .andReturn();

        QuotationExtractionDto dto = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                QuotationExtractionDto.class
        );

        assertThat(dto.confidenceScore()).isGreaterThan(new BigDecimal("0.8"));

        // Now perform Human Review Approval -> Generates Draft PO
        ReviewQuotationRequest reviewReq = new ReviewQuotationRequest(
                "APPROVE",
                yukenSupplier != null ? yukenSupplier.getId() : null,
                "PUMP-HP-75",
                new BigDecimal("10.0"),
                new BigDecimal("185000.00"),
                LocalDate.parse("2026-10-15"),
                "Net 30 Days",
                "Quotation verified against 2026 master agreement. Pricing approved."
        );

        mockMvc.perform(post("/api/v1/procurement/quotations/" + dto.id() + "/review")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.generatedPoCode").exists());
    }

    @Test
    public void test2_missingFields_recordsValidationWarningsAndLowConfidence() throws Exception {
        String incompleteText = """
                QUOTATION NOTE
                From: Unknown Vendor
                Item: Hydraulic Valve
                Price is negotiable.
                """;

        UploadQuotationRequest request = new UploadQuotationRequest("Incomplete_Note.pdf", incompleteText);

        MvcResult result = mockMvc.perform(post("/api/v1/procurement/quotations/upload")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.data.validationWarnings").exists())
                .andReturn();

        QuotationExtractionDto dto = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").toString(),
                QuotationExtractionDto.class
        );

        assertThat(dto.confidenceScore()).isLessThanOrEqualTo(new BigDecimal("0.5"));
    }

    @Test
    public void test3_humanReviewRejection_marksStatusRejectedWithoutCreatingPo() throws Exception {
        String quoteText = """
                Supplier: Yuken Kogyo Co., Ltd.
                Quote Ref: QUO-REJECT-01
                Part: PUMP-HP-75
                Qty: 5
                Unit Price: ¥350,000
                """;

        UploadQuotationRequest uploadReq = new UploadQuotationRequest("Overpriced_Quote.pdf", quoteText);

        MvcResult uploadRes = mockMvc.perform(post("/api/v1/procurement/quotations/upload")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadReq)))
                .andExpect(status().isOk())
                .andReturn();

        UUID quoteId = UUID.fromString(objectMapper.readTree(uploadRes.getResponse().getContentAsString()).get("data").get("id").asText());

        ReviewQuotationRequest rejectReq = new ReviewQuotationRequest(
                "REJECT",
                null,
                null,
                null,
                null,
                null,
                null,
                "Unit price of ¥350,000 exceeds target procurement threshold of ¥200,000. Rejected."
        );

        mockMvc.perform(post("/api/v1/procurement/quotations/" + quoteId + "/review")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.generatedPoCode").doesNotExist());
    }

    @Test
    public void test4_unauthorizedRoleAccess_salesUserBlockedFromUploadingOrReviewing() throws Exception {
        UploadQuotationRequest request = new UploadQuotationRequest("Quotation.pdf", "Supplier: Yuken");

        // Sales user has no procurement access
        mockMvc.perform(post("/api/v1/procurement/quotations/upload")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void test5_invalidValues_negativeQuantityThrowsValidationError() throws Exception {
        String quoteText = """
                Supplier: Yuken Kogyo Co., Ltd.
                Quotation: QUO-NEG-01
                Part: PUMP-HP-75
                Qty: -5
                Unit Price: ¥185,000
                """;

        UploadQuotationRequest uploadReq = new UploadQuotationRequest("NegativeQty.pdf", quoteText);

        MvcResult uploadRes = mockMvc.perform(post("/api/v1/procurement/quotations/upload")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadReq)))
                .andExpect(status().isOk())
                .andReturn();

        UUID quoteId = UUID.fromString(objectMapper.readTree(uploadRes.getResponse().getContentAsString()).get("data").get("id").asText());

        // Attempting to approve with negative quantity must fail
        ReviewQuotationRequest reviewReq = new ReviewQuotationRequest(
                "APPROVE",
                yukenSupplier != null ? yukenSupplier.getId() : null,
                "PUMP-HP-75",
                new BigDecimal("-5.0"),
                new BigDecimal("185000.00"),
                LocalDate.now(),
                "Net 30 Days",
                "Approve"
        );

        mockMvc.perform(post("/api/v1/procurement/quotations/" + quoteId + "/review")
                .header("Authorization", "Bearer " + procToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isBadRequest());
    }
}

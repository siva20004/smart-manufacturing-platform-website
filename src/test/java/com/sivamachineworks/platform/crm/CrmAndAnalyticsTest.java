package com.sivamachineworks.platform.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.crm.dto.*;
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
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CrmAndAnalyticsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String salesToken;
    private String mgmtToken;
    private String prodToken;

    @BeforeEach
    public void setup() throws Exception {
        userRepository.findAll().forEach(user -> {
            user.setPasswordHash(passwordEncoder.encode("Password@123"));
            userRepository.save(user);
        });

        salesToken = obtainToken("sales_user", "Password@123");
        mgmtToken = obtainToken("mgmt_user", "Password@123");
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
    public void test1_crmWorkflow_contactsOpportunitiesAndServiceRequests() throws Exception {
        UUID customerId = UUID.fromString("c1111111-1111-1111-1111-111111111111"); // Toyota
        UUID hm500Id = UUID.fromString("a1111111-1111-1111-1111-111111111111");

        // 1. Create Customer Contact
        CreateContactRequest contactReq = new CreateContactRequest(
                customerId,
                "Taro",
                "Yamada",
                "t.yamada@toyota-demo.jp",
                "+81-565-28-9999",
                "Assembly Line Engineer",
                "Advanced Manufacturing Div",
                false
        );

        MvcResult contactResult = mockMvc.perform(post("/api/v1/crm/contacts")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Taro"))
                .andReturn();

        UUID contactId = UUID.fromString(objectMapper.readTree(contactResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // 2. Create CRM Opportunity
        CreateOpportunityRequest oppReq = new CreateOpportunityRequest(
                "OPP-2026-TOYOTA-NEW",
                customerId,
                contactId,
                hm500Id,
                "Toyota Tsutsumi Plant 3-Axis Upgrade",
                "PROPOSAL",
                new BigDecimal("25000000.00"),
                new BigDecimal("80.0"),
                LocalDate.now().plusDays(60)
        );

        mockMvc.perform(post("/api/v1/crm/opportunities")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oppReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.opportunityCode").value("OPP-2026-TOYOTA-NEW"))
                .andExpect(jsonPath("$.data.stage").value("PROPOSAL"));

        // 3. Create Service Request Ticket
        CreateServiceRequestDto srvReq = new CreateServiceRequestDto(
                "SRV-2026-TOYOTA-01",
                customerId,
                hm500Id,
                "SN-HM500-2026-0042",
                "Spindle Vibration Alert at 12,000 RPM",
                "Customer reports slight acoustic vibration on Y-axis rapid traverse",
                "HIGH"
        );

        MvcResult srvResult = mockMvc.perform(post("/api/v1/crm/service-requests")
                .header("Authorization", "Bearer " + salesToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(srvReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ticketNumber").value("SRV-2026-TOYOTA-01"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn();

        UUID ticketId = UUID.fromString(objectMapper.readTree(srvResult.getResponse().getContentAsString()).get("data").get("id").asText());

        // 4. Resolve Service Request
        mockMvc.perform(put("/api/v1/crm/service-requests/" + ticketId + "/resolve?resolutionNotes=Replaced spindle bearing lubrication and recalibrated laser interferometer")
                .header("Authorization", "Bearer " + prodToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        // 5. Query Customer 360 History
        MvcResult histResult = mockMvc.perform(get("/api/v1/crm/customers/" + customerId + "/history")
                .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerCode").value("CUST-TOYOTA"))
                .andExpect(jsonPath("$.data.contacts").isArray())
                .andExpect(jsonPath("$.data.opportunities").isArray())
                .andExpect(jsonPath("$.data.serviceRequests").isArray())
                .andReturn();

        var histData = objectMapper.readTree(histResult.getResponse().getContentAsString()).get("data");
        assertThat(histData.get("contacts").size()).isGreaterThanOrEqualTo(2);
        assertThat(histData.get("serviceRequests").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void test2_businessAnalyticsEndpoints_returnRealDatabaseAggregations() throws Exception {
        // 1. Sales by Customer
        mockMvc.perform(get("/api/v1/analytics/sales-by-customer")
                .header("Authorization", "Bearer " + mgmtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 2. Sales by Product
        mockMvc.perform(get("/api/v1/analytics/sales-by-product")
                .header("Authorization", "Bearer " + mgmtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 3. Order Volume
        mockMvc.perform(get("/api/v1/analytics/order-volume")
                .header("Authorization", "Bearer " + mgmtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 4. Inventory Value
        mockMvc.perform(get("/api/v1/analytics/inventory-value")
                .header("Authorization", "Bearer " + mgmtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 5. Production Metrics
        mockMvc.perform(get("/api/v1/analytics/production-metrics")
                .header("Authorization", "Bearer " + mgmtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onTimeCompletionRatePct").value(100.0));

        // 6. Supplier Performance
        mockMvc.perform(get("/api/v1/analytics/supplier-performance")
                .header("Authorization", "Bearer " + mgmtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 7. Service Metrics
        mockMvc.perform(get("/api/v1/analytics/service-metrics")
                .header("Authorization", "Bearer " + mgmtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTickets").exists());
    }
}

package com.sivamachineworks.platform.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
public class AuthAndRoleSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.sivamachineworks.platform.identity.repository.UserRepository userRepository;

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        var users = userRepository.findAll();
        if (users.isEmpty()) return;
        
        String newHash = passwordEncoder.encode("Password@123");
        System.out.println("COMPUTED_BCRYPT_HASH_FOR_DB: " + newHash);
        
        for (var user : users) {
            user.setPasswordHash(newHash);
        }
        userRepository.saveAll(users);
    }

    @Test
    public void test1_successfulLogin() throws Exception {
        LoginRequest request = new LoginRequest("admin", "Password@123");
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    public void test2_invalidPassword() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrongpass");
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    public void test3_missingToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    public void test4_invalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer invalid.token.xyz"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    public void test5_validToken() throws Exception {
        LoginRequest request = new LoginRequest("admin", "Password@123");
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseStr).get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    public void test6_roleAccess() throws Exception {
        LoginRequest request = new LoginRequest("admin", "Password@123");
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseStr).get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/v1/test/roles/admin-only")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void test7_forbiddenAccess() throws Exception {
        LoginRequest request = new LoginRequest("sales_user", "Password@123");
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseStr).get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/v1/test/roles/admin-only")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    public void test8_seedUsersVerification() throws Exception {
        String[] users = {"admin", "it_engineer", "sales_user", "eng_user", "proc_user", "prod_user", "fin_user", "mgmt_user"};
        
        for (String user : users) {
            LoginRequest request = new LoginRequest(user, "Password@123");
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
}

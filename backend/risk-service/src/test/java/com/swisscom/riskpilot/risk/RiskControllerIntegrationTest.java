package com.swisscom.riskpilot.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.riskpilot.risk.dto.LoginRequest;
import com.swisscom.riskpilot.risk.dto.RiskRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String viewerToken;
    private static String reviewerToken;
    private static String adminToken;
    private static Long createdRiskId;

    // ── 1. Valid login returns 200 + JWT ──────────────────────────────────────

    @Test
    @Order(1)
    void loginWithValidCredentials_returns200AndToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("viewer", "viewer123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("viewer"))
                .andExpect(jsonPath("$.role").value("VIEWER"))
                .andReturn();

        viewerToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    // ── 2. Wrong password returns 401 ─────────────────────────────────────────

    @Test
    @Order(2)
    void loginWithWrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("viewer", "wrongpass"))))
                .andExpect(status().isUnauthorized());
    }

    // ── 3. GET /api/risks without token returns 403 ───────────────────────────

    @Test
    @Order(3)
    void listRisks_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/risks"))
                .andExpect(status().isForbidden());
    }

    // ── 4. GET /api/risks with VIEWER JWT returns 200 + list ─────────────────

    @Test
    @Order(4)
    void listRisks_withViewerToken_returns200() throws Exception {
        // Ensure viewerToken is initialized (in case test order changes)
        if (viewerToken == null) {
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest("viewer", "viewer123"))))
                    .andReturn();
            viewerToken = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("token").asText();
        }

        mockMvc.perform(get("/api/risks")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(6)));
    }

    // ── 5. POST /api/risks with VIEWER JWT returns 403 ───────────────────────

    @Test
    @Order(5)
    void createRisk_withViewerToken_returns403() throws Exception {
        if (viewerToken == null) {
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest("viewer", "viewer123"))))
                    .andReturn();
            viewerToken = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("token").asText();
        }

        RiskRequest req = new RiskRequest("Test Risk", "desc", "IT", "HIGH", "Owner A");

        mockMvc.perform(post("/api/risks")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ── 6. POST /api/risks with REVIEWER JWT returns 201 + created risk ───────

    @Test
    @Order(6)
    void createRisk_withReviewerToken_returns201() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("reviewer", "reviewer123"))))
                .andReturn();
        reviewerToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        RiskRequest req = new RiskRequest(
                "Integration Test Risk",
                "Created during integration test",
                "IT",
                "MEDIUM",
                "Test Owner"
        );

        MvcResult result = mockMvc.perform(post("/api/risks")
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Integration Test Risk"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        createdRiskId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    // ── 7. PUT /api/risks/{id} with REVIEWER JWT updates risk ─────────────────

    @Test
    @Order(7)
    void updateRisk_withReviewerToken_returns200() throws Exception {
        if (reviewerToken == null || createdRiskId == null) {
            Assumptions.assumeTrue(false, "Skipping – depends on test 6");
        }

        RiskRequest req = new RiskRequest(
                "Updated Integration Test Risk",
                "Updated description",
                "Operations",
                "HIGH",
                "Updated Owner"
        );

        mockMvc.perform(put("/api/risks/" + createdRiskId)
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Test Risk"))
                .andExpect(jsonPath("$.severity").value("HIGH"));
    }

    // ── 8. DELETE /api/risks/{id} with REVIEWER JWT returns 403 ──────────────

    @Test
    @Order(8)
    void deleteRisk_withReviewerToken_returns403() throws Exception {
        if (reviewerToken == null || createdRiskId == null) {
            Assumptions.assumeTrue(false, "Skipping – depends on test 6");
        }

        mockMvc.perform(delete("/api/risks/" + createdRiskId)
                        .header("Authorization", "Bearer " + reviewerToken))
                .andExpect(status().isForbidden());
    }

    // ── 9. DELETE /api/risks/{id} with ADMIN JWT returns 204 ─────────────────

    @Test
    @Order(9)
    void deleteRisk_withAdminToken_returns204() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "admin123"))))
                .andReturn();
        adminToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        if (createdRiskId == null) {
            Assumptions.assumeTrue(false, "Skipping – depends on test 6");
        }

        mockMvc.perform(delete("/api/risks/" + createdRiskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    // ── 10. GET /api/health returns 200 ──────────────────────────────────────

    @Test
    @Order(10)
    void health_returns200() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("risk-service"));
    }
}

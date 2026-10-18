package com.swisscom.riskpilot.decision.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.riskpilot.decision.domain.AuditEntry;
import com.swisscom.riskpilot.decision.domain.DecisionStatus;
import com.swisscom.riskpilot.decision.dto.DecisionRequest;
import com.swisscom.riskpilot.decision.repository.AuditRepository;
import com.swisscom.riskpilot.decision.repository.DecisionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DecisionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Test
    @DisplayName("Health endpoint is publicly accessible without authentication")
    void testPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("decision-service"));
    }

    @Test
    @DisplayName("Unauthenticated request to submit decision returns 401 Unauthorized")
    void testSubmitDecisionUnauthenticated() throws Exception {
        DecisionRequest request = new DecisionRequest(
                1L,
                DecisionStatus.APPROVED,
                "Justification has at least twenty characters here.",
                "Evidence checklist",
                "HIGH"
        );

        mockMvc.perform(post("/api/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    @DisplayName("User with VIEWER role is forbidden from submitting a decision")
    void testSubmitDecisionForbiddenForViewer() throws Exception {
        DecisionRequest request = new DecisionRequest(
                1L,
                DecisionStatus.APPROVED,
                "Justification has at least twenty characters here.",
                "Evidence checklist",
                "HIGH"
        );

        mockMvc.perform(post("/api/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "reviewer", roles = {"REVIEWER"})
    @DisplayName("Reviewer submits decision: succeeds and creates audit entry")
    void testSubmitDecisionSuccessAndAuditCreated() throws Exception {
        long riskId = 42L;
        DecisionRequest request = new DecisionRequest(
                riskId,
                DecisionStatus.APPROVED,
                "Comprehensive validation of guardrails and data privacy performed.",
                "Audit logs and model card attached",
                "HIGH"
        );

        mockMvc.perform(post("/api/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riskId").value(riskId))
                .andExpect(jsonPath("$.reviewer").value("reviewer"))
                .andExpect(jsonPath("$.decision").value("APPROVED"))
                .andExpect(jsonPath("$.confidenceLevel").value("HIGH"));

        // Verify audit entry was logged
        List<AuditEntry> auditEntries = auditRepository.findByRiskIdOrderByTimestampDesc(riskId);
        assertThat(auditEntries).isNotEmpty();
        AuditEntry latest = auditEntries.get(0);
        assertThat(latest.getAction()).isEqualTo("DECISION_CREATED");
        assertThat(latest.getActor()).isEqualTo("reviewer");
        assertThat(latest.getDetails()).contains("APPROVED");
    }

    @Test
    @WithMockUser(username = "reviewer", roles = {"REVIEWER"})
    @DisplayName("Submitting decision with justification < 20 chars returns 400 Bad Request")
    void testSubmitDecisionValidationFailure() throws Exception {
        DecisionRequest request = new DecisionRequest(
                1L,
                DecisionStatus.APPROVED,
                "Too short", // < 20 chars
                "Evidence",
                "HIGH"
        );

        mockMvc.perform(post("/api/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.justification").exists());
    }

    @Test
    @WithMockUser(username = "reviewer", roles = {"REVIEWER"})
    @DisplayName("Get decisions for risk succeeds for reviewer")
    void testGetDecisionsByRiskId() throws Exception {
        mockMvc.perform(get("/api/decisions/risk/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].riskId").value(1));
    }

    @Test
    @WithMockUser(username = "reviewer", roles = {"REVIEWER"})
    @DisplayName("Get audit entries for risk succeeds for reviewer")
    void testGetAuditEntriesByRiskId() throws Exception {
        mockMvc.perform(get("/api/audit/risk/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].riskId").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Get all audit entries succeeds for admin")
    void testGetAllAuditEntriesForAdmin() throws Exception {
        mockMvc.perform(get("/api/audit?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    @DisplayName("User with VIEWER role cannot access audit logs")
    void testAuditForbiddenForViewer() throws Exception {
        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isForbidden());
    }
}

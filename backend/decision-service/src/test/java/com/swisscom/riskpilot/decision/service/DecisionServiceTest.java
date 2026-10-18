package com.swisscom.riskpilot.decision.service;

import com.swisscom.riskpilot.decision.domain.AuditEntry;
import com.swisscom.riskpilot.decision.domain.Decision;
import com.swisscom.riskpilot.decision.domain.DecisionStatus;
import com.swisscom.riskpilot.decision.dto.DecisionRequest;
import com.swisscom.riskpilot.decision.dto.DecisionResponse;
import com.swisscom.riskpilot.decision.exception.ResourceNotFoundException;
import com.swisscom.riskpilot.decision.repository.DecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DecisionService decisionService;

    private Decision sampleDecision;
    private DecisionRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleDecision = Decision.builder()
                .id(100L)
                .riskId(1L)
                .reviewer("reviewer")
                .decision(DecisionStatus.APPROVED)
                .justification("Sufficient guardrails implemented according to Swisscom standards.")
                .evidenceReviewed("Security scan report #14")
                .confidenceLevel("HIGH")
                .decidedAt(LocalDateTime.now())
                .build();

        sampleRequest = new DecisionRequest(
                1L,
                DecisionStatus.APPROVED,
                "Sufficient guardrails implemented according to Swisscom standards.",
                "Security scan report #14",
                "HIGH"
        );
    }

    @Test
    @DisplayName("createDecision saves decision and logs audit entry")
    void testCreateDecisionSuccess() {
        when(decisionRepository.save(any(Decision.class))).thenAnswer(invocation -> {
            Decision d = invocation.getArgument(0);
            d.setId(101L);
            d.setDecidedAt(LocalDateTime.now());
            return d;
        });

        DecisionResponse response = decisionService.createDecision(sampleRequest, "john_reviewer");

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.riskId()).isEqualTo(1L);
        assertThat(response.reviewer()).isEqualTo("john_reviewer");
        assertThat(response.decision()).isEqualTo(DecisionStatus.APPROVED);

        // Verify auditService.logEntry was called with correct parameters
        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService, times(1)).logEntry(
                eq(1L),
                eq("DECISION_CREATED"),
                eq("john_reviewer"),
                detailsCaptor.capture()
        );
        assertThat(detailsCaptor.getValue()).contains("APPROVED");
        assertThat(detailsCaptor.getValue()).contains("HIGH");
    }

    @Test
    @DisplayName("getDecisionById returns response when decision exists")
    void testGetDecisionByIdFound() {
        when(decisionRepository.findById(100L)).thenReturn(Optional.of(sampleDecision));

        DecisionResponse response = decisionService.getDecisionById(100L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.decision()).isEqualTo(DecisionStatus.APPROVED);
        verify(decisionRepository, times(1)).findById(100L);
    }

    @Test
    @DisplayName("getDecisionById throws ResourceNotFoundException when decision does not exist")
    void testGetDecisionByIdNotFound() {
        when(decisionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionService.getDecisionById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(decisionRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("getDecisionsByRiskId returns list of decisions")
    void testGetDecisionsByRiskId() {
        when(decisionRepository.findByRiskIdOrderByDecidedAtDesc(1L))
                .thenReturn(List.of(sampleDecision));

        List<DecisionResponse> list = decisionService.getDecisionsByRiskId(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).riskId()).isEqualTo(1L);
        verify(decisionRepository, times(1)).findByRiskIdOrderByDecidedAtDesc(1L);
    }
}

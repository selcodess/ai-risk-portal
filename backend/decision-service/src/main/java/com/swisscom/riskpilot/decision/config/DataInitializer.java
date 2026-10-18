package com.swisscom.riskpilot.decision.config;

import com.swisscom.riskpilot.decision.domain.AuditEntry;
import com.swisscom.riskpilot.decision.domain.Decision;
import com.swisscom.riskpilot.decision.domain.DecisionStatus;
import com.swisscom.riskpilot.decision.repository.AuditRepository;
import com.swisscom.riskpilot.decision.repository.DecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DecisionRepository decisionRepository;
    private final AuditRepository auditRepository;

    @Override
    public void run(String... args) {
        if (decisionRepository.count() > 0) {
            log.info("Demo data already present, skipping initialization.");
            return;
        }

        log.info("Seeding initial demo decisions and audit entries for 6 sample AI risks...");

        // Risk 1: Customer Facing GenAI Chatbot
        Decision d1 = Decision.builder()
                .riskId(1L)
                .reviewer("reviewer")
                .decision(DecisionStatus.APPROVED)
                .justification("Guardrails against prompt injection and PII leakage verified with automated eval suite. Swisscom security policy compliant.")
                .evidenceReviewed("Penetration test report v1.2, NeMo Guardrails evaluation metrics, GDPR DPIA assessment")
                .confidenceLevel("HIGH")
                .decidedAt(LocalDateTime.now().minusDays(3))
                .build();

        // Risk 2: Automated Network Anomaly Detection
        Decision d2 = Decision.builder()
                .riskId(2L)
                .reviewer("admin")
                .decision(DecisionStatus.APPROVED)
                .justification("Model drift monitoring and fallback alert channels successfully configured in staging environment.")
                .evidenceReviewed("Confusion matrix on historical 2025 outage data, SLA failover tests, Grafana dashboard checks")
                .confidenceLevel("HIGH")
                .decidedAt(LocalDateTime.now().minusDays(2))
                .build();

        // Risk 3: AI Code Assistant for Developers
        Decision d3 = Decision.builder()
                .riskId(3L)
                .reviewer("reviewer")
                .decision(DecisionStatus.NEEDS_INFO)
                .justification("Awaiting confirmation on telemetry data residency and compliance with Swiss Federal Data Protection Act (FADP).")
                .evidenceReviewed("Vendor SOC2 type II compliance summary, pending DPA addendum")
                .confidenceLevel("MEDIUM")
                .decidedAt(LocalDateTime.now().minusDays(1))
                .build();

        // Risk 4: Predictive Billing Churn Analysis
        Decision d4 = Decision.builder()
                .riskId(4L)
                .reviewer("reviewer")
                .decision(DecisionStatus.DEFERRED)
                .justification("Scheduled for comprehensive ethics board review regarding demographic parity and fair customer treatment.")
                .evidenceReviewed("Fairness metrics report (Disparate Impact Ratio = 0.96), demographic bias audit draft")
                .confidenceLevel("MEDIUM")
                .decidedAt(LocalDateTime.now().minusHours(12))
                .build();

        // Risk 5: Smart Call Center Voice Transcriber
        Decision d5 = Decision.builder()
                .riskId(5L)
                .reviewer("admin")
                .decision(DecisionStatus.APPROVED)
                .justification("Real-time redaction of payment credentials and Swiss IBAN patterns validated with 99.8% precision.")
                .evidenceReviewed("Audio synthetic redaction benchmark, Whisper-Swisscom acoustic model validation")
                .confidenceLevel("HIGH")
                .decidedAt(LocalDateTime.now().minusHours(6))
                .build();

        // Risk 6: HR Candidate Screening Pilot
        Decision d6 = Decision.builder()
                .riskId(6L)
                .reviewer("reviewer")
                .decision(DecisionStatus.REJECTED)
                .justification("EU AI Act high-risk classification requires human-in-the-loop auditability that is not currently satisfied by vendor architecture.")
                .evidenceReviewed("EU AI Act Annex III assessment, algorithmic impact assessment (AIA), internal legal memo")
                .confidenceLevel("HIGH")
                .decidedAt(LocalDateTime.now().minusHours(2))
                .build();

        decisionRepository.saveAll(List.of(d1, d2, d3, d4, d5, d6));

        // Audit log entries
        List<AuditEntry> auditLogs = List.of(
                AuditEntry.builder()
                        .riskId(1L)
                        .action("RISK_CREATED")
                        .actor("admin")
                        .details("Registered initial risk assessment for Customer GenAI Chatbot")
                        .timestamp(LocalDateTime.now().minusDays(5))
                        .build(),
                AuditEntry.builder()
                        .riskId(1L)
                        .action("DECISION_CREATED")
                        .actor("reviewer")
                        .details("Decision: APPROVED, Confidence: HIGH, Justification: Guardrails against prompt injection and PII leakage verified with automated eval suite.")
                        .timestamp(LocalDateTime.now().minusDays(3))
                        .build(),
                AuditEntry.builder()
                        .riskId(2L)
                        .action("DECISION_CREATED")
                        .actor("admin")
                        .details("Decision: APPROVED, Confidence: HIGH, Justification: Model drift monitoring and fallback alert channels successfully configured.")
                        .timestamp(LocalDateTime.now().minusDays(2))
                        .build(),
                AuditEntry.builder()
                        .riskId(3L)
                        .action("DECISION_CREATED")
                        .actor("reviewer")
                        .details("Decision: NEEDS_INFO, Confidence: MEDIUM, Justification: Awaiting confirmation on telemetry data residency.")
                        .timestamp(LocalDateTime.now().minusDays(1))
                        .build(),
                AuditEntry.builder()
                        .riskId(4L)
                        .action("DECISION_CREATED")
                        .actor("reviewer")
                        .details("Decision: DEFERRED, Confidence: MEDIUM, Justification: Scheduled for ethics board review.")
                        .timestamp(LocalDateTime.now().minusHours(12))
                        .build(),
                AuditEntry.builder()
                        .riskId(5L)
                        .action("DECISION_CREATED")
                        .actor("admin")
                        .details("Decision: APPROVED, Confidence: HIGH, Justification: Real-time redaction validated.")
                        .timestamp(LocalDateTime.now().minusHours(6))
                        .build(),
                AuditEntry.builder()
                        .riskId(6L)
                        .action("DECISION_CREATED")
                        .actor("reviewer")
                        .details("Decision: REJECTED, Confidence: HIGH, Justification: EU AI Act compliance deficiency.")
                        .timestamp(LocalDateTime.now().minusHours(2))
                        .build()
        );

        auditRepository.saveAll(auditLogs);
        log.info("Initialized {} decisions and {} audit entries successfully.", 6, auditLogs.size());
    }
}

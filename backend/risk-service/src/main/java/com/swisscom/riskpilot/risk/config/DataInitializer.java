package com.swisscom.riskpilot.risk.config;

import com.swisscom.riskpilot.risk.domain.*;
import com.swisscom.riskpilot.risk.repository.AppUserRepository;
import com.swisscom.riskpilot.risk.repository.RiskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final AppUserRepository appUserRepository;
    private final RiskRepository riskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (appUserRepository.count() == 0) {
            seedUsers();
        }
        if (riskRepository.count() == 0) {
            seedRisks();
        }
        log.info("Data initializer complete – users: {}, risks: {}",
                appUserRepository.count(), riskRepository.count());
    }

    private void seedUsers() {
        appUserRepository.saveAll(List.of(
                AppUser.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .role(UserRole.ADMIN)
                        .displayName("Admin User")
                        .build(),
                AppUser.builder()
                        .username("reviewer")
                        .password(passwordEncoder.encode("reviewer123"))
                        .role(UserRole.REVIEWER)
                        .displayName("Risk Reviewer")
                        .build(),
                AppUser.builder()
                        .username("viewer")
                        .password(passwordEncoder.encode("viewer123"))
                        .role(UserRole.VIEWER)
                        .displayName("Risk Viewer")
                        .build()
        ));
        log.info("Seeded 3 demo users");
    }

    private void seedRisks() {
        riskRepository.saveAll(List.of(
                Risk.builder()
                        .title("Customer data exposure via partner API")
                        .description("Partner API endpoints do not enforce OAuth 2.0 token scopes, potentially exposing PII of Swisscom customers to unauthorized third parties.")
                        .area("Information Security")
                        .severity(RiskSeverity.CRITICAL)
                        .status(RiskStatus.PENDING)
                        .owner("Alex Morgan")
                        .createdBy("admin")
                        .build(),
                Risk.builder()
                        .title("Unplanned downtime during service migration")
                        .description("Migration of legacy billing system to cloud-native infrastructure lacks a tested rollback procedure, risking prolonged outages.")
                        .area("Operations")
                        .severity(RiskSeverity.HIGH)
                        .status(RiskStatus.NEEDS_INFO)
                        .owner("Maya Chen")
                        .createdBy("reviewer")
                        .build(),
                Risk.builder()
                        .title("Delayed vendor security assessment")
                        .description("Third-party vendor's security self-assessment is 3 months overdue, leaving a gap in the supplier risk register.")
                        .area("Vendor Management")
                        .severity(RiskSeverity.MEDIUM)
                        .status(RiskStatus.PENDING)
                        .owner("Sam Taylor")
                        .createdBy("reviewer")
                        .build(),
                Risk.builder()
                        .title("Insufficient audit trail for manual approvals")
                        .description("Manual approval workflows in the finance system do not generate immutable audit logs, creating compliance gaps under FINMA guidelines.")
                        .area("Compliance")
                        .severity(RiskSeverity.HIGH)
                        .status(RiskStatus.APPROVED)
                        .owner("Jordan Lee")
                        .createdBy("admin")
                        .build(),
                Risk.builder()
                        .title("Data processing dependency on one supplier")
                        .description("Core SMS routing depends on a single supplier with no contractual SLA for failover, creating single point of failure risk.")
                        .area("Vendor Management")
                        .severity(RiskSeverity.MEDIUM)
                        .status(RiskStatus.PENDING)
                        .owner("Alex Morgan")
                        .createdBy("reviewer")
                        .build(),
                Risk.builder()
                        .title("Outdated internal documentation")
                        .description("Network topology diagrams and runbooks have not been reviewed since Q1 2024, leading to onboarding inefficiencies and incident response delays.")
                        .area("Operations")
                        .severity(RiskSeverity.LOW)
                        .status(RiskStatus.APPROVED)
                        .owner("Maya Chen")
                        .createdBy("reviewer")
                        .build()
        ));
        log.info("Seeded 6 sample risks");
    }
}

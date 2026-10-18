package com.swisscom.riskpilot.risk.repository;

import com.swisscom.riskpilot.risk.domain.Risk;
import com.swisscom.riskpilot.risk.domain.RiskSeverity;
import com.swisscom.riskpilot.risk.domain.RiskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskRepository extends JpaRepository<Risk, Long> {

    Page<Risk> findByStatus(RiskStatus status, Pageable pageable);

    Page<Risk> findBySeverity(RiskSeverity severity, Pageable pageable);

    Page<Risk> findByOwnerContainingIgnoreCase(String owner, Pageable pageable);
}

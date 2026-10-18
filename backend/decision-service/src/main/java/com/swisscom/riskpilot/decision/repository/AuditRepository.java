package com.swisscom.riskpilot.decision.repository;

import com.swisscom.riskpilot.decision.domain.AuditEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditEntry, Long> {

    List<AuditEntry> findByRiskIdOrderByTimestampDesc(Long riskId);

    Page<AuditEntry> findAllByOrderByTimestampDesc(Pageable pageable);
}

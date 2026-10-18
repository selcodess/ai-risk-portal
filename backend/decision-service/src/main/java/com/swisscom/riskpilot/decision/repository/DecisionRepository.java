package com.swisscom.riskpilot.decision.repository;

import com.swisscom.riskpilot.decision.domain.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {

    List<Decision> findByRiskIdOrderByDecidedAtDesc(Long riskId);
}

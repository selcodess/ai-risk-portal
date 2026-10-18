package com.swisscom.riskpilot.risk.service;

import com.swisscom.riskpilot.risk.domain.Risk;
import com.swisscom.riskpilot.risk.domain.RiskSeverity;
import com.swisscom.riskpilot.risk.domain.RiskStatus;
import com.swisscom.riskpilot.risk.dto.PagedResponse;
import com.swisscom.riskpilot.risk.dto.RiskRequest;
import com.swisscom.riskpilot.risk.dto.RiskResponse;
import com.swisscom.riskpilot.risk.repository.RiskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskService {

    private final RiskRepository riskRepository;

    public PagedResponse<RiskResponse> findAll(Pageable pageable) {
        Page<Risk> page = riskRepository.findAll(pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<RiskResponse> findByStatus(String status, Pageable pageable) {
        RiskStatus riskStatus = RiskStatus.valueOf(status.toUpperCase());
        Page<Risk> page = riskRepository.findByStatus(riskStatus, pageable);
        return toPagedResponse(page);
    }

    public Optional<RiskResponse> findById(Long id) {
        return riskRepository.findById(id).map(this::toResponse);
    }

    @Transactional
    public RiskResponse create(RiskRequest req, String createdBy) {
        Risk risk = Risk.builder()
                .title(req.title())
                .description(req.description())
                .area(req.area())
                .severity(RiskSeverity.valueOf(req.severity().toUpperCase()))
                .status(RiskStatus.PENDING)
                .owner(req.owner())
                .createdBy(createdBy)
                .build();
        return toResponse(riskRepository.save(risk));
    }

    @Transactional
    public RiskResponse update(Long id, RiskRequest req) {
        Risk risk = riskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Risk not found: " + id));
        risk.setTitle(req.title());
        risk.setDescription(req.description());
        risk.setArea(req.area());
        risk.setSeverity(RiskSeverity.valueOf(req.severity().toUpperCase()));
        risk.setOwner(req.owner());
        return toResponse(riskRepository.save(risk));
    }

    @Transactional
    public void delete(Long id) {
        if (!riskRepository.existsById(id)) {
            throw new EntityNotFoundException("Risk not found: " + id);
        }
        riskRepository.deleteById(id);
    }

    public RiskResponse toResponse(Risk risk) {
        return new RiskResponse(
                risk.getId(),
                risk.getTitle(),
                risk.getDescription(),
                risk.getArea(),
                risk.getSeverity(),
                risk.getStatus(),
                risk.getOwner(),
                risk.getCreatedAt(),
                risk.getUpdatedAt(),
                risk.getCreatedBy()
        );
    }

    private PagedResponse<RiskResponse> toPagedResponse(Page<Risk> page) {
        return new PagedResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }
}

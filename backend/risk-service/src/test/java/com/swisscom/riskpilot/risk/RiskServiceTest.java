package com.swisscom.riskpilot.risk;

import com.swisscom.riskpilot.risk.domain.Risk;
import com.swisscom.riskpilot.risk.domain.RiskSeverity;
import com.swisscom.riskpilot.risk.domain.RiskStatus;
import com.swisscom.riskpilot.risk.dto.PagedResponse;
import com.swisscom.riskpilot.risk.dto.RiskRequest;
import com.swisscom.riskpilot.risk.dto.RiskResponse;
import com.swisscom.riskpilot.risk.repository.RiskRepository;
import com.swisscom.riskpilot.risk.service.RiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskServiceTest {

    @Mock
    private RiskRepository riskRepository;

    @InjectMocks
    private RiskService riskService;

    private Risk sampleRisk;

    @BeforeEach
    void setUp() {
        sampleRisk = Risk.builder()
                .id(1L)
                .title("Test Risk")
                .description("Test description")
                .area("IT")
                .severity(RiskSeverity.HIGH)
                .status(RiskStatus.PENDING)
                .owner("Test Owner")
                .createdBy("reviewer")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── 1. create() saves risk with correct fields ────────────────────────────

    @Test
    void create_savesRiskWithCorrectFields() {
        RiskRequest request = new RiskRequest(
                "Test Risk", "Test description", "IT", "HIGH", "Test Owner"
        );

        when(riskRepository.save(any(Risk.class))).thenReturn(sampleRisk);

        RiskResponse response = riskService.create(request, "reviewer");

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Test Risk");
        assertThat(response.severity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(response.status()).isEqualTo(RiskStatus.PENDING);
        assertThat(response.createdBy()).isEqualTo("reviewer");

        verify(riskRepository, times(1)).save(any(Risk.class));
    }

    // ── 2. findAll() returns paged results ────────────────────────────────────

    @Test
    void findAll_returnsPagedResults() {
        Risk risk2 = Risk.builder()
                .id(2L)
                .title("Second Risk")
                .description("Another risk")
                .area("Operations")
                .severity(RiskSeverity.MEDIUM)
                .status(RiskStatus.APPROVED)
                .owner("Another Owner")
                .createdBy("admin")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        Page<Risk> page = new PageImpl<>(List.of(sampleRisk, risk2), pageable, 2);

        when(riskRepository.findAll(pageable)).thenReturn(page);

        PagedResponse<RiskResponse> result = riskService.findAll(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getCurrentPage()).isEqualTo(0);
    }

    // ── 3. findById() returns empty for missing id ────────────────────────────

    @Test
    void findById_returnsEmptyForMissingId() {
        when(riskRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<RiskResponse> result = riskService.findById(999L);

        assertThat(result).isEmpty();
        verify(riskRepository).findById(999L);
    }

    // ── 4. update() changes risk fields ──────────────────────────────────────

    @Test
    void update_changesRiskFields() {
        RiskRequest updateRequest = new RiskRequest(
                "Updated Title", "Updated description", "Operations", "CRITICAL", "New Owner"
        );

        Risk updatedRisk = Risk.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated description")
                .area("Operations")
                .severity(RiskSeverity.CRITICAL)
                .status(RiskStatus.PENDING)
                .owner("New Owner")
                .createdBy("reviewer")
                .createdAt(sampleRisk.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(riskRepository.findById(1L)).thenReturn(Optional.of(sampleRisk));
        when(riskRepository.save(any(Risk.class))).thenReturn(updatedRisk);

        RiskResponse response = riskService.update(1L, updateRequest);

        assertThat(response.title()).isEqualTo("Updated Title");
        assertThat(response.area()).isEqualTo("Operations");
        assertThat(response.severity()).isEqualTo(RiskSeverity.CRITICAL);
        assertThat(response.owner()).isEqualTo("New Owner");

        verify(riskRepository).findById(1L);
        verify(riskRepository).save(any(Risk.class));
    }
}

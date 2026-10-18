package com.swisscom.riskpilot.decision.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long riskId;

    @NotBlank
    @Column(nullable = false)
    private String reviewer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionStatus decision;

    @NotBlank
    @Size(min = 20, max = 2000)
    @Column(nullable = false, length = 2000)
    private String justification;

    @Column(length = 4000)
    private String evidenceReviewed;

    @Column(length = 50)
    private String confidenceLevel;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime decidedAt;
}

package com.swisscom.riskpilot.risk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "risks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Risk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskStatus status;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String owner;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String createdBy;
}

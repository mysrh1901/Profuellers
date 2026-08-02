package com.regulith.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChainReactionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private String engagementId;
    private String domain;       // SOX, SECURITY, REGULATORY, CONTRACTUAL, FAIR_LENDING, AUDIT
    private String severity;     // CRITICAL, HIGH, MEDIUM, LOW
    private String reason;
    private String actionRequired;
    private String sla;
    private String controlsAffected;
    private boolean blocking;
    private LocalDateTime timestamp;
}

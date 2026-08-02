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
public class ComplianceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String engagementId;
    private String eventType; // CODE_COMMIT, INFRA_CHANGE, REGULATORY_UPDATE, ACCESS_CHANGE
    private String source;
    private String description;
    private LocalDateTime timestamp;
    private boolean processed;

    @Column(length = 5000)
    private String payload; // JSON payload with event details
}

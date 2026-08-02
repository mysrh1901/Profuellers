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
public class AuditNarrative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private String engagementId;
    private String eventType;
    private LocalDateTime generatedAt;

    @Column(length = 10000)
    private String narrativeText;

    @Column(length = 2000)
    private String controlsSatisfied;

    @Column(length = 2000)
    private String evidenceArtifacts;
}

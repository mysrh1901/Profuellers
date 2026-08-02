package com.regulith.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientEngagement {

    @Id
    private String engagementId;

    private String clientName;
    private String industry;
    private String geography;
    private String riskTier;
    private String auditor;
    private boolean soxApplicable;
    private boolean pciApplicable;
    private boolean gdprApplicable;
    private boolean fairLendingApplicable;

    private double complianceScore;
    private double securityScore;
    private double soxScore;
    private double regulatoryScore;
    private double contractualScore;
    private double auditReadiness;
    private double complianceDebtUsd;
    private int openRisks;
    private String trend;

    @Column(length = 2000)
    private String applicableFrameworks;

    @Column(length = 2000)
    private String contractualSlas;
}

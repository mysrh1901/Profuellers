package com.regulith.config;

import com.regulith.model.*;
import com.regulith.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds the H2 database with realistic data on startup.
 * Includes client profiles AND pre-existing findings so detail pages are populated.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ClientEngagementRepository clientRepo;
    private final ChainReactionResultRepository chainRepo;
    private final ComplianceEventRepository eventRepo;
    private final AuditNarrativeRepository narrativeRepo;

    @Override
    public void run(String... args) {
        log.info("[Data Seeder] Loading data...");

        // === CLIENT ENGAGEMENTS ===
        clientRepo.save(new ClientEngagement(
                "ENG-001", "MortgageFirst National Bank",
                "Mortgage / Financial Services", "United States (Multi-state)",
                "Tier 1", "EY (Ernst & Young)",
                true, true, false, true,
                87.2, 55.0, 83.3, 80.0, 70.0, 82.0, 1075000.0, 5,
                "STABLE",
                "SOX, SOC 2, PCI-DSS, TILA, RESPA, ECOA, HMDA, GLBA",
                "critical_vuln:48h, high_vuln:7d, incident_notify:4h, data_residency:US-only"
        ));

        clientRepo.save(new ClientEngagement(
                "ENG-002", "EuroLend Financial Group",
                "Mortgage / Financial Services", "EU (Germany, France, Netherlands)",
                "Tier 1", "Deloitte",
                true, false, true, false,
                80.5, 70.0, 86.7, 75.0, 85.0, 78.0, 725000.0, 2,
                "STABLE",
                "SOX, GDPR, DORA, PSD2, EBA Guidelines, ISO 27001",
                "critical_vuln:24h, high_vuln:72h, breach_notify:72h, dora_report:4h, data_residency:EU-only"
        ));

        clientRepo.save(new ClientEngagement(
                "ENG-003", "HomePath Insurance Corp",
                "Insurance / Mortgage Insurance", "United States",
                "Tier 2", "PwC",
                true, false, false, false,
                91.0, 88.0, 90.0, 92.0, 95.0, 85.0, 600000.0, 1,
                "IMPROVING",
                "SOC 2, SOX, NAIC Model Laws, CCPA, State Insurance Regulations",
                "critical_vuln:72h, high_vuln:14d, change_notice:48h, pen_test:semi-annual"
        ));

        // === PRE-EXISTING EVENTS (simulate history) ===
        ComplianceEvent evt1 = new ComplianceEvent();
        evt1.setEngagementId("ENG-001");
        evt1.setEventType("CODE_COMMIT");
        evt1.setSource("GitHub");
        evt1.setDescription("Modified loan interest calculation for Q2 rate adjustment");
        evt1.setTimestamp(LocalDateTime.now().minusDays(3));
        evt1.setProcessed(true);
        evt1.setPayload("{}");
        eventRepo.save(evt1);

        // === PRE-EXISTING CHAIN REACTIONS (the 5 open risks) ===
        seedChainReaction(evt1.getId(), "ENG-001", "SOX", "HIGH",
                "[FIN-SOX-001] Change to financially-significant system requires ITGC documentation",
                "Dual approval + Change ticket + Test evidence required",
                "ITGC-CM-01, ITGC-CM-02, ITGC-SD-01, SOX-404",
                "Document within 24 hours", false, 3);

        seedChainReaction(evt1.getId(), "ENG-001", "SECURITY", "HIGH",
                "[UNIV-SEC-001] Outdated Jackson library with deserialization vulnerability (CVE-2024-7254)",
                "Upgrade jackson-databind to 2.17.0+ — approaching 7-day SLA deadline",
                "OWASP-TOP10, CWE-502, PCI-DSS-6.5",
                "7 days (3 days remaining)", true, 3);

        seedChainReaction(evt1.getId(), "ENG-001", "REGULATORY", "HIGH",
                "[FIN-REG-001] APR calculation logic changed — TILA Regulation Z validation pending",
                "Validate APR calculation accuracy to 1/8 of 1 percent before next release",
                "TILA-REG-Z, 12CFR1026",
                "Before production deployment", true, 3);

        seedChainReaction(evt1.getId(), "ENG-001", "CONTRACTUAL", "MEDIUM",
                "[FIN-CON-001] MSA §7.4 requires 48h client notification for financial logic changes",
                "Client notification sent but deployment happened within 36 hours — near-miss",
                "MSA-CHANGE-NOTIFY",
                "48 hours before deployment", false, 3);

        seedChainReaction(evt1.getId(), "ENG-001", "FAIR_LENDING", "MEDIUM",
                "[FIN-FAIR-001] Rate logic change pending disparate impact analysis",
                "Fair lending team reviewing — expected completion in 2 days",
                "ECOA-REG-B, HMDA-REG-C",
                "Before production deployment", true, 3);

        // === PRE-EXISTING FINDINGS FOR ENG-002 ===
        ComplianceEvent evt2 = new ComplianceEvent();
        evt2.setEngagementId("ENG-002");
        evt2.setEventType("INFRA_CHANGE");
        evt2.setSource("AWS Config");
        evt2.setDescription("RDS backup cross-region replication detected");
        evt2.setTimestamp(LocalDateTime.now().minusDays(1));
        evt2.setProcessed(true);
        evt2.setPayload("{}");
        eventRepo.save(evt2);

        seedChainReaction(evt2.getId(), "ENG-002", "INFRASTRUCTURE", "CRITICAL",
                "[INFRA-DR-001] Data residency violation — RDS backup replicating to us-east-1",
                "Revert cross-region replication immediately — client contract requires EU-only",
                "GDPR-Art44, CONTRACTUAL-DATA-RESIDENCY",
                "Immediate", true, 1);

        seedChainReaction(evt2.getId(), "ENG-002", "PRIVACY", "HIGH",
                "[UNIV-PII-001] Personal data detected in non-compliant region",
                "Verify no EU citizen data was transferred, prepare incident assessment",
                "GDPR-Art33, GDPR-Art5",
                "72 hours (GDPR notification window)", false, 1);

        // === PRE-EXISTING FINDING FOR ENG-003 ===
        ComplianceEvent evt3 = new ComplianceEvent();
        evt3.setEngagementId("ENG-003");
        evt3.setEventType("CODE_COMMIT");
        evt3.setSource("GitHub");
        evt3.setDescription("Updated claims processing dependency versions");
        evt3.setTimestamp(LocalDateTime.now().minusDays(5));
        evt3.setProcessed(true);
        evt3.setPayload("{}");
        eventRepo.save(evt3);

        seedChainReaction(evt3.getId(), "ENG-003", "SECURITY", "MEDIUM",
                "[UNIV-SEC-001] lodash 4.17.20 prototype pollution — low risk, non-blocking",
                "Upgrade lodash to 4.17.25+ during next sprint",
                "CWE-1321, OWASP-A06",
                "14 days", false, 5);

        log.info("[Data Seeder] Loaded {} clients, {} events, {} chain reactions",
                clientRepo.count(), eventRepo.count(), chainRepo.count());
    }

    private void seedChainReaction(Long eventId, String engId, String domain,
            String severity, String reason, String action, String controls,
            String sla, boolean blocking, int daysAgo) {
        ChainReactionResult cr = new ChainReactionResult();
        cr.setEventId(eventId);
        cr.setEngagementId(engId);
        cr.setDomain(domain);
        cr.setSeverity(severity);
        cr.setReason(reason);
        cr.setActionRequired(action);
        cr.setControlsAffected(controls);
        cr.setSla(sla);
        cr.setBlocking(blocking);
        cr.setTimestamp(LocalDateTime.now().minusDays(daysAgo));
        chainRepo.save(cr);
    }
}

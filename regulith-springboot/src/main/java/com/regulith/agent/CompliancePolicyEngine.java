package com.regulith.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * COMPLIANCE POLICY ENGINE
 * ========================
 * This is the heart of Regulith AI's domain-agnostic design.
 * 
 * Policies define WHAT to check. The engine is the same regardless of industry.
 * To use Regulith in a new vertical (healthcare, retail, manufacturing),
 * you only add new policies here — zero code changes to the agents.
 * 
 * ARCHITECTURE:
 *   Policy = a rule that maps (trigger condition) → (compliance domain + action)
 *   Policies are loaded at startup and evaluated by the Chain Reactor Agent.
 * 
 * EXTENSIBILITY:
 *   - Mortgage: SOX, TILA, RESPA, ECOA, PCI-DSS, GLBA
 *   - Healthcare: HIPAA, HITECH, FDA 21 CFR Part 11
 *   - Retail: PCI-DSS, CCPA, GDPR, SOX
 *   - Manufacturing: ISO 27001, NIST, SOX, Export Controls
 *   - Public Sector: FedRAMP, FISMA, CMMC, ITAR
 * 
 * In production, policies would be loaded from a database or YAML config.
 * For this demo, they're defined in code for clarity.
 */
@Service
@Slf4j
public class CompliancePolicyEngine {

    private final List<CompliancePolicy> policies = new ArrayList<>();

    @PostConstruct
    public void loadPolicies() {
        // ================================================================
        // UNIVERSAL POLICIES (Apply to ALL verticals)
        // ================================================================
        
        policies.add(CompliancePolicy.builder()
                .id("UNIV-SEC-001")
                .domain("SECURITY")
                .name("SAST Finding Gate")
                .description("Block deployment when Critical/High SAST findings exist")
                .triggerCondition("sastHighCount > 0")
                .severity("HIGH")
                .action("Resolve all Critical/High findings before production deployment")
                .controls("OWASP-TOP10, CWE-TOP25")
                .sla("Before deployment")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("UNIV-SEC-002")
                .domain("SECURITY")
                .name("Secrets Detection")
                .description("Block when hardcoded secrets detected in code")
                .triggerCondition("secretsDetected == true")
                .severity("CRITICAL")
                .action("Remove hardcoded secrets, move to vault/secrets manager")
                .controls("CWE-798, OWASP-A07")
                .sla("Immediate (blocking)")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("UNIV-CHG-001")
                .domain("CHANGE_MANAGEMENT")
                .name("Segregation of Duties")
                .description("Code author cannot be the sole approver")
                .triggerCondition("eventType == 'CODE_COMMIT'")
                .severity("MEDIUM")
                .action("Verify PR was approved by someone other than the author")
                .controls("ITGC-CM-02, ISO27001-A.12.1.2")
                .sla("Before merge to main")
                .blocking(false)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("UNIV-AUD-001")
                .domain("AUDIT")
                .name("Change Traceability")
                .description("Every production change must have traceable business justification")
                .triggerCondition("eventType == 'CODE_COMMIT' || eventType == 'DEPLOYMENT'")
                .severity("MEDIUM")
                .action("Generate audit narrative linking commit to ticket to approval to test evidence")
                .controls("ITGC-CM-01, SOC2-CC8.1")
                .sla("Within 24 hours of event")
                .blocking(false)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("UNIV-PII-001")
                .domain("PRIVACY")
                .name("PII Processing Detection")
                .description("Flag changes that introduce or modify PII handling")
                .triggerCondition("touchesPii == true")
                .severity("HIGH")
                .action("Verify data minimization, consent, encryption at rest/transit")
                .controls("GDPR-Art5, CCPA-1798.100, GLBA-Safeguards")
                .sla("Before deployment")
                .blocking(false)
                .verticals(List.of("ALL"))
                .build());

        // ================================================================
        // FINANCIAL SERVICES POLICIES (Mortgage, Banking, Insurance)
        // ================================================================

        policies.add(CompliancePolicy.builder()
                .id("FIN-SOX-001")
                .domain("SOX")
                .name("SOX ITGC - Financial System Change")
                .description("Changes to financially-significant systems require full ITGC documentation")
                .triggerCondition("touchesFinancialLogic == true && soxApplicable == true")
                .severity("HIGH")
                .action("Dual approval + Change ticket + Test evidence + Rollback plan")
                .controls("ITGC-CM-01, ITGC-CM-02, ITGC-SD-01, SOX-404")
                .sla("Document within 24 hours")
                .blocking(false)
                .verticals(List.of("FINANCIAL_SERVICES", "INSURANCE", "MORTGAGE"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("FIN-REG-001")
                .domain("REGULATORY")
                .name("TILA Rate Accuracy")
                .description("Rate/APR calculation changes require Regulation Z validation")
                .triggerCondition("touchesFinancialLogic == true && frameworks.contains('TILA')")
                .severity("HIGH")
                .action("Validate APR calculation accuracy to 1/8 of 1 percent")
                .controls("TILA-REG-Z, 12CFR1026")
                .sla("Before production deployment")
                .blocking(true)
                .verticals(List.of("MORTGAGE", "CONSUMER_LENDING"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("FIN-FAIR-001")
                .domain("FAIR_LENDING")
                .name("Disparate Impact Analysis")
                .description("Pricing or eligibility logic changes need fair lending review")
                .triggerCondition("touchesFinancialLogic == true && fairLendingApplicable == true")
                .severity("MEDIUM")
                .action("Run disparate impact testing against protected classes")
                .controls("ECOA-REG-B, HMDA-REG-C")
                .sla("Before production deployment")
                .blocking(true)
                .verticals(List.of("MORTGAGE", "CONSUMER_LENDING", "INSURANCE"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("FIN-CON-001")
                .domain("CONTRACTUAL")
                .name("Client Change Notification")
                .description("Client MSA requires advance notification for financial system changes")
                .triggerCondition("touchesFinancialLogic == true")
                .severity("MEDIUM")
                .action("Notify client per MSA notification clause before deployment")
                .controls("MSA-CHANGE-NOTIFY")
                .sla("48 hours before production deployment")
                .blocking(false)
                .verticals(List.of("FINANCIAL_SERVICES", "MORTGAGE", "INSURANCE"))
                .build());

        // ================================================================
        // SOX ITGC — ACCESS MANAGEMENT CONTROLS
        // (Per SOX ITGC Testing Framework)
        // Evidence: Access tickets, approvals, AD logs, role matrix, HR docs
        // ================================================================

        policies.add(CompliancePolicy.builder()
                .id("ITGC-AM-01")
                .domain("ACCESS_CONTROL")
                .name("User Provisioning — Manager Approval Required")
                .description("Access granted only after manager approval. Verify request, approval, role and provisioning timing.")
                .triggerCondition("eventType == 'ACCESS_CHANGE'")
                .severity("HIGH")
                .action("Verify access request ticket with manager approval before provisioning. Evidence: access ticket, approval, user access report, role matrix, HR onboarding docs.")
                .controls("ITGC-AM-01, SOX-404, ISO27001-A.9.2.2")
                .sla("Before access is granted")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("ITGC-AM-02")
                .domain("ACCESS_CONTROL")
                .name("Access Modification — Approved Before Implementation")
                .description("Changes to access/roles approved before implementation. Verify request, approval and updated role.")
                .triggerCondition("eventType == 'ACCESS_CHANGE'")
                .severity("HIGH")
                .action("Verify modification ticket and approval exist BEFORE change. Evidence: modification ticket, approval, access report, HR transfer doc.")
                .controls("ITGC-AM-02, SOX-404, ISO27001-A.9.2.5")
                .sla("Before access modification")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("ITGC-AM-03")
                .domain("ACCESS_CONTROL")
                .name("Access Removal — Timely Termination")
                .description("Terminate access within defined timeframe. Compare termination date vs disable date.")
                .triggerCondition("eventType == 'ACCESS_CHANGE'")
                .severity("CRITICAL")
                .action("Compare HR termination date against AD/IAM disable date. Flag gap > SLA. Evidence: HR termination report, AD logs, active user list, IAM logs.")
                .controls("ITGC-AM-03, SOX-404, ISO27001-A.9.2.6")
                .sla("Within 4 hours of termination")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("ITGC-AM-04")
                .domain("ACCESS_CONTROL")
                .name("Privileged Access — Justified and Approved")
                .description("Admin access requires documented justification and approval. No standing admin without business need.")
                .triggerCondition("eventType == 'ACCESS_CHANGE'")
                .severity("CRITICAL")
                .action("Verify privileged access request has justification + manager + security approval. Evidence: admin request, approval, privileged access report.")
                .controls("ITGC-AM-04, SOX-404, PCI-DSS-7.1, ISO27001-A.9.4.4")
                .sla("Before privileged access is granted")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("ITGC-AM-05")
                .domain("ACCESS_CONTROL")
                .name("Quarterly Access Review — Periodic Certification")
                .description("Periodic review performed. Verify sign-off and remediation of excess access.")
                .triggerCondition("eventType == 'ACCESS_CHANGE' || eventType == 'DEPLOYMENT'")
                .severity("HIGH")
                .action("Verify quarterly review completed with system owner sign-off. Remediate excess access within 5 days. Evidence: user listing, signed review, certification email.")
                .controls("ITGC-AM-05, SOX-404, PCI-DSS-7.1.2")
                .sla("Quarterly (every 90 days)")
                .blocking(false)
                .verticals(List.of("ALL"))
                .build());

        // ================================================================
        // SOX ITGC — CHANGE MANAGEMENT CONTROLS
        // (Per SOX ITGC Testing Framework)
        // Evidence: Change tickets, approvals, test plans, deployment logs
        // ================================================================

        policies.add(CompliancePolicy.builder()
                .id("ITGC-CM-01A")
                .domain("CHANGE_MANAGEMENT")
                .name("Normal Change Approval — Business/Technical/CAB")
                .description("Changes approved before deployment. Verify business/technical/CAB approvals.")
                .triggerCondition("eventType == 'CODE_COMMIT' || eventType == 'DEPLOYMENT'")
                .severity("HIGH")
                .action("Verify change ticket has business approval + technical review + CAB sign-off. Evidence: change ticket, approvals, CAB minutes.")
                .controls("ITGC-CM-01, SOX-404, ITIL-CHG")
                .sla("Before production deployment")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("ITGC-CM-03A")
                .domain("CHANGE_MANAGEMENT")
                .name("Change Testing — UAT/QA Before Production")
                .description("Changes tested before production. Verify UAT/QA and signoff.")
                .triggerCondition("eventType == 'DEPLOYMENT'")
                .severity("HIGH")
                .action("Verify test plan exists, UAT executed, QA sign-off obtained before deploy. Evidence: test plan, UAT results, QA signoff, Jira.")
                .controls("ITGC-CM-03, ITGC-SD-01, SOX-404")
                .sla("Before production deployment")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("ITGC-CM-02A")
                .domain("CHANGE_MANAGEMENT")
                .name("Segregation of Duties — Dev Cannot Deploy to Prod")
                .description("Developers cannot deploy to production. Verify deployment permissions separated from development.")
                .triggerCondition("eventType == 'DEPLOYMENT'")
                .severity("CRITICAL")
                .action("Verify deployer != developer. Check role matrix and prod access report. Evidence: role matrix, prod access report, deployment logs.")
                .controls("ITGC-CM-02, SOX-404, PCI-DSS-6.4.2")
                .sla("Before deployment (blocking)")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("ITGC-CM-04")
                .domain("CHANGE_MANAGEMENT")
                .name("Production Migration — Only Approved/Tested Changes")
                .description("Only approved and tested changes deployed. Verify deployment logs match approved change.")
                .triggerCondition("eventType == 'DEPLOYMENT'")
                .severity("CRITICAL")
                .action("Cross-reference deployment log against approved ticket. Version deployed must match tested version. Evidence: deployment logs, Azure DevOps/Jenkins, release record.")
                .controls("ITGC-CM-04, SOX-404, ITIL-Release")
                .sla("At time of deployment (blocking)")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("ITGC-CM-05")
                .domain("CHANGE_MANAGEMENT")
                .name("Emergency Changes — Retrospective Review")
                .description("Emergency changes reviewed after implementation. Verify retrospective CAB approval.")
                .triggerCondition("eventType == 'DEPLOYMENT'")
                .severity("HIGH")
                .action("For emergency changes: verify retrospective CAB review within 5 business days. Evidence: emergency ticket, incident record, CAB review minutes.")
                .controls("ITGC-CM-05, SOX-404, ITIL-Emergency")
                .sla("Retrospective review within 5 business days")
                .blocking(false)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("ITGC-CM-06")
                .domain("CHANGE_MANAGEMENT")
                .name("Code Review — Independent Review Before Merge")
                .description("Independent code review before merge. Verify PR approval by reviewer != author.")
                .triggerCondition("eventType == 'CODE_COMMIT'")
                .severity("HIGH")
                .action("Verify PR/MR has approval from reviewer different from author. No self-merged PRs. Evidence: GitHub/GitLab PR, reviewer approval.")
                .controls("ITGC-CM-06, PCI-DSS-6.3.2, SOX-404")
                .sla("Before merge to main")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        // ================================================================
        // HEALTHCARE POLICIES
        // ================================================================

        policies.add(CompliancePolicy.builder()
                .id("HC-HIPAA-001")
                .domain("PRIVACY")
                .name("PHI Access Control")
                .description("Changes to systems handling Protected Health Information")
                .triggerCondition("touchesPii == true && frameworks.contains('HIPAA')")
                .severity("CRITICAL")
                .action("Verify minimum necessary access, audit logging, encryption per HIPAA Security Rule")
                .controls("HIPAA-164.312, HITECH-13402")
                .sla("Before deployment (blocking)")
                .blocking(true)
                .verticals(List.of("HEALTHCARE", "PHARMA", "HEALTH_INSURANCE"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("HC-FDA-001")
                .domain("REGULATORY")
                .name("FDA Validation Required")
                .description("Changes to clinical/drug systems require 21 CFR Part 11 validation")
                .triggerCondition("touchesClinicalLogic == true && frameworks.contains('FDA')")
                .severity("CRITICAL")
                .action("Execute IQ/OQ/PQ validation protocol before production use")
                .controls("21CFR11, FDA-CSV")
                .sla("Before deployment (blocking)")
                .blocking(true)
                .verticals(List.of("PHARMA", "MEDICAL_DEVICES"))
                .build());

        // ================================================================
        // INFRASTRUCTURE POLICIES (All verticals)
        // ================================================================

        policies.add(CompliancePolicy.builder()
                .id("INFRA-DR-001")
                .domain("INFRASTRUCTURE")
                .name("Data Residency Violation")
                .description("Data moved outside contractually-specified region")
                .triggerCondition("eventType == 'INFRA_CHANGE' && dataResidencyViolation == true")
                .severity("CRITICAL")
                .action("Revert configuration, ensure data remains in specified region")
                .controls("GDPR-Art44, CONTRACTUAL-DATA-RESIDENCY")
                .sla("Immediate (blocking)")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("INFRA-ACC-001")
                .domain("ACCESS_CONTROL")
                .name("Privileged Access Change")
                .description("IAM role or permission changes to production systems")
                .triggerCondition("eventType == 'ACCESS_CHANGE'")
                .severity("HIGH")
                .action("Verify least privilege principle, log access justification")
                .controls("ITGC-AC-01, ISO27001-A.9, NIST-AC-6")
                .sla("Review within 4 hours")
                .blocking(false)
                .verticals(List.of("ALL"))
                .build());

        // ================================================================
        // DEPLOYMENT PIPELINE POLICIES
        // ================================================================

        policies.add(CompliancePolicy.builder()
                .id("PIPE-DEP-001")
                .domain("DEPLOYMENT")
                .name("Selective Deployment Compliance")
                .description("Only approved microservices can deploy to production")
                .triggerCondition("eventType == 'DEPLOYMENT' && targetEnv == 'production'")
                .severity("HIGH")
                .action("Verify service is in approved deployment manifest, all gates passed")
                .controls("ITGC-CM-03, CHANGE-APPROVAL")
                .sla("Before deployment (blocking)")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        policies.add(CompliancePolicy.builder()
                .id("PIPE-DEP-002")
                .domain("DEPLOYMENT")
                .name("Container Image Scan")
                .description("Docker images must pass vulnerability scan before deploy")
                .triggerCondition("eventType == 'DEPLOYMENT' && containerized == true")
                .severity("HIGH")
                .action("Run container image scan, block if Critical CVEs found")
                .controls("CIS-DOCKER, NIST-SI-2")
                .sla("Before deployment (blocking)")
                .blocking(true)
                .verticals(List.of("ALL"))
                .build());

        log.info("[Policy Engine] Loaded {} compliance policies across {} verticals",
                policies.size(), getUniqueVerticals().size());
    }

    /**
     * Evaluate all applicable policies against an event.
     * Returns the list of policies that are triggered.
     */
    public List<CompliancePolicy> evaluate(Map<String, Object> eventContext) {
        List<CompliancePolicy> triggered = new ArrayList<>();
        String clientVertical = (String) eventContext.getOrDefault("vertical", "ALL");
        
        for (CompliancePolicy policy : policies) {
            if (isApplicable(policy, clientVertical) && isTriggered(policy, eventContext)) {
                triggered.add(policy);
            }
        }
        return triggered;
    }

    /**
     * Check if a policy applies to the given vertical.
     */
    private boolean isApplicable(CompliancePolicy policy, String vertical) {
        if (policy.getVerticals().contains("ALL")) return true;
        // Fuzzy match: check if any vertical keyword appears in the client's industry
        String industryLower = vertical.toLowerCase();
        for (String v : policy.getVerticals()) {
            String vLower = v.toLowerCase().replace("_", " ");
            if (industryLower.contains(vLower) || vLower.contains(industryLower.split("/")[0].trim())) {
                return true;
            }
        }
        // Also match exact
        return policy.getVerticals().contains(vertical);
    }

    /**
     * Evaluate if a policy's trigger condition is met by the event context.
     * In production, this would use a proper expression engine (SpEL or MVEL).
     * For demo clarity, using direct condition evaluation.
     */
    private boolean isTriggered(CompliancePolicy policy, Map<String, Object> context) {
        String condition = policy.getTriggerCondition();

        // SAST findings check
        if (condition.contains("sastHighCount > 0")) {
            return getInt(context, "sastHighCount") > 0;
        }
        // Secrets detected
        if (condition.contains("secretsDetected == true")) {
            return getBool(context, "secretsDetected");
        }
        // Financial logic conditions (SOX, TILA, Fair Lending, Contractual)
        if (condition.contains("touchesFinancialLogic == true")) {
            if (!getBool(context, "touchesFinancialLogic")) return false;
            // Now check additional qualifiers
            if (condition.contains("soxApplicable == true")) return getBool(context, "soxApplicable");
            if (condition.contains("fairLendingApplicable == true")) return getBool(context, "fairLendingApplicable");
            if (condition.contains("frameworks.contains")) return checkFramework(condition, context);
            return true; // Just touchesFinancialLogic with no qualifier
        }
        // PII conditions
        if (condition.contains("touchesPii == true")) {
            if (!getBool(context, "touchesPii")) return false;
            if (condition.contains("frameworks.contains")) return checkFramework(condition, context);
            return true;
        }
        // Clinical logic (healthcare)
        if (condition.contains("touchesClinicalLogic == true")) {
            if (!getBool(context, "touchesClinicalLogic")) return false;
            if (condition.contains("frameworks.contains")) return checkFramework(condition, context);
            return true;
        }
        // Event type based conditions
        if (condition.contains("eventType ==") || condition.contains("eventType ==")) {
            String actualType = (String) context.getOrDefault("eventType", "");
            // Check if any of the event types in the condition match
            if (condition.contains("CODE_COMMIT") && "CODE_COMMIT".equals(actualType)) {
                if (condition.contains("targetEnv")) return "production".equals(context.get("targetEnv"));
                if (condition.contains("containerized")) return getBool(context, "containerized");
                if (condition.contains("dataResidencyViolation")) return getBool(context, "dataResidencyViolation");
                return true;
            }
            if (condition.contains("DEPLOYMENT") && "DEPLOYMENT".equals(actualType)) {
                if (condition.contains("targetEnv == 'production'")) return "production".equals(context.get("targetEnv"));
                if (condition.contains("containerized == true")) return getBool(context, "containerized");
                return true;
            }
            if (condition.contains("INFRA_CHANGE") && "INFRA_CHANGE".equals(actualType)) {
                if (condition.contains("dataResidencyViolation")) return getBool(context, "dataResidencyViolation");
                return true;
            }
            if (condition.contains("ACCESS_CHANGE") && "ACCESS_CHANGE".equals(actualType)) {
                return true;
            }
            if (condition.contains("TICKET_CHANGE") && "TICKET_CHANGE".equals(actualType)) {
                return true;
            }
            // Handle OR conditions (||)
            if (condition.contains("||")) {
                return condition.contains(actualType);
            }
        }
        return false;
    }

    private boolean checkFramework(String condition, Map<String, Object> context) {
        String framework = condition.split("'")[1];
        String frameworks = (String) context.getOrDefault("frameworks", "");
        return frameworks.contains(framework);
    }

    private boolean getBool(Map<String, Object> ctx, String key) {
        Object val = ctx.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        return false;
    }

    private int getInt(Map<String, Object> ctx, String key) {
        Object val = ctx.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return 0;
    }

    public List<CompliancePolicy> getAllPolicies() {
        return policies;
    }

    public Set<String> getUniqueVerticals() {
        Set<String> verticals = new HashSet<>();
        policies.forEach(p -> verticals.addAll(p.getVerticals()));
        return verticals;
    }

    // ========================================
    // Policy Data Structure
    // ========================================
    @Data
    @lombok.Builder
    public static class CompliancePolicy {
        private String id;
        private String domain;
        private String name;
        private String description;
        private String triggerCondition;
        private String severity;
        private String action;
        private String controls;
        private String sla;
        private boolean blocking;
        private List<String> verticals;
    }
}

package com.regulith.agents.model;

import java.util.List;
import java.util.Map;

/**
 * Client compliance profile — defines what frameworks and obligations
 * apply to this specific client. The agents use this to ask the LLM
 * the right questions. NOT hardcoded logic — just context for the AI.
 */
public class ClientProfile {

    private String clientId;
    private String clientName;
    private String industry;            // Mortgage, Healthcare, Insurance, Retail, etc.
    private String geography;           // US, EU, APAC — affects which regulations apply
    private List<String> frameworks;    // SOX, TILA, GDPR, HIPAA, PCI-DSS, etc.
    private Map<String, String> slas;   // Contractual SLAs (e.g., "critical_vuln": "48h")
    private String auditor;             // External auditor name
    private String riskTier;            // Tier 1, Tier 2, Tier 3

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private ClientProfile p = new ClientProfile();
        public Builder clientId(String v) { p.clientId = v; return this; }
        public Builder clientName(String v) { p.clientName = v; return this; }
        public Builder industry(String v) { p.industry = v; return this; }
        public Builder geography(String v) { p.geography = v; return this; }
        public Builder frameworks(List<String> v) { p.frameworks = v; return this; }
        public Builder slas(Map<String, String> v) { p.slas = v; return this; }
        public Builder auditor(String v) { p.auditor = v; return this; }
        public Builder riskTier(String v) { p.riskTier = v; return this; }
        public ClientProfile build() { return p; }
    }

    public String getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public String getIndustry() { return industry; }
    public String getGeography() { return geography; }
    public List<String> getFrameworks() { return frameworks; }
    public Map<String, String> getSlas() { return slas; }
    public String getAuditor() { return auditor; }
    public String getRiskTier() { return riskTier; }

    /**
     * Convert profile to context string for LLM prompt.
     */
    public String toPromptContext() {
        return String.format(
            "Client: %s | Industry: %s | Geography: %s | Frameworks: %s | Risk Tier: %s",
            clientName, industry, geography, String.join(", ", frameworks), riskTier
        );
    }
}

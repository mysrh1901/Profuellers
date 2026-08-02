package com.regulith.agents.model;

import java.util.List;

/**
 * Output of the ComplianceReasonerAgent.
 * Contains the LLM's reasoning about which domains are affected and why.
 */
public class ReasoningResult {

    private List<DomainImpact> impacts;
    private int domainsAffected;
    private int blockingIssues;
    private String overallAssessment;
    private String rawLLMResponse;
    private boolean deploymentAllowed;

    public static class DomainImpact {
        private String domain;
        private String severity;       // CRITICAL, HIGH, MEDIUM, LOW
        private String reasoning;      // WHY this domain is affected (LLM's explanation)
        private String regulation;     // Specific regulation reference
        private String actionRequired;
        private boolean blocking;

        public DomainImpact(String domain, String severity, String reasoning,
                           String regulation, String actionRequired, boolean blocking) {
            this.domain = domain;
            this.severity = severity;
            this.reasoning = reasoning;
            this.regulation = regulation;
            this.actionRequired = actionRequired;
            this.blocking = blocking;
        }

        public String getDomain() { return domain; }
        public String getSeverity() { return severity; }
        public String getReasoning() { return reasoning; }
        public String getRegulation() { return regulation; }
        public String getActionRequired() { return actionRequired; }
        public boolean isBlocking() { return blocking; }
    }

    public ReasoningResult(List<DomainImpact> impacts, String overallAssessment,
                          boolean deploymentAllowed, String rawLLMResponse) {
        this.impacts = impacts;
        this.overallAssessment = overallAssessment;
        this.deploymentAllowed = deploymentAllowed;
        this.rawLLMResponse = rawLLMResponse;
        this.domainsAffected = impacts.size();
        this.blockingIssues = (int) impacts.stream().filter(DomainImpact::isBlocking).count();
    }

    public List<DomainImpact> getImpacts() { return impacts; }
    public int getDomainsAffected() { return domainsAffected; }
    public int getBlockingIssues() { return blockingIssues; }
    public String getOverallAssessment() { return overallAssessment; }
    public String getRawLLMResponse() { return rawLLMResponse; }
    public boolean isDeploymentAllowed() { return deploymentAllowed; }
}

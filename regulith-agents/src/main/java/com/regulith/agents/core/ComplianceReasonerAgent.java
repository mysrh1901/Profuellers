package com.regulith.agents.core;

import com.regulith.agents.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * COMPLIANCE REASONER AGENT
 * ==========================
 * The primary reasoning agent. ZERO hardcoded rules.
 *
 * How it works:
 *   1. Receives a ComplianceEvent (code diff, description, client profile)
 *   2. Constructs a prompt with full context
 *   3. Sends to LLM (Claude/GPT/Llama — any provider)
 *   4. LLM reads the code, understands the change, reasons about compliance impact
 *   5. Agent parses LLM response into structured ReasoningResult
 *
 * The AI does ALL the reasoning. The agent only orchestrates.
 * No if-else. No pattern matching. No regex. Pure LLM intelligence.
 *
 * UNIQUE: No product on the market uses an LLM to perform cross-domain
 * compliance reasoning on code diffs with client-specific context.
 *
 * @author Imam Sayyad
 * @version 1.0.0
 */
public class ComplianceReasonerAgent {

    private final LLMProvider llm;

    private static final String SYSTEM_PROMPT = """
        You are an expert compliance analyst for an IT services delivery company.
        You analyze code changes and infrastructure events to determine compliance impact
        across multiple regulatory domains simultaneously.
        
        You must evaluate the following 8 compliance domains for every event:
        1. SOX (Sarbanes-Oxley) — change management, segregation of duties, ITGC
        2. SECURITY — application security, vulnerabilities, secure coding
        3. REGULATORY — industry-specific regulations (TILA, RESPA, HIPAA, etc.)
        4. FAIR_LENDING — discrimination risk in pricing or eligibility (ECOA, HMDA)
        5. CONTRACTUAL — client MSA/SOW obligations, SLA timers, notification requirements
        6. PRIVACY — PII handling, consent, data residency (GDPR, CCPA, GLBA)
        7. INFRASTRUCTURE — cloud configuration, access control, encryption
        8. AUDIT — evidence trail, documentation completeness
        
        For each affected domain, provide:
        - Domain name
        - Severity (CRITICAL, HIGH, MEDIUM, LOW)
        - Reasoning (WHY this domain is affected by this specific change)
        - Specific regulation or control reference
        - Required action
        - Whether it should BLOCK deployment (true/false)
        
        Respond in structured format. Be specific. Reference actual regulations.
        Only flag domains that are genuinely affected — do not flag everything.
        """;

    public ComplianceReasonerAgent(LLMProvider llm) {
        this.llm = llm;
    }

    /**
     * Analyze a compliance event. The LLM does all reasoning.
     * No hardcoded rules. The AI reads the code and decides.
     */
    public ReasoningResult analyze(ComplianceEvent event) {
        String prompt = buildPrompt(event);
        String llmResponse = llm.call(prompt);
        return parseResponse(llmResponse, event);
    }

    /**
     * Build the prompt that gives the LLM full context to reason.
     * The quality of this prompt IS the intelligence of the agent.
     */
    private String buildPrompt(ComplianceEvent event) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_PROMPT).append("\n\n");
        prompt.append("=== EVENT DETAILS ===\n");
        prompt.append("Event Type: ").append(event.getEventType()).append("\n");
        prompt.append("Source: ").append(event.getSource()).append("\n");
        prompt.append("Author: ").append(event.getAuthor()).append("\n");
        prompt.append("Description: ").append(event.getDescription()).append("\n\n");

        if (event.getCodeDiff() != null && !event.getCodeDiff().isEmpty()) {
            prompt.append("=== CODE DIFF ===\n");
            prompt.append(event.getCodeDiff()).append("\n\n");
        }

        if (event.getClientProfile() != null) {
            prompt.append("=== CLIENT COMPLIANCE PROFILE ===\n");
            prompt.append(event.getClientProfile().toPromptContext()).append("\n");
            if (event.getClientProfile().getSlas() != null) {
                prompt.append("SLAs: ").append(event.getClientProfile().getSlas()).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("=== YOUR TASK ===\n");
        prompt.append("Analyze this event against all 8 compliance domains.\n");
        prompt.append("For each AFFECTED domain, provide: domain, severity, reasoning, regulation, action, blocking.\n");
        prompt.append("Only include domains that are genuinely impacted by this specific change.\n");
        prompt.append("At the end, state: DEPLOYMENT_ALLOWED: true/false\n");

        return prompt.toString();
    }

    /**
     * Parse LLM response into structured result.
     * In production, use structured output (JSON mode) from the LLM.
     * This parser handles free-text responses.
     */
    private ReasoningResult parseResponse(String llmResponse, ComplianceEvent event) {
        List<ReasoningResult.DomainImpact> impacts = new ArrayList<>();
        boolean deploymentAllowed = true;

        // Parse the LLM response for domain impacts
        String[] lines = llmResponse.split("\n");
        String currentDomain = null;
        String currentSeverity = null;
        String currentReasoning = null;
        String currentRegulation = null;
        String currentAction = null;
        boolean currentBlocking = false;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.toUpperCase().contains("DOMAIN:") || line.startsWith("1.") ||
                line.startsWith("2.") || line.startsWith("3.") || line.startsWith("4.") ||
                line.startsWith("5.") || line.startsWith("6.") || line.startsWith("7.") ||
                line.startsWith("8.")) {
                // Save previous if exists
                if (currentDomain != null) {
                    impacts.add(new ReasoningResult.DomainImpact(
                        currentDomain, currentSeverity, currentReasoning,
                        currentRegulation, currentAction, currentBlocking));
                    if (currentBlocking) deploymentAllowed = false;
                }
                // Extract domain name
                currentDomain = extractValue(line, "DOMAIN", "SOX", "SECURITY", "REGULATORY",
                    "FAIR_LENDING", "CONTRACTUAL", "PRIVACY", "INFRASTRUCTURE", "AUDIT");
                currentSeverity = "MEDIUM";
                currentReasoning = "";
                currentRegulation = "";
                currentAction = "";
                currentBlocking = false;
            }
            if (line.toUpperCase().contains("SEVERITY:"))
                currentSeverity = extractSeverity(line);
            if (line.toUpperCase().contains("REASONING:") || line.toUpperCase().contains("REASON:"))
                currentReasoning = line.substring(line.indexOf(":") + 1).trim();
            if (line.toUpperCase().contains("REGULATION:") || line.toUpperCase().contains("REFERENCE:"))
                currentRegulation = line.substring(line.indexOf(":") + 1).trim();
            if (line.toUpperCase().contains("ACTION:"))
                currentAction = line.substring(line.indexOf(":") + 1).trim();
            if (line.toUpperCase().contains("BLOCKING:") || line.toUpperCase().contains("BLOCK:"))
                currentBlocking = line.toUpperCase().contains("TRUE") || line.toUpperCase().contains("YES");
            if (line.toUpperCase().contains("DEPLOYMENT_ALLOWED: FALSE"))
                deploymentAllowed = false;
        }

        // Save last domain
        if (currentDomain != null) {
            impacts.add(new ReasoningResult.DomainImpact(
                currentDomain, currentSeverity, currentReasoning,
                currentRegulation, currentAction, currentBlocking));
            if (currentBlocking) deploymentAllowed = false;
        }

        String assessment = deploymentAllowed ?
            "Deployment ALLOWED — no blocking compliance issues found" :
            "Deployment BLOCKED — " + impacts.stream().filter(ReasoningResult.DomainImpact::isBlocking).count() + " blocking issues require resolution";

        return new ReasoningResult(impacts, assessment, deploymentAllowed, llmResponse);
    }

    private String extractValue(String line, String... keywords) {
        String upper = line.toUpperCase();
        for (String kw : keywords) {
            if (upper.contains(kw)) return kw;
        }
        return line.replaceAll("[^A-Za-z_]", "").toUpperCase();
    }

    private String extractSeverity(String line) {
        String upper = line.toUpperCase();
        if (upper.contains("CRITICAL")) return "CRITICAL";
        if (upper.contains("HIGH")) return "HIGH";
        if (upper.contains("MEDIUM")) return "MEDIUM";
        return "LOW";
    }
}

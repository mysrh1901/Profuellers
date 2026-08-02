package com.regulith.agents.core;

import com.regulith.agents.model.*;

import java.util.List;

/**
 * DRIFT SENTINEL AGENT
 * =====================
 * Detects silent compliance degradation — things that drift
 * out of compliance without any explicit event triggering them.
 *
 * How it works:
 *   1. Accepts a snapshot of current system state (infra config, access list, etc.)
 *   2. Sends to LLM along with expected compliance baseline
 *   3. LLM identifies gaps between actual and expected state
 *   4. Returns drift findings with severity and remediation
 *
 * Examples it catches:
 *   - SSL cert expiring in 5 days (nobody noticed)
 *   - IAM role from 45 days ago still active (should have been deleted)
 *   - Logging disabled (8-day audit gap)
 *   - Backup retention below contractual requirement
 *   - Dependencies with known CVEs unpatched for 20+ days
 *
 * NOT event-driven. Runs on a schedule (e.g., every hour, daily).
 *
 * @author Imam Sayyad
 * @version 1.0.0
 */
public class DriftSentinelAgent {

    private final LLMProvider llm;

    private static final String DRIFT_PROMPT = """
        You are a compliance drift detection analyst.
        You compare the EXPECTED compliance state against the ACTUAL state
        and identify gaps that have silently developed.
        
        For each drift found, provide:
        - Title (what drifted)
        - Severity (CRITICAL, HIGH, MEDIUM, LOW)
        - Expected state (what should be true)
        - Actual state (what is actually true)
        - How long it has been drifting
        - Compliance impact (which regulations/controls are affected)
        - Remediation action
        - Can it be auto-fixed? (yes/no)
        
        Only report real drifts. Do not flag things that are within acceptable tolerance.
        """;

    public DriftSentinelAgent(LLMProvider llm) {
        this.llm = llm;
    }

    /**
     * Scan for drift between expected and actual compliance state.
     * LLM does the comparison and reasoning.
     */
    public String detectDrift(String expectedState, String actualState, ClientProfile client) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(DRIFT_PROMPT).append("\n\n");

        prompt.append("=== CLIENT CONTEXT ===\n");
        prompt.append(client.toPromptContext()).append("\n\n");

        prompt.append("=== EXPECTED COMPLIANCE STATE ===\n");
        prompt.append(expectedState).append("\n\n");

        prompt.append("=== ACTUAL CURRENT STATE ===\n");
        prompt.append(actualState).append("\n\n");

        prompt.append("=== FIND ALL DRIFTS ===\n");
        prompt.append("Compare expected vs actual. Report every compliance drift found.\n");

        return llm.call(prompt.toString());
    }

    /**
     * Quick scan — checks a specific area for drift.
     */
    public String checkArea(String area, String currentConfig, ClientProfile client) {
        String prompt = DRIFT_PROMPT + "\n\n" +
            "Client: " + client.toPromptContext() + "\n\n" +
            "Area being checked: " + area + "\n\n" +
            "Current configuration:\n" + currentConfig + "\n\n" +
            "Is this configuration compliant? If not, what drifted and what's the risk?\n";

        return llm.call(prompt);
    }
}

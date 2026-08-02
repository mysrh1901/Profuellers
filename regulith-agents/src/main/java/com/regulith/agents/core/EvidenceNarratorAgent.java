package com.regulith.agents.core;

import com.regulith.agents.model.*;
import com.regulith.agents.graph.CausalPath;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EVIDENCE NARRATOR AGENT
 * ========================
 * Generates audit-grade evidence narratives using LLM.
 * NOT a template filler. The LLM writes the narrative like a human auditor would.
 *
 * What makes it unique:
 *   - Generates narrative in real-time (not assembled weeks later)
 *   - Grounded in Knowledge Graph causal paths (not hallucinated)
 *   - Includes specific regulation references from graph traversal
 *   - Writes in professional audit language suitable for external auditors
 *   - Every narrative is traceable to source events and evidence artifacts
 *
 * @author Imam Sayyad
 * @version 1.0.0
 */
public class EvidenceNarratorAgent {

    private final LLMProvider llm;

    private static final String NARRATOR_PROMPT = """
        You are a senior internal audit evidence writer.
        Your narratives are reviewed by external auditors (Big 4 firms).
        
        Write a complete audit evidence narrative for the following event.
        The narrative must be:
        - Factual and precise (no opinions, only verifiable statements)
        - Structured (numbered sections)
        - Reference specific controls and regulations
        - Include the causal compliance path showing WHY domains are affected
        - State which controls are SATISFIED and which have GAPS
        - Suitable for direct inclusion in audit workpapers
        
        Use formal audit language. This is a legal document.
        """;

    public EvidenceNarratorAgent(LLMProvider llm) {
        this.llm = llm;
    }

    /**
     * Generate an audit narrative for an event.
     * LLM writes it — agent only provides context.
     */
    public String generate(ComplianceEvent event, ReasoningResult reasoning, List<CausalPath> causalPaths) {
        String prompt = buildPrompt(event, reasoning, causalPaths);
        return llm.call(prompt);
    }

    private String buildPrompt(ComplianceEvent event, ReasoningResult reasoning, List<CausalPath> causalPaths) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(NARRATOR_PROMPT).append("\n\n");

        prompt.append("=== EVENT ===\n");
        prompt.append("Type: ").append(event.getEventType()).append("\n");
        prompt.append("Source: ").append(event.getSource()).append("\n");
        prompt.append("Author: ").append(event.getAuthor()).append("\n");
        prompt.append("Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        prompt.append("Description: ").append(event.getDescription()).append("\n");
        if (event.getCodeDiff() != null) {
            prompt.append("Code Diff:\n").append(event.getCodeDiff()).append("\n");
        }
        prompt.append("\n");

        if (event.getClientProfile() != null) {
            prompt.append("=== CLIENT ===\n");
            prompt.append(event.getClientProfile().toPromptContext()).append("\n\n");
        }

        prompt.append("=== COMPLIANCE IMPACT (from Reasoner Agent) ===\n");
        prompt.append("Domains Affected: ").append(reasoning.getDomainsAffected()).append("\n");
        prompt.append("Blocking Issues: ").append(reasoning.getBlockingIssues()).append("\n");
        prompt.append("Deployment: ").append(reasoning.isDeploymentAllowed() ? "ALLOWED" : "BLOCKED").append("\n");
        for (ReasoningResult.DomainImpact impact : reasoning.getImpacts()) {
            prompt.append(String.format("  [%s] %s — %s | Regulation: %s\n",
                impact.getSeverity(), impact.getDomain(), impact.getReasoning(), impact.getRegulation()));
        }
        prompt.append("\n");

        if (causalPaths != null && !causalPaths.isEmpty()) {
            prompt.append("=== CAUSAL PATHS (from Knowledge Graph) ===\n");
            for (CausalPath path : causalPaths) {
                prompt.append("  ").append(path.getExplanation()).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("=== WRITE THE AUDIT NARRATIVE NOW ===\n");
        prompt.append("Include sections: Event Identification, Compliance Assessment, ");
        prompt.append("Causal Analysis, Controls Status, Evidence Artifacts, Attestation.\n");

        return prompt.toString();
    }
}

package com.regulith.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * CONTROL INGESTION AGENT (Agentic AI)
 * =====================================
 * This agent uses LLM to PARSE compliance framework documents into
 * machine-readable policies. No manual policy coding needed.
 *
 * HOW IT WORKS:
 *   1. User provides raw control text (SOX section, HITRUST control, regulation PDF text)
 *   2. LLM reads and REASONS about what the control means
 *   3. LLM extracts structured policy: trigger conditions, actions, severity
 *   4. Policy is added to CompliancePolicyEngine automatically
 *
 * THIS IS THE AGENTIC PART:
 *   - The LLM decides what domain the control belongs to
 *   - The LLM decides what triggers a violation
 *   - The LLM decides severity based on regulatory context
 *   - The LLM maps it to code patterns it should detect
 *   - No human writes rules. The agent understands compliance.
 *
 * USE CASE: Client says "we just adopted HITRUST v11" →
 *   Feed the 156 controls to this agent → all policies auto-generated.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ControlIngestionAgent {

    private final CompliancePolicyEngine policyEngine;
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    // Custom fine-tuned model (use "regulith-compliance-v1" after training, or "llama3.2:1b" for base)
    private static final String MODEL = "regulith-compliance-v1";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Ingest a raw compliance control text and convert it to a policy.
     * The LLM does ALL the parsing — no hardcoded extraction logic.
     */
    public CompliancePolicyEngine.CompliancePolicy ingestControl(
            String frameworkName, String controlText) {

        log.info("[Control Ingestion Agent] Processing: {} - {}...",
                frameworkName, controlText.substring(0, Math.min(50, controlText.length())));

        String llmResponse = callLLMForParsing(frameworkName, controlText);

        // Parse LLM response into a policy
        CompliancePolicyEngine.CompliancePolicy policy = parseLLMResponse(
                llmResponse, frameworkName, controlText);

        if (policy != null) {
            // Add to the live policy engine — takes effect immediately
            policyEngine.getAllPolicies().add(policy);
            log.info("[Control Ingestion Agent] Added policy: {} - {}",
                    policy.getId(), policy.getName());
        }

        return policy;
    }

    /**
     * Batch ingest: process multiple controls at once.
     * Useful when adopting an entire framework (all HITRUST controls, all SOX ITGCs).
     */
    public List<CompliancePolicyEngine.CompliancePolicy> ingestFramework(
            String frameworkName, List<String> controlTexts) {

        List<CompliancePolicyEngine.CompliancePolicy> results = new ArrayList<>();
        for (String text : controlTexts) {
            CompliancePolicyEngine.CompliancePolicy policy = ingestControl(frameworkName, text);
            if (policy != null) {
                results.add(policy);
            }
        }
        log.info("[Control Ingestion Agent] Framework '{}' ingested: {} policies created",
                frameworkName, results.size());
        return results;
    }

    private String callLLMForParsing(String framework, String controlText) {
        String prompt = """
            You are a compliance framework parser agent. Parse the following control into a structured policy.

            FRAMEWORK: %s

            CONTROL TEXT:
            %s

            Extract and respond with EXACTLY these fields (one per line, field: value):
            ID: [generate a unique ID like FRAMEWORK-CATEGORY-NUMBER, e.g., HITRUST-09ab-001]
            DOMAIN: [one of: SECURITY, SOX, REGULATORY, FAIR_LENDING, CONTRACTUAL, PRIVACY, INFRASTRUCTURE, AUDIT, ACCESS_CONTROL, CHANGE_MANAGEMENT, DEPLOYMENT]
            NAME: [short name for the control, max 50 chars]
            DESCRIPTION: [one sentence describing what the control requires]
            TRIGGER: [what event or condition would trigger this control check, as a code-like condition]
            SEVERITY: [CRITICAL, HIGH, MEDIUM, or LOW based on regulatory impact]
            ACTION: [what action must be taken to satisfy this control]
            CONTROLS: [specific control references, e.g., HITRUST 09.ab, NIST AU-9]
            SLA: [time requirement, e.g., "Before deployment", "Within 72 hours"]
            BLOCKING: [true if this should block deployment, false otherwise]
            VERTICALS: [comma-separated list of industries this applies to, or ALL]

            Be precise. Use actual regulatory references. If unsure about severity, default to HIGH.
            """.formatted(framework, controlText);

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", MODEL);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .timeout(java.time.Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode responseJson = objectMapper.readTree(response.body());
            return responseJson.get("response").asText();

        } catch (Exception e) {
            log.warn("[Control Ingestion Agent] LLM unavailable, using fallback parser: {}", e.getMessage());
            return fallbackParse(framework, controlText);
        }
    }

    /**
     * Fallback when LLM is unavailable — basic keyword extraction.
     * Still functional, but less intelligent than LLM-based parsing.
     */
    private String fallbackParse(String framework, String controlText) {
        String id = framework.toUpperCase().replaceAll("[^A-Z0-9]", "")
                + "-" + String.format("%03d", controlText.hashCode() & 0xFFF);
        String domain = inferDomain(controlText);
        String severity = inferSeverity(controlText);
        boolean blocking = controlText.toLowerCase().contains("must") ||
                controlText.toLowerCase().contains("shall") ||
                controlText.toLowerCase().contains("required");

        return String.format("""
                ID: %s
                DOMAIN: %s
                NAME: %s
                DESCRIPTION: %s
                TRIGGER: eventType == 'CODE_COMMIT' || eventType == 'DEPLOYMENT'
                SEVERITY: %s
                ACTION: Verify compliance with %s control requirements
                CONTROLS: %s
                SLA: Before deployment
                BLOCKING: %s
                VERTICALS: ALL
                """, id, domain,
                controlText.substring(0, Math.min(50, controlText.length())).replaceAll("[\\n\\r]", " "),
                controlText.substring(0, Math.min(100, controlText.length())).replaceAll("[\\n\\r]", " "),
                severity, framework, framework,
                blocking);
    }

    private String inferDomain(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("audit") || lower.contains("evidence") || lower.contains("log")) return "AUDIT";
        if (lower.contains("access") || lower.contains("authentication") || lower.contains("privilege")) return "ACCESS_CONTROL";
        if (lower.contains("encrypt") || lower.contains("vulnerability") || lower.contains("security")) return "SECURITY";
        if (lower.contains("change") || lower.contains("approval") || lower.contains("segregation")) return "CHANGE_MANAGEMENT";
        if (lower.contains("privacy") || lower.contains("personal data") || lower.contains("pii")) return "PRIVACY";
        if (lower.contains("financial") || lower.contains("sox") || lower.contains("itgc")) return "SOX";
        if (lower.contains("deploy") || lower.contains("production") || lower.contains("release")) return "DEPLOYMENT";
        return "REGULATORY";
    }

    private String inferSeverity(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("critical") || lower.contains("immediately") || lower.contains("breach")) return "CRITICAL";
        if (lower.contains("must") || lower.contains("shall") || lower.contains("required")) return "HIGH";
        if (lower.contains("should") || lower.contains("recommend")) return "MEDIUM";
        return "LOW";
    }

    private CompliancePolicyEngine.CompliancePolicy parseLLMResponse(
            String llmResponse, String framework, String originalText) {
        try {
            Map<String, String> fields = new HashMap<>();
            for (String line : llmResponse.split("\n")) {
                if (line.contains(":")) {
                    int idx = line.indexOf(":");
                    String key = line.substring(0, idx).trim().toUpperCase();
                    String val = line.substring(idx + 1).trim();
                    fields.put(key, val);
                }
            }

            if (!fields.containsKey("ID") || !fields.containsKey("NAME")) {
                return null;
            }

            List<String> verticals = fields.containsKey("VERTICALS")
                    ? Arrays.asList(fields.get("VERTICALS").split(",\\s*"))
                    : List.of("ALL");

            return CompliancePolicyEngine.CompliancePolicy.builder()
                    .id(fields.getOrDefault("ID", framework + "-AUTO"))
                    .domain(fields.getOrDefault("DOMAIN", "REGULATORY"))
                    .name(fields.getOrDefault("NAME", "Auto-parsed control"))
                    .description(fields.getOrDefault("DESCRIPTION", originalText.substring(0, Math.min(100, originalText.length()))))
                    .triggerCondition(fields.getOrDefault("TRIGGER", "eventType == 'CODE_COMMIT'"))
                    .severity(fields.getOrDefault("SEVERITY", "HIGH"))
                    .action(fields.getOrDefault("ACTION", "Verify compliance"))
                    .controls(fields.getOrDefault("CONTROLS", framework))
                    .sla(fields.getOrDefault("SLA", "Before deployment"))
                    .blocking(Boolean.parseBoolean(fields.getOrDefault("BLOCKING", "false")))
                    .verticals(verticals)
                    .build();

        } catch (Exception e) {
            log.error("[Control Ingestion Agent] Failed to parse LLM response: {}", e.getMessage());
            return null;
        }
    }
}

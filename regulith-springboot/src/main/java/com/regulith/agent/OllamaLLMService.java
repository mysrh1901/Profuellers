package com.regulith.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * OLLAMA LLM SERVICE
 * ===================
 * Calls a LOCAL LLM (Ollama) for real AI reasoning.
 * No API keys. No cloud dependency. Runs on your machine.
 * 
 * THIS is what makes the agent truly AGENTIC:
 * - The LLM READS the actual code/config content
 * - The LLM REASONS about what compliance domains are affected
 * - The LLM EXPLAINS why with specific regulation references
 * - No hardcoded rules. Pure AI reasoning.
 */
@Service
@Slf4j
public class OllamaLLMService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama3.2:1b";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send content to LLM and get compliance reasoning back.
     * The LLM does ALL the thinking. No rules in this method.
     */
    public String reason(String fileContent, String fileName, String clientContext) {
        String prompt = buildCompliancePrompt(fileContent, fileName, clientContext);

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", MODEL);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode responseJson = objectMapper.readTree(response.body());
            String llmResponse = responseJson.get("response").asText();

            log.info("[Ollama LLM] Reasoning complete for: {}", fileName);
            return llmResponse;

        } catch (Exception e) {
            log.error("[Ollama LLM] Error calling LLM: {}", e.getMessage());
            return "LLM unavailable: " + e.getMessage();
        }
    }

    /**
     * The prompt that makes the agent intelligent.
     * This is the core of the agentic AI — the LLM receives full context
     * and reasons about compliance impact WITHOUT any hardcoded rules.
     */
    private String buildCompliancePrompt(String fileContent, String fileName, String clientContext) {
        return """
            You are an autonomous compliance intelligence agent.
            Analyze the following file and determine ALL compliance violations or risks.
            
            CLIENT CONTEXT: %s
            
            FILE: %s
            CONTENT:
            %s
            
            For each issue found, respond with EXACTLY this format (one per line):
            DOMAIN: [SOX|SECURITY|REGULATORY|FAIR_LENDING|CONTRACTUAL|PRIVACY|INFRASTRUCTURE|AUDIT]
            SEVERITY: [CRITICAL|HIGH|MEDIUM|LOW]
            FINDING: [what you found]
            REGULATION: [specific regulation or control reference]
            ACTION: [what must be done]
            BLOCKING: [true|false]
            
            Only report REAL issues. Be specific. Reference actual regulations.
            If no issues found, respond with: NO_ISSUES_FOUND
            """.formatted(clientContext, fileName, fileContent);
    }

    /**
     * Generate an audit narrative using LLM.
     * The LLM writes it like a human auditor — not a template.
     */
    public String generateNarrative(String eventDescription, String findings, String clientName) {
        String prompt = """
            You are a senior audit evidence writer. Write a formal audit narrative for this event.
            
            Client: %s
            Event: %s
            Findings: %s
            
            Write a professional audit evidence narrative suitable for external auditor review.
            Include: event identification, compliance impact, controls affected, evidence trail.
            Be concise but thorough.
            """.formatted(clientName, eventDescription, findings);

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", MODEL);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode responseJson = objectMapper.readTree(response.body());
            return responseJson.get("response").asText();

        } catch (Exception e) {
            return "Narrative generation failed: " + e.getMessage();
        }
    }

    /**
     * Check if Ollama is available.
     */
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/tags"))
                    .GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}

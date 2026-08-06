package com.regulith.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

/**
 * Bedrock Claude Service - Calls Claude on Amazon Bedrock for:
 * 1. Code diff understanding (what does this change actually do?)
 * 2. Audit narrative generation (natural language evidence)
 * 3. Contract obligation extraction (parse MSA into rules)
 * 
 * This is what makes the agent truly "agentic" - it REASONS using AI,
 * not just pattern matching.
 * 
 * Hexaware is an Anthropic authorized reseller for Bedrock.
 * Data stays in your AWS account. SOC 2 / HIPAA compliant.
 */
@Service
@Slf4j
public class BedrockClaudeService {

    @Value("${spring.bedrock.enabled:true}")
    private boolean enabled;

    @Value("${spring.bedrock.use-llm:false}")
    private boolean useLlm;

    @Value("${spring.bedrock.region:us-east-1}")
    private String region;

    @Value("${spring.bedrock.model-id:anthropic.claude-3-sonnet-20240229-v1:0}")
    private String modelId;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private BedrockRuntimeClient client;

    private BedrockRuntimeClient getClient() {
        if (client == null && useLlm) {
            client = BedrockRuntimeClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
        return client;
    }

    /**
     * Analyze a code diff to understand compliance implications.
     * Claude reads the actual code and determines what changed semantically.
     */
    public String analyzeCodeDiff(String codeDiff, String clientFrameworks) {
        if (!useLlm) {
            return localAnalyzeCodeDiff(codeDiff);
        }

        String prompt = String.format("""
                You are a compliance analyst for a mortgage/financial services IT delivery company.
                Analyze this code diff and identify compliance implications.
                
                The client is subject to these frameworks: %s
                
                Code diff:
                %s
                
                For each applicable compliance domain, state:
                1. Domain name (SOX, SECURITY, REGULATORY, FAIR_LENDING, CONTRACTUAL, PRIVACY, AUDIT)
                2. Why it's affected
                3. What action is required
                4. Whether it should block deployment (yes/no)
                
                Be specific. Reference actual regulation sections.
                """, clientFrameworks, codeDiff);

        return callClaude(prompt);
    }

    /**
     * Generate an audit narrative from event data.
     * Claude writes it like a human auditor would — professional, detailed, evidence-backed.
     */
    public String generateAuditNarrative(String eventData, String impactSummary, String clientName) {
        if (!useLlm) {
            return localGenerateNarrative(eventData, impactSummary, clientName);
        }

        String prompt = String.format("""
                You are an internal audit evidence writer for a financial services company.
                Generate a complete audit evidence narrative for the following event.
                Write it as if preparing for an EY or Deloitte SOX audit review.
                
                Client: %s
                
                Event Details:
                %s
                
                Compliance Impact Assessment:
                %s
                
                Write the narrative in a formal audit evidence format including:
                1. Event identification (who, what, when, why)
                2. Impact assessment (which controls and frameworks affected)
                3. Actions taken or required
                4. Controls satisfied or gaps identified
                5. Evidence artifacts referenced
                
                Be factual and precise. This will be reviewed by external auditors.
                """, clientName, eventData, impactSummary);

        return callClaude(prompt);
    }

    /**
     * Parse contract text to extract compliance obligations.
     * Claude reads legal language and extracts structured rules.
     */
    public String parseContractObligations(String contractText) {
        if (!useLlm) {
            return "Contract parsing requires LLM. Enable bedrock.use-llm=true with AWS credentials.";
        }

        String prompt = String.format("""
                You are a legal compliance analyst. Parse this contract excerpt and extract
                all compliance obligations in structured format.
                
                For each obligation, provide:
                - Source clause (e.g., "MSA §7.2")
                - Type (Security SLA, Change Management, Data Residency, etc.)
                - Requirement (what must be done)
                - SLA (time limit if any)
                - Penalty (consequence of breach)
                - Trigger (what event activates this obligation)
                
                Contract text:
                %s
                """, contractText);

        return callClaude(prompt);
    }

    /**
     * Call Claude on Bedrock.
     */
    private String callClaude(String prompt) {
        try {
            BedrockRuntimeClient bedrockClient = getClient();
            if (bedrockClient == null) {
                log.warn("[Bedrock] Client not initialized. Falling back to local.");
                return "LLM unavailable - using local reasoning.";
            }

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 2048);
            requestBody.put("temperature", 0.1);

            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");
            ArrayNode content = message.putArray("content");
            ObjectNode textContent = content.addObject();
            textContent.put("type", "text");
            textContent.put("text", prompt);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(requestBody.toString()))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            JsonNode responseJson = objectMapper.readTree(response.body().asUtf8String());

            return responseJson.get("content").get(0).get("text").asText();

        } catch (Exception e) {
            log.error("[Bedrock] Error calling Claude: {}. Falling back to local.", e.getMessage());
            return "LLM call failed - using local reasoning. Error: " + e.getMessage();
        }
    }

    // =========================================================================
    // LOCAL FALLBACK METHODS (used when AWS credentials not configured)
    // These demonstrate the logic without needing Bedrock access.
    // =========================================================================

    private String localAnalyzeCodeDiff(String codeDiff) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("[Local Analysis - LLM disabled]\n\n");

        if (codeDiff.toLowerCase().contains("rate") || codeDiff.toLowerCase().contains("apr")
                || codeDiff.toLowerCase().contains("interest")) {
            analysis.append("REGULATORY: This code modifies financial calculation logic. ")
                    .append("TILA Regulation Z requires APR accuracy to 1/8 of 1%. Validation needed.\n\n");
            analysis.append("SOX: Change to financially-significant system. ")
                    .append("ITGC-CM-01 (dual approval) and ITGC-SD-01 (security testing) apply.\n\n");
            analysis.append("FAIR_LENDING: Pricing/rate logic change. ")
                    .append("ECOA requires disparate impact analysis before production.\n\n");
        }

        if (codeDiff.toLowerCase().contains("borrower") || codeDiff.toLowerCase().contains("income")
                || codeDiff.toLowerCase().contains("customer")) {
            analysis.append("PRIVACY: Code processes personal/financial data. ")
                    .append("GLBA safeguards apply. Verify encryption and access controls.\n\n");
        }

        if (codeDiff.toLowerCase().contains("todo") || codeDiff.toLowerCase().contains("fixme")
                || codeDiff.toLowerCase().contains("hardcoded")) {
            analysis.append("SECURITY: Code quality issue detected. ")
                    .append("PCI-DSS 6.5 requires resolution before production deployment.\n\n");
        }

        analysis.append("AUDIT: Auto-generating evidence narrative for this change.\n");
        return analysis.toString();
    }

    private String localGenerateNarrative(String eventData, String impactSummary, String clientName) {
        return String.format("""
                AUDIT EVIDENCE NARRATIVE
                [Generated by KAVACH AI - Local Reasoning Engine]
                
                Client: %s
                Generated: %s
                
                EVENT SUMMARY:
                %s
                
                COMPLIANCE IMPACT:
                %s
                
                ATTESTATION:
                This narrative was autonomously generated by the KAVACH AI agent system.
                All data sourced from system records (Git, SAST tools, ServiceNow).
                
                In production with Bedrock enabled, this narrative would be generated by
                Claude (Anthropic) via Amazon Bedrock, producing richer natural language
                evidence suitable for direct inclusion in audit workpapers.
                
                --- KAVACH AI Audit Narrator Agent ---
                """,
                clientName,
                java.time.LocalDateTime.now().toString(),
                eventData,
                impactSummary);
    }
}

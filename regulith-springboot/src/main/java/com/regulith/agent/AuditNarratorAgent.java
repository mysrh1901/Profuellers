package com.regulith.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.regulith.model.*;
import com.regulith.repository.AuditNarrativeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Audit Narrator Agent
 * 
 * HYBRID ARCHITECTURE:
 * - Uses Claude on Amazon Bedrock (when enabled) for natural language narrative generation
 * - Falls back to local template-based generation (when Bedrock not configured)
 * 
 * This makes the agent truly AGENTIC:
 * - It PERCEIVES the event (receives from Chain Reactor)
 * - It REASONS about what to write (via Claude LLM or local logic)
 * - It DECIDES what controls are satisfied
 * - It ACTS by generating and storing the evidence
 * 
 * Hexaware is Anthropic authorized reseller for Amazon Bedrock.
 * Data stays in AWS account. No legal concerns.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditNarratorAgent {

    private final AuditNarrativeRepository narrativeRepo;
    private final BedrockClaudeService bedrockService;

    public void generateNarrative(JsonNode event, List<ChainReactionResult> impacts, ClientEngagement client) {
        log.info("[Audit Narrator] Generating narrative for event {} on {}",
                event.get("eventId").asLong(), client.getClientName());

        // Build context for LLM
        String eventData = formatEventData(event);
        String impactSummary = formatImpactSummary(impacts);

        // Call Claude via Bedrock for intelligent narrative generation
        // Falls back to local logic if Bedrock not configured
        String narrative = bedrockService.generateAuditNarrative(
                eventData, impactSummary, client.getClientName());

        String controls = impacts.stream()
                .map(ChainReactionResult::getControlsAffected)
                .collect(Collectors.joining(", "));

        AuditNarrative record = new AuditNarrative();
        record.setEventId(event.get("eventId").asLong());
        record.setEngagementId(client.getEngagementId());
        record.setEventType(event.get("eventType").asText());
        record.setGeneratedAt(LocalDateTime.now());
        record.setNarrativeText(narrative);
        record.setControlsSatisfied(controls);
        record.setEvidenceArtifacts("Git commit, Jira ticket, SAST report, PR approval, This narrative");

        narrativeRepo.save(record);
        log.info("[Audit Narrator] Narrative generated and stored (ID: {})", record.getId());
    }

    private String formatEventData(JsonNode event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ").append(event.get("eventType").asText()).append("\n");
        if (event.has("commitId")) sb.append("Commit: ").append(event.get("commitId").asText()).append("\n");
        if (event.has("author")) sb.append("Author: ").append(event.get("author").asText()).append("\n");
        if (event.has("description")) sb.append("Description: ").append(event.get("description").asText()).append("\n");
        if (event.has("jiraTicket")) sb.append("Jira: ").append(event.get("jiraTicket").asText()).append("\n");
        if (event.has("touchesFinancialLogic")) sb.append("Financial Logic: ").append(event.get("touchesFinancialLogic").asBoolean()).append("\n");
        if (event.has("touchesPii")) sb.append("PII Data: ").append(event.get("touchesPii").asBoolean()).append("\n");
        if (event.has("sastHighCount")) sb.append("SAST High Findings: ").append(event.get("sastHighCount").asInt()).append("\n");
        return sb.toString();
    }

    private String formatImpactSummary(List<ChainReactionResult> impacts) {
        StringBuilder sb = new StringBuilder();
        sb.append("Domains Affected: ").append(impacts.size()).append("\n");
        sb.append("Blocking Issues: ").append(impacts.stream().filter(ChainReactionResult::isBlocking).count()).append("\n\n");
        for (int i = 0; i < impacts.size(); i++) {
            ChainReactionResult impact = impacts.get(i);
            sb.append(String.format("[%d] %s (%s): %s\n    Action: %s\n    Controls: %s\n\n",
                    i + 1, impact.getDomain(), impact.getSeverity(),
                    impact.getReason(), impact.getActionRequired(),
                    impact.getControlsAffected()));
        }
        return sb.toString();
    }
}

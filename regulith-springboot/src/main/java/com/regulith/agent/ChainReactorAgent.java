package com.regulith.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regulith.model.*;
import com.regulith.repository.*;
import com.regulith.graph.GraphRAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * CHAIN REACTOR AGENT
 * ====================
 * The core intelligence of Regulith AI.
 * 
 * HOW IT WORKS:
 * 1. Listens to ActiveMQ queue for compliance events
 * 2. Builds event context from the message payload
 * 3. Passes context to the Policy Engine for evaluation
 * 4. Policy Engine returns all triggered policies (across ALL domains)
 * 5. Chain Reactor stores results and triggers downstream agents
 * 
 * WHY IT'S AGENTIC:
 * - PERCEIVES: Listens to event stream autonomously (no human trigger)
 * - REASONS: Evaluates policies against event context (domain-specific logic)
 * - DECIDES: Determines severity, blocking status, priority
 * - ACTS: Stores results, triggers Audit Narrator, updates scores, blocks deploys
 * - ADAPTS: Adding new policies = new behavior, zero code changes
 * 
 * DOMAIN-AGNOSTIC:
 * - The agent code never changes between verticals
 * - Only the policies in CompliancePolicyEngine define industry behavior
 * - Same agent handles Mortgage, Healthcare, Retail, Manufacturing
 * 
 * EVENT SOURCES (all feed into the same queue):
 * - Git post-commit hooks
 * - Jenkins pipeline webhooks
 * - Jira ticket state changes
 * - Docker image build events
 * - AWS Config change notifications
 * - Manual API calls
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChainReactorAgent {

    private final ChainReactionResultRepository resultRepo;
    private final ClientEngagementRepository clientRepo;
    private final AuditNarratorAgent narratorAgent;
    private final CompliancePolicyEngine policyEngine;
    private final GraphRAGService graphRAGService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Main event listener — picks events from ActiveMQ and processes them.
     * This is the autonomous perception loop of the agent.
     */
    @JmsListener(destination = "compliance-events")
    public void processEvent(String eventJson) {
        try {
            JsonNode event = objectMapper.readTree(eventJson);
            String engagementId = event.get("engagementId").asText();
            Long eventId = event.get("eventId").asLong();

            log.info("[Chain Reactor] Event received: {} | Engagement: {} | Source: {}",
                    event.get("eventType").asText(), engagementId,
                    event.has("source") ? event.get("source").asText() : "unknown");

            // Load client context
            ClientEngagement client = clientRepo.findById(engagementId).orElse(null);
            if (client == null) {
                log.warn("[Chain Reactor] Engagement not found: {}", engagementId);
                return;
            }

            // Build evaluation context (merge event data + client profile)
            Map<String, Object> context = buildEvaluationContext(event, client);

            // CORE REASONING: Evaluate all policies against this event
            List<CompliancePolicyEngine.CompliancePolicy> triggeredPolicies =
                    policyEngine.evaluate(context);

            log.info("[Chain Reactor] {} policies triggered for event {}",
                    triggeredPolicies.size(), eventId);

            // GRAPH RAG: Traverse Knowledge Graph for causal paths
            String description = event.has("description") ? event.get("description").asText() : "";
            List<String> filesFromDesc = List.of(description.toLowerCase());
            GraphRAGService.GraphRAGResult graphResult = graphRAGService.retrieveComplianceContext(
                    filesFromDesc,
                    context.containsKey("touchesFinancialLogic") && (boolean) context.get("touchesFinancialLogic"),
                    context.containsKey("touchesPii") && (boolean) context.get("touchesPii")
            );
            log.info("[Chain Reactor] GraphRAG found {} causal paths across {} domains",
                    graphResult.getCausalPaths().size(), graphResult.getDomainImpacts().size());

            // Convert triggered policies to results
            List<ChainReactionResult> impacts = new ArrayList<>();
            for (CompliancePolicyEngine.CompliancePolicy policy : triggeredPolicies) {
                impacts.add(createResult(eventId, engagementId, policy));
            }

            // Persist results
            resultRepo.saveAll(impacts);

            // AUTONOMOUS ACTION 1: Trigger audit narrative generation
            narratorAgent.generateNarrative(event, impacts, client);

            // AUTONOMOUS ACTION 2: Update client compliance score
            updateClientScore(client, impacts);

            // AUTONOMOUS ACTION 3: Log blocking decisions
            long blockingCount = impacts.stream().filter(ChainReactionResult::isBlocking).count();
            if (blockingCount > 0) {
                log.warn("[Chain Reactor] DEPLOYMENT BLOCKED — {} blocking issues found for {}",
                        blockingCount, client.getClientName());
            }

        } catch (Exception e) {
            log.error("[Chain Reactor] Processing error: {}", e.getMessage(), e);
        }
    }

    /**
     * Build evaluation context by merging event data with client profile.
     * This context is what the Policy Engine evaluates against.
     */
    private Map<String, Object> buildEvaluationContext(JsonNode event, ClientEngagement client) {
        Map<String, Object> context = new HashMap<>();

        // Event properties
        context.put("eventType", event.has("eventType") ? event.get("eventType").asText() : "UNKNOWN");
        context.put("touchesFinancialLogic", event.has("touchesFinancialLogic") && event.get("touchesFinancialLogic").asBoolean());
        context.put("touchesPii", event.has("touchesPii") && event.get("touchesPii").asBoolean());
        context.put("sastHighCount", event.has("sastHighCount") ? event.get("sastHighCount").asInt() : 0);
        context.put("secretsDetected", event.has("secretsDetected") && event.get("secretsDetected").asBoolean());
        context.put("targetEnv", event.has("targetEnv") ? event.get("targetEnv").asText() : "development");
        context.put("containerized", event.has("containerized") && event.get("containerized").asBoolean());
        context.put("dataResidencyViolation", event.has("dataResidencyViolation") && event.get("dataResidencyViolation").asBoolean());
        context.put("touchesClinicalLogic", event.has("touchesClinicalLogic") && event.get("touchesClinicalLogic").asBoolean());

        // Client profile properties
        context.put("soxApplicable", client.isSoxApplicable());
        context.put("pciApplicable", client.isPciApplicable());
        context.put("gdprApplicable", client.isGdprApplicable());
        context.put("fairLendingApplicable", client.isFairLendingApplicable());
        context.put("frameworks", client.getApplicableFrameworks() != null ? client.getApplicableFrameworks() : "");
        context.put("vertical", client.getIndustry() != null ? client.getIndustry() : "ALL");

        return context;
    }

    /**
     * Convert a triggered policy into a persisted result.
     */
    private ChainReactionResult createResult(Long eventId, String engagementId,
                                             CompliancePolicyEngine.CompliancePolicy policy) {
        ChainReactionResult result = new ChainReactionResult();
        result.setEventId(eventId);
        result.setEngagementId(engagementId);
        result.setDomain(policy.getDomain());
        result.setSeverity(policy.getSeverity());
        result.setReason(String.format("[%s] %s", policy.getId(), policy.getDescription()));
        result.setActionRequired(policy.getAction());
        result.setControlsAffected(policy.getControls());
        result.setSla(policy.getSla());
        result.setBlocking(policy.isBlocking());
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * Update client compliance score based on triggered policies.
     */
    private void updateClientScore(ClientEngagement client, List<ChainReactionResult> impacts) {
        // Get ALL chain reactions for this client (cumulative)
        List<ChainReactionResult> allChains = resultRepo.findByEngagementIdOrderByTimestampDesc(client.getEngagementId());

        double deduction = impacts.stream()
                .mapToDouble(i -> switch (i.getSeverity()) {
                    case "CRITICAL" -> 8.0;
                    case "HIGH" -> 5.0;
                    case "MEDIUM" -> 2.5;
                    default -> 1.0;
                }).sum();

        double newScore = Math.max(0, client.getComplianceScore() - deduction);
        client.setComplianceScore(newScore);

        // Open risks = total findings across all events
        client.setOpenRisks(allChains.size());

        // Trend based on score
        client.setTrend(newScore < 70 ? "DEGRADING" : "STABLE");

        // Recalculate debt from ALL accumulated findings
        long criticals = allChains.stream().filter(c -> "CRITICAL".equals(c.getSeverity())).count();
        long highs = allChains.stream().filter(c -> "HIGH".equals(c.getSeverity())).count();
        long blockings = allChains.stream().filter(ChainReactionResult::isBlocking).count();
        double debt = (criticals * 100000) + (highs * 25000) + (blockings * 25000);
        if (client.isSoxApplicable()) debt += 500000;
        debt += 50000; // base SLA exposure
        client.setComplianceDebtUsd(debt);

        clientRepo.save(client);

        log.info("[Chain Reactor] Updated: Score={} | Risks={} | Debt=${}K | {}",
                String.format("%.0f", newScore), allChains.size(),
                String.format("%.0f", debt/1000), client.getClientName());
    }
}

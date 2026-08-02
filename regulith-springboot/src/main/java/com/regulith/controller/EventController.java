package com.regulith.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.regulith.model.ComplianceEvent;
import com.regulith.repository.ComplianceEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST API for submitting compliance events.
 * Events are persisted and sent to JMS queue for agent processing.
 */
@RestController
@RequestMapping("/api/events")
@Slf4j
@RequiredArgsConstructor
public class EventController {

    private final ComplianceEventRepository eventRepo;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Submit a new compliance event (code commit, infra change, etc.)
     * This triggers the full agent pipeline: Chain Reactor -> Audit Narrator -> Score Update
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitEvent(@RequestBody Map<String, Object> payload) {
        try {
            ComplianceEvent event = new ComplianceEvent();
            event.setEngagementId((String) payload.get("engagementId"));
            event.setEventType((String) payload.get("eventType"));
            event.setSource((String) payload.getOrDefault("source", "API"));
            event.setDescription((String) payload.getOrDefault("description", ""));
            event.setTimestamp(LocalDateTime.now());
            event.setProcessed(false);
            event.setPayload(objectMapper.writeValueAsString(payload));

            event = eventRepo.save(event);

            // Publish to JMS queue for agent processing
            ObjectNode message = objectMapper.createObjectNode();
            message.put("eventId", event.getId());
            message.put("engagementId", event.getEngagementId());
            message.put("eventType", event.getEventType());
            message.put("touchesFinancialLogic",
                    (Boolean) payload.getOrDefault("touchesFinancialLogic", false));
            message.put("touchesPii",
                    (Boolean) payload.getOrDefault("touchesPii", false));
            message.put("sastHighCount",
                    ((Number) payload.getOrDefault("sastHighCount", 0)).intValue());
            message.put("author", (String) payload.getOrDefault("author", "unknown"));
            message.put("commitId", (String) payload.getOrDefault("commitId", "N/A"));
            message.put("description", event.getDescription());
            message.put("jiraTicket", (String) payload.getOrDefault("jiraTicket", "N/A"));

            jmsTemplate.convertAndSend("compliance-events", message.toString());

            log.info("[API] Event submitted and queued: {} for {}", event.getEventType(), event.getEngagementId());

            return ResponseEntity.ok(Map.of(
                    "status", "accepted",
                    "eventId", event.getId(),
                    "message", "Event queued for agent processing",
                    "queue", "compliance-events"
            ));
        } catch (Exception e) {
            log.error("Error submitting event: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Simulate a code commit event (for demo purposes).
     * Sends a realistic mortgage rate calculation change.
     */
    @PostMapping("/simulate/code-commit")
    public ResponseEntity<Map<String, Object>> simulateCodeCommit() {
        Map<String, Object> event = Map.of(
                "engagementId", "ENG-001",
                "eventType", "CODE_COMMIT",
                "source", "GitHub",
                "description", "Update ARM rate cap calculation logic for new CFPB guidance",
                "author", "developer.patel@hexaware.com",
                "commitId", "a3f7b2c",
                "jiraTicket", "MORT-1542",
                "touchesFinancialLogic", true,
                "touchesPii", true,
                "sastHighCount", 1
        );
        return submitEvent(event);
    }

    /**
     * Simulate an infrastructure change event.
     */
    @PostMapping("/simulate/infra-change")
    public ResponseEntity<Map<String, Object>> simulateInfraChange() {
        Map<String, Object> event = Map.of(
                "engagementId", "ENG-002",
                "eventType", "INFRA_CHANGE",
                "source", "AWS Config",
                "description", "RDS backup replication configured to us-east-1 (outside EU)",
                "author", "devops.kumar@hexaware.com",
                "commitId", "infra-4e5f",
                "jiraTicket", "OPS-892",
                "touchesFinancialLogic", false,
                "touchesPii", true,
                "sastHighCount", 0
        );
        return submitEvent(event);
    }
}

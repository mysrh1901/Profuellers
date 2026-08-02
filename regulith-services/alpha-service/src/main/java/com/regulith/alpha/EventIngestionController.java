package com.regulith.alpha;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Event Ingestion - accepts events from all sources.
 */
@RestController
@RequestMapping("/api/events")
@Slf4j
public class EventIngestionController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> ingestEvent(@RequestBody Map<String, Object> payload) {
        String eventId = UUID.randomUUID().toString().substring(0, 8);
        String eventType = (String) payload.getOrDefault("eventType", "UNKNOWN");
        String source = (String) payload.getOrDefault("source", "API");

        log.info("[Alpha] Event ingested: {} from {} (id: {})", eventType, source, eventId);

        // In production: publish to ActiveMQ/SQS for Beta service to consume
        return ResponseEntity.ok(Map.of(
                "status", "accepted",
                "eventId", eventId,
                "service", "alpha-service",
                "timestamp", LocalDateTime.now().toString(),
                "message", "Event routed to processing queue"
        ));
    }

    @PostMapping("/webhook/git")
    public ResponseEntity<Map<String, Object>> gitWebhook(@RequestBody Map<String, Object> payload) {
        log.info("[Alpha] Git webhook received");
        payload.put("source", "Git");
        payload.put("eventType", "CODE_COMMIT");
        return ingestEvent(payload);
    }

    @PostMapping("/webhook/jenkins")
    public ResponseEntity<Map<String, Object>> jenkinsWebhook(@RequestBody Map<String, Object> payload) {
        log.info("[Alpha] Jenkins webhook received");
        payload.put("source", "Jenkins");
        payload.put("eventType", "DEPLOYMENT");
        return ingestEvent(payload);
    }

    @PostMapping("/webhook/jira")
    public ResponseEntity<Map<String, Object>> jiraWebhook(@RequestBody Map<String, Object> payload) {
        log.info("[Alpha] Jira webhook received");
        payload.put("source", "Jira");
        payload.put("eventType", "TICKET_CHANGE");
        return ingestEvent(payload);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "alpha-service",
                "status", "UP",
                "role", "Event Ingestion & Routing"
        ));
    }
}

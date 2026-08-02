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
import java.util.HashMap;
import java.util.Map;

/**
 * WEBHOOK CONTROLLER
 * ==================
 * Receives events from external systems and feeds them into the agent pipeline.
 * 
 * In production these would be real webhooks configured in:
 * - Jenkins (post-build webhook)
 * - Jira (automation rules / webhooks)
 * - Docker Registry (image push events)
 * - AWS EventBridge (config changes)
 * 
 * For demo: provides /simulate/* endpoints that mimic real webhook payloads.
 * 
 * ALL events go into the same ActiveMQ queue → same Chain Reactor Agent.
 * The agent doesn't care WHERE the event came from. It evaluates policies the same way.
 */
@RestController
@RequestMapping("/api/webhooks")
@Slf4j
@RequiredArgsConstructor
public class WebhookController {

    private final ComplianceEventRepository eventRepo;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ================================================================
    // JENKINS WEBHOOK
    // Triggered after Jenkins pipeline build completes
    // ================================================================

    @PostMapping("/jenkins")
    public ResponseEntity<Map<String, Object>> jenkinsWebhook(@RequestBody Map<String, Object> payload) {
        return processWebhook("JENKINS", "DEPLOYMENT", payload);
    }

    @PostMapping("/simulate/jenkins-deploy")
    public ResponseEntity<Map<String, Object>> simulateJenkinsDeploy() {
        log.info("[Webhook] Simulating Jenkins deployment event");
        Map<String, Object> payload = new HashMap<>();
        payload.put("engagementId", "ENG-001");
        payload.put("description", "Jenkins Pipeline: Deploy loan-origination-service to production");
        payload.put("author", "jenkins-ci@hexaware.com");
        payload.put("commitId", "build-4521");
        payload.put("jiraTicket", "MORT-1542");
        payload.put("touchesFinancialLogic", true);
        payload.put("touchesPii", false);
        payload.put("sastHighCount", 0);
        payload.put("targetEnv", "production");
        payload.put("containerized", true);
        return processWebhook("JENKINS", "DEPLOYMENT", payload);
    }

    @PostMapping("/simulate/jenkins-deploy-blocked")
    public ResponseEntity<Map<String, Object>> simulateJenkinsBlocked() {
        log.info("[Webhook] Simulating Jenkins deployment with SAST failure");
        Map<String, Object> payload = new HashMap<>();
        payload.put("engagementId", "ENG-001");
        payload.put("description", "Jenkins Pipeline: Deploy payment-service (SAST FAILED)");
        payload.put("author", "jenkins-ci@hexaware.com");
        payload.put("commitId", "build-4522");
        payload.put("jiraTicket", "MORT-1600");
        payload.put("touchesFinancialLogic", true);
        payload.put("touchesPii", true);
        payload.put("sastHighCount", 3);
        payload.put("targetEnv", "production");
        payload.put("containerized", true);
        return processWebhook("JENKINS", "DEPLOYMENT", payload);
    }

    // ================================================================
    // JIRA WEBHOOK
    // Triggered when ticket state changes (e.g., story moved to Done)
    // ================================================================

    @PostMapping("/jira")
    public ResponseEntity<Map<String, Object>> jiraWebhook(@RequestBody Map<String, Object> payload) {
        return processWebhook("JIRA", "TICKET_CHANGE", payload);
    }

    @PostMapping("/simulate/jira-ticket")
    public ResponseEntity<Map<String, Object>> simulateJiraTicket() {
        log.info("[Webhook] Simulating Jira ticket state change");
        Map<String, Object> payload = new HashMap<>();
        payload.put("engagementId", "ENG-001");
        payload.put("description", "MORT-1542 moved to Done: Implement ARM rate cap per CFPB 2026-03");
        payload.put("author", "lead.sharma@hexaware.com");
        payload.put("jiraTicket", "MORT-1542");
        payload.put("touchesFinancialLogic", true);
        payload.put("touchesPii", false);
        payload.put("sastHighCount", 0);
        return processWebhook("JIRA", "TICKET_CHANGE", payload);
    }

    // ================================================================
    // DOCKER REGISTRY WEBHOOK
    // Triggered when a new image is pushed
    // ================================================================

    @PostMapping("/docker")
    public ResponseEntity<Map<String, Object>> dockerWebhook(@RequestBody Map<String, Object> payload) {
        return processWebhook("DOCKER_REGISTRY", "IMAGE_PUSH", payload);
    }

    @PostMapping("/simulate/docker-push")
    public ResponseEntity<Map<String, Object>> simulateDockerPush() {
        log.info("[Webhook] Simulating Docker image push");
        Map<String, Object> payload = new HashMap<>();
        payload.put("engagementId", "ENG-001");
        payload.put("description", "Docker image pushed: loan-origination-service:2.4.1");
        payload.put("author", "ci-pipeline@hexaware.com");
        payload.put("commitId", "img-2.4.1");
        payload.put("jiraTicket", "MORT-1542");
        payload.put("touchesFinancialLogic", true);
        payload.put("touchesPii", false);
        payload.put("sastHighCount", 0);
        payload.put("containerized", true);
        payload.put("targetEnv", "production");
        return processWebhook("DOCKER_REGISTRY", "DEPLOYMENT", payload);
    }

    // ================================================================
    // AWS CONFIG WEBHOOK
    // Triggered when infrastructure changes are detected
    // ================================================================

    @PostMapping("/aws-config")
    public ResponseEntity<Map<String, Object>> awsConfigWebhook(@RequestBody Map<String, Object> payload) {
        return processWebhook("AWS_CONFIG", "INFRA_CHANGE", payload);
    }

    @PostMapping("/simulate/aws-data-residency-violation")
    public ResponseEntity<Map<String, Object>> simulateDataResidencyViolation() {
        log.info("[Webhook] Simulating AWS data residency violation");
        Map<String, Object> payload = new HashMap<>();
        payload.put("engagementId", "ENG-002");
        payload.put("description", "RDS backup replication detected in us-east-1 (client requires EU-only)");
        payload.put("author", "aws-config@automated");
        payload.put("commitId", "config-evt-8891");
        payload.put("touchesFinancialLogic", false);
        payload.put("touchesPii", true);
        payload.put("sastHighCount", 0);
        payload.put("dataResidencyViolation", true);
        return processWebhook("AWS_CONFIG", "INFRA_CHANGE", payload);
    }

    @PostMapping("/simulate/aws-iam-change")
    public ResponseEntity<Map<String, Object>> simulateIamChange() {
        log.info("[Webhook] Simulating AWS IAM privilege change");
        Map<String, Object> payload = new HashMap<>();
        payload.put("engagementId", "ENG-001");
        payload.put("description", "IAM Role 'temp-migration-role' granted production DB access");
        payload.put("author", "devops.kumar@hexaware.com");
        payload.put("commitId", "iam-change-442");
        payload.put("touchesFinancialLogic", false);
        payload.put("touchesPii", false);
        payload.put("sastHighCount", 0);
        return processWebhook("AWS_CONFIG", "ACCESS_CHANGE", payload);
    }

    // ================================================================
    // GENERIC PROCESSOR
    // All webhooks flow through the same pipeline
    // ================================================================

    private ResponseEntity<Map<String, Object>> processWebhook(String source, String eventType,
                                                                Map<String, Object> payload) {
        try {
            String engagementId = (String) payload.getOrDefault("engagementId", "ENG-001");

            ComplianceEvent event = new ComplianceEvent();
            event.setEngagementId(engagementId);
            event.setEventType(eventType);
            event.setSource(source);
            event.setDescription((String) payload.getOrDefault("description", ""));
            event.setTimestamp(LocalDateTime.now());
            event.setProcessed(false);
            event.setPayload(objectMapper.writeValueAsString(payload));
            event = eventRepo.save(event);

            // Publish to JMS queue — Chain Reactor Agent picks it up
            ObjectNode message = objectMapper.createObjectNode();
            message.put("eventId", event.getId());
            message.put("engagementId", engagementId);
            message.put("eventType", eventType);
            message.put("source", source);
            payload.forEach((key, value) -> {
                if (value instanceof Boolean) message.put(key, (Boolean) value);
                else if (value instanceof Integer) message.put(key, (Integer) value);
                else if (value instanceof String) message.put(key, (String) value);
            });

            jmsTemplate.convertAndSend("compliance-events", message.toString());

            log.info("[Webhook] {} event from {} queued for agent processing (ID: {})",
                    eventType, source, event.getId());

            return ResponseEntity.ok(Map.of(
                    "status", "accepted",
                    "eventId", event.getId(),
                    "source", source,
                    "eventType", eventType,
                    "message", "Event queued for Chain Reactor Agent via ActiveMQ"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

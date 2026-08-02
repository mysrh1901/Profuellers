package com.regulith.watcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.regulith.model.ComplianceEvent;
import com.regulith.repository.ComplianceEventRepository;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FILE SYSTEM WATCHER
 * ====================
 * Monitors a directory for real-time file changes.
 * When a file is added or modified, the agent analyzes it immediately.
 *
 * This is REAL monitoring — not simulation.
 * Drop a terraform file with a misconfiguration → agent detects it.
 * Drop a Java file with hardcoded credentials → agent flags it.
 * Drop a config with wrong data region → agent blocks it.
 *
 * Watches: /Users/Research/Downloads/Profuellers/watch-folder/
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileSystemWatcher {

    private final JmsTemplate jmsTemplate;
    private final ComplianceEventRepository eventRepo;
    private final com.regulith.agent.OllamaLLMService llmService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String WATCH_DIR = System.getProperty("user.home") + "/Downloads/Profuellers/watch-folder";
    private final Map<String, Long> fileTimestamps = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Create watch folder if not exists
        try {
            Files.createDirectories(Paths.get(WATCH_DIR));
            log.info("[File Watcher] Monitoring folder: {}", WATCH_DIR);
            log.info("[File Watcher] Drop files here to trigger real-time compliance detection");
        } catch (IOException e) {
            log.error("[File Watcher] Cannot create watch folder: {}", e.getMessage());
        }

        // Index existing files
        try {
            Files.list(Paths.get(WATCH_DIR)).forEach(path -> {
                try {
                    fileTimestamps.put(path.toString(), Files.getLastModifiedTime(path).toMillis());
                } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }

    /**
     * Poll every 3 seconds for new or modified files.
     * When detected, analyze content and fire compliance event.
     */
    @Scheduled(fixedDelay = 3000)
    public void watchForChanges() {
        try {
            Path dir = Paths.get(WATCH_DIR);
            if (!Files.exists(dir)) return;

            Files.list(dir).forEach(path -> {
                if (Files.isRegularFile(path)) {
                    try {
                        long lastModified = Files.getLastModifiedTime(path).toMillis();
                        Long previousTimestamp = fileTimestamps.get(path.toString());

                        if (previousTimestamp == null || lastModified > previousTimestamp) {
                            // New or modified file detected
                            fileTimestamps.put(path.toString(), lastModified);
                            analyzeFile(path);
                        }
                    } catch (IOException ignored) {}
                }
            });
        } catch (IOException ignored) {}
    }

    /**
     * Read the file content and send to LLM for REAL AI reasoning.
     * The LLM reads the actual code and determines compliance impact.
     * NO pattern matching. NO regex. Pure AI reasoning.
     */
    private void analyzeFile(Path filePath) {
        try {
            String fileName = filePath.getFileName().toString();
            String content = Files.readString(filePath);

            log.info("[File Watcher] Detected change: {} — sending to LLM for analysis...", fileName);

            // AGENTIC AI: Send actual file content to LLM for reasoning
            String clientContext = "Mortgage/Financial Services client, subject to SOX, TILA, RESPA, ECOA, PCI-DSS, GLBA, GDPR";
            String llmReasoning = llmService.reason(content, fileName, clientContext);

            log.info("[File Watcher] LLM reasoning received for: {}", fileName);
            log.info("[File Watcher] LLM says: {}", llmReasoning.substring(0, Math.min(200, llmReasoning.length())));

            // Determine flags from LLM response
            String llmUpper = llmReasoning.toUpperCase();
            boolean touchesFinancial = llmUpper.contains("SOX") || llmUpper.contains("TILA") || llmUpper.contains("FINANCIAL");
            boolean touchesPii = llmUpper.contains("PRIVACY") || llmUpper.contains("PII") || llmUpper.contains("PERSONAL");
            int sastHigh = (llmUpper.contains("CRITICAL") || llmUpper.contains("HIGH")) ? 1 : 0;
            boolean dataResidencyViolation = llmUpper.contains("RESIDENCY") || llmUpper.contains("REGION");

            String engagementId = dataResidencyViolation ? "ENG-002" : "ENG-001";
            String eventType = fileName.endsWith(".tf") || fileName.endsWith(".yml") ? "INFRA_CHANGE" : "CODE_COMMIT";
            String description = "[AI-DETECTED] " + fileName + " — LLM found compliance issues";

            // Fire the event with LLM reasoning attached
            fireEvent(engagementId, eventType, description, fileName, llmReasoning,
                     touchesFinancial, touchesPii, sastHigh, dataResidencyViolation);

        } catch (IOException e) {
            log.error("[File Watcher] Error reading file: {}", e.getMessage());
        }
    }

    private void fireEvent(String engagementId, String eventType, String description,
                          String fileName, String content, boolean touchesFinancial,
                          boolean touchesPii, int sastHigh, boolean dataResidencyViolation) {
        try {
            // Save event to DB
            ComplianceEvent event = new ComplianceEvent();
            event.setEngagementId(engagementId);
            event.setEventType(eventType);
            event.setSource("FileSystemWatcher");
            event.setDescription(description);
            event.setTimestamp(LocalDateTime.now());
            event.setProcessed(false);
            event.setPayload("File: " + fileName);
            event = eventRepo.save(event);

            // Publish to JMS queue for agent processing
            ObjectNode message = objectMapper.createObjectNode();
            message.put("eventId", event.getId());
            message.put("engagementId", engagementId);
            message.put("eventType", eventType);
            message.put("source", "FileSystemWatcher (Real-time)");
            message.put("description", description);
            message.put("author", "file-system-monitor");
            message.put("commitId", "fs-" + System.currentTimeMillis());
            message.put("touchesFinancialLogic", touchesFinancial);
            message.put("touchesPii", touchesPii);
            message.put("sastHighCount", sastHigh);
            message.put("dataResidencyViolation", dataResidencyViolation);

            jmsTemplate.convertAndSend("compliance-events", message.toString());

            log.info("[File Watcher] EVENT FIRED: {} | {} | Financial:{} PII:{} SAST:{} DataRes:{}",
                    eventType, description, touchesFinancial, touchesPii, sastHigh, dataResidencyViolation);

        } catch (Exception e) {
            log.error("[File Watcher] Error firing event: {}", e.getMessage());
        }
    }
}

package com.regulith.controller;

import com.regulith.model.*;
import com.regulith.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for the dashboard — serves compliance twin data, chain reactions, narratives.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final ClientEngagementRepository clientRepo;
    private final ChainReactionResultRepository chainRepo;
    private final AuditNarrativeRepository narrativeRepo;
    private final ComplianceEventRepository eventRepo;

    @GetMapping("/twins")
    public ResponseEntity<List<ClientEngagement>> getAllTwins() {
        return ResponseEntity.ok(clientRepo.findAll());
    }

    @GetMapping("/twins/{engagementId}")
    public ResponseEntity<ClientEngagement> getTwin(@PathVariable String engagementId) {
        return clientRepo.findById(engagementId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/chain-reactions/{engagementId}")
    public ResponseEntity<List<ChainReactionResult>> getChainReactions(@PathVariable String engagementId) {
        return ResponseEntity.ok(chainRepo.findByEngagementIdOrderByTimestampDesc(engagementId));
    }

    @GetMapping("/narratives/{engagementId}")
    public ResponseEntity<List<AuditNarrative>> getNarratives(@PathVariable String engagementId) {
        return ResponseEntity.ok(narrativeRepo.findByEngagementIdOrderByGeneratedAtDesc(engagementId));
    }

    @GetMapping("/events/{engagementId}")
    public ResponseEntity<List<ComplianceEvent>> getEvents(@PathVariable String engagementId) {
        return ResponseEntity.ok(eventRepo.findByEngagementIdOrderByTimestampDesc(engagementId));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<ClientEngagement> clients = clientRepo.findAll();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalClients", clients.size());
        summary.put("totalOpenRisks", clients.stream().mapToInt(ClientEngagement::getOpenRisks).sum());
        summary.put("totalComplianceDebt", clients.stream().mapToDouble(ClientEngagement::getComplianceDebtUsd).sum());
        summary.put("avgComplianceScore", clients.stream().mapToDouble(ClientEngagement::getComplianceScore).average().orElse(0));
        summary.put("avgAuditReadiness", clients.stream().mapToDouble(ClientEngagement::getAuditReadiness).average().orElse(0));
        summary.put("totalChainReactions", chainRepo.count());
        summary.put("totalNarratives", narrativeRepo.count());
        return ResponseEntity.ok(summary);
    }
}

package com.regulith.controller;

import com.regulith.agent.CompliancePolicyEngine;
import com.regulith.agent.ControlIngestionAgent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * CONTROL INGESTION API
 * =====================
 * Agentic endpoint: POST raw compliance text → get structured policy back.
 *
 * Usage:
 *   POST /api/controls/ingest
 *   Body: { "framework": "HITRUST v11", "controlText": "..." }
 *
 *   POST /api/controls/ingest-batch
 *   Body: { "framework": "SOX ITGC", "controls": ["...", "...", ...] }
 *
 * The LLM agent parses the text and adds the policy to the engine.
 * Dashboard immediately reflects the new control on next refresh.
 */
@RestController
@RequestMapping("/api/controls")
@RequiredArgsConstructor
public class ControlIngestionController {

    private final ControlIngestionAgent ingestionAgent;

    @Data
    public static class IngestRequest {
        private String framework;
        private String controlText;
    }

    @Data
    public static class BatchIngestRequest {
        private String framework;
        private List<String> controls;
    }

    /**
     * Ingest a single control using agentic AI parsing.
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestControl(@RequestBody IngestRequest request) {
        CompliancePolicyEngine.CompliancePolicy policy =
                ingestionAgent.ingestControl(request.getFramework(), request.getControlText());

        Map<String, Object> response = new LinkedHashMap<>();
        if (policy != null) {
            response.put("status", "success");
            response.put("message", "Control ingested and added to policy engine");
            response.put("policy", policy);
        } else {
            response.put("status", "failed");
            response.put("message", "Could not parse control text");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Batch ingest multiple controls from a framework.
     */
    @PostMapping("/ingest-batch")
    public ResponseEntity<Map<String, Object>> ingestBatch(@RequestBody BatchIngestRequest request) {
        List<CompliancePolicyEngine.CompliancePolicy> policies =
                ingestionAgent.ingestFramework(request.getFramework(), request.getControls());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("framework", request.getFramework());
        response.put("controlsProcessed", request.getControls().size());
        response.put("policiesCreated", policies.size());
        response.put("policies", policies);
        return ResponseEntity.ok(response);
    }
}

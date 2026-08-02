package com.regulith.controller;

import com.regulith.graph.ComplianceKnowledgeGraph;
import com.regulith.graph.GraphRAGService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Graph API — exposes Knowledge Graph and GraphRAG capabilities.
 * Allows panel to see how graph traversal works.
 */
@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class GraphController {

    private final ComplianceKnowledgeGraph knowledgeGraph;
    private final GraphRAGService graphRAGService;

    /**
     * Show graph stats.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
                "nodes", knowledgeGraph.getNodeCount(),
                "edges", knowledgeGraph.getEdgeCount(),
                "description", "Compliance Knowledge Graph mapping code → regulations → controls → domains"
        ));
    }

    /**
     * GraphRAG query: Given file paths, traverse graph and return all compliance impacts.
     * This shows the causal reasoning path.
     */
    @PostMapping("/query")
    public ResponseEntity<GraphRAGService.GraphRAGResult> queryGraph(@RequestBody Map<String, Object> request) {
        List<String> files = (List<String>) request.getOrDefault("files", List.of());
        boolean financial = (boolean) request.getOrDefault("touchesFinancial", false);
        boolean pii = (boolean) request.getOrDefault("touchesPii", false);

        GraphRAGService.GraphRAGResult result = graphRAGService.retrieveComplianceContext(files, financial, pii);
        return ResponseEntity.ok(result);
    }

    /**
     * Quick demo: show what happens when rate_calculator is changed.
     */
    @GetMapping("/demo/rate-change")
    public ResponseEntity<GraphRAGService.GraphRAGResult> demoRateChange() {
        return ResponseEntity.ok(graphRAGService.retrieveComplianceContext(
                List.of("src/services/rate_calculator.py"),
                true, false
        ));
    }

    /**
     * Quick demo: show what happens when borrower eligibility is changed.
     */
    @GetMapping("/demo/borrower-change")
    public ResponseEntity<GraphRAGService.GraphRAGResult> demoBorrowerChange() {
        return ResponseEntity.ok(graphRAGService.retrieveComplianceContext(
                List.of("src/services/borrower_eligibility.py", "src/models/loan_application.py"),
                true, true
        ));
    }
}

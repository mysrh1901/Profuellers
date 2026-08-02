package com.regulith.controller;

import com.regulith.agent.CompliancePolicyEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * POLICY CONTROLLER
 * =================
 * Exposes the policy engine for visibility.
 * The panel can see all loaded policies, filter by vertical, and understand
 * exactly what the agent checks for each domain.
 */
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final CompliancePolicyEngine policyEngine;

    @GetMapping
    public ResponseEntity<List<CompliancePolicyEngine.CompliancePolicy>> getAllPolicies() {
        return ResponseEntity.ok(policyEngine.getAllPolicies());
    }

    @GetMapping("/verticals")
    public ResponseEntity<Set<String>> getVerticals() {
        return ResponseEntity.ok(policyEngine.getUniqueVerticals());
    }

    @GetMapping("/vertical/{vertical}")
    public ResponseEntity<List<CompliancePolicyEngine.CompliancePolicy>> getByVertical(
            @PathVariable String vertical) {
        List<CompliancePolicyEngine.CompliancePolicy> filtered = policyEngine.getAllPolicies().stream()
                .filter(p -> p.getVerticals().contains(vertical) || p.getVerticals().contains("ALL"))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<List<CompliancePolicyEngine.CompliancePolicy>> getByDomain(
            @PathVariable String domain) {
        List<CompliancePolicyEngine.CompliancePolicy> filtered = policyEngine.getAllPolicies().stream()
                .filter(p -> p.getDomain().equalsIgnoreCase(domain))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPolicySummary() {
        List<CompliancePolicyEngine.CompliancePolicy> all = policyEngine.getAllPolicies();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalPolicies", all.size());
        summary.put("verticals", policyEngine.getUniqueVerticals());
        summary.put("domains", all.stream().map(CompliancePolicyEngine.CompliancePolicy::getDomain)
                .distinct().sorted().collect(Collectors.toList()));
        summary.put("blockingPolicies", all.stream().filter(CompliancePolicyEngine.CompliancePolicy::isBlocking).count());
        summary.put("nonBlockingPolicies", all.stream().filter(p -> !p.isBlocking()).count());
        return ResponseEntity.ok(summary);
    }
}

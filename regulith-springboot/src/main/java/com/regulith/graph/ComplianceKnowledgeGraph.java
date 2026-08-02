package com.regulith.graph;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * COMPLIANCE KNOWLEDGE GRAPH
 * ==========================
 * Models the causal relationships between:
 *   Code Modules → Regulations → Controls → Obligations → Clients
 *
 * This is NOT a flat database. It's a directed weighted graph where:
 *   - Nodes = code modules, regulations, controls, obligations, clients
 *   - Edges = causal relationships (governs, requires, impacts, owned_by)
 *   - Weights = strength of causal connection
 *
 * When an event occurs, GraphRAG traverses this graph to find ALL connected
 * compliance domains — not by text similarity, but by actual regulatory relationships.
 *
 * Built with JGraphT (Apache 2.0, free, in-memory, no external server).
 */
@Service
@Slf4j
public class ComplianceKnowledgeGraph {

    private Graph<GraphNode, DefaultWeightedEdge> graph;
    private Map<String, GraphNode> nodeIndex = new HashMap<>();

    @Data
    public static class GraphNode {
        private final String id;
        private final String type;  // CODE_MODULE, REGULATION, CONTROL, OBLIGATION, CLIENT, DOMAIN
        private final String name;
        private final String description;

        @Override
        public String toString() {
            return "[" + type + "] " + name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return id.equals(((GraphNode) o).id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    @PostConstruct
    public void buildGraph() {
        graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        log.info("[Knowledge Graph] Building compliance ontology...");

        // === CODE MODULES ===
        addNode("code:rate_calculator", "CODE_MODULE", "Rate Calculator", "ARM/Fixed rate and APR calculation logic");
        addNode("code:borrower_eligibility", "CODE_MODULE", "Borrower Eligibility", "DTI, credit score, LTV checks");
        addNode("code:loan_application", "CODE_MODULE", "Loan Application", "Application data model with borrower PII");
        addNode("code:payment_service", "CODE_MODULE", "Payment Service", "Payment processing and fee calculation");
        addNode("code:disclosure_engine", "CODE_MODULE", "Disclosure Engine", "TILA/RESPA disclosure generation");
        addNode("code:auth_service", "CODE_MODULE", "Auth Service", "Authentication and access control");
        addNode("code:data_store", "CODE_MODULE", "Data Store", "Database and storage layer");

        // === REGULATIONS ===
        addNode("reg:tila", "REGULATION", "TILA (Regulation Z)", "Truth in Lending Act — APR disclosure accuracy to 1/8%");
        addNode("reg:respa", "REGULATION", "RESPA (Regulation X)", "Real Estate Settlement Procedures Act — closing cost disclosures");
        addNode("reg:ecoa", "REGULATION", "ECOA (Regulation B)", "Equal Credit Opportunity — no discrimination in lending");
        addNode("reg:hmda", "REGULATION", "HMDA (Regulation C)", "Home Mortgage Disclosure Act — data reporting requirements");
        addNode("reg:sox", "REGULATION", "SOX Section 404", "Sarbanes-Oxley — internal controls over financial reporting");
        addNode("reg:pci", "REGULATION", "PCI-DSS", "Payment Card Industry Data Security Standard");
        addNode("reg:glba", "REGULATION", "GLBA", "Gramm-Leach-Bliley Act — financial privacy");
        addNode("reg:gdpr", "REGULATION", "GDPR", "General Data Protection Regulation — EU data privacy");
        addNode("reg:ccpa", "REGULATION", "CCPA", "California Consumer Privacy Act");
        addNode("reg:hipaa", "REGULATION", "HIPAA", "Health Insurance Portability and Accountability Act");
        addNode("reg:dora", "REGULATION", "DORA", "Digital Operational Resilience Act — EU financial ICT");

        // === CONTROLS ===
        addNode("ctrl:itgc_cm01", "CONTROL", "ITGC-CM-01", "Change Management — Dual Approval Required");
        addNode("ctrl:itgc_cm02", "CONTROL", "ITGC-CM-02", "Segregation of Duties — Author != Approver");
        addNode("ctrl:itgc_sd01", "CONTROL", "ITGC-SD-01", "SDLC — Security Testing Before Release");
        addNode("ctrl:itgc_ac01", "CONTROL", "ITGC-AC-01", "Access Control — Privileged Access Review");
        addNode("ctrl:pci_6_5", "CONTROL", "PCI-DSS 6.5", "Secure Coding — No OWASP Top 10 vulnerabilities");
        addNode("ctrl:encrypt_rest", "CONTROL", "Encryption at Rest", "All PII encrypted at rest (AES-256)");
        addNode("ctrl:encrypt_transit", "CONTROL", "Encryption in Transit", "TLS 1.2+ for all data transmission");
        addNode("ctrl:data_residency", "CONTROL", "Data Residency", "Data stays in contractually specified region");

        // === COMPLIANCE DOMAINS ===
        addNode("domain:sox", "DOMAIN", "SOX", "Sarbanes-Oxley compliance domain");
        addNode("domain:security", "DOMAIN", "SECURITY", "Application and infrastructure security");
        addNode("domain:regulatory", "DOMAIN", "REGULATORY", "Financial regulatory compliance");
        addNode("domain:fair_lending", "DOMAIN", "FAIR_LENDING", "Fair lending and anti-discrimination");
        addNode("domain:contractual", "DOMAIN", "CONTRACTUAL", "Client contractual obligations");
        addNode("domain:privacy", "DOMAIN", "PRIVACY", "Data privacy and protection");
        addNode("domain:infrastructure", "DOMAIN", "INFRASTRUCTURE", "Infrastructure security posture");
        addNode("domain:audit", "DOMAIN", "AUDIT", "Audit evidence and readiness");

        // === OBLIGATIONS (from client contracts) ===
        addNode("obl:vuln_48h", "OBLIGATION", "Critical Vuln 48h SLA", "Remediate critical vulns within 48 hours");
        addNode("obl:change_notify", "OBLIGATION", "Change Notification", "Notify client 48h before financial system changes");
        addNode("obl:data_us_only", "OBLIGATION", "US Data Residency", "All data must remain in US regions");
        addNode("obl:data_eu_only", "OBLIGATION", "EU Data Residency", "All data must remain in EU regions");

        // === BUILD EDGES (Causal Relationships) ===

        // Code → Regulations (what regulations govern this code)
        addEdge("code:rate_calculator", "reg:tila", 1.0);      // Rate calc governed by TILA
        addEdge("code:rate_calculator", "reg:sox", 0.9);        // Financial system → SOX
        addEdge("code:rate_calculator", "reg:ecoa", 0.8);       // Pricing → Fair Lending
        addEdge("code:borrower_eligibility", "reg:ecoa", 1.0);  // Eligibility → Fair Lending
        addEdge("code:borrower_eligibility", "reg:hmda", 0.7);  // Eligibility data → HMDA reporting
        addEdge("code:loan_application", "reg:glba", 0.9);      // PII → GLBA privacy
        addEdge("code:loan_application", "reg:ccpa", 0.8);      // PII → CCPA
        addEdge("code:loan_application", "reg:gdpr", 0.8);      // PII → GDPR (if EU client)
        addEdge("code:payment_service", "reg:pci", 1.0);        // Payments → PCI-DSS
        addEdge("code:payment_service", "reg:sox", 0.9);        // Financial → SOX
        addEdge("code:disclosure_engine", "reg:tila", 1.0);     // Disclosures → TILA
        addEdge("code:disclosure_engine", "reg:respa", 1.0);    // Disclosures → RESPA
        addEdge("code:data_store", "reg:gdpr", 0.9);            // Storage → GDPR
        addEdge("code:data_store", "reg:glba", 0.8);            // Storage → GLBA

        // Regulations → Controls (what controls satisfy this regulation)
        addEdge("reg:sox", "ctrl:itgc_cm01", 1.0);
        addEdge("reg:sox", "ctrl:itgc_cm02", 1.0);
        addEdge("reg:sox", "ctrl:itgc_sd01", 0.9);
        addEdge("reg:sox", "ctrl:itgc_ac01", 0.8);
        addEdge("reg:pci", "ctrl:pci_6_5", 1.0);
        addEdge("reg:pci", "ctrl:encrypt_transit", 0.9);
        addEdge("reg:gdpr", "ctrl:encrypt_rest", 0.9);
        addEdge("reg:gdpr", "ctrl:data_residency", 1.0);
        addEdge("reg:glba", "ctrl:encrypt_rest", 0.8);

        // Regulations → Domains
        addEdge("reg:tila", "domain:regulatory", 1.0);
        addEdge("reg:respa", "domain:regulatory", 1.0);
        addEdge("reg:ecoa", "domain:fair_lending", 1.0);
        addEdge("reg:hmda", "domain:fair_lending", 0.8);
        addEdge("reg:sox", "domain:sox", 1.0);
        addEdge("reg:pci", "domain:security", 1.0);
        addEdge("reg:gdpr", "domain:privacy", 1.0);
        addEdge("reg:ccpa", "domain:privacy", 1.0);
        addEdge("reg:glba", "domain:privacy", 0.9);
        addEdge("reg:dora", "domain:infrastructure", 1.0);
        addEdge("reg:hipaa", "domain:privacy", 1.0);

        // Controls → Domains
        addEdge("ctrl:itgc_cm01", "domain:sox", 1.0);
        addEdge("ctrl:itgc_cm02", "domain:sox", 1.0);
        addEdge("ctrl:itgc_sd01", "domain:security", 1.0);
        addEdge("ctrl:pci_6_5", "domain:security", 1.0);
        addEdge("ctrl:data_residency", "domain:contractual", 1.0);
        addEdge("ctrl:encrypt_rest", "domain:privacy", 0.9);

        // Obligations → Domains
        addEdge("obl:vuln_48h", "domain:contractual", 1.0);
        addEdge("obl:change_notify", "domain:contractual", 1.0);
        addEdge("obl:data_us_only", "domain:contractual", 1.0);
        addEdge("obl:data_eu_only", "domain:contractual", 1.0);

        log.info("[Knowledge Graph] Built: {} nodes, {} edges",
                graph.vertexSet().size(), graph.edgeSet().size());
    }

    /**
     * GRAPH RAG: Given a code module, traverse the graph to find all
     * connected compliance domains with causal paths.
     */
    public List<CausalPath> findComplianceImpact(String codeModuleId) {
        GraphNode source = nodeIndex.get(codeModuleId);
        if (source == null) return Collections.emptyList();

        List<CausalPath> paths = new ArrayList<>();
        DijkstraShortestPath<GraphNode, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(graph);

        // Find paths to all domain nodes
        for (GraphNode node : graph.vertexSet()) {
            if ("DOMAIN".equals(node.getType())) {
                try {
                    GraphPath<GraphNode, DefaultWeightedEdge> path = dijkstra.getPath(source, node);
                    if (path != null) {
                        List<String> pathNodes = path.getVertexList().stream()
                                .map(GraphNode::getName)
                                .collect(Collectors.toList());

                        paths.add(new CausalPath(
                                node.getName(),
                                node.getId().replace("domain:", ""),
                                pathNodes,
                                path.getWeight(),
                                buildCausalExplanation(path)
                        ));
                    }
                } catch (Exception e) {
                    // No path exists — domain not affected
                }
            }
        }

        // Sort by weight (lower weight = stronger causal connection)
        paths.sort(Comparator.comparingDouble(CausalPath::getWeight));
        return paths;
    }

    /**
     * Find which code modules are affected by a set of file paths.
     */
    public List<String> identifyCodeModules(List<String> filePaths) {
        List<String> modules = new ArrayList<>();
        for (String file : filePaths) {
            String lower = file.toLowerCase();
            if (lower.contains("rate") || lower.contains("apr") || lower.contains("interest"))
                modules.add("code:rate_calculator");
            if (lower.contains("borrower") || lower.contains("eligib") || lower.contains("dti"))
                modules.add("code:borrower_eligibility");
            if (lower.contains("loan") || lower.contains("application"))
                modules.add("code:loan_application");
            if (lower.contains("payment") || lower.contains("fee"))
                modules.add("code:payment_service");
            if (lower.contains("disclos"))
                modules.add("code:disclosure_engine");
            if (lower.contains("auth") || lower.contains("login") || lower.contains("iam"))
                modules.add("code:auth_service");
            if (lower.contains("data") || lower.contains("store") || lower.contains("db") || lower.contains("s3"))
                modules.add("code:data_store");
        }
        return modules.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Build a human-readable causal explanation from a graph path.
     */
    private String buildCausalExplanation(GraphPath<GraphNode, DefaultWeightedEdge> path) {
        List<GraphNode> nodes = path.getVertexList();
        StringBuilder explanation = new StringBuilder();
        for (int i = 0; i < nodes.size() - 1; i++) {
            GraphNode from = nodes.get(i);
            GraphNode to = nodes.get(i + 1);
            if (i > 0) explanation.append(" → ");
            explanation.append(from.getName());
            if (i == nodes.size() - 2) {
                explanation.append(" → ").append(to.getName());
            }
        }
        return explanation.toString();
    }

    public int getNodeCount() { return graph.vertexSet().size(); }
    public int getEdgeCount() { return graph.edgeSet().size(); }

    private void addNode(String id, String type, String name, String description) {
        GraphNode node = new GraphNode(id, type, name, description);
        graph.addVertex(node);
        nodeIndex.put(id, node);
    }

    private void addEdge(String fromId, String toId, double weight) {
        GraphNode from = nodeIndex.get(fromId);
        GraphNode to = nodeIndex.get(toId);
        if (from != null && to != null) {
            DefaultWeightedEdge edge = graph.addEdge(from, to);
            if (edge != null) graph.setEdgeWeight(edge, weight);
        }
    }

    @Data
    public static class CausalPath {
        private final String domainName;
        private final String domainId;
        private final List<String> pathNodes;
        private final double weight;
        private final String causalExplanation;
    }
}

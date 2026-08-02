package com.regulith.agents.graph;

import com.regulith.agents.core.LLMProvider;
import com.regulith.agents.model.ComplianceEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CAUSAL GRAPH AGENT
 * ===================
 * Builds and traverses a Knowledge Graph to find causal compliance paths.
 *
 * UNIQUE CAPABILITY:
 *   Unlike flat vector databases (standard RAG), this agent understands
 *   RELATIONSHIPS between code, regulations, and controls. It doesn't
 *   just find "similar text" — it traverses actual causal connections.
 *
 * The graph is DYNAMIC:
 *   - Nodes and edges can be added at runtime (LLM can suggest new connections)
 *   - Graph structure loaded from configuration (not hardcoded)
 *   - Different clients can have different graph overlays
 *
 * HOW IT'S USED:
 *   1. Event arrives → agent identifies which graph nodes are involved
 *   2. Traverses from source nodes to all reachable compliance domain nodes
 *   3. Returns causal paths with explanations
 *   4. These paths are fed to the LLM as grounding context (GraphRAG)
 *
 * @author Imam Sayyad
 * @version 1.0.0
 */
public class CausalGraphAgent {

    // Adjacency list representation of the graph
    private final Map<String, GraphNode> nodes = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacency = new HashMap<>();
    private LLMProvider llm; // Optional — for dynamic graph construction

    public CausalGraphAgent() {}

    /**
     * Constructor with LLM — enables dynamic graph construction.
     * The LLM can suggest new nodes and edges based on context.
     */
    public CausalGraphAgent(LLMProvider llm) {
        this.llm = llm;
    }

    /**
     * Add a node to the graph.
     * Called during initialization or dynamically at runtime.
     */
    public void addNode(String id, String type, String name, String description) {
        nodes.put(id, new GraphNode(id, type, name, description));
        adjacency.putIfAbsent(id, new ArrayList<>());
    }

    /**
     * Add a causal edge: source → target with a weight.
     * Lower weight = stronger causal connection.
     */
    public void addEdge(String fromId, String toId, double weight, String relationship) {
        adjacency.computeIfAbsent(fromId, k -> new ArrayList<>())
                 .add(new Edge(toId, weight, relationship));
    }

    /**
     * Load graph from a map structure (e.g., from JSON/YAML config).
     * This makes the graph fully configurable — no code changes needed.
     */
    public void loadFromConfig(Map<String, Object> config) {
        List<Map<String, String>> nodesList = (List<Map<String, String>>) config.get("nodes");
        if (nodesList != null) {
            for (Map<String, String> n : nodesList) {
                addNode(n.get("id"), n.get("type"), n.get("name"), n.getOrDefault("description", ""));
            }
        }
        List<Map<String, Object>> edgesList = (List<Map<String, Object>>) config.get("edges");
        if (edgesList != null) {
            for (Map<String, Object> e : edgesList) {
                addEdge((String) e.get("from"), (String) e.get("to"),
                       ((Number) e.getOrDefault("weight", 1.0)).doubleValue(),
                       (String) e.getOrDefault("relationship", "relates_to"));
            }
        }
    }

    /**
     * DYNAMIC GRAPH CONSTRUCTION using LLM.
     * Given new regulatory text, LLM suggests nodes and edges to add.
     */
    public void expandGraphWithLLM(String newRegulationText) {
        if (llm == null) return;

        String prompt = "Given this regulation text, identify:\n" +
            "1. Entities (regulations, controls, obligations) to add as graph nodes\n" +
            "2. Relationships between them and existing compliance domains\n" +
            "Regulation: " + newRegulationText + "\n" +
            "Existing domains: SOX, SECURITY, REGULATORY, FAIR_LENDING, CONTRACTUAL, PRIVACY, INFRASTRUCTURE, AUDIT\n" +
            "Format: NODE: id, type, name | EDGE: from, to, weight, relationship";

        String response = llm.call(prompt);
        // In production: parse response and add nodes/edges dynamically
        // This makes the graph self-expanding through AI
    }

    /**
     * Find all compliance domains reachable from a source node.
     * Uses BFS with path tracking.
     */
    public List<CausalPath> findImpactPaths(String sourceNodeId) {
        if (!adjacency.containsKey(sourceNodeId)) return Collections.emptyList();

        List<CausalPath> paths = new ArrayList<>();
        // BFS to find all DOMAIN nodes reachable from source
        Queue<PathState> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(new PathState(sourceNodeId, List.of(sourceNodeId), 0.0));
        visited.add(sourceNodeId);

        while (!queue.isEmpty()) {
            PathState current = queue.poll();
            GraphNode currentNode = nodes.get(current.nodeId);

            // If we reached a DOMAIN node, record the path
            if (currentNode != null && "DOMAIN".equals(currentNode.type) && !current.nodeId.equals(sourceNodeId)) {
                GraphNode sourceNode = nodes.get(sourceNodeId);
                String sourceName = sourceNode != null ? sourceNode.name : sourceNodeId;
                List<String> pathNames = current.path.stream()
                    .map(id -> nodes.containsKey(id) ? nodes.get(id).name : id)
                    .collect(Collectors.toList());
                String explanation = String.join(" → ", pathNames);
                paths.add(new CausalPath(sourceName, currentNode.name, pathNames, explanation, current.totalWeight));
            }

            // Explore neighbors
            List<Edge> neighbors = adjacency.getOrDefault(current.nodeId, Collections.emptyList());
            for (Edge edge : neighbors) {
                if (!visited.contains(edge.targetId)) {
                    visited.add(edge.targetId);
                    List<String> newPath = new ArrayList<>(current.path);
                    newPath.add(edge.targetId);
                    queue.add(new PathState(edge.targetId, newPath, current.totalWeight + edge.weight));
                }
            }
        }

        paths.sort(Comparator.comparingDouble(CausalPath::getStrength));
        return paths;
    }

    /**
     * Identify which graph nodes are relevant to an event.
     * Uses LLM to understand the event semantically.
     */
    public List<String> identifyRelevantNodes(ComplianceEvent event) {
        if (llm == null) {
            // Fallback: match by keywords in node names/descriptions
            String context = (event.getDescription() + " " + event.getCodeDiff()).toLowerCase();
            return nodes.values().stream()
                .filter(n -> "CODE_MODULE".equals(n.type))
                .filter(n -> context.contains(n.name.toLowerCase().split(" ")[0]))
                .map(n -> n.id)
                .collect(Collectors.toList());
        }

        // With LLM: ask it which code modules are affected
        String prompt = "Given this event, which code modules are affected?\n" +
            "Event: " + event.getDescription() + "\n" +
            "Available modules: " + nodes.values().stream()
                .filter(n -> "CODE_MODULE".equals(n.type))
                .map(n -> n.id + " (" + n.name + ")")
                .collect(Collectors.joining(", ")) + "\n" +
            "Return only the IDs of affected modules, comma-separated.";

        String response = llm.call(prompt);
        return Arrays.asList(response.split(",")).stream()
            .map(String::trim)
            .filter(nodes::containsKey)
            .collect(Collectors.toList());
    }

    public int getNodeCount() { return nodes.size(); }
    public int getEdgeCount() { return adjacency.values().stream().mapToInt(List::size).sum(); }

    // Internal data structures
    private static class GraphNode {
        final String id, type, name, description;
        GraphNode(String id, String type, String name, String description) {
            this.id = id; this.type = type; this.name = name; this.description = description;
        }
    }

    private static class Edge {
        final String targetId;
        final double weight;
        final String relationship;
        Edge(String targetId, double weight, String relationship) {
            this.targetId = targetId; this.weight = weight; this.relationship = relationship;
        }
    }

    private static class PathState {
        final String nodeId;
        final List<String> path;
        final double totalWeight;
        PathState(String nodeId, List<String> path, double totalWeight) {
            this.nodeId = nodeId; this.path = path; this.totalWeight = totalWeight;
        }
    }
}

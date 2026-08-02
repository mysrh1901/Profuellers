package com.regulith.agents.graph;

import java.util.List;

/**
 * Represents a causal path through the Knowledge Graph.
 * Shows WHY a compliance domain is affected by a specific code change.
 *
 * Example: "Rate Calculator → TILA (Regulation Z) → REGULATORY"
 * This means: the rate calculator module is governed by TILA, which belongs to the REGULATORY domain.
 */
public class CausalPath {

    private final String sourceName;
    private final String targetDomain;
    private final List<String> pathNodes;
    private final String explanation;
    private final double strength;  // Lower = stronger causal connection

    public CausalPath(String sourceName, String targetDomain,
                     List<String> pathNodes, String explanation, double strength) {
        this.sourceName = sourceName;
        this.targetDomain = targetDomain;
        this.pathNodes = pathNodes;
        this.explanation = explanation;
        this.strength = strength;
    }

    public String getSourceName() { return sourceName; }
    public String getTargetDomain() { return targetDomain; }
    public List<String> getPathNodes() { return pathNodes; }
    public String getExplanation() { return explanation; }
    public double getStrength() { return strength; }

    @Override
    public String toString() {
        return String.join(" → ", pathNodes) + " [strength: " + strength + "]";
    }
}

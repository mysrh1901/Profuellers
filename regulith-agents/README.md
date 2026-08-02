# Regulith Agents — Reusable AI Agent Library

## What This Is

A set of standalone, reusable AI agents for autonomous compliance intelligence.
These agents are not tied to any specific project, vertical, or technology stack.
Drop them into any Java application and they work.

## Agents

| Agent | Purpose |
|-------|---------|
| ComplianceReasonerAgent | Sends event context + compliance profile to LLM. LLM reasons which domains are affected. No hardcoded rules. |
| CausalGraphAgent | Builds and traverses a Knowledge Graph dynamically. Finds causal paths between any code change and compliance impact. |
| EvidenceNarratorAgent | Generates audit-grade evidence narratives using LLM + graph context. Grounded in retrieved regulatory text. |
| DriftSentinelAgent | Continuously compares expected vs actual state. Detects silent compliance degradation. |

## Key Design Principles

1. NO HARDCODED RULES — LLM does all reasoning
2. PORTABLE — works in any vertical (mortgage, healthcare, retail, manufacturing)
3. PLUGGABLE — accepts any event source, any LLM provider, any graph store
4. EXPLAINABLE — every decision has a traceable causal path
5. ORIGINAL IP — these agents do not exist in any product on the market

## Usage

```java
// Initialize with any LLM provider
LLMProvider llm = new BedrockClaudeProvider(region, modelId);
// Or: new OllamaProvider("localhost:11434", "llama3");
// Or: new MockLLMProvider(); // for testing

// Create agents
ComplianceReasonerAgent reasoner = new ComplianceReasonerAgent(llm);
CausalGraphAgent graphAgent = new CausalGraphAgent();
EvidenceNarratorAgent narrator = new EvidenceNarratorAgent(llm);

// Feed an event — agent reasons dynamically
ComplianceEvent event = ComplianceEvent.builder()
    .codeDiff("+ public double calculateAPR(...)  ...")
    .clientProfile(clientProfile)
    .build();

// Agent reasons (no hardcoded rules — LLM decides)
ReasoningResult result = reasoner.analyze(event);

// Get causal explanation from graph
List<CausalPath> paths = graphAgent.findImpactPaths(event);

// Generate audit narrative
String narrative = narrator.generate(event, result, paths);
```

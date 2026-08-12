# KAVACH AI — Code Walkthrough Guide

## Complete Class-by-Class Description

---

## THE FLOW (Simple Version)

```
YOU edit LoanService.java and save
       ↓
PYTHON: dashboard.py detects the file was modified (checks every 5 seconds)
       ↓
PYTHON: ai_scanner.py reads the file content and finds violations
       ↓
PYTHON: ai_scanner.py sends event to Spring Boot via HTTP POST
       ↓
JAVA: WebhookController.java receives the event, saves to database
       ↓
JAVA: WebhookController puts a message in ActiveMQ queue
       ↓
JAVA: ChainReactorAgent.java automatically picks up from queue
       ↓
JAVA: ChainReactorAgent calls CompliancePolicyEngine.java (checks 26+ rules)
       ↓
JAVA: ChainReactorAgent calls GraphRAGService.java (traces causal paths)
       ↓
JAVA: ChainReactorAgent calls AuditNarratorAgent.java (writes evidence)
       ↓
JAVA: ChainReactorAgent updates client compliance score in database
       ↓
PYTHON: dashboard.py shows updated findings, score, and status on UI
```

---

## PYTHON FILES (Dashboard + Scanner)

---

### 1. `dashboard.py` — The Web Server

**What it does:** Runs a simple HTTP server on port 8080. Every time the browser loads the page, it scans files and generates fresh HTML.

**Key parts:**
- `get_live_scan()` — Called on every page request. Creates an AIScanner, scans `watch-folder/`, returns findings.
- `generate_html()` — Takes findings and builds the full HTML dashboard page.
- `DashboardHandler` — Simple HTTP handler that serves the generated HTML.
- Auto-refresh: JavaScript on the page calls `fetch('/')` every 5 seconds and replaces the page content (no full reload, so scroll position is preserved).

**How it detects changes:** It doesn't watch for changes in real-time. Instead, every time the browser refreshes (every 5 seconds), it scans all files in `watch-folder/` and checks their "last modified time." If a file was modified in the last 10 minutes, it scans it and marks findings as "recent."

---

### 2. `ai_scanner.py` — The AI-Powered Code Scanner

**What it does:** Reads Java files and identifies compliance violations. Has two modes:

- **AI Mode (Ollama running):** Sends actual file content to our LLM. LLM reads code and returns structured findings with domain, severity, regulation references.
- **Heuristic Mode (Ollama not running):** Falls back to pattern matching — regex for SQL injection, entropy analysis for secrets, pattern detection for System.out, weak crypto, etc.

**Key parts:**
- `AIScanner.scan_recent(directory)` — Finds all files in directory, analyzes each one.
- `_analyze_with_ai()` — Sends file to Ollama LLM, parses response into findings.
- `_analyze_with_heuristic()` — Falls back to java_scanner.py regex rules.
- `_fire_event_to_backend()` — After scanning, sends a summary event to Spring Boot (port 9090) so the Java agents can process it too.

**How it finds violations (heuristic mode):**
- SQL injection: looks for `"SELECT/UPDATE/INSERT..." + variable` pattern
- Hardcoded secrets: measures entropy (randomness) of string literals — high entropy = likely a secret
- Insecure logging: `System.out.println` pattern
- Weak crypto: `DES`, `MD5`, `SHA-1` keywords
- SSL bypass: `TrustAll`, `ALLOW_ALL` patterns

---

### 3. `java_scanner.py` — Heuristic Pattern Scanner

**What it does:** The fallback scanner used when Ollama is not running. Uses regex and entropy analysis.

**Key parts:**
- `_shannon_entropy()` — Calculates randomness of a string. High entropy (>3.5) + mixed characters = likely a secret.
- `_looks_like_secret()` — Combines entropy + character analysis + known secret prefixes (sk-, AKIA, eyJ) to decide if a string is a secret.
- `_scan_file()` — Goes through file line by line, applies 10 detection rules.

**The 10 detection rules:**
1. Hardcoded secrets (entropy-based)
2. System.out/err (audit trail bypass)
3. SQL string concatenation (injection)
4. Sensitive data in log statements
5. SSL/TLS certificate bypass
6. Weak random (java.util.Random)
7. Insecure HTTP connections
8. Empty catch blocks (error swallowing)
9. Runtime.exec (command injection)
10. Deprecated crypto (DES, MD5, SHA-1, RC4)

---

### 4. `dashboard_utils.py` — Data Provider for Dashboard

**What it does:** Computes all the numbers shown on the dashboard. Takes live findings and calculates scores, risk, debt, trends.

**Key parts:**
- `DashboardData.__init__()` — Sets healthy baseline (87% score, $0 debt, STABLE trend).
- `get_header_metrics()` — Calculates the 5 numbers in the header bar (agents, domains, time, audit %, risk $).
- `get_twin_cards_html()` — Generates the Digital Twin panel (score circle, risks, domain scores).
- `get_gate_data()` — Determines if deployment is blocked (any CRITICAL finding = blocked).
- `get_narrative_compact()` — Generates audit narrator content (clean message or findings list).
- `get_drift_cards_html()` — Shows drift items (only when findings exist).
- `_live_score_penalty()` — Calculates how much score drops based on findings (CRITICAL=1.5, HIGH=0.8, MEDIUM=0.3, LOW=0.1).
- `_live_risk_cost()` — Calculates $ risk (CRITICAL=$150K, HIGH=$50K, MEDIUM=$15K, LOW=$5K per finding).

---

## JAVA FILES (Spring Boot Backend)

---

### 5. `RegulithApplication.java` — Main Spring Boot App

**What it does:** Starts the Spring Boot application on port 9090. Enables JMS (message queue) and scheduling (file watcher polling).

**One-liner:** The entry point. Like `public static void main` for the whole backend.

---

### 6. `WebhookController.java` — Event Receiver

**What it does:** Accepts HTTP POST requests from external systems (Git, Jenkins, Jira, AWS, or our Python scanner). Creates a ComplianceEvent record in the database and puts a message in the ActiveMQ queue.

**Key methods:**
- `POST /api/webhooks/jenkins` — Receives Jenkins events
- `POST /api/webhooks/jira` — Receives Jira events
- `POST /api/webhooks/aws-config` — Receives AWS events
- `POST /api/events/simulate/code-commit` — Simulates a code commit event for demo
- `processWebhook()` — Common method: saves event to DB → publishes to JMS queue

**Why this matters:** This is how ANY system can trigger KAVACH. You configure a webhook URL in Git/Jenkins/Jira pointing to our API. When something happens in those tools, they POST here, and our agents take over.

---

### 7. `ChainReactorAgent.java` — THE BRAIN (Central Agent)

**What it does:** This is the most important class. It listens to the ActiveMQ queue. When an event arrives, it orchestrates the entire compliance analysis.

**How it works (step by step):**

1. `@JmsListener(destination = "compliance-events")` — Spring automatically calls `processEvent()` when any message appears in the queue. No human triggers this.

2. `buildEvaluationContext()` — Merges event data (what happened) with client profile (what frameworks apply). Creates a context map like:
   ```
   {eventType: "CODE_COMMIT", touchesFinancialLogic: true, 
    soxApplicable: true, frameworks: "SOX,TILA,PCI-DSS"}
   ```

3. `policyEngine.evaluate(context)` — Passes context to the Policy Engine. Gets back a list of ALL triggered policies (e.g., 5 out of 26 policies fire).

4. `graphRAGService.retrieveComplianceContext()` — Traverses the Knowledge Graph to find causal paths (which regulations connect to which penalties).

5. `narratorAgent.generateNarrative()` — Tells the Audit Narrator to write evidence.

6. `updateClientScore()` — Calculates new compliance score:
   - CRITICAL finding = -8 points
   - HIGH = -5 points
   - MEDIUM = -2.5 points
   - Saves new score to database.

**Why "Chain Reactor":** Because one event triggers a CHAIN of impacts across multiple compliance domains — like a nuclear chain reaction.

---

### 8. `CompliancePolicyEngine.java` — The Rule Engine

**What it does:** Stores 26+ compliance policies and evaluates them against events. This is NOT hardcoded if/else logic — each policy is a data object with a trigger condition.

**How it works:**

Each policy looks like:
```java
Policy {
  id: "ITGC-CM-06",
  domain: "CHANGE_MANAGEMENT",
  name: "Code Review Required",
  triggerCondition: "eventType == 'CODE_COMMIT'",
  severity: "HIGH",
  action: "Verify PR has reviewer != author",
  blocking: true
}
```

When `evaluate(context)` is called, it loops through ALL 26 policies and checks if the trigger condition matches the event context. Returns only the ones that fire.

**Why this is NOT hardcoded:** To add a new policy, you just add a new data entry. The engine code never changes. The Control Ingestion Agent can add policies at runtime without restart.

---

### 9. `AuditNarratorAgent.java` — Evidence Writer

**What it does:** Called by Chain Reactor after policies are evaluated. Generates a formal audit narrative documenting what happened, what was affected, and what actions are needed.

**How it works:**
1. Receives: event data + list of triggered policies + client info
2. Formats it into a prompt for the LLM (BedrockClaudeService)
3. LLM writes a professional audit narrative (or uses template if LLM unavailable)
4. Saves narrative to database (AuditNarrative table)

**Why this matters:** Auditors spend 3-4 weeks manually writing this documentation. This agent does it automatically, every time something happens.

---

### 10. `OllamaLLMService.java` — Local LLM Interface

**What it does:** Sends prompts to our locally-running Ollama model (kavach-compliance-v1) and gets back compliance analysis.

**Key methods:**
- `reason(fileContent, fileName, clientContext)` — Sends file content to LLM, gets back structured compliance findings (DOMAIN, SEVERITY, FINDING, REGULATION, ACTION).
- `generateNarrative()` — Asks LLM to write an audit narrative.
- `isAvailable()` — Checks if Ollama is running on localhost:11434.
- `buildCompliancePrompt()` — Constructs the prompt that tells the LLM what to look for and how to format the response.

**The prompt tells the LLM:**
"You are a compliance intelligence agent. Analyze this file. For each issue, tell me: DOMAIN, SEVERITY, FINDING, REGULATION, ACTION, BLOCKING. Only report real issues. Reference actual regulations."

---

### 11. `BedrockClaudeService.java` — Cloud LLM Interface

**What it does:** Same as OllamaLLMService but calls Claude on AWS Bedrock (for production). Has fallback to local template-based generation when AWS isn't configured.

**Key methods:**
- `analyzeCodeDiff()` — Send code diff to Claude, get compliance analysis back.
- `generateAuditNarrative()` — Ask Claude to write audit evidence.
- `parseContractObligations()` — Ask Claude to extract obligations from MSA text.
- `localAnalyzeCodeDiff()` — Fallback: keyword-based analysis when LLM unavailable.

---

### 12. `ControlIngestionAgent.java` — Dynamic Policy Creator

**What it does:** This is the "zero code change" agent. Accepts raw regulation text, sends it to the LLM, LLM parses it into a structured policy, and adds it to the live Policy Engine.

**How it works:**
1. Receives: framework name + regulation text (e.g., "DORA Article 19...")
2. Sends to LLM with prompt: "Parse this into a policy — extract ID, domain, trigger, severity, action, blocking, SLA"
3. LLM responds with structured fields
4. `parseLLMResponse()` — Converts LLM output into a CompliancePolicy object
5. `policyEngine.getAllPolicies().add(policy)` — Adds to live engine immediately
6. Next event will be evaluated against this new policy

**Why this proves nothing is hardcoded:** Feed ANY regulation text → AI creates the policy → system enforces it. No developer, no restart.

---

### 13. `FileSystemWatcher.java` — Real-Time File Monitor

**What it does:** Polls `watch-folder/` every 3 seconds for new or modified files. When detected, reads the file content and sends it to OllamaLLMService for AI analysis. Then fires an event into the JMS queue.

**How it works:**
1. `@Scheduled(fixedDelay = 3000)` — Runs every 3 seconds automatically.
2. `watchForChanges()` — Lists all files, compares timestamps to last known values.
3. If new/modified file found → `analyzeFile(path)`
4. `analyzeFile()` — Reads file content → calls `llmService.reason()` → parses response → calls `fireEvent()`
5. `fireEvent()` — Saves ComplianceEvent to DB → publishes to JMS queue → Chain Reactor picks it up

---

### 14. `GraphRAGService.java` — Knowledge Graph + RAG

**What it does:** Maintains a graph of compliance relationships. When asked, traverses the graph to find all connected regulations, penalties, and required actions for a given finding.

**How it works:**
- Graph nodes: regulations (PCI-DSS-6.5), controls (ITGC-CM-01), findings (SQL_INJECTION), penalties ($50K)
- Graph edges: "violates", "requires", "triggers", "penalizes"
- `retrieveComplianceContext()` — Given a finding type, traverses 3 hops to find all connected nodes
- Returns: list of causal paths that are passed to the LLM as context (the "RAG" part)

**Example traversal:**
SQL Injection → violates PCI-DSS-6.5 → requires CODE_REVIEW → satisfies ITGC-CM-06
SQL Injection → triggers MSA-7.2 → penalizes $50K

---

### 15. `ComplianceKnowledgeGraph.java` — Graph Structure

**What it does:** Builds and maintains the JGraphT graph structure. Defines nodes and edges.

**Key methods:**
- `buildGraph()` — Creates all nodes and relationships at startup
- `findCausalPaths()` — BFS/DFS traversal to find multi-hop connections
- `getImpactedDomains()` — Given a finding, returns which compliance domains are affected

---

## DATA MODEL FILES

---

### 16. `ComplianceEvent.java` — Database Entity

**What it is:** JPA entity stored in H2 database. Represents one compliance event (code commit, config change, etc.)

**Fields:** id, engagementId, eventType, source, description, timestamp, processed, payload

---

### 17. `ClientEngagement.java` — Client Profile Entity

**What it is:** JPA entity representing one client engagement.

**Fields:** engagementId, clientName, industry, complianceScore, openRisks, complianceDebtUsd, trend, soxApplicable, pciApplicable, fairLendingApplicable, applicableFrameworks

---

### 18. `ChainReactionResult.java` — Analysis Result Entity

**What it is:** One row per triggered policy. Stored after Chain Reactor processes an event.

**Fields:** id, eventId, engagementId, domain, severity, reason, actionRequired, controlsAffected, sla, blocking, timestamp

---

### 19. `AuditNarrative.java` — Generated Evidence Entity

**What it is:** The auto-generated audit narrative stored in database.

**Fields:** id, eventId, engagementId, eventType, generatedAt, narrativeText, controlsSatisfied, evidenceArtifacts

---

## SUMMARY: Which Class Does What

| Class | Role | One-Line Description |
|-------|------|---------------------|
| dashboard.py | Web UI | Serves the dashboard, triggers scan on every page load |
| ai_scanner.py | Code analyzer | Reads files, finds violations (AI or heuristic) |
| java_scanner.py | Pattern matcher | Regex/entropy detection (fallback) |
| dashboard_utils.py | Data calculator | Computes scores, risk, trends from findings |
| WebhookController.java | Event receiver | Accepts events from external tools, queues them |
| ChainReactorAgent.java | Central brain | Picks events from queue, evaluates policies, triggers agents |
| CompliancePolicyEngine.java | Rule engine | Stores 26+ policies, evaluates triggers dynamically |
| AuditNarratorAgent.java | Evidence writer | Generates audit narratives using LLM |
| OllamaLLMService.java | Local LLM | Sends prompts to our model, gets compliance findings |
| BedrockClaudeService.java | Cloud LLM | Same as Ollama but uses AWS Bedrock (production) |
| ControlIngestionAgent.java | Policy creator | Parses regulation text → creates live policies (zero code) |
| FileSystemWatcher.java | File monitor | Polls for changes every 3s, triggers analysis |
| GraphRAGService.java | Graph reasoning | Traverses knowledge graph for causal compliance paths |
| ComplianceKnowledgeGraph.java | Graph builder | Defines regulation relationships as a graph |

---

## FOR DEMO: When They Say "Walk Me Through The Code"

Open these files IN THIS ORDER:

1. **LoanService.java** — "This is the file I'll edit. Right now it's clean."
2. **dashboard.py** → `get_live_scan()` — "Every 5 seconds, this function scans the folder."
3. **ai_scanner.py** → `scan_recent()` — "It reads each file and checks for violations."
4. **java_scanner.py** → `_scan_file()` — "These are the 10 detection rules."
5. **WebhookController.java** → `processWebhook()` — "Events enter here, go to queue."
6. **ChainReactorAgent.java** → `processEvent()` — "This picks from queue, evaluates all policies."
7. **CompliancePolicyEngine.java** → `evaluate()` — "26 policies checked against this event."
8. **ControlIngestionAgent.java** → `ingestControl()` — "This is how new policies are added by AI."

Then say: "Let me show it live" → uncomment a block → save → watch dashboard update.

---

# 🛡️ KAVACH AI — Kontinuous Audit & Vulnerability Analysis for Compliant Hardening

> **"One commit. Six domains. Zero breaches."**

KAVACH AI is an **Agentic AI platform** that creates real-time compliance digital twins for every client engagement. It reasons across Security, SOX, Regulatory, Contractual, Privacy, and Fair Lending domains — predicting compliance impact before code ships.

**Team:** Hexaware Profuellers  
**Competition:** Agentic Arena 2026  
**Category:** Autonomous Security, Compliance & Audit Intelligence

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         KAVACH AI PLATFORM                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────────────────────────────────────────────────────────┐     │
│   │              SPRING BOOT BACKEND (Port 9090)                      │     │
│   │                                                                    │     │
│   │   REST API ──► JMS Queue (ActiveMQ) ──► Agent Pipeline            │     │
│   │       │                                       │                    │     │
│   │       ▼                                       ▼                    │     │
│   │   H2 Database                         Compliance Reasoning         │     │
│   │   (Event Store)                       (Chain Reactor + Narrator)   │     │
│   │       │                                       │                    │     │
│   │       ▼                                       ▼                    │     │
│   │   Knowledge Graph ◄──────────────────► GraphRAG Service            │     │
│   │   (JGraphT)                           (Causal Path Analysis)       │     │
│   └──────────────────────────────────────────────────────────────────┘     │
│                                                                             │
│   ┌──────────────────────────────────────────────────────────────────┐     │
│   │              PYTHON DASHBOARD (Port 8080)                         │     │
│   │                                                                    │     │
│   │   Live Code Scanner ──► Compliance Mapping ──► Real-time UI       │     │
│   │   (Java file watcher)   (Multi-agent)          (Auto-refresh 3s)  │     │
│   └──────────────────────────────────────────────────────────────────┘     │
│                                                                             │
│   ┌──────────────────────────────────────────────────────────────────┐     │
│   │              MICROSERVICES LAYER (Ports 8081-8083)                 │     │
│   │                                                                    │     │
│   │   Alpha Service ──► Beta Service ──► Gamma Service                │     │
│   │   (Event Ingest)    (Intelligence)   (Audit & Dash)               │     │
│   └──────────────────────────────────────────────────────────────────┘     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤖 Multi-Agent Architecture

| # | Agent | Role | Key Capability |
|---|-------|------|----------------|
| 1 | **Digital Twin Agent** | Per-engagement compliance state | Live compliance score, risk tiers, framework coverage |
| 2 | **Chain Reactor Agent** | Cross-domain causal analysis | 1 code commit → propagates impact across 6 domains |
| 3 | **Audit Narrator Agent** | Autonomous evidence generation | Writes audit-ready narratives in real-time |
| 4 | **Drift Detector Agent** | Silent degradation detection | Catches compliance drift before auditors do |
| 5 | **Obligation Parser Agent** | Contract-to-control intelligence | Parses MSA clauses into machine-readable rules |

### Agent Communication Flow

```
  Code Commit / Event
       │
       ▼
  ┌─────────────────┐
  │   Orchestrator   │ ◄── Coordinates all agents
  └─────┬───────────┘
        │
        ├──► Digital Twin Agent ──► Updates per-client compliance state
        │
        ├──► Chain Reactor Agent ──► Traces causal impact across:
        │         SOX │ Security │ Regulatory │ Contractual │ Privacy │ Fair Lending
        │
        ├──► Audit Narrator Agent ──► Generates evidence narrative
        │
        ├──► Drift Detector Agent ──► Checks for silent compliance degradation
        │
        └──► Obligation Parser Agent ──► Maps event against contract obligations
```

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Backend | Spring Boot 3.4.4 | REST APIs, JMS pipeline, WebSocket |
| Message Queue | Apache ActiveMQ (embedded) | Event-driven agent pipeline |
| Database | H2 (embedded) | Event store, compliance state |
| Graph Engine | JGraphT | Compliance knowledge graph, causal paths |
| LLM (optional) | AWS Bedrock (Claude) | Contract reasoning, narrative generation |
| Dashboard | Python 3.9+ | Live UI with real-time code scanning |
| Code Scanner | Custom heuristic engine | Entropy-based secret detection, pattern matching |
| Microservices | Gradle multi-project | Alpha/Beta/Gamma selective deployment |
| CI/CD | Jenkins (parameterized) | Selective service deployment |

---

## 📋 Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Java | 17+ | Required for Spring Boot backend |
| Python | 3.9+ | Required for dashboard (zero pip dependencies) |
| Gradle | 8.8+ | Included via wrapper (`./gradlew`) |

**No Docker, no external database, no message queue installation needed.**  
Everything runs embedded.

---

## 🚀 Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/mysrh1901/Profuellers.git
cd Profuellers
```

### 2. Start All Services (Recommended)

```bash
chmod +x start-all.sh stop-all.sh
./start-all.sh
```

This starts:
- **Spring Boot Backend** → http://localhost:9090
- **Dashboard UI** → http://localhost:8080

### 3. Start Services Individually

#### Spring Boot Backend (Port 9090)

```bash
cd regulith-springboot
./gradlew bootRun
```

#### Python Dashboard (Port 8080)

```bash
python3 dashboard.py
```

#### CLI Demo (no server needed)

```bash
python3 main.py            # Full demo with all 8 scenarios
python3 main.py --quick    # Quick summary
python3 main.py --scenario # Scenario selection menu
```

### 4. Stop All Services

```bash
./stop-all.sh
```

---

## 🔌 API Endpoints (Spring Boot — Port 9090)

### Event Submission

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events` | Submit any compliance event |
| POST | `/api/events/simulate/code-commit` | Simulate a mortgage rate code change |
| POST | `/api/events/simulate/infra-change` | Simulate an infra change (GDPR violation) |

### Query Data

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/twins` | All client engagement compliance twins |
| GET | `/api/twins/{id}` | Single client twin |
| GET | `/api/chain-reactions/{id}` | Chain reaction results for a client |
| GET | `/api/narratives/{id}` | Audit narratives for a client |
| GET | `/api/events/{id}` | Event history for a client |
| GET | `/api/summary` | Aggregate metrics |

### Utilities

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/h2-console` | Database console (inspect raw data) |
| GET | `/actuator/health` | Health check |

---

## 🖥️ Dashboard Views (Port 8080)

The web dashboard provides 5 interactive views:

| View | What It Shows |
|------|---------------|
| **Dashboard** | All 5 agents summarized — twins, chain reactor, gate, narrative, drift |
| **Audit Trail** | Timeline of compliance events + auto-generated evidence |
| **Audit Controls** | SOX/PCI/OWASP control status (SATISFIED ↔ VIOLATED) |
| **Drift Monitor** | Detected compliance drifts with remediation status |
| **Live Scan** | Real-time Java code scanner watching `watch-folder/` |

### Live Code Scanning Demo

The dashboard watches `watch-folder/*.java` and auto-refreshes every 3 seconds:

```bash
# Edit the sample file to trigger live scanning:
vim watch-folder/bad-code.java

# Add any of these to see real-time compliance findings:
# - A hardcoded secret (high entropy string)
# - System.out.println() statements
# - SQL string concatenation
# - SSL/TLS bypass patterns
```

---

## 📂 Project Structure

```
Profuellers/
├── agents/                          # Python multi-agent framework
│   ├── orchestrator.py              # Master orchestrator (coordinates all agents)
│   ├── digital_twin_agent.py        # Per-client compliance state
│   ├── chain_reactor_agent.py       # Cross-domain causal analysis
│   ├── audit_narrator_agent.py      # Autonomous evidence generation
│   ├── drift_detector_agent.py      # Silent compliance drift detection
│   └── obligation_parser_agent.py   # Contract clause parsing
│
├── regulith-springboot/             # Enterprise Spring Boot backend
│   └── src/main/java/com/regulith/
│       ├── RegulithApplication.java # Main app (port 9090)
│       ├── agent/                   # Java agent implementations
│       ├── controller/              # REST endpoints
│       ├── model/                   # JPA entities
│       ├── repository/              # Data access layer
│       ├── graph/                   # Knowledge graph + GraphRAG
│       ├── config/                  # App configuration
│       └── watcher/                 # File system watcher
│
├── regulith-services/               # Microservices (multi-project Gradle)
│   ├── alpha-service/               # Event ingestion (port 8081)
│   ├── beta-service/                # Compliance intelligence (port 8082)
│   ├── gamma-service/               # Audit evidence & dashboard (port 8083)
│   └── Jenkinsfile                  # Selective deployment pipeline
│
├── regulith-agents/                 # Java agent library (standalone)
│   └── src/main/java/com/regulith/agents/
│       ├── core/                    # Core agent implementations
│       ├── graph/                   # Causal graph analysis
│       └── model/                   # Domain models
│
├── sample-mortgage-app/             # Demo mortgage app (triggers agents)
│   └── src/services/
│       ├── rate_calculator.py       # ARM rate logic (SOX + TILA trigger)
│       ├── borrower_eligibility.py  # Fair lending (ECOA trigger)
│       └── fee_engine.py            # Fee calculation
│
├── simulators/                      # Mock data (Snyk, Checkmarx, Wiz, etc.)
├── models/                          # Shared data models
├── watch-folder/                    # Live-scanned Java files
├── test-files/                      # Test payloads (bad code samples)
│
├── dashboard.py                     # Web dashboard server (port 8080)
├── dashboard_utils.py               # Dashboard data mapping
├── java_scanner.py                  # Heuristic code scanner
├── main.py                          # CLI demo entry point
├── start-all.sh                     # Start all services
└── stop-all.sh                      # Stop all services
```

---

## 🎯 Demo Scenarios

### Scenario 1: Code Commit → Cross-Domain Impact

A developer commits a change to ARM rate calculation logic. The Chain Reactor traces impact across:

1. **SOX** → Change to financially-significant system requires dual approval
2. **Security** → SAST scan finds race condition (HIGH severity)
3. **TILA/Reg Z** → APR calculation accuracy requirement triggered
4. **Fair Lending (ECOA)** → Eligibility logic touches borrower data
5. **Contractual** → MSA §7.2 critical vulnerability SLA starts
6. **PCI-DSS** → Code review requirement before release

### Scenario 2: Regulatory Change Propagation

New CFPB guidance drops. The platform instantly determines:
- Which clients are affected (per-engagement analysis)
- Which obligations need updating
- Which controls need re-testing

### Scenario 3: Live Code Scanning

Edit `watch-folder/bad-code.java` — the dashboard detects and maps findings to compliance frameworks in real-time (3-second refresh).

---

## 🏗️ Microservices Deployment

The `regulith-services/` layer provides selective deployment via Jenkins:

```bash
# Build all
cd regulith-services && ./gradlew build

# Build individual service
./gradlew :alpha-service:build
./gradlew :beta-service:build
./gradlew :gamma-service:build

# Run individual service
./gradlew :alpha-service:bootRun   # port 8081
./gradlew :beta-service:bootRun    # port 8082
./gradlew :gamma-service:bootRun   # port 8083
```

---

## 🔐 Configuration

### AWS Bedrock (Optional — for LLM reasoning)

Set in `regulith-springboot/src/main/resources/application.yml`:

```yaml
spring:
  bedrock:
    enabled: true
    region: us-east-1
    model-id: anthropic.claude-3-sonnet-20240229-v1:0
    use-llm: false   # Set to true if you have AWS credentials configured
```

When `use-llm: false`, the platform uses rule-based fallback logic (no AWS account needed).

### Ports Summary

| Service | Port | Purpose |
|---------|------|---------|
| Dashboard UI | 8080 | Web interface with live scanning |
| Spring Boot Backend | 9090 | REST APIs, JMS pipeline, H2 DB |
| Alpha Service | 8081 | Event ingestion |
| Beta Service | 8082 | Compliance intelligence |
| Gamma Service | 8083 | Audit evidence |

---

## 💡 What Makes This Unique

| Existing Tools | Limitation | KAVACH AI Difference |
|----------------|-----------|----------------------|
| Vanta / Drata | Single-company compliance | **Per-engagement** compliance twin |
| ServiceNow | Aggregation without reasoning | **Causal chain** across 6 domains |
| Checkmarx / Snyk | Detect vulns, no business context | **Business impact** reasoning |
| Panther / SOAR | Incident response only | **Predictive** compliance simulation |
| Fieldguide | Helps auditors work | **Generates** evidence autonomously |
| Regology | Tracks regulations | **Simulates blast radius** per client |

---

## 📊 Business Value

| Metric | Impact |
|--------|--------|
| Audit prep time reduction | 70% (saves $2-4M/year) |
| Risk exposure prevented | $50K+ per incident |
| Domains analyzed per commit | 6 simultaneously |
| Real-time agents active | 5 (24/7) |
| Analysis time per event | < 2 seconds |

---

## 👥 Team

**Hexaware Profuellers** — Agentic Arena 2026

---

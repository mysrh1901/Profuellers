"""
Compliance Twin Orchestrator
The master agent that coordinates all sub-agents and runs the complete analysis pipeline.
"""

import time
from datetime import datetime
from models.data_models import *
from simulators.mock_data import *
from agents.digital_twin_agent import DigitalTwinAgent
from agents.chain_reactor_agent import ChainReactorAgent
from agents.audit_narrator_agent import AuditNarratorAgent
from agents.drift_detector_agent import DriftDetectorAgent
from agents.obligation_parser_agent import ObligationParserAgent


class ComplianceTwinOrchestrator:
    """
    Master Orchestrator — Coordinates all compliance intelligence agents.
    This is the brain of the platform.
    """

    def __init__(self):
        self.twin_agent = DigitalTwinAgent()
        self.reactor_agent = ChainReactorAgent()
        self.narrator_agent = AuditNarratorAgent()
        self.drift_agent = DriftDetectorAgent()
        self.obligation_agent = ObligationParserAgent()
        self.clients = get_mock_clients()

    def run_full_demo(self):
        """Run the complete demonstration pipeline."""

        self._print_header()

        # Phase 1: Build Compliance Digital Twins
        print("\n" + "=" * 70)
        print("  PHASE 1: Building Compliance Digital Twins for All Engagements")
        print("=" * 70)
        self._animate_thinking("Aggregating data from Snyk, Checkmarx, Wiz, Qualys, ServiceNow, Git...")

        twins = []
        for client in self.clients:
            twin = self.twin_agent.build_twin(client)
            twins.append(twin)
            summary = self.twin_agent.get_twin_summary(twin)
            print(summary)

        # Phase 2: Multi-Client Comparison Dashboard
        print("\n")
        self._print_multi_client_dashboard(twins)

        # Phase 3: Cross-Domain Chain Reaction Analysis
        print("\n" + "=" * 70)
        print("  PHASE 2: Cross-Domain Chain Reaction Analysis")
        print("  Simulating: Developer commits code change to ARM rate calculation")
        print("=" * 70)
        self._animate_thinking("Analyzing cross-domain compliance impact...")

        code_change = get_mock_code_change()
        client = self.clients[0]  # MortgageFirst
        chain_reaction = self.reactor_agent.analyze_code_change(code_change, client)
        print(self.reactor_agent.format_chain_reaction(chain_reaction))

        # Phase 4: Regulatory Change Propagation
        print("\n" + "=" * 70)
        print("  PHASE 3: Regulatory Change Propagation")
        print("  Simulating: New CFPB guidance drops — which clients are affected?")
        print("=" * 70)
        self._animate_thinking("Scanning all engagements for regulatory impact...")

        print(self.reactor_agent.simulate_regulatory_change_propagation())

        # Phase 5: Autonomous Audit Narrative
        print("\n" + "=" * 70)
        print("  PHASE 4: Autonomous Audit Narrative Generation")
        print("  Generating audit-ready evidence for the code change")
        print("=" * 70)
        self._animate_thinking("Composing audit narrative from development activity...")

        narrative = self.narrator_agent.generate_change_narrative(code_change, chain_reaction)
        print(self.narrator_agent.format_narrative(narrative))

        # Phase 6: Weekly Summary for Auditors
        print("\n" + "=" * 70)
        print("  PHASE 5: Weekly Compliance Summary (For EY Auditors)")
        print("=" * 70)
        self._animate_thinking("Generating weekly compliance activity report...")

        print(self.narrator_agent.generate_periodic_summary("MortgageFirst National Bank"))

        # Phase 7: Drift Detection
        print("\n" + "=" * 70)
        print("  PHASE 6: Compliance Drift Detection")
        print("  Scanning for silent compliance degradation...")
        print("=" * 70)
        self._animate_thinking("Running drift detection scan across all systems...")

        print(self.drift_agent.run_drift_scan("ENG-001"))

        # Phase 8: Inherited Risk Detection
        print("\n" + "=" * 70)
        print("  PHASE 7: Inherited Risk Detection")
        print("  Checking for cross-engagement risk contamination...")
        print("=" * 70)
        self._animate_thinking("Analyzing cross-engagement dependencies...")

        print(self.drift_agent.detect_inherited_risk())

        # Phase 9: Obligation Parsing
        print("\n" + "=" * 70)
        print("  PHASE 8: Contract Obligation Extraction")
        print("  Parsing MSA to extract machine-readable obligations...")
        print("=" * 70)
        self._animate_thinking("LLM processing contract text...")

        print(self.obligation_agent.parse_contract("ENG-001"))
        print(self.obligation_agent.show_obligation_coverage_matrix())

        # Final: Executive Summary
        self._print_executive_summary(twins, chain_reaction)

    def _print_header(self):
        """Print the platform header."""
        print("""
╔══════════════════════════════════════════════════════════════════════════╗
║                                                                          ║
║          🛡️  KAVACH AI                                                   ║
║          Kontinuous Audit & Vulnerability Analysis for                ║
║          Compliance Health                                               ║
║                                                                          ║
║          "One commit. Six domains. Zero breaches."                       ║
║                                                                          ║
║          For: Hexaware Agentic Arena Competition 2026                    ║
║          Category: Autonomous Security, Compliance & Audit Intelligence  ║
║                                                                          ║
╠══════════════════════════════════════════════════════════════════════════╣
║                                                                          ║
║  AGENTS ACTIVE:                                                          ║
║    [1] 🏗️  Digital Twin Agent      — Per-engagement compliance state     ║
║    [2] ⚡ Chain Reactor Agent      — Cross-domain causal analysis        ║
║    [3] 📝 Audit Narrator Agent    — Autonomous evidence generation       ║
║    [4] 🔍 Drift Detector Agent    — Silent degradation detection         ║
║    [5] 📜 Obligation Parser Agent — Contract-to-control intelligence     ║
║                                                                          ║
║  SIMULATED INTEGRATIONS:                                                 ║
║    Snyk | Checkmarx | Wiz | Qualys | CrowdStrike | ServiceNow           ║
║    Jira | GitHub | AWS Config | Splunk | PagerDuty                       ║
║                                                                          ║
╚══════════════════════════════════════════════════════════════════════════╝
""")

    def _print_multi_client_dashboard(self, twins):
        """Print a multi-client comparison dashboard."""

        separator = "=" * 70
        thin_sep = "-" * 70

        print(f"""
{separator}
  📊 MULTI-CLIENT COMPLIANCE COMMAND CENTER
  Hexaware — All Engagement Compliance Posture at a Glance
{separator}

  ┌────────────────────────────────────────────────────────────────────┐
  │ Client              │ Score │ Trend      │ Risk │ Debt      │ Tier │
  ├────────────────────────────────────────────────────────────────────┤""")

        for twin in twins:
            name = twin.client.client_name[:20]
            score = twin.overall_score
            trend = twin.trend[:12]
            risks = twin.open_risks
            debt = f"${twin.compliance_debt_usd/1000:.0f}K"
            tier = twin.client.risk_tier

            score_bar = "█" * int(score / 10) + "░" * (10 - int(score / 10))
            icon = "🟢" if score >= 80 else "🟡" if score >= 60 else "🔴"

            print(f"  │ {icon} {name:20s}│ {score_bar} {score:4.0f} │ {trend:10s} │  {risks:2d}  │ {debt:9s} │ {tier:4s} │")

        print(f"""  └────────────────────────────────────────────────────────────────────┘

  AGGREGATE METRICS:
    Total Open Risks Across All Engagements:  {sum(t.open_risks for t in twins)}
    Total Compliance Debt:                    ${sum(t.compliance_debt_usd for t in twins):,.0f}
    Engagements At Risk:                      {len([t for t in twins if t.overall_score < 80])}
    Average Audit Readiness:                  {sum(t.audit_readiness_pct for t in twins) / len(twins):.0f}%

{separator}
""")

    def _print_executive_summary(self, twins, chain_reaction):
        """Print final executive summary."""

        separator = "═" * 70

        print(f"""
{separator}
  🎯 EXECUTIVE SUMMARY — COMPLIANCE TWIN PLATFORM DEMO
{separator}

  WHAT WAS DEMONSTRATED:
  ─────────────────────
  ✅ Real-time compliance digital twins for 3 client engagements
  ✅ Cross-domain causal chain reaction (1 code commit → 6 domains affected)
  ✅ Multi-client blast radius analysis (same event, different impact per client)
  ✅ Autonomous audit narrative generation (eliminates 70% audit prep time)
  ✅ Weekly compliance reporting for auditors (EY/Deloitte ready)
  ✅ Compliance drift detection (6 silent drifts found and prioritized)
  ✅ Inherited risk detection across engagements (cross-VPC DORA cascade)
  ✅ Contract obligation extraction (8 obligations auto-mapped to controls)
  ✅ Obligation-to-tool-to-control coverage matrix (100% coverage)

  WHY THIS DOESN'T EXIST IN THE MARKET:
  ──────────────────────────────────────
  ❌ Vanta/Drata      → Single-company compliance. No multi-client context.
  ❌ ServiceNow       → Aggregation without causal reasoning across domains.
  ❌ Panther/SOAR     → Incident response, not compliance intelligence.
  ❌ Checkmarx/Snyk   → Detect vulnerabilities, don't reason about business impact.
  ❌ Fieldguide       → Helps auditors work, doesn't generate evidence autonomously.
  ❌ Regology         → Tracks regulations, doesn't simulate blast radius per client.

  WHAT COMPLIANCE TWIN UNIQUELY DOES:
  ────────────────────────────────────
  ✨ Per-engagement compliance state (not per-company)
  ✨ Cross-domain causal propagation (SOX + Security + Regulatory + Contractual)
  ✨ Compliance simulation BEFORE deployment (predictive, not reactive)
  ✨ Contract-aware (parses actual MSA language into machine rules)
  ✨ Multi-client blast radius (same event → different urgency per client)
  ✨ Inherited risk detection (Client A's new regulation affects Client B)
  ✨ Autonomous audit evidence (EY walks in → evidence already generated)

  BUSINESS VALUE FOR HEXAWARE:
  ────────────────────────────
  💰 Revenue: Offer as premium managed service ($20-50K/month per client)
  📉 Cost: Reduce audit prep time by 70% (saves $2-4M/year)
  🏆 Differentiation: No competitor (TCS, Infosys, Wipro) has this
  🤝 Client Trust: Real-time compliance visibility builds retention
  ⚡ Speed: Compliance gates in CI/CD, not months-later audit findings
  🛡️ Risk: Prevent $50K/incident SLA breach penalties proactively

  PRODUCTION ROADMAP:
  ───────────────────
  Phase 1 (8 weeks):  Core Twin + Chain Reactor for 1 pilot client
  Phase 2 (12 weeks): Add Obligation Parser + Audit Narrator
  Phase 3 (16 weeks): Multi-client dashboard + Drift Detection
  Phase 4 (20 weeks): Full platform with real tool integrations

  TECH STACK (Production):
  ────────────────────────
  • Agent Orchestration: LangGraph / CrewAI
  • LLM: GPT-4o / Claude (contract parsing, narrative generation)
  • Graph DB: Neo4j (obligation graph, cross-domain relationships)
  • Vector DB: Pinecone (regulatory text embeddings)
  • Integrations: Snyk API, Checkmarx API, Wiz API, ServiceNow API
  • Infrastructure: AWS / Azure (multi-tenant, per-client isolation)
  • UI: Real-time dashboard with compliance scores, SLA timers, agent feed

{separator}
  💡 KAVACH AI — "One commit. Six domains. Zero breaches."
      It's the compliance BRAIN for IT services companies.
{separator}
""")

    def _animate_thinking(self, message: str):
        """Simple animation to simulate agent thinking."""
        print(f"\n  🤖 Agent Processing: {message}")
        stages = ["  ▸ Collecting data...", "  ▸ Analyzing...", "  ▸ Reasoning...", "  ▸ Complete ✓"]
        for stage in stages:
            time.sleep(0.3)
            print(stage)
        print()

"""
Agent 5: Obligation Parser Agent
Parses client contracts (MSA, SOW, BAA, DPA) and regulatory texts
to extract machine-readable compliance obligations.

Uses simulated NLP/LLM extraction to demonstrate the capability.
"""

from datetime import datetime
from models.data_models import *


class ObligationParserAgent:
    """
    Contract & Regulation Obligation Extraction Agent.
    Transforms legal text into structured, actionable compliance obligations.
    In production: Uses LLM (GPT-4o/Claude) for extraction.
    In demo: Simulates extraction with realistic outputs.
    """

    def __init__(self):
        self.extracted_obligations = []

    def parse_contract(self, client_id: str) -> str:
        """
        Simulate parsing a Master Service Agreement to extract obligations.
        In production, this would use LLM to process actual contract PDFs.
        """

        separator = "=" * 70
        thin_sep = "-" * 70

        # Simulated contract text (what LLM would process)
        contract_excerpt = """
        MASTER SERVICE AGREEMENT — MortgageFirst National Bank & Hexaware Technologies
        Effective: January 1, 2025 | Agreement No: MSA-2025-MFNB-001

        §7.2 Security Vulnerability Management
        Provider shall remediate all Critical severity vulnerabilities within forty-eight (48) 
        hours of discovery or notification, whichever is earlier. High severity vulnerabilities 
        shall be remediated within seven (7) calendar days. Provider shall notify Client within 
        four (4) hours of discovering any security incident affecting Client data.

        §7.4 Change Management
        Provider shall not implement changes to Production systems affecting financial 
        calculations, data processing logic, or regulatory reporting without: (a) minimum 
        48 hours advance written notification to Client, (b) documented testing evidence, 
        and (c) rollback capability verified.

        §12.1 Data Residency and Sovereignty
        All Client Data shall be processed and stored exclusively within the continental 
        United States. No Client Data shall be transferred, replicated, or backed up to 
        any geographic location outside the United States without prior written consent.

        §14.3 Audit Rights
        Client and its designated auditors (including Ernst & Young LLP) shall have the 
        right to audit Provider's compliance with this Agreement upon fifteen (15) business 
        days written notice. Provider shall make available all relevant personnel, systems, 
        and documentation within five (5) business days of audit commencement.

        §18.7 Penalties and Remedies
        Failure to meet the vulnerability remediation SLAs defined in §7.2 shall result in 
        a penalty of $50,000 per incident. Repeated failures (three or more in any calendar 
        quarter) shall constitute a material breach enabling Client termination rights.
        """

        output = f"""
{separator}
  📜 OBLIGATION PARSER AGENT — Contract Analysis
{separator}

  📄 CONTRACT: MSA-2025-MFNB-001
     Parties:   MortgageFirst National Bank ↔ Hexaware Technologies
     Effective: January 1, 2025
     Client ID: ENG-001

{thin_sep}
  🔍 PARSING CONTRACT TEXT (Simulated LLM Extraction)...
{thin_sep}

  ✅ Extraction Complete. Found 8 actionable obligations.

{thin_sep}
  📋 EXTRACTED OBLIGATIONS:
{thin_sep}

  [OBL-001] Source: MSA §7.2
  ┌─────────────────────────────────────────────────────────────┐
  │ Type:        Security SLA                                    │
  │ Obligation:  Critical vulnerabilities → 48-hour remediation │
  │ Trigger:     Discovery or notification of Critical vuln     │
  │ SLA:         48 hours                                        │
  │ Metric:      Time from discovery to verified remediation    │
  │ Penalty:     $50,000 per incident                           │
  │ Escalation:  3+ breaches/quarter = material breach          │
  │ Monitoring:  Map to: Snyk, Checkmarx, Wiz, Qualys findings │
  │              with severity = CRITICAL                        │
  └─────────────────────────────────────────────────────────────┘
  → MAPPED TO CONTROLS: ITGC-SD-01, VRM-01
  → AUTO-MONITORING: Enabled (tracks all Critical findings against 48h clock)

  [OBL-002] Source: MSA §7.2
  ┌─────────────────────────────────────────────────────────────┐
  │ Type:        Security SLA                                    │
  │ Obligation:  High vulnerabilities → 7-day remediation       │
  │ Trigger:     Discovery or notification of High vuln         │
  │ SLA:         168 hours (7 calendar days)                    │
  │ Penalty:     Included in §18.7 penalty structure            │
  │ Monitoring:  Map to: Snyk, Checkmarx, Wiz, Qualys findings │
  │              with severity = HIGH                            │
  └─────────────────────────────────────────────────────────────┘
  → MAPPED TO CONTROLS: ITGC-SD-01, VRM-01
  → AUTO-MONITORING: Enabled

  [OBL-003] Source: MSA §7.2
  ┌─────────────────────────────────────────────────────────────┐
  │ Type:        Incident Notification                           │
  │ Obligation:  Notify client within 4 hours of security       │
  │              incident affecting client data                  │
  │ Trigger:     Security incident detection                    │
  │ SLA:         4 hours                                         │
  │ Penalty:     Implicit (trust/relationship damage)           │
  │ Monitoring:  Map to: SIEM alerts, incident tickets          │
  └─────────────────────────────────────────────────────────────┘
  → MAPPED TO CONTROLS: INC-RESP-01
  → AUTO-MONITORING: Enabled (triggers client notification workflow)

  [OBL-004] Source: MSA §7.4
  ┌─────────────────────────────────────────────────────────────┐
  │ Type:        Change Management                               │
  │ Obligation:  48h advance notice for production changes to   │
  │              financial/data/regulatory systems               │
  │ Trigger:     Any PR targeting production branch that        │
  │              modifies financial logic                        │
  │ Requirements: Written notification + testing evidence       │
  │              + rollback capability verified                  │
  │ Monitoring:  Map to: Git PR labels, deployment pipeline     │
  └─────────────────────────────────────────────────────────────┘
  → MAPPED TO CONTROLS: ITGC-CM-01, ITGC-CM-02
  → AUTO-MONITORING: Enabled (flags PRs touching financial modules)

  [OBL-005] Source: MSA §12.1
  ┌─────────────────────────────────────────────────────────────┐
  │ Type:        Data Residency                                  │
  │ Obligation:  All data within US continental boundaries      │
  │ Trigger:     Any infrastructure change, backup config,      │
  │              or new service deployment                       │
  │ Prohibition: No cross-border transfer without written       │
  │              consent                                         │
  │ Monitoring:  Map to: AWS Config rules, Wiz cloud posture   │
  └─────────────────────────────────────────────────────────────┘
  → MAPPED TO CONTROLS: DATA-RES-01
  → AUTO-MONITORING: Enabled (AWS Config rule: deny non-US regions)

  [OBL-006] Source: MSA §14.3
  ┌─────────────────────────────────────────────────────────────┐
  │ Type:        Audit Rights                                    │
  │ Obligation:  Support audit within 5 business days of        │
  │              commencement. EY is designated auditor.         │
  │ Trigger:     15 business days written notice                │
  │ Requirement: Personnel, systems, documentation available    │
  │ Monitoring:  Audit readiness score must stay > 85%          │
  └─────────────────────────────────────────────────────────────┘
  → MAPPED TO CONTROLS: AUDIT-READY-01
  → AUTO-MONITORING: Continuous audit readiness scoring

{thin_sep}
  📊 EXTRACTION SUMMARY:
{thin_sep}
     Total Obligations Extracted:    8
     With Quantified SLAs:           5
     With Explicit Penalties:        3
     Auto-Monitoring Enabled:        8/8 (100%)
     Mapped to Existing Controls:    8/8 (100%)
     New Controls Needed:            0

  🔗 OBLIGATION GRAPH UPDATED:
     Obligations → Controls → Tools → Evidence (fully linked)

  🤖 AUTONOMOUS ACTIONS:
     1. All SLA timers configured in monitoring system
     2. Alert rules created for each obligation trigger
     3. Client notification templates prepared
     4. Audit evidence mapping documented
     5. Compliance Twin updated with obligation set

{separator}
"""
        return output

    def show_obligation_coverage_matrix(self) -> str:
        """Show how obligations map across tools and controls."""

        separator = "=" * 70
        thin_sep = "-" * 70

        return f"""
{separator}
  🗺️ OBLIGATION → TOOL → CONTROL COVERAGE MATRIX
{separator}

  ┌──────────────┬───────────────────┬──────────────────┬─────────────┐
  │ Obligation   │ Monitoring Tool   │ Control          │ Auto-Action │
  ├──────────────┼───────────────────┼──────────────────┼─────────────┤
  │ §7.2 Crit   │ Snyk, Wiz, CKX    │ ITGC-SD-01      │ SLA Timer   │
  │ §7.2 High   │ Snyk, Wiz, CKX    │ ITGC-SD-01      │ SLA Timer   │
  │ §7.2 Notify │ SIEM, PagerDuty   │ INC-RESP-01     │ Auto-Notify │
  │ §7.4 Change │ Git, ServiceNow   │ ITGC-CM-01/02   │ Gate Check  │
  │ §12.1 Data  │ AWS Config, Wiz   │ DATA-RES-01     │ Auto-Block  │
  │ §14.3 Audit │ Compliance Twin   │ AUDIT-READY-01  │ Score Alert │
  │ §18.7 Penal │ All of above      │ Aggregate       │ Exec Alert  │
  │ SOX §404    │ ServiceNow, Git   │ All ITGCs       │ Narrative   │
  └──────────────┴───────────────────┴──────────────────┴─────────────┘

  COVERAGE: 100% of contractual obligations have automated monitoring.
  GAP:      None — all obligations mapped to at least one tool and control.

{separator}
"""

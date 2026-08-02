"""
Agent 4: Drift Detector Agent
Monitors compliance drift — when the actual state silently diverges from expected state.
Detects inherited risks, shadow compliance debt, and configuration drift.
"""

from datetime import datetime
from models.data_models import *


class DriftDetectorAgent:
    """
    Compliance Drift Detection Agent.
    Identifies when compliance posture silently degrades without explicit events.
    """

    def __init__(self):
        self.drift_events = []

    def run_drift_scan(self, client_id: str) -> str:
        """Run a comprehensive drift detection scan for a client engagement."""

        separator = "=" * 70
        thin_sep = "-" * 70

        # Simulated drift detections
        drifts = self._detect_drifts(client_id)

        output = f"""
{separator}
  🔍 COMPLIANCE DRIFT DETECTION REPORT
  Client: {'MortgageFirst National Bank' if client_id == 'ENG-001' else client_id}
  Scan Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S UTC')}
  Agent: Drift Detector v1.0
{separator}

  📊 DRIFT SUMMARY:
     Critical Drifts:    {len([d for d in drifts if d['severity'] == 'CRITICAL'])}
     High Drifts:        {len([d for d in drifts if d['severity'] == 'HIGH'])}
     Medium Drifts:      {len([d for d in drifts if d['severity'] == 'MEDIUM'])}
     Low Drifts:         {len([d for d in drifts if d['severity'] == 'LOW'])}

{thin_sep}
  🚨 DETECTED COMPLIANCE DRIFTS:
{thin_sep}
"""
        for i, drift in enumerate(drifts, 1):
            severity_icon = "🔴" if drift['severity'] == 'CRITICAL' else "🟠" if drift['severity'] == 'HIGH' else "🟡" if drift['severity'] == 'MEDIUM' else "🔵"
            output += f"""
  [{i}] {severity_icon} {drift['title']}
      Severity:     {drift['severity']}
      Category:     {drift['category']}
      Expected:     {drift['expected_state']}
      Actual:       {drift['actual_state']}
      Drift Since:  {drift['drift_since']}
      Root Cause:   {drift['root_cause']}
      Impact:       {drift['impact']}
      Remediation:  {drift['remediation']}
      Auto-Fix:     {drift['auto_fixable']}
"""

        output += f"""
{thin_sep}
  📈 DRIFT TREND (Last 30 Days):
{thin_sep}

  Week 1 (Jun 1-7):    ██░░░░░░░░ 2 drifts detected
  Week 2 (Jun 8-14):   ███░░░░░░░ 3 drifts detected
  Week 3 (Jun 15-21):  ████░░░░░░ 4 drifts detected  ← trending up
  Week 4 (Jun 22-28):  ██████░░░░ 6 drifts detected  ← ALERT: acceleration

  ⚠️ DRIFT VELOCITY ALERT: Compliance drift is ACCELERATING.
     Recommend: Immediate stabilization sprint + architecture review.

{thin_sep}
  🤖 AUTONOMOUS ACTIONS TAKEN:
{thin_sep}
     1. Created ServiceNow incident INC-2026-4471 for SSL cert drift
     2. Auto-remediated: Re-enabled S3 bucket encryption (non-breaking)
     3. Sent alert to Security Operations team for access drift
     4. Scheduled emergency review with Client Delivery Manager
     5. Updated Compliance Twin score: 87.2 → 79.8 (-7.4 points)

{separator}
"""
        return output

    def _detect_drifts(self, client_id: str) -> list:
        """Simulate drift detection findings."""

        drifts = [
            {
                "title": "SSL Certificate Approaching Expiry on Payment Gateway",
                "severity": "CRITICAL",
                "category": "Infrastructure Security",
                "expected_state": "SSL certificates renewed 30 days before expiry",
                "actual_state": "Certificate expires in 5 days — no renewal initiated",
                "drift_since": "2026-06-23 (5 days ago)",
                "root_cause": "Certificate auto-renewal failed silently. Monitoring alert misconfigured.",
                "impact": "PCI-DSS violation (Req 4.1) + service outage risk + client SLA breach",
                "remediation": "Immediate certificate renewal via AWS Certificate Manager",
                "auto_fixable": "YES — Initiating auto-renewal now"
            },
            {
                "title": "IAM Role with Production DB Access Not in Approved List",
                "severity": "HIGH",
                "category": "Access Control / SOX",
                "expected_state": "Only 4 approved IAM roles can access production financial database",
                "actual_state": "5 roles found — 'temp-migration-role' created 45 days ago still active",
                "drift_since": "2026-05-14 (45 days ago)",
                "root_cause": "Temporary role for data migration was never decommissioned post-migration.",
                "impact": "SOX ITGC-AC-01 violation (excessive access). EY will flag in next audit.",
                "remediation": "Delete temp-migration-role after confirming no active sessions",
                "auto_fixable": "YES (with approval) — Role has 0 activity in 30 days"
            },
            {
                "title": "Logging Configuration Drift — Audit Logs Missing for 3 Microservices",
                "severity": "HIGH",
                "category": "Audit / SOX",
                "expected_state": "All 12 microservices emit structured audit logs to central SIEM",
                "actual_state": "Services rate-calc, fee-engine, and notifier have logging disabled since last deployment",
                "drift_since": "2026-06-20 (8 days ago)",
                "root_cause": "Deployment config overwritten logging.level from INFO to OFF during performance tuning.",
                "impact": "SOX audit trail gap. 8 days of financial transactions have no audit log.",
                "remediation": "Restore logging configuration. Assess if gap requires disclosure to auditor.",
                "auto_fixable": "YES — Restoring log config via GitOps"
            },
            {
                "title": "Backup Retention Policy Reduced Below Contractual Requirement",
                "severity": "MEDIUM",
                "category": "Contractual / Operations",
                "expected_state": "MSA requires 90-day backup retention for all financial data",
                "actual_state": "AWS Backup plan modified to 30-day retention on June 15",
                "drift_since": "2026-06-15 (13 days ago)",
                "root_cause": "Cost optimization initiative changed retention without checking contractual requirements.",
                "impact": "MSA §8.3 breach — client can invoke penalty clause if data loss occurs",
                "remediation": "Restore 90-day retention policy. Flag cost optimization changes for contract review.",
                "auto_fixable": "YES — Updating backup plan"
            },
            {
                "title": "Dependency Version Drift — 3 Libraries Behind Security Patches",
                "severity": "MEDIUM",
                "category": "Application Security",
                "expected_state": "All dependencies updated within 14 days of security patch release",
                "actual_state": "jackson-databind, spring-security, log4j-core have patches available for 20+ days",
                "drift_since": "2026-06-08 (20 days ago)",
                "root_cause": "Dependabot PRs created but not merged. Team backlog overwhelming reviewers.",
                "impact": "Known CVEs unpatched. SLA breach approaching (7-day High finding SLA).",
                "remediation": "Merge Dependabot PRs after automated test validation.",
                "auto_fixable": "PARTIAL — Can auto-merge if tests pass"
            },
            {
                "title": "WAF Rules Outdated — Missing OWASP 2025 Protections",
                "severity": "LOW",
                "category": "Infrastructure Security",
                "expected_state": "WAF rules updated quarterly per OWASP latest guidance",
                "actual_state": "Last WAF rule update was 6 months ago. Missing protections for 5 new attack patterns.",
                "drift_since": "2026-01-15 (5+ months ago)",
                "root_cause": "No automated process for WAF rule updates. Manual quarterly process missed.",
                "impact": "Increased attack surface. Not yet exploited but gap growing.",
                "remediation": "Deploy updated AWS WAF managed rule set. Implement automated quarterly check.",
                "auto_fixable": "YES — Applying managed rule update"
            },
        ]
        return drifts

    def detect_inherited_risk(self) -> str:
        """
        NOVEL CAPABILITY: Detect inherited compliance risks.
        When one client's compliance state affects another due to shared infrastructure.
        """

        separator = "=" * 70
        thin_sep = "-" * 70

        return f"""
{separator}
  🔗 INHERITED RISK DETECTION — Cross-Engagement Impact
{separator}

  ⚠️ INHERITED RISK DETECTED:

  SCENARIO: Client ENG-002 (EuroLend) newly subject to DORA regulation.
  EuroLend's infrastructure shares a VPC peering connection with
  ENG-001 (MortgageFirst) for cross-Atlantic data sync.

{thin_sep}
  CASCADING IMPACT:
{thin_sep}

  Source:  ENG-002 now subject to DORA (Digital Operational Resilience Act)
  
  Inherited by ENG-001 because:
    • Shared VPC peering: vpc-0a1b2c3d ↔ vpc-4e5f6g7h
    • DORA Article 28 (Third-party ICT risk) applies to interconnected systems
    • Network-level access between environments creates shared risk boundary
    
  Impact on ENG-001 (MortgageFirst):
    • DORA ICT incident reporting may now cascade to MortgageFirst's environment
    • A security incident in EuroLend's VPC could traverse peering to MortgageFirst
    • EuroLend's DORA penetration testing scope may include shared network paths
    • MortgageFirst's SOX auditor (EY) may ask about interconnected EU systems

{thin_sep}
  RECOMMENDED ACTIONS:
{thin_sep}
    1. 🔴 Review VPC peering necessity — consider removing if not business-critical
    2. 🟠 Implement network segmentation (Security Groups) to limit cross-VPC traffic
    3. 🟠 Add DORA considerations to MortgageFirst's risk register
    4. 🟡 Brief EY on the interconnection during next quarterly review
    5. 🟡 Document accepted risk if peering is maintained

  RISK SCORE IMPACT:
    ENG-001 Compliance Score: -3.2 points (inherited risk penalty)
    ENG-002 Compliance Score: No change (DORA is their own obligation)

  🤖 AUTONOMOUS ACTION: Added to both engagement risk registers.
     Created ServiceNow risk item RSK-2026-0892 for review.

{separator}
"""

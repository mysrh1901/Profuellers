"""
Agent 1: Digital Twin Agent
Maintains the live compliance state (digital twin) for each client engagement.
Aggregates findings from all tools and computes compliance scores.
"""

from models.data_models import *
from simulators.mock_data import *


class DigitalTwinAgent:
    """
    Maintains real-time compliance digital twin per client engagement.
    Aggregates security findings, SOX controls, obligations, and computes unified scores.
    """

    def __init__(self):
        self.twins = {}

    def build_twin(self, client: ClientProfile) -> ComplianceTwinState:
        """Builds the complete compliance digital twin for a client engagement."""

        findings = get_mock_security_findings(client.client_id)
        controls = get_mock_sox_controls(client.client_id)
        obligations = get_mock_obligations(client.client_id)

        # Calculate domain scores
        domain_scores = self._calculate_domain_scores(client, findings, controls, obligations)

        # Calculate overall score (weighted average)
        weights = {
            "SOX": 0.25,
            "Security": 0.25,
            "Regulatory": 0.20,
            "Contractual": 0.15,
            "Audit Readiness": 0.15
        }
        overall_score = sum(
            domain_scores.get(domain, 100) * weight
            for domain, weight in weights.items()
        )

        # Calculate compliance debt in USD
        compliance_debt = self._calculate_compliance_debt(findings, controls, obligations)

        # Determine trend
        trend = self._determine_trend(findings, controls)

        # Count open risks
        open_risks = len([f for f in findings if f.status in [FindingStatus.OPEN, FindingStatus.IN_PROGRESS]])

        # Audit readiness
        audit_readiness = self._calculate_audit_readiness(controls, findings)

        twin = ComplianceTwinState(
            client=client,
            overall_score=round(overall_score, 1),
            domain_scores=domain_scores,
            obligations=obligations,
            security_findings=findings,
            sox_controls=controls,
            compliance_debt_usd=compliance_debt,
            trend=trend,
            last_updated=datetime.now().strftime("%Y-%m-%dT%H:%M:%SZ"),
            audit_readiness_pct=audit_readiness,
            open_risks=open_risks,
            days_since_last_audit=47
        )

        self.twins[client.client_id] = twin
        return twin

    def _calculate_domain_scores(self, client, findings, controls, obligations):
        """Calculate compliance score per domain (0-100, 100 = fully compliant)."""

        scores = {}

        # Security Score: Based on open findings and their severity
        critical_open = len([f for f in findings if f.severity == Severity.CRITICAL and f.status == FindingStatus.OPEN])
        high_open = len([f for f in findings if f.severity == Severity.HIGH and f.status == FindingStatus.OPEN])
        medium_open = len([f for f in findings if f.severity == Severity.MEDIUM and f.status == FindingStatus.OPEN])

        security_deductions = (critical_open * 15) + (high_open * 8) + (medium_open * 3)
        scores["Security"] = max(0, 100 - security_deductions)

        # SOX Score: Based on control effectiveness
        total_controls = len(controls)
        effective = len([c for c in controls if c.status == ControlStatus.EFFECTIVE])
        partial = len([c for c in controls if c.status == ControlStatus.PARTIALLY_EFFECTIVE])
        deficient = len([c for c in controls if c.status == ControlStatus.DEFICIENT])

        if total_controls > 0:
            sox_score = ((effective * 100) + (partial * 60) + (deficient * 0)) / total_controls
            scores["SOX"] = round(sox_score, 1)
        else:
            scores["SOX"] = 100

        # Regulatory Score: Based on whether obligated timelines are being met
        sla_breaches = len([f for f in findings
                          if f.status == FindingStatus.OPEN
                          and f.severity == Severity.CRITICAL
                          and f.sla_deadline])
        scores["Regulatory"] = max(0, 100 - (sla_breaches * 20))

        # Contractual Score: Based on SLA compliance
        contractual_risk = critical_open * 10 + high_open * 5
        scores["Contractual"] = max(0, 100 - contractual_risk)

        # Audit Readiness
        evidence_coverage = (effective + partial) / max(total_controls, 1) * 100
        scores["Audit Readiness"] = round(evidence_coverage, 1)

        return scores

    def _calculate_compliance_debt(self, findings, controls, obligations):
        """Translates compliance gaps into dollar risk exposure."""

        debt = 0.0

        # Critical findings: $100K each (potential breach cost)
        critical_open = len([f for f in findings if f.severity == Severity.CRITICAL and f.status == FindingStatus.OPEN])
        debt += critical_open * 100000

        # High findings: $25K each
        high_open = len([f for f in findings if f.severity == Severity.HIGH and f.status == FindingStatus.OPEN])
        debt += high_open * 25000

        # Deficient SOX controls: $500K each (material weakness risk)
        deficient = len([c for c in controls if c.status == ControlStatus.DEFICIENT])
        debt += deficient * 500000

        # Partially effective controls: $100K each
        partial = len([c for c in controls if c.status == ControlStatus.PARTIALLY_EFFECTIVE])
        debt += partial * 100000

        # SLA breach penalties
        for obl in obligations:
            if obl.sla_hours and obl.domain == ComplianceDomain.CONTRACTUAL:
                # Check if any critical finding is approaching SLA
                for f in findings:
                    if f.severity == Severity.CRITICAL and f.status == FindingStatus.OPEN:
                        debt += 50000  # Potential penalty

        return debt

    def _determine_trend(self, findings, controls):
        """Determine if compliance posture is improving, stable, or degrading."""

        critical_open = len([f for f in findings if f.severity == Severity.CRITICAL and f.status == FindingStatus.OPEN])
        deficient = len([c for c in controls if c.status == ControlStatus.DEFICIENT])

        if critical_open >= 3 or deficient >= 2:
            return "⚠️ DEGRADING"
        elif critical_open >= 1 or deficient >= 1:
            return "→ STABLE (AT RISK)"
        else:
            return "✓ IMPROVING"

    def _calculate_audit_readiness(self, controls, findings):
        """Calculate how ready the engagement is for an audit today."""

        total_factors = 10  # Simplified scoring model
        ready_factors = 0

        # Controls documented and tested
        tested_controls = len([c for c in controls if c.last_tested and c.status != ControlStatus.NOT_TESTED])
        if tested_controls == len(controls):
            ready_factors += 2
        elif tested_controls > len(controls) * 0.8:
            ready_factors += 1

        # No deficient controls
        if not any(c.status == ControlStatus.DEFICIENT for c in controls):
            ready_factors += 2
        elif len([c for c in controls if c.status == ControlStatus.DEFICIENT]) <= 1:
            ready_factors += 1

        # No critical open findings
        critical_open = len([f for f in findings if f.severity == Severity.CRITICAL and f.status == FindingStatus.OPEN])
        if critical_open == 0:
            ready_factors += 2
        elif critical_open == 1:
            ready_factors += 1

        # Evidence available
        evidence_controls = len([c for c in controls if c.evidence_links])
        if evidence_controls == len(controls):
            ready_factors += 2
        elif evidence_controls > len(controls) * 0.7:
            ready_factors += 1

        # SLA compliance
        ready_factors += 2  # Assume good for simulation

        return round((ready_factors / total_factors) * 100, 1)

    def get_twin_summary(self, twin: ComplianceTwinState) -> str:
        """Generate a human-readable summary of the compliance twin."""

        separator = "=" * 70
        thin_sep = "-" * 70

        summary = f"""
{separator}
  📊 COMPLIANCE DIGITAL TWIN — {twin.client.client_name}
{separator}

  Engagement ID:     {twin.client.client_id}
  Industry:          {twin.client.industry}
  Geography:         {twin.client.geography}
  Risk Tier:         {twin.client.risk_tier}
  Auditor:           {twin.client.auditor}
  Frameworks:        {', '.join(twin.client.applicable_frameworks)}

{thin_sep}
  📈 COMPLIANCE POSTURE SCORE: {twin.overall_score}/100
  📉 Trend: {twin.trend}
  💰 Compliance Debt: ${twin.compliance_debt_usd:,.0f}
  🔍 Audit Readiness: {twin.audit_readiness_pct}%
  ⚠️  Open Risks: {twin.open_risks}
{thin_sep}

  DOMAIN SCORES:
"""
        for domain, score in twin.domain_scores.items():
            bar_length = int(score / 5)
            bar = "█" * bar_length + "░" * (20 - bar_length)
            status_icon = "✅" if score >= 80 else "⚠️" if score >= 60 else "🔴"
            summary += f"    {status_icon} {domain:20s} [{bar}] {score:.0f}%\n"

        # Security Findings Summary
        summary += f"""
{thin_sep}
  🔒 SECURITY FINDINGS:
"""
        severity_counts = {}
        for f in twin.security_findings:
            key = f"{f.severity.value} ({f.status.value})"
            severity_counts[key] = severity_counts.get(key, 0) + 1

        for sev, count in sorted(severity_counts.items()):
            summary += f"    • {sev}: {count}\n"

        # SOX Controls Summary
        summary += f"""
{thin_sep}
  📋 SOX CONTROLS:
"""
        for control in twin.sox_controls:
            icon = "✅" if control.status == ControlStatus.EFFECTIVE else "⚠️" if control.status == ControlStatus.PARTIALLY_EFFECTIVE else "🔴"
            summary += f"    {icon} {control.control_id}: {control.control_name} — {control.status.value}\n"

        # Key Obligations
        summary += f"""
{thin_sep}
  📜 KEY OBLIGATIONS ({len(twin.obligations)} total):
"""
        for obl in twin.obligations[:5]:
            sla_info = f" [SLA: {obl.sla_hours}h]" if obl.sla_hours else ""
            summary += f"    • [{obl.source}] {obl.description[:80]}...{sla_info}\n"

        summary += f"\n{separator}\n"
        return summary

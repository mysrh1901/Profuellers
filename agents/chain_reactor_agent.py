"""
Agent 2: Chain Reactor Agent
Performs cross-domain compliance impact analysis.
When an event occurs (code commit, infra change, regulatory update),
it propagates the impact across ALL compliance domains simultaneously.

THIS IS THE KEY DIFFERENTIATOR — Nobody else does this.
"""

from models.data_models import *
from simulators.mock_data import get_mock_obligations


class ChainReactorAgent:
    """
    Cross-Domain Compliance Impact Propagation Engine.
    Takes a single event and computes the causal chain across all compliance domains.
    """

    def __init__(self):
        # Domain-specific rule engines (simulated)
        self.rules = self._load_rules()

    def _load_rules(self):
        """Load cross-domain propagation rules."""
        return {
            "financial_logic_change": {
                ComplianceDomain.SOX: {
                    "triggered": True,
                    "reason": "Change to financially-significant system requires SOX ITGC documentation",
                    "controls_affected": ["ITGC-CM-01", "ITGC-CM-02", "ITGC-SD-01"],
                    "action_required": "Dual approval + Change ticket + Test evidence"
                },
                ComplianceDomain.REGULATORY: {
                    "triggered": True,
                    "reason": "Rate/APR calculation changes require TILA Regulation Z validation",
                    "controls_affected": ["TILA-APR-ACCURACY"],
                    "action_required": "APR calculation validation to 1/8 of 1% accuracy"
                },
                ComplianceDomain.FAIR_LENDING: {
                    "triggered": True,
                    "reason": "Loan pricing logic change requires disparate impact analysis",
                    "controls_affected": ["ECOA-DI-TEST"],
                    "action_required": "Run disparate impact testing on new rate logic against protected classes"
                },
                ComplianceDomain.SECURITY: {
                    "triggered": True,
                    "reason": "New code path requires SAST scan completion before merge",
                    "controls_affected": ["PCI-DSS-6.5", "SAST-GATE"],
                    "action_required": "Resolve all Critical/High SAST findings before production"
                },
                ComplianceDomain.AUDIT: {
                    "triggered": True,
                    "reason": "Audit trail required for change to SOX-relevant system",
                    "controls_affected": ["AUDIT-EVIDENCE-01"],
                    "action_required": "Generate change narrative with business justification, approvals, and test results"
                },
                ComplianceDomain.CONTRACTUAL: {
                    "triggered": True,
                    "reason": "MSA §7.4 requires client notification for changes to core financial logic",
                    "controls_affected": ["MSA-CHANGE-NOTIFY"],
                    "action_required": "Notify client within 48 hours of production deployment"
                }
            },
            "pii_data_change": {
                ComplianceDomain.PRIVACY: {
                    "triggered": True,
                    "reason": "Processing of personal/financial data requires privacy impact assessment",
                    "controls_affected": ["GDPR-DPIA", "CCPA-PI-PROCESSING"],
                    "action_required": "Verify data minimization, purpose limitation, and consent"
                },
                ComplianceDomain.SECURITY: {
                    "triggered": True,
                    "reason": "PII handling requires encryption validation and access control review",
                    "controls_affected": ["ENCRYPT-AT-REST", "ENCRYPT-IN-TRANSIT", "ACCESS-LEAST-PRIV"],
                    "action_required": "Confirm encryption and access controls for new PII processing"
                },
                ComplianceDomain.REGULATORY: {
                    "triggered": True,
                    "reason": "New PII field may require HMDA/Fair Lending reporting updates",
                    "controls_affected": ["HMDA-REPORTING"],
                    "action_required": "Assess if new data field impacts regulatory reporting"
                }
            },
            "infrastructure_change": {
                ComplianceDomain.CONTRACTUAL: {
                    "triggered": True,
                    "reason": "Infrastructure changes may affect data residency requirements",
                    "controls_affected": ["DATA-RESIDENCY"],
                    "action_required": "Verify data remains within contractually-specified regions"
                },
                ComplianceDomain.INFRASTRUCTURE: {
                    "triggered": True,
                    "reason": "Cloud configuration change requires security review",
                    "controls_affected": ["CLOUD-SECURITY-BASELINE"],
                    "action_required": "Run Wiz/Prisma scan post-change, verify no new exposures"
                },
                ComplianceDomain.SOX: {
                    "triggered": True,
                    "reason": "Infrastructure supporting financial systems requires change management",
                    "controls_affected": ["ITGC-CM-01"],
                    "action_required": "Standard change management process applies"
                }
            }
        }

    def analyze_code_change(self, code_change: dict, client: ClientProfile) -> ChainReaction:
        """
        Analyze a code change and compute the cross-domain compliance impact.
        This is the CORE intelligence — the Chain Reactor.
        """

        impacts = []
        total_risk_delta = 0.0
        actions = []
        requires_human = False

        # Determine change characteristics
        touches_financial = any(f.get("touches_financial_logic") for f in code_change["files_changed"])
        touches_pii = any(f.get("touches_pii") for f in code_change["files_changed"])
        has_sast_findings = code_change.get("sast_scan_result", {}).get("critical", 0) > 0 or \
                           code_change.get("sast_scan_result", {}).get("high", 0) > 0

        # === FINANCIAL LOGIC CHAIN REACTION ===
        if touches_financial:
            rules = self.rules["financial_logic_change"]
            for domain, rule in rules.items():
                if rule["triggered"]:
                    # Check if this domain applies to this client
                    if self._domain_applies_to_client(domain, client):
                        impact = {
                            "domain": domain.value,
                            "severity": "HIGH" if domain in [ComplianceDomain.SOX, ComplianceDomain.REGULATORY] else "MEDIUM",
                            "reason": rule["reason"],
                            "controls_affected": rule["controls_affected"],
                            "action_required": rule["action_required"],
                            "sla": self._get_sla_for_domain(domain, client)
                        }
                        impacts.append(impact)
                        total_risk_delta += 5.0 if impact["severity"] == "HIGH" else 2.5
                        actions.append(rule["action_required"])

                        if domain in [ComplianceDomain.SOX, ComplianceDomain.FAIR_LENDING]:
                            requires_human = True

        # === PII DATA CHAIN REACTION ===
        if touches_pii:
            rules = self.rules["pii_data_change"]
            for domain, rule in rules.items():
                if rule["triggered"] and self._domain_applies_to_client(domain, client):
                    impact = {
                        "domain": domain.value,
                        "severity": "HIGH" if domain == ComplianceDomain.PRIVACY else "MEDIUM",
                        "reason": rule["reason"],
                        "controls_affected": rule["controls_affected"],
                        "action_required": rule["action_required"],
                        "sla": self._get_sla_for_domain(domain, client)
                    }
                    impacts.append(impact)
                    total_risk_delta += 4.0
                    actions.append(rule["action_required"])

        # === SAST FINDINGS IMPACT ===
        if has_sast_findings:
            sast = code_change["sast_scan_result"]
            impact = {
                "domain": ComplianceDomain.SECURITY.value,
                "severity": "CRITICAL" if sast["critical"] > 0 else "HIGH",
                "reason": f"SAST scan found {sast['critical']} Critical, {sast['high']} High findings in changed code",
                "controls_affected": ["ITGC-SD-01", "PCI-DSS-6.5"],
                "action_required": "Resolve Critical/High findings before production deployment. SOX ITGC-SD-01 requires clean scan.",
                "sla": "Before deployment (blocking)"
            }
            impacts.append(impact)
            total_risk_delta += 8.0 if sast["critical"] > 0 else 5.0
            requires_human = True

        # === MULTI-CLIENT BLAST RADIUS (if shared components) ===
        # Check if the change affects shared components used by multiple clients
        blast_radius_note = self._check_multi_client_blast_radius(code_change)
        if blast_radius_note:
            impacts.append(blast_radius_note)

        chain_reaction = ChainReaction(
            trigger_event=f"Code Commit: {code_change['commit_id']} - {code_change['message']}",
            trigger_type="code_commit",
            timestamp=code_change["timestamp"],
            impacts=impacts,
            total_domains_affected=len(set(i["domain"] for i in impacts)),
            risk_score_delta=total_risk_delta,
            recommended_actions=actions,
            requires_human_approval=requires_human,
            auto_remediation_possible=not requires_human
        )

        return chain_reaction

    def _domain_applies_to_client(self, domain: ComplianceDomain, client: ClientProfile) -> bool:
        """Check if a compliance domain is applicable to this client."""
        domain_framework_map = {
            ComplianceDomain.SOX: client.sox_applicable,
            ComplianceDomain.SECURITY: True,  # Always applies
            ComplianceDomain.REGULATORY: True,
            ComplianceDomain.CONTRACTUAL: True,
            ComplianceDomain.FAIR_LENDING: client.fair_lending_applicable,
            ComplianceDomain.PRIVACY: "GDPR" in client.applicable_frameworks or "CCPA" in client.applicable_frameworks,
            ComplianceDomain.INFRASTRUCTURE: True,
            ComplianceDomain.AUDIT: True,
        }
        return domain_framework_map.get(domain, False)

    def _get_sla_for_domain(self, domain: ComplianceDomain, client: ClientProfile) -> str:
        """Get the applicable SLA for this domain and client."""
        sla_map = {
            ComplianceDomain.SOX: "Before next audit cycle (documented within 24h)",
            ComplianceDomain.SECURITY: client.contractual_slas.get("critical_vuln_remediation", "48 hours"),
            ComplianceDomain.REGULATORY: "Per regulatory timeline (varies)",
            ComplianceDomain.CONTRACTUAL: "Per MSA terms",
            ComplianceDomain.FAIR_LENDING: "Before production deployment (blocking)",
            ComplianceDomain.PRIVACY: "72 hours (if breach), otherwise before deployment",
            ComplianceDomain.AUDIT: "Evidence generated within 24 hours of event",
        }
        return sla_map.get(domain, "As per framework")

    def _check_multi_client_blast_radius(self, code_change: dict) -> dict:
        """Check if the change affects shared infrastructure/libraries used by multiple clients."""

        # Simulate: shared libraries or shared infrastructure
        shared_components = ["common-auth-library", "shared-api-gateway", "logging-framework"]

        for file_info in code_change["files_changed"]:
            if any(comp in file_info["path"] for comp in shared_components):
                return {
                    "domain": "MULTI-CLIENT BLAST RADIUS",
                    "severity": "HIGH",
                    "reason": "This change affects a shared component used by multiple client engagements",
                    "controls_affected": ["MULTI-TENANT-ISOLATION"],
                    "action_required": "Verify change doesn't break isolation between client environments",
                    "sla": "Before deployment (blocking)"
                }
        return None

    def format_chain_reaction(self, reaction: ChainReaction) -> str:
        """Format chain reaction analysis for display."""

        separator = "=" * 70
        thin_sep = "-" * 70

        output = f"""
{separator}
  ⚡ CROSS-DOMAIN COMPLIANCE CHAIN REACTION ANALYSIS
{separator}

  🔄 TRIGGER EVENT:
     {reaction.trigger_event}
     Type: {reaction.trigger_type}
     Time: {reaction.timestamp}

  📊 IMPACT SUMMARY:
     Domains Affected:        {reaction.total_domains_affected}
     Risk Score Delta:        -{reaction.risk_score_delta:.1f} points
     Human Approval Required: {'YES ⛔' if reaction.requires_human_approval else 'No (Auto-remediation possible)'}
     Auto-Remediation:        {'Possible ✅' if reaction.auto_remediation_possible else 'Not possible — requires human review'}

{thin_sep}
  🔗 CAUSAL CHAIN (Impact Propagation):
{thin_sep}
"""
        for i, impact in enumerate(reaction.impacts, 1):
            severity_icon = "🔴" if impact["severity"] == "CRITICAL" else "🟠" if impact["severity"] == "HIGH" else "🟡"
            output += f"""
  [{i}] {severity_icon} {impact['domain']}
      Severity: {impact['severity']}
      Reason:   {impact['reason']}
      Controls: {', '.join(impact['controls_affected'])}
      Action:   {impact['action_required']}
      SLA:      {impact.get('sla', 'N/A')}
"""

        output += f"""
{thin_sep}
  📋 RECOMMENDED ACTIONS (Priority Order):
{thin_sep}
"""
        for i, action in enumerate(reaction.recommended_actions, 1):
            output += f"     {i}. {action}\n"

        if reaction.requires_human_approval:
            output += f"""
{thin_sep}
  ⛔ DEPLOYMENT GATE: This change CANNOT proceed to production without:
     • SOX dual-approval documented in change management system
     • Fair Lending disparate impact test results (if applicable)
     • All Critical/High SAST findings resolved
     • Audit narrative generated and stored
{thin_sep}
"""
        output += f"\n{separator}\n"
        return output

    def simulate_regulatory_change_propagation(self):
        """
        Simulates what happens when a NEW regulation drops.
        Example: CFPB issues new guidance affecting ARM rate calculations.
        Propagates impact across all affected client engagements.
        """

        reg_change = {
            "source": "CFPB Bulletin 2026-03",
            "effective_date": "2026-07-01",
            "title": "Updated ARM Rate Cap Calculation Methodology",
            "description": "New methodology for calculating Adjustable Rate Mortgage rate caps. Requires updated disclosure calculations and borrower notifications.",
            "affected_products": ["ARM Loans", "Hybrid ARM", "5/1 ARM", "7/1 ARM"],
        }

        separator = "=" * 70
        thin_sep = "-" * 70

        output = f"""
{separator}
  🏛️ REGULATORY CHANGE PROPAGATION ANALYSIS
{separator}

  📢 NEW REGULATION:
     Source:     {reg_change['source']}
     Title:      {reg_change['title']}
     Effective:  {reg_change['effective_date']}
     Products:   {', '.join(reg_change['affected_products'])}

{thin_sep}
  🔍 AFFECTED CLIENT ENGAGEMENTS:
{thin_sep}

  [1] 🔴 MortgageFirst National Bank (ENG-001)
      Impact Level: CRITICAL
      Reason: Client offers ARM products. Rate calculation logic must be updated.
      Domains Affected:
        • REGULATORY: TILA APR recalculation required
        • SOX: Change to financial system → ITGC change management
        • FAIR LENDING: New logic must pass disparate impact testing
        • CONTRACTUAL: MSA requires implementation within 30 days of effective date
        • SECURITY: New code requires SAST scan
        • AUDIT: Full audit trail of implementation required
      Deadline: July 1, 2026 (3 days remaining)
      Action Required: IMMEDIATE — Begin implementation sprint

  [2] 🟡 EuroLend Financial Group (ENG-002)
      Impact Level: LOW
      Reason: EU-based lender. CFPB guidance does not apply directly.
      Note: Monitor for ECB/EBA equivalent guidance adoption.
      Action Required: WATCH — No immediate action needed

  [3] ⚪ HomePath Insurance Corp (ENG-003)
      Impact Level: NONE
      Reason: Insurance company, does not originate mortgages.
      Action Required: None

{thin_sep}
  📊 SUMMARY:
     Total Clients Analyzed:  3
     Critically Affected:     1 (MortgageFirst National Bank)
     Monitoring Required:     1 (EuroLend Financial Group)
     No Impact:               1 (HomePath Insurance Corp)

  🚨 AUTONOMOUS ACTIONS TAKEN:
     1. Created Jira Epic MORT-1540 for MortgageFirst implementation
     2. Assigned to Engineering Lead with CRITICAL priority
     3. Notified Client Delivery Manager of regulatory deadline
     4. Pre-created SOX change ticket template
     5. Scheduled Fair Lending impact test slot for June 30
     6. Alert sent to EuroLend team for monitoring

{separator}
"""
        return output

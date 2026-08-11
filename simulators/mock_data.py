"""
Compliance Twin - Simulated Data
Realistic mock data representing Hexaware's mortgage/financial services client engagements.
Simulates outputs from: Snyk, Checkmarx, Wiz, Qualys, ServiceNow, Jira, Git
"""

from datetime import datetime, timedelta
from models.data_models import *


def get_mock_clients():
    """Simulates 3 real-world client engagements with different compliance profiles."""

    client_a = ClientProfile(
        client_id="ENG-001",
        client_name="Mortgage Clients(Freddie Mac)",
        industry="Mortgage / Financial Services",
        geography="United States (Multi-state)",
        engagement_type="Application Development & Maintenance",
        applicable_frameworks=["SOX", "SOC 2", "PCI-DSS", "TILA", "RESPA", "ECOA", "HMDA", "GLBA"],
        contractual_slas={
            "critical_vuln_remediation": "48 hours",
            "high_vuln_remediation": "7 days",
            "sox_change_approval": "Dual approval required",
            "incident_notification": "4 hours",
            "audit_evidence_delivery": "5 business days",
            "data_residency": "US-only (AWS us-east-1, us-west-2)"
        },
        regulatory_bodies=["SEC", "CFPB", "OCC", "State Banking Regulators"],
        risk_tier="Tier 1",
        auditor="EY (Ernst & Young)",
        audit_frequency="Annual SOX + Quarterly Internal",
        sox_applicable=True,
        pci_applicable=True,
        fair_lending_applicable=True
    )

    client_b = ClientProfile(
        client_id="ENG-002",
        client_name="EuroLend Financial Group",
        industry="Mortgage / Financial Services",
        geography="European Union (Germany, France, Netherlands)",
        engagement_type="Cloud Migration & Managed Services",
        applicable_frameworks=["SOX", "GDPR", "DORA", "PSD2", "EBA Guidelines", "ISO 27001"],
        contractual_slas={
            "critical_vuln_remediation": "24 hours",
            "high_vuln_remediation": "72 hours",
            "data_breach_notification": "72 hours (GDPR Art. 33)",
            "dora_ict_incident_report": "4 hours (initial), 72 hours (full)",
            "data_residency": "EU-only (AWS eu-central-1, eu-west-1)",
            "right_to_erasure": "30 days"
        },
        regulatory_bodies=["BaFin", "ECB", "CNIL", "DNB"],
        risk_tier="Tier 1",
        auditor="Deloitte",
        audit_frequency="Annual SOX + DORA Testing Quarterly",
        sox_applicable=True,
        pci_applicable=False,
        hipaa_applicable=False,
        fair_lending_applicable=False
    )

    client_c = ClientProfile(
        client_id="ENG-003",
        client_name="HomePath Insurance Corp",
        industry="Insurance / Mortgage Insurance",
        geography="United States",
        engagement_type="Platform Modernization",
        applicable_frameworks=["SOC 2", "SOX", "NAIC Model Laws", "CCPA", "State Insurance Regulations"],
        contractual_slas={
            "critical_vuln_remediation": "72 hours",
            "high_vuln_remediation": "14 days",
            "change_notification": "48 hours advance notice",
            "data_residency": "US-only",
            "pen_test_frequency": "Semi-annual"
        },
        regulatory_bodies=["State Insurance Commissioners", "SEC", "NAIC"],
        risk_tier="Tier 2",
        auditor="PwC",
        audit_frequency="Annual",
        sox_applicable=True,
        pci_applicable=False,
        fair_lending_applicable=False
    )

    return [client_a, client_b, client_c]


def get_mock_security_findings(client_id):
    """Simulates security findings from multiple tools for a given client."""

    findings_map = {
        "ENG-001": [
            SecurityFinding(
                finding_id="SNYK-2024-001",
                source_tool="Snyk",
                title="SQL Injection in Loan Query Module",
                severity=Severity.CRITICAL,
                description="User input directly concatenated in SQL query in loan_search.py. Allows unauthorized access to loan records.",
                affected_component="loan-origination-service",
                cve_id="CVE-2025-3891",
                file_path="src/services/loan_search.py",
                line_number=142,
                status=FindingStatus.OPEN,
                discovered_at="2026-06-25T14:30:00Z",
                sla_deadline="2026-06-27T14:30:00Z",
                remediation_suggestion="Use parameterized queries with SQLAlchemy ORM"
            ),
            SecurityFinding(
                finding_id="CKX-2024-015",
                source_tool="Checkmarx",
                title="Hardcoded AWS Credentials in Configuration",
                severity=Severity.CRITICAL,
                description="AWS access key and secret found hardcoded in application.properties file.",
                affected_component="loan-origination-service",
                file_path="src/main/resources/application.properties",
                line_number=47,
                status=FindingStatus.OPEN,
                discovered_at="2026-06-26T09:15:00Z",
                sla_deadline="2026-06-28T09:15:00Z",
                remediation_suggestion="Move to AWS Secrets Manager or environment variables"
            ),
            SecurityFinding(
                finding_id="WIZ-2024-088",
                source_tool="Wiz",
                title="S3 Bucket with Loan Documents Publicly Accessible",
                severity=Severity.CRITICAL,
                description="S3 bucket 'mortgagefirst-loan-docs-prod' has public read access. Contains PII and financial documents.",
                affected_component="AWS S3 (us-east-1)",
                status=FindingStatus.OPEN,
                discovered_at="2026-06-26T11:00:00Z",
                sla_deadline="2026-06-28T11:00:00Z",
                remediation_suggestion="Remove public access, enable bucket policy with least privilege"
            ),
            SecurityFinding(
                finding_id="SNYK-2024-002",
                source_tool="Snyk",
                title="Outdated Jackson Library with Deserialization Vulnerability",
                severity=Severity.HIGH,
                description="Jackson-databind 2.13.1 has known deserialization vulnerability.",
                affected_component="loan-origination-service",
                cve_id="CVE-2024-7254",
                file_path="pom.xml",
                status=FindingStatus.IN_PROGRESS,
                discovered_at="2026-06-20T08:00:00Z",
                sla_deadline="2026-06-27T08:00:00Z",
                remediation_suggestion="Upgrade to jackson-databind 2.17.0+"
            ),
            SecurityFinding(
                finding_id="QUAL-2024-033",
                source_tool="Qualys",
                title="TLS 1.0 Enabled on Loan Processing API",
                severity=Severity.HIGH,
                description="TLS 1.0 still enabled on loan-processing-api.mortgagefirst.internal. Violates PCI-DSS requirement.",
                affected_component="loan-processing-api (Load Balancer)",
                status=FindingStatus.OPEN,
                discovered_at="2026-06-22T16:00:00Z",
                sla_deadline="2026-06-29T16:00:00Z",
                remediation_suggestion="Disable TLS 1.0/1.1 on ALB, enforce TLS 1.2+"
            ),
        ],
        "ENG-002": [
            SecurityFinding(
                finding_id="WIZ-EU-001",
                source_tool="Wiz",
                title="Database Backup Stored in US Region",
                severity=Severity.CRITICAL,
                description="RDS automated backup for eu-mortgage-db replicating to us-east-1. Violates GDPR data residency.",
                affected_component="AWS RDS (eu-central-1 → us-east-1)",
                status=FindingStatus.OPEN,
                discovered_at="2026-06-26T07:00:00Z",
                sla_deadline="2026-06-27T07:00:00Z",
                remediation_suggestion="Disable cross-region replication, configure EU-only backup retention"
            ),
            SecurityFinding(
                finding_id="CKX-EU-003",
                source_tool="Checkmarx",
                title="PII Logged in Application Debug Logs",
                severity=Severity.HIGH,
                description="Customer personal data (name, IBAN, address) written to debug logs without masking.",
                affected_component="euro-lending-platform",
                file_path="src/main/java/com/eurolend/service/CustomerService.java",
                line_number=89,
                status=FindingStatus.OPEN,
                discovered_at="2026-06-24T10:30:00Z",
                sla_deadline="2026-06-27T10:30:00Z",
                remediation_suggestion="Implement PII masking in logging framework, use structured logging with redaction"
            ),
        ],
        "ENG-003": [
            SecurityFinding(
                finding_id="SNYK-HC-001",
                source_tool="Snyk",
                title="Known Vulnerable Dependency in Claims Processing",
                severity=Severity.MEDIUM,
                description="lodash 4.17.20 with prototype pollution vulnerability in claims module.",
                affected_component="claims-processing-ui",
                cve_id="CVE-2024-5432",
                file_path="package.json",
                status=FindingStatus.OPEN,
                discovered_at="2026-06-23T09:00:00Z",
                sla_deadline="2026-07-07T09:00:00Z",
                remediation_suggestion="Upgrade lodash to 4.17.25+"
            ),
        ]
    }
    return findings_map.get(client_id, [])


def get_mock_sox_controls(client_id):
    """Simulates SOX ITGC controls for a client engagement."""

    controls = [
        SOXControl(
            control_id="ITGC-CM-01",
            control_name="Change Management - Dual Approval",
            category="Change Management",
            description="All changes to production systems require approval from both Development Lead and Release Manager.",
            status=ControlStatus.EFFECTIVE,
            last_tested="2026-06-01",
            test_result="25/25 sampled changes had dual approval documented",
            evidence_links=["ServiceNow CHG records", "Git PR approval logs"],
            owner="Release Management Team"
        ),
        SOXControl(
            control_id="ITGC-CM-02",
            control_name="Change Management - Segregation of Duties",
            category="Change Management",
            description="Developer who writes code cannot approve their own PR or deploy to production.",
            status=ControlStatus.EFFECTIVE,
            last_tested="2026-06-01",
            test_result="All 25 sampled PRs reviewed by different person than author",
            evidence_links=["GitHub PR logs", "Deployment audit trail"],
            owner="Engineering Management"
        ),
        SOXControl(
            control_id="ITGC-AC-01",
            control_name="Access Control - Privileged Access Review",
            category="Access Control",
            description="Quarterly review of all privileged access to production systems.",
            status=ControlStatus.PARTIALLY_EFFECTIVE,
            last_tested="2026-05-15",
            test_result="2 of 48 accounts found with excessive privileges not yet remediated",
            evidence_links=["Access review spreadsheet", "ServiceNow tickets"],
            owner="Security Operations"
        ),
        SOXControl(
            control_id="ITGC-AC-02",
            control_name="Access Control - Joiner/Mover/Leaver",
            category="Access Control",
            description="Access provisioned within 24h of start, revoked within 4h of departure.",
            status=ControlStatus.EFFECTIVE,
            last_tested="2026-06-10",
            test_result="All 12 terminations in sample had access revoked within SLA",
            evidence_links=["HR system logs", "AD audit logs"],
            owner="Identity & Access Management"
        ),
        SOXControl(
            control_id="ITGC-OP-01",
            control_name="Operations - Backup & Recovery",
            category="Operations",
            description="Daily backups of all financial systems with monthly recovery testing.",
            status=ControlStatus.EFFECTIVE,
            last_tested="2026-06-05",
            test_result="Recovery test completed in 2.3 hours (SLA: 4 hours)",
            evidence_links=["AWS Backup reports", "Recovery test documentation"],
            owner="Infrastructure Team"
        ),
        SOXControl(
            control_id="ITGC-SD-01",
            control_name="SDLC - Security Testing Before Release",
            category="SDLC",
            description="All releases must pass SAST/DAST scanning with no critical findings before production deployment.",
            status=ControlStatus.DEFICIENT,
            last_tested="2026-06-20",
            test_result="3 of 10 sampled releases deployed with unresolved critical SAST findings",
            evidence_links=["Checkmarx scan reports", "Release notes"],
            owner="Application Security Team"
        ),
    ]
    return controls


def get_mock_obligations(client_id):
    """Returns client-specific compliance obligations parsed from contracts and regulations."""

    obligations_map = {
        "ENG-001": [
            ComplianceObligation(
                obligation_id="OBL-001-01",
                source="MSA §7.2",
                clause="Critical Vulnerability Remediation",
                description="All critical security vulnerabilities must be remediated within 48 hours of discovery.",
                domain=ComplianceDomain.CONTRACTUAL,
                sla_hours=48,
                penalty_description="$50,000 per incident + right to terminate",
                auto_notify_client=True
            ),
            ComplianceObligation(
                obligation_id="OBL-001-02",
                source="SOX Section 404",
                clause="ITGC Change Management",
                description="All changes to financially-significant systems require documented approval, testing, and segregation of duties.",
                domain=ComplianceDomain.SOX,
                penalty_description="Material weakness finding in annual audit"
            ),
            ComplianceObligation(
                obligation_id="OBL-001-03",
                source="TILA (Regulation Z)",
                clause="APR Calculation Accuracy",
                description="Annual Percentage Rate must be calculated accurately within 1/8 of 1%. System changes affecting calculation logic require validation.",
                domain=ComplianceDomain.REGULATORY,
                penalty_description="CFPB enforcement action + borrower restitution"
            ),
            ComplianceObligation(
                obligation_id="OBL-001-04",
                source="ECOA / Regulation B",
                clause="Fair Lending - Non-Discrimination",
                description="Loan decision systems must not discriminate based on protected characteristics. Any model or logic change requires disparate impact testing.",
                domain=ComplianceDomain.FAIR_LENDING,
                penalty_description="DOJ civil rights action + reputational damage"
            ),
            ComplianceObligation(
                obligation_id="OBL-001-05",
                source="PCI-DSS Req 6.5",
                clause="Secure Coding",
                description="Applications handling cardholder data must be free of common vulnerabilities (OWASP Top 10). Code must be reviewed before release.",
                domain=ComplianceDomain.SECURITY,
                sla_hours=168,
                penalty_description="PCI non-compliance, potential card brand fines"
            ),
            ComplianceObligation(
                obligation_id="OBL-001-06",
                source="MSA §12.1",
                clause="Data Residency",
                description="All client data must remain within US boundaries (AWS us-east-1 or us-west-2 only).",
                domain=ComplianceDomain.CONTRACTUAL,
                penalty_description="Immediate contract termination clause",
                auto_notify_client=True
            ),
        ],
        "ENG-002": [
            ComplianceObligation(
                obligation_id="OBL-002-01",
                source="GDPR Article 33",
                clause="Data Breach Notification",
                description="Personal data breaches must be reported to supervisory authority within 72 hours.",
                domain=ComplianceDomain.PRIVACY,
                sla_hours=72,
                penalty_description="Up to €20M or 4% of global annual turnover",
                auto_notify_client=True
            ),
            ComplianceObligation(
                obligation_id="OBL-002-02",
                source="DORA Article 19",
                clause="ICT Incident Reporting",
                description="Major ICT-related incidents must be reported to competent authority. Initial notification within 4 hours.",
                domain=ComplianceDomain.REGULATORY,
                sla_hours=4,
                penalty_description="Regulatory sanctions under DORA enforcement",
                auto_notify_client=True
            ),
            ComplianceObligation(
                obligation_id="OBL-002-03",
                source="MSA §9.4",
                clause="Data Sovereignty",
                description="All data processing and storage must occur within EU boundaries. No cross-border transfer without explicit authorization.",
                domain=ComplianceDomain.CONTRACTUAL,
                penalty_description="Contract termination + GDPR fine exposure",
                auto_notify_client=True
            ),
        ],
        "ENG-003": [
            ComplianceObligation(
                obligation_id="OBL-003-01",
                source="SOW §5.1",
                clause="Vulnerability Management",
                description="Critical vulnerabilities remediated within 72 hours, High within 14 days.",
                domain=ComplianceDomain.CONTRACTUAL,
                sla_hours=72,
                penalty_description="Service credit of 5% monthly fee per breach"
            ),
            ComplianceObligation(
                obligation_id="OBL-003-02",
                source="NAIC Model Law §668",
                clause="Information Security Program",
                description="Maintain comprehensive written information security program. Annual risk assessment required.",
                domain=ComplianceDomain.REGULATORY,
                penalty_description="State insurance commissioner enforcement"
            ),
        ]
    }
    return obligations_map.get(client_id, [])


def get_mock_code_change():
    """Simulates a developer code commit that will trigger cross-domain analysis."""

    return {
        "commit_id": "a3f7b2c",
        "author": "imam@hexaware.com",
        "timestamp": "2026-06-28T10:30:00Z",
        "branch": "feature/MORT-1542-arm-rate-adjustment",
        "message": "feat: Update ARM rate cap calculation logic for new CFPB guidance",
        "files_changed": [
            {
                "path": "src/services/rate_calculator.py",
                "type": "modified",
                "description": "Modified ARM interest rate calculation to implement new rate cap ceiling",
                "lines_added": 45,
                "lines_removed": 12,
                "touches_financial_logic": True,
                "touches_pii": False
            },
            {
                "path": "src/models/loan_application.py",
                "type": "modified",
                "description": "Added new field 'adjusted_rate_cap' to LoanApplication model",
                "lines_added": 8,
                "lines_removed": 0,
                "touches_financial_logic": True,
                "touches_pii": False
            },
            {
                "path": "src/services/borrower_eligibility.py",
                "type": "modified",
                "description": "Updated eligibility check to consider new rate cap in DTI calculation",
                "lines_added": 22,
                "lines_removed": 5,
                "touches_financial_logic": True,
                "touches_pii": True  # Uses borrower income data
            },
            {
                "path": "tests/test_rate_calculator.py",
                "type": "modified",
                "description": "Added unit tests for new rate cap logic",
                "lines_added": 67,
                "lines_removed": 0,
                "touches_financial_logic": False,
                "touches_pii": False
            }
        ],
        "jira_ticket": "MORT-1542",
        "jira_description": "Implement new ARM rate cap per CFPB Bulletin 2026-03 effective July 1, 2026",
        "client_id": "ENG-001",
        "sast_scan_result": {
            "tool": "Checkmarx",
            "critical": 0,
            "high": 1,
            "medium": 2,
            "details": [
                {
                    "severity": "HIGH",
                    "title": "Potential Race Condition in Rate Calculation",
                    "file": "src/services/rate_calculator.py",
                    "line": 78,
                    "description": "Non-atomic read-modify-write on shared rate configuration could lead to incorrect rate calculation under concurrent access."
                },
                {
                    "severity": "MEDIUM",
                    "title": "Missing Input Validation",
                    "file": "src/services/borrower_eligibility.py",
                    "line": 34,
                    "description": "Income parameter not validated for negative values before DTI calculation."
                },
                {
                    "severity": "MEDIUM",
                    "title": "Logging of Calculation Intermediate Values",
                    "file": "src/services/rate_calculator.py",
                    "line": 92,
                    "description": "Debug logging includes intermediate calculation values that could reveal pricing logic."
                }
            ]
        },
        "pr_reviewers": ["imam@hexaware.com", "imam@hexaware.com"],
        "pr_approved": True
    }

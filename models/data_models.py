"""
Compliance Twin - Core Data Models
Defines the structure for clients, engagements, obligations, findings, and compliance state.
"""

import json
from datetime import datetime, timedelta
from enum import Enum
from dataclasses import dataclass, field, asdict
from typing import List, Dict, Optional


class Severity(Enum):
    CRITICAL = "CRITICAL"
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"
    INFO = "INFO"


class ComplianceDomain(Enum):
    SOX = "SOX (Sarbanes-Oxley)"
    SECURITY = "Application Security (SAST/DAST/SCA)"
    REGULATORY = "Regulatory (TILA/RESPA/ECOA/GDPR)"
    CONTRACTUAL = "Contractual (MSA/SOW/SLA)"
    FAIR_LENDING = "Fair Lending (ECOA/HMDA)"
    PRIVACY = "Privacy (CCPA/GDPR)"
    INFRASTRUCTURE = "Infrastructure Security (Cloud/Network)"
    AUDIT = "Audit Readiness"


class ControlStatus(Enum):
    EFFECTIVE = "Effective"
    PARTIALLY_EFFECTIVE = "Partially Effective"
    DEFICIENT = "Deficient"
    NOT_TESTED = "Not Tested"


class FindingStatus(Enum):
    OPEN = "Open"
    IN_PROGRESS = "In Progress"
    REMEDIATED = "Remediated"
    ACCEPTED_RISK = "Accepted Risk"
    FALSE_POSITIVE = "False Positive"


@dataclass
class ClientProfile:
    """Represents a client engagement with their specific compliance requirements."""
    client_id: str
    client_name: str
    industry: str
    geography: str
    engagement_type: str
    applicable_frameworks: List[str]
    contractual_slas: Dict[str, str]
    regulatory_bodies: List[str]
    risk_tier: str  # Tier 1 (highest) to Tier 3
    auditor: str
    audit_frequency: str
    sox_applicable: bool = False
    pci_applicable: bool = False
    hipaa_applicable: bool = False
    fair_lending_applicable: bool = False


@dataclass
class ComplianceObligation:
    """A specific obligation from contract, regulation, or framework."""
    obligation_id: str
    source: str  # "MSA", "SOX", "TILA", "PCI-DSS", etc.
    clause: str
    description: str
    domain: ComplianceDomain
    sla_hours: Optional[int] = None  # Hours to resolve if breached
    penalty_description: Optional[str] = None
    auto_notify_client: bool = False


@dataclass
class SecurityFinding:
    """A finding from any security tool (Snyk, Checkmarx, Wiz, etc.)."""
    finding_id: str
    source_tool: str
    title: str
    severity: Severity
    description: str
    affected_component: str
    cve_id: Optional[str] = None
    file_path: Optional[str] = None
    line_number: Optional[int] = None
    status: FindingStatus = FindingStatus.OPEN
    discovered_at: str = ""
    sla_deadline: Optional[str] = None
    remediation_suggestion: Optional[str] = None


@dataclass
class SOXControl:
    """A SOX ITGC or application control."""
    control_id: str
    control_name: str
    category: str  # "Change Management", "Access Control", "Operations", "SDLC"
    description: str
    status: ControlStatus = ControlStatus.EFFECTIVE
    last_tested: str = ""
    test_result: str = ""
    evidence_links: List[str] = field(default_factory=list)
    owner: str = ""


@dataclass
class ComplianceTwinState:
    """The digital twin - represents complete compliance state of an engagement."""
    client: ClientProfile
    overall_score: float  # 0-100
    domain_scores: Dict[str, float] = field(default_factory=dict)
    obligations: List[ComplianceObligation] = field(default_factory=list)
    security_findings: List[SecurityFinding] = field(default_factory=list)
    sox_controls: List[SOXControl] = field(default_factory=list)
    compliance_debt_usd: float = 0.0
    trend: str = "stable"  # "improving", "stable", "degrading"
    last_updated: str = ""
    audit_readiness_pct: float = 0.0
    open_risks: int = 0
    days_since_last_audit: int = 0


@dataclass
class ChainReaction:
    """Represents a cross-domain impact propagation from a single event."""
    trigger_event: str
    trigger_type: str  # "code_commit", "infra_change", "regulatory_update", "personnel_change"
    timestamp: str
    impacts: List[Dict[str, str]] = field(default_factory=list)
    total_domains_affected: int = 0
    risk_score_delta: float = 0.0
    recommended_actions: List[str] = field(default_factory=list)
    requires_human_approval: bool = False
    auto_remediation_possible: bool = False


@dataclass
class AuditNarrative:
    """An auto-generated audit evidence narrative."""
    narrative_id: str
    timestamp: str
    event_type: str
    narrative_text: str
    controls_satisfied: List[str] = field(default_factory=list)
    evidence_artifacts: List[str] = field(default_factory=list)
    personnel_involved: List[str] = field(default_factory=list)
    client_id: str = ""

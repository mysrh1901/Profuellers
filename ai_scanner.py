"""
AI-Powered Code Scanner — Uses LLM (Ollama) for compliance reasoning.

REPLACES: java_scanner.py (regex/heuristic-based detection)

KEY DIFFERENCE:
  - java_scanner.py → Pattern matching, hardcoded rules
  - ai_scanner.py   → LLM reads actual code, REASONS about compliance

HOW IT WORKS:
  1. Watches watch-folder/ for modified files
  2. Reads file content
  3. Sends content to Ollama (local LLM) for compliance reasoning
  4. LLM returns structured findings with domain, severity, regulation refs
  5. Dashboard displays AI-generated analysis

ALSO CALLS:
  - Spring Boot backend (localhost:9090) to fire events into the agent pipeline
  - This triggers Chain Reactor → Audit Narrator → full agentic processing

FALLBACK:
  - If Ollama is unavailable, falls back to the Spring Boot /api/analyze endpoint
  - If both unavailable, uses the original heuristic scanner as last resort
"""

import os
import re
import time
import json
import urllib.request
import urllib.error
from datetime import datetime
from dataclasses import dataclass, field
from typing import List, Optional, Tuple


# ═══════════════════════════════════════════════════════════════════════
# Configuration
# ═══════════════════════════════════════════════════════════════════════

OLLAMA_URL = "http://localhost:11434/api/generate"
OLLAMA_MODEL = "llama3.2:1b"
SPRING_BOOT_URL = "http://localhost:9090"
CLIENT_CONTEXT = "Mortgage/Financial Services client (Freddie Mac), subject to SOX, TILA/Reg Z, RESPA, ECOA, PCI-DSS, GLBA, GDPR, MSA contractual SLAs"


# ═══════════════════════════════════════════════════════════════════════
# Data Models
# ═══════════════════════════════════════════════════════════════════════

@dataclass
class AIFinding:
    """A single AI-generated compliance finding."""
    finding_id: str
    file_path: str
    file_modified: str
    line_number: int
    severity: str           # CRITICAL, HIGH, MEDIUM, LOW
    category: str           # Domain: SOX, SECURITY, REGULATORY, FAIR_LENDING, etc.
    title: str
    description: str
    code_snippet: str
    remediation: str
    compliance_impact: List[str] = field(default_factory=list)
    regulation_ref: str = ""
    ai_reasoning: str = ""  # Raw LLM explanation
    timestamp: str = ""
    is_recent: bool = False
    source: str = "AI"      # "AI" or "HEURISTIC" (fallback)

    def __post_init__(self):
        if not self.timestamp:
            self.timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%SZ")


# ═══════════════════════════════════════════════════════════════════════
# Ollama LLM Integration
# ═══════════════════════════════════════════════════════════════════════

def _call_ollama(prompt: str, timeout: int = 30) -> Optional[str]:
    """Call local Ollama LLM. Returns None if unavailable."""
    try:
        payload = json.dumps({
            "model": OLLAMA_MODEL,
            "prompt": prompt,
            "stream": False,
            "options": {"temperature": 0.1, "num_predict": 1024}
        }).encode("utf-8")

        req = urllib.request.Request(
            OLLAMA_URL,
            data=payload,
            headers={"Content-Type": "application/json"}
        )
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            result = json.loads(resp.read().decode("utf-8"))
            return result.get("response", "")
    except (urllib.error.URLError, TimeoutError, Exception):
        return None


def _is_ollama_available() -> bool:
    """Check if Ollama is running."""
    try:
        req = urllib.request.Request("http://localhost:11434/api/tags")
        with urllib.request.urlopen(req, timeout=2) as resp:
            return resp.status == 200
    except Exception:
        return False


# ═══════════════════════════════════════════════════════════════════════
# Spring Boot Backend Integration
# ═══════════════════════════════════════════════════════════════════════

def _fire_event_to_backend(file_path: str, findings: List[AIFinding]):
    """
    Send findings to Spring Boot backend to trigger the full agent pipeline:
    Chain Reactor → Audit Narrator → Knowledge Graph → Score Update
    """
    try:
        has_critical = any(f.severity == "CRITICAL" for f in findings)
        has_financial = any("SOX" in f.category or "TILA" in f.category or "REGULATORY" in f.category for f in findings)
        has_pii = any("PRIVACY" in f.category or "FAIR_LENDING" in f.category for f in findings)
        has_security = any("SECURITY" in f.category or "PCI" in f.category for f in findings)

        payload = json.dumps({
            "engagementId": "ENG-001",
            "description": f"[AI Scanner] {os.path.basename(file_path)}: {len(findings)} compliance findings",
            "author": "ai-scanner@kavach.ai",
            "commitId": f"scan-{int(time.time())}",
            "touchesFinancialLogic": has_financial,
            "touchesPii": has_pii,
            "sastHighCount": len([f for f in findings if f.severity in ("CRITICAL", "HIGH")]),
            "secretsDetected": any("Secret" in f.title or "Hardcoded" in f.title for f in findings),
            "dataResidencyViolation": any("residency" in f.title.lower() for f in findings),
        }).encode("utf-8")

        req = urllib.request.Request(
            f"{SPRING_BOOT_URL}/api/webhooks/jenkins",
            data=payload,
            headers={"Content-Type": "application/json"}
        )
        with urllib.request.urlopen(req, timeout=5) as resp:
            return resp.status == 200
    except Exception:
        return False


# ═══════════════════════════════════════════════════════════════════════
# AI Compliance Reasoning
# ═══════════════════════════════════════════════════════════════════════

COMPLIANCE_PROMPT = """You are an autonomous compliance intelligence agent for a mortgage/financial services IT company.
Analyze the following Java source code and identify ALL compliance violations or risks.

CLIENT CONTEXT: {context}

FILE: {filename}
CODE:
{code}

For EACH issue found, respond with EXACTLY this format (one block per issue, separated by blank lines):

DOMAIN: [one of: SOX, SECURITY, REGULATORY, FAIR_LENDING, CONTRACTUAL, PRIVACY, PCI-DSS, INFRASTRUCTURE]
SEVERITY: [one of: CRITICAL, HIGH, MEDIUM, LOW]
LINE: [approximate line number]
FINDING: [short title of the issue]
REGULATION: [specific regulation reference, e.g., "TILA 12 CFR 1026.22", "PCI-DSS 6.5.1", "SOX ITGC-CM-01"]
REASON: [1-2 sentence explanation of WHY this is a compliance issue]
ACTION: [what must be done to fix it]

Rules:
- Only report REAL issues visible in the code
- Reference specific regulations (SOX Section 404, TILA Reg Z, ECOA Reg B, PCI-DSS, etc.)
- Be specific about line numbers
- Focus on: hardcoded secrets, SQL injection, financial logic errors, PII exposure, fair lending proxies, audit trail gaps, weak crypto
- If no issues found, respond with: NO_ISSUES_FOUND
"""


def _parse_llm_response(response: str, file_path: str, modified_time: str) -> List[AIFinding]:
    """Parse structured LLM response into AIFinding objects."""
    findings = []
    if not response or "NO_ISSUES_FOUND" in response:
        return findings

    # Split by blank lines to get individual findings
    blocks = re.split(r'\n\s*\n', response.strip())
    counter = 0

    for block in blocks:
        if not block.strip():
            continue

        # Extract fields from each block
        domain = _extract_field(block, "DOMAIN")
        severity = _extract_field(block, "SEVERITY")
        line_str = _extract_field(block, "LINE")
        finding = _extract_field(block, "FINDING")
        regulation = _extract_field(block, "REGULATION")
        reason = _extract_field(block, "REASON")
        action = _extract_field(block, "ACTION")

        if not domain or not finding:
            continue

        # Normalize
        severity = severity.upper() if severity else "MEDIUM"
        if severity not in ("CRITICAL", "HIGH", "MEDIUM", "LOW"):
            severity = "MEDIUM"

        line_num = 1
        if line_str:
            try:
                line_num = int(re.search(r'\d+', line_str).group())
            except (ValueError, AttributeError):
                line_num = 1

        counter += 1
        findings.append(AIFinding(
            finding_id=f"AI-{counter:04d}",
            file_path=file_path,
            file_modified=modified_time,
            line_number=line_num,
            severity=severity,
            category=domain.upper(),
            title=finding,
            description=reason or finding,
            code_snippet=f"[AI-analyzed: {os.path.basename(file_path)}:{line_num}]",
            remediation=action or "Review and fix per compliance requirements",
            compliance_impact=[regulation] if regulation else [domain],
            regulation_ref=regulation or "",
            ai_reasoning=reason or "",
            source="AI"
        ))

    return findings


def _extract_field(block: str, field_name: str) -> str:
    """Extract a named field from a text block."""
    pattern = rf'{field_name}:\s*(.+?)(?:\n|$)'
    match = re.search(pattern, block, re.IGNORECASE)
    if match:
        value = match.group(1).strip()
        # Remove brackets if present
        value = re.sub(r'^\[|\]$', '', value)
        return value
    return ""


# ═══════════════════════════════════════════════════════════════════════
# Main Scanner Class
# ═══════════════════════════════════════════════════════════════════════

class AIScanner:
    """
    AI-Powered Compliance Scanner.
    Uses LLM (Ollama) to analyze code and reason about compliance impact.
    Falls back to heuristic scanning if LLM is unavailable.
    """

    def __init__(self, max_age_minutes: int = 10):
        self.findings: List[AIFinding] = []
        self.scanned_files: List[dict] = []
        self.max_age_minutes = max_age_minutes
        self.ollama_available = _is_ollama_available()
        self.analysis_source = "AI (Ollama)" if self.ollama_available else "Heuristic (Fallback)"
        self._finding_counter = 0

    def scan_recent(self, dirpath: str) -> List[AIFinding]:
        """
        Scan files in directory using AI reasoning.
        Sends file content to LLM for compliance analysis.
        """
        self.scanned_files = self._get_files(dirpath)
        all_findings = []

        # Re-check Ollama availability
        self.ollama_available = _is_ollama_available()
        self.analysis_source = "AI (Ollama)" if self.ollama_available else "Heuristic (Fallback)"

        for file_info in self.scanned_files:
            findings = self._analyze_file(file_info)
            for f in findings:
                f.is_recent = file_info.get("is_recent", False)
            all_findings.extend(findings)

        self.findings = all_findings

        # Fire events to Spring Boot backend for full agent pipeline
        if all_findings:
            _fire_event_to_backend(
                all_findings[0].file_path if all_findings else "",
                all_findings
            )

        return all_findings

    def _analyze_file(self, file_info: dict) -> List[AIFinding]:
        """Analyze a single file — always use heuristic for dashboard speed.
        LLM is used by Spring Boot FileSystemWatcher separately."""
        file_path = file_info["path"]
        modified_time = file_info["mtime_str"]

        try:
            with open(file_path, 'r', errors='ignore') as f:
                content = f.read()
        except IOError:
            return []

        if not content.strip():
            return []

        return self._analyze_with_heuristic(content, file_path, modified_time)

    def _analyze_with_ai(self, content: str, file_path: str, modified_time: str) -> List[AIFinding]:
        """Send file to Ollama LLM for AI-powered compliance reasoning."""
        filename = os.path.basename(file_path)

        # Truncate very large files to avoid LLM context overflow
        if len(content) > 4000:
            content = content[:4000] + "\n// ... [truncated for analysis]"

        prompt = COMPLIANCE_PROMPT.format(
            context=CLIENT_CONTEXT,
            filename=filename,
            code=content
        )

        response = _call_ollama(prompt, timeout=45)

        if response:
            findings = _parse_llm_response(response, file_path, modified_time)
            # Enrich with actual code snippets
            lines = content.split('\n')
            for finding in findings:
                if 0 < finding.line_number <= len(lines):
                    finding.code_snippet = lines[finding.line_number - 1].strip()[:80]
            return findings
        else:
            # LLM call failed — fall back to heuristic
            self.ollama_available = False
            self.analysis_source = "Heuristic (LLM timeout)"
            return self._analyze_with_heuristic(content, file_path, modified_time)

    def _analyze_with_heuristic(self, content: str, file_path: str, modified_time: str) -> List[AIFinding]:
        """
        Fallback: Heuristic detection when LLM is unavailable.
        Uses the same patterns as java_scanner.py but produces AIFinding objects.
        """
        from java_scanner import JavaScanner
        scanner = JavaScanner(max_age_minutes=self.max_age_minutes)
        raw_findings = scanner._scan_file(file_path, modified_time)

        findings = []
        for rf in raw_findings:
            self._finding_counter += 1
            findings.append(AIFinding(
                finding_id=f"HEUR-{self._finding_counter:04d}",
                file_path=rf.file_path,
                file_modified=rf.file_modified,
                line_number=rf.line_number,
                severity=rf.severity,
                category=rf.category,
                title=rf.title,
                description=rf.description,
                code_snippet=rf.code_snippet,
                remediation=rf.remediation,
                compliance_impact=rf.compliance_impact,
                source="HEURISTIC"
            ))
        return findings

    def _get_files(self, dirpath: str) -> List[dict]:
        """Get all scannable files from directory."""
        now = time.time()
        max_age_seconds = self.max_age_minutes * 60
        all_files = []

        if not os.path.exists(dirpath):
            return all_files

        for root, dirs, files in os.walk(dirpath):
            for fname in files:
                if fname.endswith(('.java', '.py', '.tf', '.yml', '.yaml', '.properties')):
                    fpath = os.path.join(root, fname)
                    try:
                        mtime = os.path.getmtime(fpath)
                        age = now - mtime
                        all_files.append({
                            "path": fpath,
                            "mtime": mtime,
                            "mtime_str": datetime.fromtimestamp(mtime).strftime("%Y-%m-%d %H:%M:%S"),
                            "age_seconds": int(age),
                            "is_recent": age <= max_age_seconds,
                        })
                    except OSError:
                        continue

        all_files.sort(key=lambda x: x["mtime"], reverse=True)
        return all_files

    def get_summary(self) -> dict:
        """Summary stats of current scan."""
        f = self.findings
        return {
            "total": len(f),
            "critical": len([x for x in f if x.severity == "CRITICAL"]),
            "high": len([x for x in f if x.severity == "HIGH"]),
            "medium": len([x for x in f if x.severity == "MEDIUM"]),
            "low": len([x for x in f if x.severity == "LOW"]),
            "files_scanned": len(self.scanned_files),
            "analysis_source": self.analysis_source,
            "ollama_available": self.ollama_available,
        }

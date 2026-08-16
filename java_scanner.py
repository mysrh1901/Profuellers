"""
Java Code Scanner — Detects compliance-relevant issues in Java files.

KEY BEHAVIOR:
  - Only scans files modified RECENTLY (within last 10 minutes by default)
  - Uses heuristic detection (entropy, structure) — NOT keyword matching
  - Dashboard shows which file was modified, when, and what's wrong

Detection approach:
  1. Find .java files modified recently (based on file mtime vs current time)
  2. String literals that LOOK like secrets (high entropy, known formats)
  3. Structural patterns (SQL concat, System.out, SSL bypass, etc.)
  4. PII handling / sensitive logging patterns
"""

import re
import os
import math
import string
import time
from datetime import datetime
from dataclasses import dataclass, field
from typing import List


@dataclass
class ScanFinding:
    """A single finding from scanning a Java file."""
    finding_id: str
    file_path: str
    file_modified: str  # When the file was last modified
    line_number: int
    severity: str
    category: str
    title: str
    description: str
    code_snippet: str
    remediation: str
    compliance_impact: List[str] = field(default_factory=list)
    timestamp: str = ""
    is_recent: bool = False  # True if file was modified in last 10 min

    def __post_init__(self):
        if not self.timestamp:
            self.timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%SZ")


def _shannon_entropy(s: str) -> float:
    """Calculate Shannon entropy. High entropy = likely a secret."""
    if not s:
        return 0.0
    freq = {}
    for c in s:
        freq[c] = freq.get(c, 0) + 1
    length = len(s)
    return -sum((count / length) * math.log2(count / length)
                for count in freq.values())


def _looks_like_secret(value: str) -> bool:
    """
    Heuristic: Does this string literal look like a hardcoded secret?
    Uses entropy + character mix + length + known formats.
    No keyword matching — works regardless of variable name.
    """
    if len(value) < 6:
        return False

    # Skip obvious non-secrets (URLs, formats, SQL keywords, booleans)
    skip_patterns = [
        r'^https?://', r'^ftp://', r'^file://',
        r'^(true|false|null|none|yes|no)$',
        r'^(SELECT|INSERT|UPDATE|DELETE|CREATE|DROP|FROM|WHERE|AND|OR)\b',
        r'^(application|classpath|UTF-8|ISO-8859|Content-Type)$',
        r'^\d{4}-\d{2}-\d{2}',  # date formats
        r'^(yyyy|MM|dd|HH|mm|ss)',  # date patterns
        r'^(localhost|127\.0\.0|0\.0\.0)',
        r'^\w+\.\w+\.\w+$',  # package names like com.example.service
    ]
    for pat in skip_patterns:
        if re.match(pat, value, re.IGNORECASE):
            return False

    # Known secret format prefixes (always a secret regardless of entropy)
    secret_prefixes = [
        "sk-", "sk_", "pk-", "pk_", "api-", "api_",
        "ghp_", "gho_", "ghu_", "ghs_", "github_pat_",
        "xoxb-", "xoxp-", "xoxa-",
        "AKIA", "ASIA",  # AWS keys
        "eyJ",  # JWT
        "AIza",  # Google
        "SG.",  # SendGrid
        "rk_live_", "rk_test_", "sk_live_", "sk_test_",  # Stripe
    ]
    for prefix in secret_prefixes:
        if value.startswith(prefix):
            return True

    # Entropy + character diversity analysis
    entropy = _shannon_entropy(value)
    has_upper = any(c in string.ascii_uppercase for c in value)
    has_lower = any(c in string.ascii_lowercase for c in value)
    has_digit = any(c in string.digits for c in value)
    has_special = any(c in string.punctuation for c in value)
    char_types = sum([has_upper, has_lower, has_digit, has_special])

    # High entropy + mixed character types = very likely a secret
    if entropy >= 3.5 and char_types >= 3:
        return True
    if entropy >= 3.0 and char_types >= 3 and len(value) >= 8:
        return True
    # Long random-looking strings
    if entropy >= 4.0 and len(value) >= 16:
        return True
    # Short but clearly mixed (like "Test@123", "P@ss1word")
    if char_types >= 3 and len(value) >= 6 and not value.startswith("/"):
        return True

    return False


def _get_recently_modified_files(dirpath: str, max_age_minutes: int = 10) -> List[dict]:
    """
    Find all .java files in the directory.
    Marks which ones were modified recently (within max_age_minutes).
    Always returns ALL files so issues never disappear.
    """
    now = time.time()
    max_age_seconds = max_age_minutes * 60
    all_files = []

    for root, dirs, files in os.walk(dirpath):
        for fname in files:
            if fname.endswith('.java'):
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

    # Sort by most recently modified first
    all_files.sort(key=lambda x: x["mtime"], reverse=True)
    return all_files


class JavaScanner:
    """
    Scans RECENTLY MODIFIED Java files for compliance issues.
    Only processes files changed within the configured time window.
    """

    def __init__(self, max_age_minutes: int = 10):
        self.findings: List[ScanFinding] = []
        self.scanned_files: List[dict] = []
        self.max_age_minutes = max_age_minutes
        self._finding_counter = 0

    def scan_recent(self, dirpath: str) -> List[ScanFinding]:
        """
        Scan ALL .java files in the directory.
        Marks findings from recently-modified files so UI can highlight them.
        Issues never disappear — old files still show their problems.
        """
        self.scanned_files = _get_recently_modified_files(dirpath, self.max_age_minutes)
        all_findings = []

        for file_info in self.scanned_files:
            findings = self._scan_file(file_info["path"], file_info["mtime_str"])
            # Tag findings from recently modified files
            for f in findings:
                f.is_recent = file_info.get("is_recent", False)
            all_findings.extend(findings)

        self.findings = all_findings
        return all_findings

    def _scan_file(self, filepath: str, modified_time: str) -> List[ScanFinding]:
        """Scan a single Java file with all detection rules."""
        if not os.path.exists(filepath):
            return []

        with open(filepath, 'r', errors='ignore') as f:
            lines = f.readlines()

        file_findings = []

        for line_num, line in enumerate(lines, 1):
            stripped = line.strip()

            # Skip blank lines
            if not stripped:
                continue

            # Check comments for TODOs (still useful to flag)
            if stripped.startswith("//") or stripped.startswith("*"):
                if re.search(r'(TODO|FIXME|HACK|TEMP|XXX).*\b(fix|security|before prod|remove)', stripped, re.IGNORECASE):
                    file_findings.append(self._make_finding(
                        filepath, modified_time, line_num, "LOW",
                        "Technical Debt",
                        "Unresolved security/quality marker",
                        "Known deferred work that may hide compliance gaps.",
                        stripped[:80],
                        "Resolve or create tracked Jira ticket",
                        ["ITGC-SD-01"]
                    ))
                continue

            # === DETECTION 1: Hardcoded secrets (entropy-based) ===
            self._check_hardcoded_secrets(stripped, filepath, modified_time, line_num, file_findings)

            # === DETECTION 2: System.out/err ===
            if re.search(r'System\.(out|err)\.(print|println|printf|format)\s*\(', stripped):
                file_findings.append(self._make_finding(
                    filepath, modified_time, line_num, "HIGH",
                    "Insecure Logging",
                    "System.out/err bypasses log management",
                    "Console output cannot be monitored, masked, or sent to SIEM.",
                    stripped[:80],
                    "Use SLF4J/Log4j with proper log levels",
                    ["SOX Audit Trail", "ITGC-OP-01", "PCI-DSS 10.3"]
                ))

            # === DETECTION 3: SQL string concatenation ===
            if re.search(r'["\'].*\b(SELECT|INSERT|UPDATE|DELETE)\b.*["\']\s*\+\s*\w+', stripped, re.IGNORECASE):
                file_findings.append(self._make_finding(
                    filepath, modified_time, line_num, "CRITICAL",
                    "SQL Injection",
                    "SQL built with string concatenation",
                    "Dynamic SQL via concatenation allows injection attacks.",
                    stripped[:80],
                    "Use PreparedStatement with parameterized queries",
                    ["PCI-DSS 6.5.1", "OWASP A03:2021", "SOX Data Integrity"]
                ))

            # === DETECTION 4: Sensitive data in log statements ===
            if re.search(r'\blog(ger)?\.(info|debug|warn|error|trace)\s*\(.*\+\s*\w+', stripped, re.IGNORECASE):
                # Check if logging a variable (any variable concatenation in a log = risk)
                file_findings.append(self._make_finding(
                    filepath, modified_time, line_num, "HIGH",
                    "Data Exposure in Logs",
                    "Variable data concatenated into log statement",
                    "Logging variables may expose PII, credentials, or financial data. Logs often have weaker access controls than the application.",
                    stripped[:80],
                    "Use structured logging with masking. Never log raw variables.",
                    ["GDPR Art.5", "PCI-DSS 3.4", "SOX Data Protection"]
                ))

            # === DETECTION 5: SSL/TLS bypass ===
            if re.search(r'(setHostnameVerifier|TrustAll|ALLOW_ALL|setSSLSocketFactory|X509TrustManager.*checkServer)', stripped, re.IGNORECASE):
                file_findings.append(self._make_finding(
                    filepath, modified_time, line_num, "CRITICAL",
                    "SSL/TLS Bypass",
                    "SSL certificate validation disabled or bypassed",
                    "Disabling SSL verification enables man-in-the-middle attacks.",
                    stripped[:80],
                    "Remove SSL bypass. Use proper cert management.",
                    ["PCI-DSS 4.1", "SOX Data Integrity", "MSA §7.2"]
                ))

            # === DETECTION 6: Weak random ===
            if re.search(r'\bnew\s+Random\s*\(|Math\.random\s*\(', stripped):
                file_findings.append(self._make_finding(
                    filepath, modified_time, line_num, "MEDIUM",
                    "Weak Cryptography",
                    "Non-cryptographic random number generator",
                    "java.util.Random is predictable. Exploitable if used for tokens or security decisions.",
                    stripped[:80],
                    "Use java.security.SecureRandom",
                    ["PCI-DSS 6.5.3", "NIST SP 800-90A"]
                ))

            # === DETECTION 7: Insecure HTTP ===
            if re.search(r'["\']http://[^"\']*\.(com|io|org|net|internal)', stripped):
                file_findings.append(self._make_finding(
                    filepath, modified_time, line_num, "MEDIUM",
                    "Insecure Transport",
                    "Unencrypted HTTP connection to external service",
                    "HTTP traffic can be intercepted. All external communication must use TLS.",
                    stripped[:80],
                    "Use HTTPS. Enforce TLS 1.2+.",
                    ["PCI-DSS 4.1", "GDPR Art.32"]
                ))

            # === DETECTION 8: Empty catch blocks ===
            if re.search(r'catch\s*\([^)]+\)\s*\{\s*\}', stripped) or re.search(r'catch\s*\([^)]+\)\s*\{\s*$', stripped):
                file_findings.append(self._make_finding(
                    filepath, modified_time, line_num, "MEDIUM",
                    "Error Swallowing",
                    "Empty or silent catch block",
                    "Swallowed exceptions hide failures. Compliance violations may go undetected.",
                    stripped[:80],
                    "Log exception, rethrow, or handle. Never swallow.",
                    ["SOX Audit Trail", "ITGC-OP-01"]
                ))

            # === DETECTION 9: Exec / Runtime.exec (command injection) ===
            if re.search(r'Runtime\s*\.\s*getRuntime\s*\(\s*\)\s*\.\s*exec|ProcessBuilder', stripped):
                file_findings.append(self._make_finding(
                    filepath, modified_time, line_num, "CRITICAL",
                    "Command Injection Risk",
                    "OS command execution detected",
                    "Runtime.exec or ProcessBuilder can lead to command injection if inputs aren't sanitized.",
                    stripped[:80],
                    "Avoid exec. If needed, use allowlists and never pass user input directly.",
                    ["PCI-DSS 6.5.1", "OWASP A03:2021"]
                ))

            # === DETECTION 10: Deprecated crypto ===
            if re.search(r'(DES|MD5|SHA-?1|RC4)\b', stripped) and not stripped.startswith("//"):
                file_findings.append(self._make_finding(
                    filepath, modified_time, line_num, "HIGH",
                    "Weak Cryptography",
                    "Deprecated/weak cryptographic algorithm",
                    "DES, MD5, SHA1, RC4 are broken. Data protected with these can be compromised.",
                    stripped[:80],
                    "Use AES-256, SHA-256+, or bcrypt/argon2 for passwords",
                    ["PCI-DSS 3.4", "NIST SP 800-131A", "FIPS 140-2"]
                ))

            # === DETECTION 11: FDA — Audit Trail Gap (data override without logging) ===
            if re.search(r'(override|overwrite|update.*result|set.*result|modify.*record)', stripped, re.IGNORECASE) and not stripped.startswith("//"):
                if not re.search(r'(audit|log|trail|history|record.*change)', stripped, re.IGNORECASE):
                    file_findings.append(self._make_finding(
                        filepath, modified_time, line_num, "CRITICAL",
                        "FDA Audit Trail Violation",
                        "Data modification without audit trail (21 CFR 11.10b)",
                        "FDA requires immutable audit trail for all data changes: who, what, when, why, old/new value.",
                        stripped[:80],
                        "Add audit logging before any data modification. Record user, timestamp, reason, previous value.",
                        ["FDA 21CFR11.10(b)", "Data Integrity ALCOA+", "GxP Audit Trail"]
                    ))

            # === DETECTION 12: FDA — Shared/Generic Account ===
            if re.search(r'(generic|shared|common|lab_analyst|default_user|admin_shared)', stripped, re.IGNORECASE) and not stripped.startswith("//"):
                if re.search(r'(user|account|login|credential|auth)', stripped, re.IGNORECASE):
                    file_findings.append(self._make_finding(
                        filepath, modified_time, line_num, "CRITICAL",
                        "FDA Shared Account Violation",
                        "Generic/shared account detected (21 CFR 11.10c)",
                        "FDA requires unique accounts per individual. Shared accounts prevent data attribution.",
                        stripped[:80],
                        "Replace with individual user accounts. Each person must have unique credentials.",
                        ["FDA 21CFR11.10(c)", "Data Integrity - Attributable", "GxP Access Control"]
                    ))

            # === DETECTION 13: FDA — Manual Date/Time Entry (backdating risk) ===
            if re.search(r'(date|time|timestamp)\s*[=,]', stripped, re.IGNORECASE) and not stripped.startswith("//"):
                if re.search(r'(manual|input|param|argument|string\s+date)', stripped, re.IGNORECASE):
                    file_findings.append(self._make_finding(
                        filepath, modified_time, line_num, "HIGH",
                        "FDA Data Integrity — Backdating Risk",
                        "Manual date/time entry allows backdating (ALCOA: Contemporaneous)",
                        "FDA ALCOA+ requires data to be recorded contemporaneously. Manual date entry enables backdating fraud.",
                        stripped[:80],
                        "Use system-generated timestamps only. No manual date entry for GxP records.",
                        ["FDA 21CFR11 ALCOA+", "Data Integrity - Contemporaneous", "GxP"]
                    ))

            # === DETECTION 14: FDA — E-Signature Without Verification ===
            if re.search(r'(sign|signature|approve|release|certif)', stripped, re.IGNORECASE) and not stripped.startswith("//"):
                if re.search(r'(name|string|param|input)', stripped, re.IGNORECASE):
                    if not re.search(r'(verify|authenticate|validate|mfa|two.?factor|password)', stripped, re.IGNORECASE):
                        file_findings.append(self._make_finding(
                            filepath, modified_time, line_num, "CRITICAL",
                            "FDA E-Signature Violation",
                            "Electronic signature without identity verification (21 CFR 11.50)",
                            "FDA requires e-signatures to be unique to individual with identity verification. No signing without authentication.",
                            stripped[:80],
                            "Implement two-factor authentication for signing. Verify identity before accepting signature.",
                            ["FDA 21CFR11.50", "FDA 21CFR11.100", "GxP E-Signature"]
                        ))

        return file_findings

    def _check_hardcoded_secrets(self, line, filepath, modified_time, line_num, findings_list):
        """
        Detect hardcoded secrets by finding string literal assignments
        and running entropy/format heuristics on the value.
        Works regardless of variable name.
        """
        # Match: any variable = "some string"
        # Covers: String x = "...", final String x = "...", private static X = "..."
        patterns = [
            r'=\s*"([^"]{6,})"',       # any assignment to a string literal (6+ chars)
            r'=\s*\'([^\']{6,})\'',     # single quotes (less common in Java but possible)
        ]
        for pat in patterns:
            for match in re.finditer(pat, line):
                value = match.group(1)
                if _looks_like_secret(value):
                    findings_list.append(self._make_finding(
                        filepath, modified_time, line_num, "CRITICAL",
                        "Hardcoded Secret",
                        f"String literal appears to be a secret (entropy: {_shannon_entropy(value):.1f}, len: {len(value)}, mixed chars)",
                        f"Detected value pattern in: {os.path.basename(filepath)}:{line_num}. Secrets in source can be extracted from bytecode, git history, or decompiled JARs.",
                        line.strip()[:80],
                        "Use environment variables, AWS Secrets Manager, or vault",
                        ["PCI-DSS 6.5.3", "SOX ITGC-AC-01", "MSA §7.2 Critical"]
                    ))

    def _make_finding(self, filepath, modified_time, line_num, severity, category,
                      title, description, code_snippet, remediation, compliance):
        self._finding_counter += 1
        return ScanFinding(
            finding_id=f"LIVE-{self._finding_counter:04d}",
            file_path=filepath,
            file_modified=modified_time,
            line_number=line_num,
            severity=severity,
            category=category,
            title=title,
            description=description,
            code_snippet=code_snippet,
            remediation=remediation,
            compliance_impact=compliance,
        )

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
            "recently_modified": [
                {"file": os.path.basename(fi["path"]), "modified": fi["mtime_str"], "age_sec": fi["age_seconds"]}
                for fi in self.scanned_files
            ],
        }

# Critical Controls Reference — Per Compliance Framework

## 1. SOX (Sarbanes-Oxley) — IT General Controls (ITGC)

| Control ID | Control Name | What It Requires |
|-----------|-------------|-----------------|
| ITGC-CM-01 | Dual Approval | All changes to financial systems require 2 approvers |
| ITGC-CM-02 | Segregation of Duties | Developer ≠ Approver ≠ Deployer |
| ITGC-CM-03 | Change Documentation | Every change must have ticket + justification + test evidence |
| ITGC-SD-01 | Security Testing | SAST/DAST scan must pass before production deployment |
| ITGC-AC-01 | Privileged Access Review | Quarterly review of all prod access |
| ITGC-AC-02 | Joiner/Mover/Leaver | Access revoked within 4 hours of departure |
| ITGC-OP-01 | Backup & Recovery | Daily backup + monthly recovery test |

---

## 2. Security (OWASP / SAST)

| Control ID | Control Name | What It Requires |
|-----------|-------------|-----------------|
| OWASP-A01 | Broken Access Control | Enforce least privilege, deny by default |
| OWASP-A02 | Cryptographic Failures | No weak crypto (DES, MD5, SHA1), enforce AES-256 |
| OWASP-A03 | Injection | No SQL/command injection — use parameterized queries |
| OWASP-A07 | Auth Failures | No hardcoded secrets, enforce MFA |
| OWASP-A09 | Logging Failures | Structured logging, no PII in logs |
| CWE-362 | Race Condition | Thread-safe operations on shared mutable state |
| CWE-798 | Hardcoded Credentials | No secrets in source code |

---

## 3. TILA / Regulation Z (Truth in Lending Act)

| Control ID | Control Name | What It Requires |
|-----------|-------------|-----------------|
| TILA-APR-01 | APR Accuracy | APR accurate to within 1/8 of 1% (0.125%) |
| TILA-DISC-01 | Loan Estimate Timing | LE delivered within 3 business days of application |
| TILA-DISC-02 | Closing Disclosure | CD delivered 3 days before consummation |
| TILA-ARM-01 | ARM Adjustment Notice | 210-day advance notice before rate adjustment |
| TILA-ARM-02 | Rate Cap Enforcement | Periodic and lifetime caps per agreement |
| TILA-RESPA-01 | Fee Tolerance | Fees cannot exceed disclosed amount by > 10% |

---

## 4. Fair Lending — ECOA / Regulation B

| Control ID | Control Name | What It Requires |
|-----------|-------------|-----------------|
| ECOA-DI-01 | Disparate Impact Testing | Model changes require testing against protected classes |
| ECOA-PROXY-01 | No Proxy Variables | No ZIP code, school name, or other race proxies in decisions |
| ECOA-AGE-01 | Age Non-Discrimination | Cannot penalize applicants based on age |
| ECOA-INCOME-01 | Income Source Neutrality | Alimony/child support income counted equally |
| ECOA-NOTICE-01 | Adverse Action Notice | Must provide reason for denial within 30 days |
| HMDA-RPT-01 | HMDA Reporting | Report all applications with demographic data |

---

## 5. Contractual — MSA / SLA

| Control ID | Control Name | What It Requires |
|-----------|-------------|-----------------|
| MSA-7.2 | Critical Vuln SLA | Fix critical vulns within 48 hours ($50K penalty) |
| MSA-7.3 | High Vuln SLA | Fix high vulns within 7 days |
| MSA-8.3 | Encryption in Transit | All data encrypted with TLS 1.2+ |
| MSA-9.1 | Credential Management | All secrets in approved vault (no hardcoding) |
| MSA-10.2 | No PII in Logs | PII must not appear in application logs |
| MSA-12.1 | Data Residency | Data stays in US-only (us-east-1, us-west-2) |
| MSA-14.1 | Client Notification | 48h advance notice for production changes |

---

## 6. PCI-DSS (Payment Card Industry)

| Control ID | Control Name | What It Requires |
|-----------|-------------|-----------------|
| PCI-3.2 | No CVV Storage | Never store CVV/CVC after authorization |
| PCI-3.4 | PAN Masking | Render PAN unreadable (show only first 6 + last 4) |
| PCI-3.5 | Strong Encryption | AES-256 minimum for stored card data |
| PCI-4.1 | Encrypt Transmission | TLS 1.2+ for card data in transit |
| PCI-6.3.2 | Code Review | Mandatory code review before release |
| PCI-6.5 | Secure Coding | Address OWASP Top 10 in all card-handling code |
| PCI-8.3 | MFA for CDE Access | Multi-factor auth for cardholder data environment |
| PCI-10.2 | Audit All Access | Log all individual access to cardholder data |

---

## 7. DORA (Digital Operational Resilience Act) — EU Financial Sector

| Control ID | Control Name | What It Requires |
|-----------|-------------|-----------------|
| DORA-19 | ICT Incident Reporting | Report major incidents within 4 hours |
| DORA-26 | Third-Party Risk | Assess and monitor all ICT third-party providers |
| DORA-24 | Threat-Led Testing | Annual penetration testing of critical systems |
| DORA-11 | ICT Risk Management | Identify, protect, detect, respond, recover |
| DORA-28 | Subcontracting Oversight | Monitor subcontractors of critical ICT providers |

---

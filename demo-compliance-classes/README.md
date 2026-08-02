# 🎯 Demo Compliance Classes

These 6 Java classes demonstrate **each compliance domain** that Regulith AI's Chain Reactor detects from a single code commit. Use them during the demo to show how one event triggers cross-domain compliance analysis.

---

## How to Use in Demo

### Option 1: Copy to `watch-folder/` for Live Dashboard Detection

```bash
# Copy one class at a time — dashboard auto-refreshes in 3 seconds
cp demo-compliance-classes/SOXChangeManagement.java watch-folder/
cp demo-compliance-classes/SecurityRaceCondition.java watch-folder/
cp demo-compliance-classes/TILARegulationZ.java watch-folder/
cp demo-compliance-classes/FairLendingECOA.java watch-folder/
cp demo-compliance-classes/ContractualSLABreach.java watch-folder/
cp demo-compliance-classes/PCIDSSCodeReview.java watch-folder/
```

Open http://localhost:8080 → **Live Scan** tab to see findings appear in real-time.

### Option 2: Show Each Class Individually (Walk-through)

Open each file and explain the compliance violation. The class headers contain:
- **Compliance domain** and specific regulation
- **Violation scenario** (what the developer did wrong)
- **What Regulith AI detects** (agent reasoning)
- **Chain reaction** (causal propagation across domains)

---

## Classes Overview

| # | File | Compliance Domain | Key Violation |
|---|------|-------------------|---------------|
| 1 | `SOXChangeManagement.java` | SOX ITGC | Change to financial system without dual approval |
| 2 | `SecurityRaceCondition.java` | Application Security | Race condition in rate calc + hardcoded secrets |
| 3 | `TILARegulationZ.java` | TILA / Regulation Z | APR precision error exceeds 1/8% tolerance |
| 4 | `FairLendingECOA.java` | Fair Lending (ECOA) | ZIP code proxy + income source discrimination |
| 5 | `ContractualSLABreach.java` | MSA Contractual | SQL injection triggers 48h/$50K SLA |
| 6 | `PCIDSSCodeReview.java` | PCI-DSS | No code review + CVV storage + weak crypto |

---

## Demo Script (Suggested Flow)

1. **Start with the scenario:** "A developer commits code to the mortgage rate calculation module..."

2. **Show Chain Reactor output:** Run `python3 main.py` — shows how ONE commit propagates across all 6 domains.

3. **Walk through each class:** Open each file to show the specific violation pattern:
   - SOX: "This change was deployed without approval — audit finding"
   - Security: "SAST found a race condition — deployment blocked"
   - TILA: "APR is off by 0.23% — CFPB enforcement risk"
   - Fair Lending: "ZIP code = proxy for race — DOJ exposure"
   - Contractual: "SQL injection = critical finding — 48h SLA, $50K penalty"
   - PCI-DSS: "Stored CVV + no code review — PCI automatic failure"

4. **Show Live Detection:** Copy files to `watch-folder/` one by one, show dashboard updating.

5. **Key Takeaway:** "One commit. Six compliance domains. Zero surprises. That's Regulith AI."

---

## What the Scanner Detects in Each File

| Detection Rule | SOX | Security | TILA | ECOA | Contractual | PCI-DSS |
|----------------|-----|----------|------|------|-------------|---------|
| Hardcoded secrets | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| System.out (audit bypass) | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| SQL injection | ✓ | | | | ✓ | ✓ |
| Weak crypto (DES/MD5) | | | | | | ✓ |
| Weak random | | ✓ | | | | ✓ |
| Insecure HTTP | | | | | ✓ | |
| Sensitive data logging | | ✓ | | ✓ | ✓ | ✓ |

---

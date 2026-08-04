#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════
# DEMO: Add New Compliance Control — System Adapts WITHOUT Code Changes
# ═══════════════════════════════════════════════════════════════════════
#
# THIS SHOWS: "When a new regulation drops, our tool adapts instantly.
#              No code changes. No redeployment. Just feed the control text."
#
# SCENARIO: Client just became subject to EU DORA regulation.
#           We add DORA controls → system immediately enforces them.
#
# USAGE: Run each curl command one at a time during demo.
# ═══════════════════════════════════════════════════════════════════════

echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║  DEMO: Adding New Compliance Framework (DORA)               ║"
echo "║  System adapts INSTANTLY — no code changes needed            ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# ─── Step 1: Show current policy count ──────────────────────────────
echo "▸ Step 1: Check current policies..."
echo ""
curl -s http://localhost:9090/api/policies/summary | python3 -m json.tool
echo ""
echo "  ↑ This is BEFORE adding DORA. Note the total policy count."
echo ""
read -p "  Press Enter to add DORA Article 19 (ICT Incident Reporting)..."

# ─── Step 2: Add DORA Article 19 — ICT Incident Reporting ──────────
echo ""
echo "▸ Step 2: Adding DORA Article 19 — ICT Incident Reporting..."
echo "  (The AI agent parses the regulation text and creates a policy)"
echo ""
curl -s -X POST http://localhost:9090/api/controls/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "framework": "DORA (EU Digital Operational Resilience Act)",
    "controlText": "Article 19 - ICT Incident Reporting: Financial entities shall report major ICT-related incidents to the competent authority. The initial notification must be submitted within 4 hours of the incident being classified as major. A full incident report must follow within 72 hours. Incidents affecting confidentiality, integrity, or availability of critical systems are classified as major."
  }' | python3 -m json.tool
echo ""
read -p "  Press Enter to add DORA Article 26 (Third-Party Risk)..."

# ─── Step 3: Add DORA Article 26 — Third-Party Risk ────────────────
echo ""
echo "▸ Step 3: Adding DORA Article 26 — Third-Party ICT Risk..."
echo ""
curl -s -X POST http://localhost:9090/api/controls/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "framework": "DORA (EU Digital Operational Resilience Act)",
    "controlText": "Article 26 - Third-Party Risk Management: Financial entities must assess, monitor, and manage risks arising from ICT third-party service providers. Before entering into contractual arrangements, entities shall perform due diligence including risk assessment of the provider. Critical ICT third-party providers must be subject to ongoing monitoring and annual review."
  }' | python3 -m json.tool
echo ""
read -p "  Press Enter to add DORA Article 24 (Threat-Led Penetration Testing)..."

# ─── Step 4: Add DORA Article 24 — Penetration Testing ─────────────
echo ""
echo "▸ Step 4: Adding DORA Article 24 — Threat-Led Penetration Testing..."
echo ""
curl -s -X POST http://localhost:9090/api/controls/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "framework": "DORA (EU Digital Operational Resilience Act)",
    "controlText": "Article 24 - Advanced Testing: Financial entities identified as significant shall carry out threat-led penetration testing (TLPT) at least every 3 years. Testing must cover critical functions and systems. Results must be reported to competent authority and remediation must be completed before the next testing cycle."
  }' | python3 -m json.tool
echo ""

# ─── Step 5: Verify — policies increased ───────────────────────────
echo "▸ Step 5: Verify — check policy count AFTER adding DORA..."
echo ""
curl -s http://localhost:9090/api/policies/summary | python3 -m json.tool
echo ""
echo "  ↑ Notice: Policy count INCREASED. DORA is now being enforced."
echo "    Next time a file triggers the watcher, DORA violations will be detected."
echo ""

# ─── Step 6: Show all DORA policies ────────────────────────────────
echo "▸ Step 6: View all policies (look for the new DORA ones)..."
echo ""
curl -s http://localhost:9090/api/policies | python3 -c "
import json, sys
data = json.load(sys.stdin)
print(f'  Total policies loaded: {len(data)}')
print()
print('  NEW DORA POLICIES:')
for p in data:
    if 'DORA' in p.get('controls', '') or 'DORA' in p.get('name', '') or 'DORA' in p.get('id', ''):
        print(f\"    [{p['id']}] {p['name']}\")
        print(f\"      Domain: {p['domain']} | Severity: {p['severity']} | Blocking: {p['blocking']}\")
        print(f\"      Action: {p['action'][:70]}\")
        print()
"
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║  ✓ DONE — DORA is now enforced with ZERO code changes       ║"
echo "║                                                              ║"
echo "║  KEY POINT FOR JUDGES:                                       ║"
echo "║  • No redeployment needed                                    ║"
echo "║  • No developer involvement                                  ║"
echo "║  • AI agent parsed regulation text into machine rules        ║"
echo "║  • System immediately enforces new controls                  ║"
echo "║  • Works for ANY framework: HITRUST, NIST, ISO, FedRAMP...  ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

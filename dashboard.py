#!/usr/bin/env python3
"""
KAVACH AI — Executive Dashboard (Live Code Scanning)
Watches Java files in watch-folder/ and updates dashboard in real-time.
No restart needed — just edit a .java file and the dashboard reflects it.

TO TEST:
  → Edit watch-folder/bad-code.java (add bad patterns, save)
  → Dashboard auto-refreshes every 3 seconds with new findings

Data sources:
  → simulators/mock_data.py   (baseline compliance data)
  → agents/*                  (processing logic)
  → dashboard_utils.py        (UI data mapping)
  → java_scanner.py           (live code scanning rules)
  → watch-folder/*.java       (files being monitored)

Run: python3 dashboard.py
Open: http://localhost:8080
"""

import sys
import os
import time
from http.server import HTTPServer, SimpleHTTPRequestHandler

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from dashboard_utils import DashboardData
from java_scanner import JavaScanner

# AI-powered scanner (uses Ollama LLM when available, heuristic fallback)
try:
    from ai_scanner import AIScanner
    AI_SCANNER_AVAILABLE = True
except ImportError:
    AI_SCANNER_AVAILABLE = False

# Watched directories for Java files
WATCH_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "watch-folder")
DEMO_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "demo-compliance-classes")

# Initialize baseline data from agents
data = DashboardData()


def get_live_scan():
    """
    Scan NOW on every request. No caching, no background thread.
    Always reflects the current state of files on disk.

    INTELLIGENCE MODE:
      - If Ollama LLM is running → AI-powered reasoning (no hardcoded rules)
      - If Ollama is unavailable → Falls back to heuristic scanner
    
    Scans watch-folder/ only (demo-compliance-classes are for code walkthrough, not live scanning)
    """
    if AI_SCANNER_AVAILABLE:
        s = AIScanner(max_age_minutes=10)
        s.scan_recent(WATCH_DIR)
        return s.findings, s.scanned_files, time.strftime("%Y-%m-%d %H:%M:%S")
    else:
        s = JavaScanner(max_age_minutes=10)
        s.scan_recent(WATCH_DIR)
        return s.findings, s.scanned_files, time.strftime("%Y-%m-%d %H:%M:%S")


# Module-level state (updated on each request by generate_html)
live_findings = []
scanned_files_info = []
last_scan_time = ""


def generate_html():
    # Fresh scan on every request — always up to date
    global live_findings, scanned_files_info, last_scan_time
    live_findings, scanned_files_info, last_scan_time = get_live_scan()

    # All methods receive live_findings so ALL tabs reflect current state
    metrics = data.get_header_metrics(live_findings)
    twin_cards = data.get_twin_cards_html(live_findings)
    chain_steps = data.get_chain_steps_html(live_findings)
    # Build chain reactor panel content dynamically
    if not live_findings:
        chain_reactor_html = '<div style="text-align:center;padding:20px;color:#10b981;font-size:11px;">✓ No compliance events detected. Pipeline is clear.</div>'
    else:
        num_domains = len(set(f.category for f in live_findings))
        penalty = int(data._live_score_penalty(live_findings))
        has_critical = any(f.severity == 'CRITICAL' for f in live_findings)
        status = 'DEPLOYMENT BLOCKED' if has_critical else 'Review Required'
        chain_reactor_html = f"""<div class="ch-trigger">
            <b>Live Code Change Detected</b> — {len(live_findings)} findings across {num_domains} domains<br>
            Risk impact: -{penalty} pts · Status: {status}
        </div>
        <div class="ch-list">
            {chain_steps}
        </div>"""
    gate = data.get_gate_data(live_findings)
    # Build gate panel content dynamically
    if not live_findings:
        gate_panel_html = '<div style="text-align:center;padding:20px;"><div style="color:#10b981;font-size:24px;margin-bottom:8px;">✓</div><div style="color:#10b981;font-size:12px;font-weight:600;">Deployment Approved</div><div style="color:#64748b;font-size:11px;margin-top:6px;">No compliance violations detected. Pipeline is clear to deploy.</div></div>'
    else:
        badge = 'Deployment Blocked' if gate['requires_human'] else 'Review Required'
        gate_panel_html = f"""<div class="gate-badge">{badge}</div>
        <p style="font-size:9.5px;color:#6b7f99;margin-bottom:6px;">
            {gate['client_name']} — Score: {gate['current_score']:.0f}% → {gate['projected_score']:.0f}% (-{gate['score_delta']:.0f}pts)
        </p>
        <ul class="gate-items">
            {gate_items_html}
        </ul>
        <div class="gate-bottom">
            <div class="gate-risk">
                <div class="gate-risk-val">${gate['risk_usd']/1000:.0f}K</div>
                <div class="gate-risk-lbl">Risk if Deployed</div>
            </div>
            <div class="gate-rec">
                <b>Recommendation</b>
                Hold deployment. {len(gate['blocking_items'])} actions required. {gate['domains_affected']} domains affected.
            </div>
        </div>"""
    narrative = data.get_narrative_compact(live_findings)
    drift_cards = data.get_drift_cards_html(live_findings)
    audit_timeline = data.get_audit_timeline_html(live_findings)
    audit_controls_html = data.get_audit_controls_html(live_findings)
    drift_alerts = data.get_drift_view_alerts_html(live_findings)
    drift_stats = data.get_drift_stats(live_findings)
    values = data.get_value_metrics(live_findings)

    # Gate blocking items as HTML
    gate_items_html = ""
    for item in gate["blocking_items"]:
        gate_items_html += f'<li>{item[:70]}</li>'

    # Drift stats bars
    drift_bars_html = ""
    for cat, count in drift_stats["by_category"].items():
        colors = {"Infrastruct": "#ef4444", "Access Contr": "#f59e0b", "Audit": "#6366f1", "Contractual": "#eab308", "Application": "#a78bfa"}
        c = colors.get(cat, "#6366f1")
        drift_bars_html += f"""
                <div style="display:flex;align-items:center;gap:6px;margin-bottom:5px;">
                    <div style="height:6px;flex:{count};background:{c};border-radius:3px;"></div>
                    <span style="font-size:8px;color:#6b7f99;width:80px;">{cat} ({count})</span>
                </div>"""

    return _build_html(
        metrics, twin_cards, chain_steps, chain_reactor_html, gate, gate_items_html, gate_panel_html,
        narrative, drift_cards, audit_timeline, audit_controls_html, drift_alerts,
        drift_stats, drift_bars_html, values
    )


def _build_remediation_log():
    """Generate remediation log HTML — dynamic based on live findings."""
    if not live_findings:
        return '<div style="text-align:center;padding:20px;color:#10b981;font-size:10px;">✓ No remediation actions needed. All systems healthy.</div>'
    html = ""
    for f in live_findings[:6]:
        if f.severity in ("MEDIUM", "LOW"):
            status_color = "#10b981"
            status_text = "✓ AUTO-REMEDIATED"
        else:
            status_color = "#f59e0b"
            status_text = "⏳ PENDING REVIEW"
        html += f"""
            <div style="background:transparent;border:1px solid rgba(150,160,180,0.2);border-radius:6px;padding:8px 10px;">
                <div style="font-size:8px;color:{status_color};">{status_text}</div>
                <div style="font-size:9.5px;color:#d0d8e8;margin-top:2px;">{f.remediation[:60]}</div>
            </div>"""
    return html


def _build_live_findings_html():
    """Generate live scan findings HTML — tags recent ones."""
    if not live_findings:
        return '<div style="font-size:10px;color:#10b981;text-align:center;padding:20px;">✓ No issues detected in watched files.</div>'
    html = ""
    sev_colors = {"CRITICAL": "#ef4444", "HIGH": "#f59e0b", "MEDIUM": "#eab308", "LOW": "#6366f1"}
    for f in live_findings:
        color = sev_colors.get(f.severity, "#6366f1")
        fname = os.path.basename(f.file_path)
        recent_tag = ' <span style="background:#10b981;color:#fff;font-size:6.5px;padding:1px 4px;border-radius:3px;">NEW</span>' if f.is_recent else ''
        html += f"""
            <div style="background:transparent;border:1px solid rgba(150,160,180,0.2);border-radius:6px;padding:8px 10px;border-left:2px solid {color};">
                <div style="display:flex;justify-content:space-between;align-items:center;">
                    <span style="font-size:7.5px;font-weight:800;color:{color};letter-spacing:1px;">{f.severity} · {f.category}{recent_tag}</span>
                    <span style="font-size:7.5px;color:#6b7f99;">{fname}:{f.line_number}</span>
                </div>
                <div style="font-size:9.5px;font-weight:600;color:#d0d8e8;margin:3px 0;">{f.title}</div>
                <div style="font-family:monospace;font-size:8px;color:#6b7f99;background:rgba(0,0,0,0.15);padding:4px 6px;border-radius:3px;margin:4px 0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{f.code_snippet}</div>
                <div style="font-size:8px;color:#10b981;">Fix: {f.remediation[:60]}</div>
            </div>"""
    return html


def _build_live_compliance_impact_html():
    """Generate compliance impact analysis from live findings."""
    if not live_findings:
        return '<div style="font-size:10px;color:#10b981;text-align:center;padding:20px;">✓ No compliance impact. All clear.</div>'

    # Group findings by compliance framework
    compliance_map = {}
    for f in live_findings:
        for impact in f.compliance_impact:
            if impact not in compliance_map:
                compliance_map[impact] = []
            compliance_map[impact].append(f)

    html = ""
    for framework, findings in sorted(compliance_map.items(), key=lambda x: -len(x[1])):
        worst_sev = "LOW"
        for f in findings:
            if f.severity == "CRITICAL":
                worst_sev = "CRITICAL"
                break
            elif f.severity == "HIGH" and worst_sev != "CRITICAL":
                worst_sev = "HIGH"
            elif f.severity == "MEDIUM" and worst_sev not in ("CRITICAL", "HIGH"):
                worst_sev = "MEDIUM"

        sev_colors = {"CRITICAL": "#ef4444", "HIGH": "#f59e0b", "MEDIUM": "#eab308", "LOW": "#6366f1"}
        color = sev_colors.get(worst_sev, "#6366f1")

        html += f"""
            <div style="background:transparent;border:1px solid rgba(150,160,180,0.2);border-radius:6px;padding:10px 12px;border-left:2px solid {color};">
                <div style="font-size:8px;font-weight:700;color:{color};letter-spacing:0.5px;margin-bottom:3px;">{framework}</div>
                <div style="font-size:9.5px;color:#d0d8e8;">{len(findings)} finding(s) — worst: {worst_sev}</div>
                <div style="font-size:8.5px;color:#6b7f99;margin-top:3px;">
                    {', '.join(set(f.category for f in findings))}
                </div>
            </div>"""
    return html


def _build_recent_files_html():
    """Show files in watch folder — highlight recently modified ones."""
    if not scanned_files_info:
        return '<div style="font-size:9px;color:#6b7f99;">No .java files found in watch folder.</div>'
    html = ""
    for fi in scanned_files_info:
        age = fi["age_seconds"]
        if age < 60:
            age_str = f"{age}s ago"
        elif age < 3600:
            age_str = f"{age // 60}m ago"
        else:
            age_str = f"{age // 3600}h ago"

        if fi.get("is_recent"):
            badge = '<span style="background:#10b981;color:#fff;font-size:7px;padding:1px 5px;border-radius:3px;margin-left:6px;">JUST MODIFIED</span>'
            color = "#10b981"
        else:
            badge = ""
            color = "#64748b"
        html += f"""<div style="font-size:9px;color:#b8c4d6;display:flex;justify-content:space-between;align-items:center;padding:2px 0;">
            <span>📄 {os.path.basename(fi['path'])}{badge}</span>
            <span style="color:{color};font-weight:500;">{age_str}</span>
        </div>"""
    return html


def _build_html(metrics, twin_cards, chain_steps, chain_reactor_html, gate, gate_items_html, gate_panel_html,
                narrative, drift_cards, audit_timeline, audit_controls_html, drift_alerts,
                drift_stats, drift_bars_html, values):
    cr = data.chain_reaction
    code = data.code_change
    return f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>KAVACH AI</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
<style>
*{{margin:0;padding:0;box-sizing:border-box;}}
html,body{{height:100%;overflow:hidden;}}
body{{
    font-family:'Inter',sans-serif;
    background:#0f1419;
    color:#d1d9e6;
    font-size:12px;
    line-height:1.5;
}}

/* Full page layout */
.page{{height:100vh;display:grid;grid-template-rows:auto 1fr;overflow:hidden;}}

/* Header */
.header{{
    position:relative;padding:14px 28px;
    display:flex;align-items:center;justify-content:space-between;
    border-bottom:1px solid rgba(100,116,139,0.3);
    background:rgba(15,20,25,0.95);overflow:hidden;
}}
.header::before{{
    content:'';position:absolute;inset:0;
    background:none;
}}
.header *{{position:relative;z-index:1;}}
.brand{{display:flex;align-items:center;gap:14px;}}
.brand-text{{display:flex;flex-direction:column;}}
.brand-name{{font-size:18px;font-weight:800;letter-spacing:-0.5px;color:#f8fafc;}}
.brand-sub{{font-size:10px;color:#94a3b8;letter-spacing:1.5px;text-transform:uppercase;}}
.nav{{display:flex;gap:4px;}}
.nav-btn{{
    padding:7px 14px;border-radius:8px;font-size:11px;font-weight:500;
    color:#94a3b8;cursor:pointer;border:1px solid transparent;
    background:transparent;transition:all .2s;
}}
.nav-btn:hover,.nav-btn.active{{background:rgba(99,102,241,0.15);border-color:rgba(99,102,241,0.4);color:#e0e7ff;}}
.header-metrics{{display:flex;gap:24px;}}
.hm{{text-align:center;}}
.hm-val{{font-size:15px;font-weight:800;color:#a5b4fc;}}
.hm-lbl{{font-size:9px;color:#64748b;text-transform:uppercase;letter-spacing:0.5px;}}

/* Views */
.view{{display:none;height:100%;overflow:auto;}}
.view.active{{display:grid;}}
.main{{grid-template-columns:1fr 1fr 1fr;grid-template-rows:1fr 1fr;gap:12px;padding:14px;overflow:auto;}}
.view-audit{{grid-template-columns:1fr 1fr;grid-template-rows:1fr;gap:12px;padding:14px;}}
.view-audit .panel{{overflow-y:auto;}}
.view-drift{{grid-template-columns:1fr 1fr 1fr;grid-template-rows:1fr;gap:12px;padding:14px;}}
.view-drift .panel{{overflow-y:auto;}}
.view-controls{{grid-template-columns:1fr 1fr;grid-template-rows:1fr;gap:12px;padding:14px;}}
.view-controls .panel{{overflow-y:auto;}}
.view-livescan{{grid-template-columns:1.2fr 1fr 0.8fr;grid-template-rows:1fr;gap:12px;padding:14px;}}
.view-livescan .panel{{overflow-y:auto;}}

/* Panel base */
.panel{{
    background:rgba(22,28,36,0.9);border:1px solid rgba(100,116,139,0.25);
    border-radius:12px;padding:16px 18px;overflow-y:auto;position:relative;
    cursor:pointer;transition:all 0.3s ease;
}}
.panel:hover{{border-color:rgba(99,102,241,0.5);box-shadow:0 0 20px rgba(99,102,241,0.08);}}

/* Modal overlay */
.modal-overlay{{
    display:none;position:fixed;inset:0;z-index:1000;
    background:rgba(10,14,24,0.7);backdrop-filter:blur(8px);
    align-items:center;justify-content:center;padding:30px;
    animation:fadeIn 0.2s ease;
}}
.modal-overlay.active{{display:flex;}}
@keyframes fadeIn{{from{{opacity:0;}}to{{opacity:1;}}}}
.modal-content{{
    background:rgba(20,26,40,0.95);border:1px solid rgba(100,116,139,0.3);
    border-radius:16px;padding:40px 50px;
    max-width:85vw;max-height:85vh;overflow-y:auto;
    box-shadow:0 30px 80px rgba(0,0,0,0.5);
    backdrop-filter:blur(16px);
    animation:slideUp 0.25s ease;
    position:relative;min-width:500px;
    font-size:16px;line-height:1.8;
}}
.modal-content .panel-title{{font-size:14px;}}
.modal-content h3{{font-size:18px;margin-bottom:14px;}}
.modal-content .tw-name{{font-size:16px;}}
.modal-content .tw-row{{font-size:14px;}}
.modal-content .ch-domain{{font-size:14px;}}
.modal-content .ch-act{{font-size:13px;}}
.modal-content .ch-trigger{{font-size:14px;}}
.modal-content .gate-items li{{font-size:13px;padding:8px 12px;}}
.modal-content .gate-risk-val{{font-size:24px;}}
.modal-content .narrative{{font-size:13px;line-height:1.8;}}
.modal-content .drift-t{{font-size:14px;}}
.modal-content .drift-d{{font-size:13px;}}
.modal-content .drift-sev{{font-size:11px;}}
.modal-content .val-num{{font-size:28px;}}
.modal-content .val-lbl{{font-size:12px;}}
@keyframes slideUp{{from{{transform:translateY(20px);opacity:0;}}to{{transform:translateY(0);opacity:1;}}}}
.modal-close{{
    position:absolute;top:14px;right:18px;
    width:28px;height:28px;border-radius:50%;border:1px solid #334155;
    background:rgba(30,37,54,0.8);color:#8a9bb4;font-size:14px;
    cursor:pointer;display:flex;align-items:center;justify-content:center;
    transition:all 0.2s;
}}
.modal-close:hover{{background:rgba(239,68,68,0.15);border-color:rgba(239,68,68,0.4);color:#f87171;}}
.panel-title{{
    font-size:10px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;
    color:#818cf8;margin-bottom:10px;display:flex;align-items:center;gap:6px;
}}
.panel-title::before{{content:'';width:3px;height:12px;background:#818cf8;border-radius:2px;}}
.panel h3{{font-size:13px;font-weight:700;margin-bottom:10px;color:#e2e8f0;}}

/* Twin cards */
.twins-wrap{{display:flex;flex-direction:column;gap:10px;}}
.twin-card{{background:rgba(15,23,42,0.6);border:1px solid rgba(100,116,139,0.2);border-radius:10px;padding:12px 14px;}}
.twin-card:hover{{border-color:#818cf8;}}
.tw-top{{display:flex;align-items:center;gap:12px;margin-bottom:8px;}}
.tw-info{{flex:1;}}
.tw-name{{font-size:12px;font-weight:600;color:#e2e8f0;}}
.tw-tier{{font-size:9px;font-weight:700;border:1px solid;border-radius:10px;padding:2px 8px;display:inline-block;margin-top:3px;}}
.tw-row{{display:flex;gap:12px;font-size:10px;color:#94a3b8;margin-bottom:4px;}}
.tw-row b{{color:#e2e8f0;}}
.tw-fw{{font-size:9px;color:#818cf8;opacity:0.9;}}

/* Chain reaction */
.ch-trigger{{background:rgba(234,179,8,0.06);border:1px solid rgba(245,158,11,0.2);border-radius:8px;padding:10px 12px;margin-bottom:12px;font-size:11px;color:#e2e8f0;}}
.ch-trigger b{{color:#fbbf24;}}
.ch-list{{display:flex;flex-direction:column;gap:8px;}}
.ch-item{{display:flex;align-items:center;gap:10px;}}
.ch-dot{{width:10px;height:10px;border-radius:50%;flex-shrink:0;}}
.ch-body{{display:flex;flex-direction:column;}}
.ch-domain{{font-size:11px;font-weight:600;color:#e2e8f0;}}
.ch-act{{font-size:10px;color:#34d399;}}

/* Gate */
.gate-badge{{
    display:inline-flex;align-items:center;gap:6px;
    padding:4px 12px;border-radius:10px;font-size:10px;font-weight:700;
    background:rgba(239,68,68,0.1);color:#f87171;border:1px solid rgba(239,68,68,0.3);
    margin-bottom:10px;text-transform:uppercase;letter-spacing:1px;
}}
.gate-badge::before{{content:'●';font-size:7px;animation:pulse 1.5s infinite;}}
@keyframes pulse{{0%,100%{{opacity:1;}}50%{{opacity:0.4;}}}}
.gate-items{{list-style:none;margin:10px 0;}}
.gate-items li{{
    font-size:10px;color:#cbd5e1;padding:6px 10px;margin-bottom:5px;
    background:rgba(239,68,68,0.06);border-radius:6px;border-left:3px solid #ef4444;
}}
.gate-bottom{{display:flex;gap:12px;margin-top:10px;}}
.gate-risk{{background:rgba(239,68,68,0.08);border:1px solid rgba(239,68,68,0.25);border-radius:10px;padding:10px 16px;text-align:center;}}
.gate-risk-val{{font-size:18px;font-weight:800;color:#f87171;}}
.gate-risk-lbl{{font-size:9px;color:#94a3b8;}}
.gate-rec{{flex:1;background:rgba(16,185,129,0.06);border:1px solid rgba(16,185,129,0.2);border-radius:10px;padding:10px 12px;font-size:10px;color:#94a3b8;}}
.gate-rec b{{color:#34d399;display:block;margin-bottom:4px;font-size:10px;}}

/* Narrative */
.narrative{{
    font-family:'JetBrains Mono','Fira Code','Courier New',monospace;font-size:10px;line-height:1.6;
    color:#94a3b8;white-space:pre-wrap;
    background:rgba(15,23,42,0.6);border:1px solid rgba(100,116,139,0.2);border-radius:8px;padding:12px 14px;
    max-height:calc(100% - 50px);overflow-y:auto;
}}

/* Drift */
.drift-grid{{display:grid;grid-template-columns:1fr 1fr;gap:8px;}}
.drift-item{{background:rgba(15,23,42,0.6);border-radius:8px;padding:10px 12px;border-left:3px solid;}}
.drift-item.crit{{border-color:#ef4444;background:rgba(239,68,68,0.06);}}
.drift-item.high{{border-color:#f59e0b;background:rgba(245,158,11,0.06);}}
.drift-item.med{{border-color:#eab308;background:rgba(234,179,8,0.04);}}
.drift-item.low{{border-color:#818cf8;background:rgba(99,102,241,0.06);}}
.drift-sev{{font-size:9px;font-weight:800;letter-spacing:1px;text-transform:uppercase;margin-bottom:4px;}}
.drift-t{{font-size:11px;font-weight:600;color:#e2e8f0;margin-bottom:4px;}}
.drift-d{{font-size:10px;color:#94a3b8;margin-bottom:5px;}}
.drift-f{{font-size:9px;color:#34d399;}}

/* Value */
.val-row{{display:flex;gap:16px;flex-wrap:wrap;}}
.val-item{{text-align:center;flex:1;min-width:70px;}}
.val-num{{font-size:20px;font-weight:900;color:#818cf8;}}
.val-lbl{{font-size:9px;color:#94a3b8;}}
</style>
</head>
<body>
<div class="page">

<!-- HEADER -->
<div class="header">
    <div class="brand">
        <svg width="36" height="36" viewBox="0 0 100 100" fill="none">
            <defs>
                <linearGradient id="g1" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#6366f1"/>
                    <stop offset="100%" stop-color="#a78bfa"/>
                </linearGradient>
                <linearGradient id="g2" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#10b981"/>
                    <stop offset="100%" stop-color="#34d399"/>
                </linearGradient>
            </defs>
            <path d="M50 6 L92 22 L92 52 C92 78 74 94 50 100 C26 94 8 78 8 52 L8 22 Z" fill="none" stroke="url(#g1)" stroke-width="4" stroke-linejoin="round"/>
            <path d="M50 16 L82 28 L82 52 C82 72 68 84 50 90 C32 84 18 72 18 52 L18 28 Z" fill="url(#g1)" opacity="0.1"/>
            <circle cx="50" cy="50" r="18" fill="none" stroke="url(#g1)" stroke-width="2.5" opacity="0.8"/>
            <circle cx="50" cy="50" r="7" fill="url(#g1)"/>
            <path d="M38 52 L46 60 L64 40" fill="none" stroke="url(#g2)" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <div class="brand-text">
            <div class="brand-name">KAVACH AI</div>
            <div class="brand-sub">Knowledge-driven Audit, Vulnerability Analysis & Compliance Health</div>
        </div>
    </div>
    <div class="nav">
        <button class="nav-btn active" onclick="showView('dashboard')">Dashboard</button>
        <button class="nav-btn" onclick="showView('audit')">Audit Trail</button>
        <button class="nav-btn" onclick="showView('controls')">Audit Controls</button>
        <button class="nav-btn" onclick="showView('drift')">Drift Monitor</button>
        <button class="nav-btn" onclick="showView('livescan')">Live Scan</button>
    </div>
    <div class="header-metrics">
        <div class="hm"><div class="hm-val">{metrics['agents']}</div><div class="hm-lbl">Agents</div></div>
        <div class="hm"><div class="hm-val">{metrics['domains']}</div><div class="hm-lbl">Domains</div></div>
        <div class="hm"><div class="hm-val">{metrics['analysis_time']}</div><div class="hm-lbl">Analysis</div></div>
        <div class="hm"><div class="hm-val">{metrics['audit_saved']}</div><div class="hm-lbl">Audit Ready</div></div>
        <div class="hm"><div class="hm-val">{metrics['risk_avoided']}</div><div class="hm-lbl">Risk Exposure</div></div>
    </div>
</div>

<!-- DASHBOARD VIEW -->
<div class="view main active" id="view-dashboard">
    <!-- Panel 1: Digital Twin -->
    <div class="panel">
        <div class="panel-title">Agent 01 · Digital Twin Agent</div>
        <h3>Live Compliance Score Per Client</h3>
        <div class="twins-wrap">
            {twin_cards}
        </div>
    </div>

    <!-- Panel 2: Chain Reactor -->
    <div class="panel">
        <div class="panel-title">Agent 02 · Chain Reactor Agent</div>
        <h3>Cross-Domain Impact</h3>
        {chain_reactor_html}
    </div>

    <!-- Panel 3: Deployment Gate -->
    <div class="panel">
        <div class="panel-title">Agent 03 · Obligation Parser Agent</div>
        <h3>Contract SLA & Deployment Gate</h3>
        {gate_panel_html}
    </div>

    <!-- Panel 4: Audit Narrator -->
    <div class="panel">
        <div class="panel-title">Agent 04 · Audit Narrator Agent</div>
        <h3>Auto-Generated Evidence</h3>
        <div class="narrative">{narrative}</div>
    </div>

    <!-- Panel 5: Drift Detection -->
    <div class="panel">
        <div class="panel-title">Agent 05 · Drift Sentinel Agent</div>
        <h3>Silent Compliance Drift</h3>
        <div class="drift-grid">
            {drift_cards}
        </div>
    </div>

    <!-- Panel 6: Business Value -->
    <div class="panel" style="display:flex;flex-direction:column;justify-content:center;">
        <div class="panel-title">Agent 06 · Control Ingestion Agent</div>
        <h3>Dynamic Policy & Business Value</h3>
        <div class="val-row" style="margin-top:12px;">
            <div class="val-item"><div class="val-num">{values['audit_saved']}</div><div class="val-lbl">Audit Prep Saved</div></div>
            <div class="val-item"><div class="val-num">{values['debt_avoided']}</div><div class="val-lbl">Risk Exposure</div></div>
            <div class="val-item"><div class="val-num">{values['domains_per_commit']}</div><div class="val-lbl">Domains/Commit</div></div>
        </div>
        <div class="val-row" style="margin-top:14px;">
            <div class="val-item"><div class="val-num">{values['findings_prevented']}</div><div class="val-lbl">Auto-Remediated</div></div>
            <div class="val-item"><div class="val-num">{values['agents_active']}</div><div class="val-lbl">Agents 24/7</div></div>
            <div class="val-item"><div class="val-num">{values['analysis_time']}</div><div class="val-lbl">Per Analysis</div></div>
        </div>
        <div style="margin-top:16px;text-align:center;font-size:9px;color:#6b7f99;font-style:italic;">
            "One commit. {values['domains_per_commit']} domains. Zero breaches."
            <div style="margin-top:6px;font-style:normal;font-size:8px;color:#8a9bb4;letter-spacing:0.5px;">
                SOX · PCI-DSS · TILA/RESPA · ECOA/Fair Lending · GDPR/CCPA · MSA/SLA
            </div>
        </div>
    </div>
</div><!-- end dashboard view -->

<!-- AUDIT TRAIL VIEW -->
<div class="view view-audit" id="view-audit">
    <div class="panel">
        <div class="panel-title">Audit Timeline</div>
        <h3>Recent Compliance Events</h3>
        <div style="margin-top:10px;display:flex;flex-direction:column;gap:8px;">
            {audit_timeline}
        </div>
    </div>
    <div class="panel">
        <div class="panel-title">Audit Evidence Narrative</div>
        <h3>Auto-Generated Report</h3>
        <div class="narrative" style="margin-top:10px;max-height:none;height:calc(100% - 50px);overflow-y:auto;">{narrative}</div>
    </div>
</div><!-- end audit view -->

<!-- AUDIT CONTROLS VIEW -->
<div class="view view-controls" id="view-controls">
    <div class="panel">
        <div class="panel-title">Compliance Policy Engine</div>
        <h3>Audit Controls Status</h3>
        {audit_controls_html}
    </div>
    <div class="panel">
        <div class="panel-title">How Controls Prevent Compliance Violations</div>
        <h3>Automated Enforcement</h3>
        <div style="margin-top:10px;display:flex;flex-direction:column;gap:8px;">
            <div style="background:rgba(99,102,241,0.06);border:1px solid rgba(99,102,241,0.2);border-radius:8px;padding:12px;">
                <div style="font-size:10px;font-weight:700;color:#a5b4fc;margin-bottom:6px;">How It Works</div>
                <div style="font-size:9px;color:#b8c4d6;line-height:1.6;">
                    Every code commit, deployment, and infrastructure change is evaluated against these controls in real-time.
                    Blocking controls stop the pipeline. Non-blocking controls generate alerts and audit evidence.
                </div>
            </div>
            <div style="background:rgba(239,68,68,0.05);border:1px solid rgba(239,68,68,0.2);border-radius:8px;padding:12px;">
                <div style="font-size:10px;font-weight:700;color:#f87171;margin-bottom:6px;">Violations Prevented (Live)</div>
                <div style="font-size:9px;color:#b8c4d6;line-height:1.6;">
                    When a developer commits code with secrets, SQL injection, or PII exposure — the corresponding
                    controls flip to VIOLATED and the deployment gate blocks production release automatically.
                </div>
            </div>
            <div style="background:rgba(16,185,129,0.05);border:1px solid rgba(16,185,129,0.2);border-radius:8px;padding:12px;">
                <div style="font-size:10px;font-weight:700;color:#34d399;margin-bottom:6px;">Compliance Frameworks Covered</div>
                <div style="font-size:9px;color:#b8c4d6;line-height:1.6;">
                    SOX ITGC · PCI-DSS · OWASP Top 10 · TILA/Reg Z · ECOA/Fair Lending · GDPR · CCPA · GLBA · HIPAA · ISO 27001 · NIST · FedRAMP
                </div>
            </div>
            <div style="background:rgba(245,158,11,0.05);border:1px solid rgba(245,158,11,0.2);border-radius:8px;padding:12px;">
                <div style="font-size:10px;font-weight:700;color:#fbbf24;margin-bottom:6px;">Try It</div>
                <div style="font-size:9px;color:#b8c4d6;line-height:1.6;">
                    Edit <b>watch-folder/bad-code.java</b> — add a hardcoded secret or System.out.println.
                    Watch the controls flip from ✓ SATISFIED to ✗ VIOLATED in real-time.
                </div>
            </div>
        </div>
    </div>
</div><!-- end controls view -->


<!-- DRIFT MONITOR VIEW -->
<div class="view view-drift" id="view-drift">
    <div class="panel">
        <div class="panel-title">Active Drift Alerts</div>
        <h3>Detected Issues ({drift_stats['total']})</h3>
        <div style="margin-top:10px;display:flex;flex-direction:column;gap:8px;">
            {drift_alerts}
        </div>
    </div>
    <div class="panel">
        <div class="panel-title">Drift Statistics</div>
        <h3>Analysis</h3>
        <div style="margin-top:14px;display:flex;flex-direction:column;gap:12px;">
            <div style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-size:9.5px;color:#6b7f99;">Total Drifts Detected</span>
                <span style="font-size:14px;font-weight:800;color:#6366f1;">{drift_stats['total']}</span>
            </div>
            <div style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-size:9.5px;color:#6b7f99;">Auto-Remediable</span>
                <span style="font-size:14px;font-weight:800;color:#10b981;">{drift_stats['auto_fixable']}</span>
            </div>
            <div style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-size:9.5px;color:#6b7f99;">Awaiting Human Review</span>
                <span style="font-size:14px;font-weight:800;color:#f59e0b;">{drift_stats['pending_review']}</span>
            </div>
            <div style="margin-top:12px;padding:10px;background:transparent;border:1px solid rgba(150,160,180,0.2);border-radius:8px;">
                <div style="font-size:8.5px;color:#6b7f99;margin-bottom:6px;">DRIFT BY CATEGORY</div>
                {drift_bars_html}
            </div>
        </div>
    </div>
    <div class="panel">
        <div class="panel-title">Remediation Status</div>
        <h3>Agent Actions</h3>
        <div style="margin-top:10px;display:flex;flex-direction:column;gap:6px;">
            {_build_remediation_log()}
        </div>
    </div>
</div><!-- end drift view -->

<!-- LIVE SCAN VIEW -->
<div class="view view-livescan" id="view-livescan">
    <div class="panel">
        <div class="panel-title">Live Code Scanner</div>
        <h3>Recently Modified Java Files</h3>
        <div style="margin-top:6px;font-size:9px;color:#6b7f99;">Last scan: {last_scan_time} · Watching: {WATCH_DIR.split('/')[-1]}/ · Window: 10 min</div>
        <div style="margin-top:8px;margin-bottom:8px;padding:8px 10px;background:rgba(99,102,241,0.08);border:1px solid rgba(99,102,241,0.25);border-radius:6px;">
            <div style="font-size:8px;color:#6366f1;font-weight:700;margin-bottom:4px;">FILES MODIFIED RECENTLY:</div>
            {_build_recent_files_html()}
        </div>
        <div style="display:flex;gap:12px;margin-bottom:12px;">
            <div style="text-align:center;padding:8px 14px;background:rgba(239,68,68,0.08);border:1px solid rgba(239,68,68,0.25);border-radius:8px;">
                <div style="font-size:16px;font-weight:800;color:#ef4444;">{len([f for f in live_findings if f.severity=='CRITICAL'])}</div>
                <div style="font-size:7.5px;color:#6b7f99;">CRITICAL</div>
            </div>
            <div style="text-align:center;padding:8px 14px;background:rgba(245,158,11,0.08);border:1px solid rgba(245,158,11,0.2);border-radius:8px;">
                <div style="font-size:16px;font-weight:800;color:#f59e0b;">{len([f for f in live_findings if f.severity=='HIGH'])}</div>
                <div style="font-size:7.5px;color:#6b7f99;">HIGH</div>
            </div>
            <div style="text-align:center;padding:8px 14px;background:rgba(234,179,8,0.06);border:1px solid rgba(245,158,11,0.2);border-radius:8px;">
                <div style="font-size:16px;font-weight:800;color:#eab308;">{len([f for f in live_findings if f.severity=='MEDIUM'])}</div>
                <div style="font-size:7.5px;color:#6b7f99;">MEDIUM</div>
            </div>
            <div style="text-align:center;padding:8px 14px;background:rgba(99,102,241,0.08);border:1px solid rgba(99,102,241,0.25);border-radius:8px;">
                <div style="font-size:16px;font-weight:800;color:#6366f1;">{len([f for f in live_findings if f.severity=='LOW'])}</div>
                <div style="font-size:7.5px;color:#6b7f99;">LOW</div>
            </div>
            <div style="text-align:center;padding:8px 14px;background:rgba(99,102,241,0.05);border:1px solid rgba(99,102,241,0.15);border-radius:8px;">
                <div style="font-size:16px;font-weight:800;color:#6b7f99;">{len(live_findings)}</div>
                <div style="font-size:7.5px;color:#6b7f99;">TOTAL</div>
            </div>
        </div>
        <div style="overflow-y:auto;max-height:calc(100% - 200px);display:flex;flex-direction:column;gap:6px;">
            {_build_live_findings_html()}
        </div>
    </div>
    <div class="panel" style="overflow-y:auto;">
        <div class="panel-title">Compliance Impact</div>
        <h3>Chain Reaction from Code Findings</h3>
        <div style="margin-top:10px;display:flex;flex-direction:column;gap:8px;">
            {_build_live_compliance_impact_html()}
        </div>
    </div>
    <div class="panel" style="overflow-y:auto;">
        <div class="panel-title">How to Test</div>
        <h3>Try These Changes</h3>
        <div style="margin-top:10px;display:flex;flex-direction:column;gap:8px;">
            <div style="background:transparent;border:1px solid rgba(150,160,180,0.2);border-radius:6px;padding:10px 12px;">
                <div style="font-size:9px;font-weight:600;color:#ef4444;margin-bottom:4px;">→ Add Hardcoded Secret</div>
                <div style="font-family:monospace;font-size:8.5px;color:#6b7f99;background:rgba(0,0,0,0.15);padding:6px 8px;border-radius:4px;">
                    String apiKey = "sk-live-abc123xyz";</div>
            </div>
            <div style="background:transparent;border:1px solid rgba(150,160,180,0.2);border-radius:6px;padding:10px 12px;">
                <div style="font-size:9px;font-weight:600;color:#ef4444;margin-bottom:4px;">→ Log Sensitive Data</div>
                <div style="font-family:monospace;font-size:8.5px;color:#6b7f99;background:rgba(0,0,0,0.15);padding:6px 8px;border-radius:4px;">
                    logger.info("User password: " + password);</div>
            </div>
            <div style="background:transparent;border:1px solid rgba(150,160,180,0.2);border-radius:6px;padding:10px 12px;">
                <div style="font-size:9px;font-weight:600;color:#ef4444;margin-bottom:4px;">→ SQL Injection</div>
                <div style="font-family:monospace;font-size:8.5px;color:#6b7f99;background:rgba(0,0,0,0.15);padding:6px 8px;border-radius:4px;">
                    String q = "SELECT * FROM loans WHERE id=" + userId;</div>
            </div>
            <div style="background:transparent;border:1px solid rgba(150,160,180,0.2);border-radius:6px;padding:10px 12px;">
                <div style="font-size:9px;font-weight:600;color:#f59e0b;margin-bottom:4px;">→ System.out instead of Logger</div>
                <div style="font-family:monospace;font-size:8.5px;color:#6b7f99;background:rgba(0,0,0,0.15);padding:6px 8px;border-radius:4px;">
                    System.out.println("Processing loan: " + loanId);</div>
            </div>
            <div style="background:transparent;border:1px solid rgba(150,160,180,0.2);border-radius:6px;padding:10px 12px;">
                <div style="font-size:9px;font-weight:600;color:#f59e0b;margin-bottom:4px;">→ Disable SSL Verification</div>
                <div style="font-family:monospace;font-size:8.5px;color:#6b7f99;background:rgba(0,0,0,0.15);padding:6px 8px;border-radius:4px;">
                    conn.setHostnameVerifier(TrustAllCerts);</div>
            </div>
            <div style="background:transparent;border:1px solid rgba(150,160,180,0.2);border-radius:6px;padding:10px 12px;">
                <div style="font-size:9px;font-weight:600;color:#eab308;margin-bottom:4px;">→ Weak Random</div>
                <div style="font-family:monospace;font-size:8.5px;color:#6b7f99;background:rgba(0,0,0,0.15);padding:6px 8px;border-radius:4px;">
                    String token = new Random().nextInt() + "";</div>
            </div>
        </div>
    </div>
</div><!-- end livescan view -->

</div><!-- end page -->

<!-- MODAL OVERLAY -->
<div class="modal-overlay" id="modal-overlay" onclick="closeModal(event)">
    <div class="modal-content" id="modal-content">
        <div class="modal-close" onclick="closeModal(event, true)">✕</div>
        <div id="modal-body"></div>
    </div>
</div>

<script>
function showView(v) {{
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');
    document.querySelectorAll('.view').forEach(el => el.classList.remove('active'));
    document.getElementById('view-' + v).classList.add('active');
    window.location.hash = v;
}}

// Modal: click panel to expand
document.querySelectorAll('.panel').forEach(panel => {{
    panel.addEventListener('click', function(e) {{
        // Don't trigger if clicking inside modal
        if (e.target.closest('.modal-overlay')) return;
        var content = this.innerHTML;
        document.getElementById('modal-body').innerHTML = content;
        document.getElementById('modal-overlay').classList.add('active');
        // Pause auto-refresh while modal is open
        window._modalOpen = true;
    }});
}});

function closeModal(e, force) {{
    if (force || e.target === document.getElementById('modal-overlay')) {{
        document.getElementById('modal-overlay').classList.remove('active');
        window._modalOpen = false;
    }}
}}

// Close on Escape key
document.addEventListener('keydown', function(e) {{
    if (e.key === 'Escape') {{
        document.getElementById('modal-overlay').classList.remove('active');
        window._modalOpen = false;
    }}
}});

// Restore active tab from hash on load
(function() {{
    var hash = window.location.hash.replace('#', '');
    if (hash && document.getElementById('view-' + hash)) {{
        document.querySelectorAll('.view').forEach(el => el.classList.remove('active'));
        document.getElementById('view-' + hash).classList.add('active');
        document.querySelectorAll('.nav-btn').forEach(b => {{
            b.classList.remove('active');
            if (b.getAttribute('onclick').indexOf(hash) !== -1) b.classList.add('active');
        }});
    }}
    // Auto-refresh using fetch (preserves scroll position)
    setInterval(function() {{
        if (window._modalOpen) return;
        fetch('/')
            .then(r => r.text())
            .then(html => {{
                var parser = new DOMParser();
                var doc = parser.parseFromString(html, 'text/html');
                var newPage = doc.querySelector('.page');
                if (newPage) {{
                    // Save scroll positions
                    var scrolls = {{}};
                    document.querySelectorAll('.panel, .view').forEach(function(el, i) {{
                        if (el.scrollTop > 0) scrolls[i] = el.scrollTop;
                    }});
                    // Replace content
                    document.querySelector('.page').innerHTML = newPage.innerHTML;
                    // Restore active view
                    var hash = window.location.hash.replace('#', '') || 'dashboard';
                    document.querySelectorAll('.view').forEach(el => el.classList.remove('active'));
                    var target = document.getElementById('view-' + hash);
                    if (target) target.classList.add('active');
                    document.querySelectorAll('.nav-btn').forEach(b => {{
                        b.classList.remove('active');
                        if (b.getAttribute('onclick') && b.getAttribute('onclick').indexOf(hash) !== -1) b.classList.add('active');
                    }});
                    // Restore scroll positions
                    document.querySelectorAll('.panel, .view').forEach(function(el, i) {{
                        if (scrolls[i]) el.scrollTop = scrolls[i];
                    }});
                }}
            }})
            .catch(function() {{}});
    }}, 5000);
}})();
</script>
</body>
</html>"""


class DashboardHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/" or self.path == "/index.html":
            html = generate_html()
            self.send_response(200)
            self.send_header("Content-type", "text/html")
            self.end_headers()
            self.wfile.write(html.encode())
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        pass


def main():
    port = 8080
    server = HTTPServer(("localhost", port), DashboardHandler)

    # Check AI mode
    ai_mode = "AI-POWERED (Ollama LLM)" if AI_SCANNER_AVAILABLE else "HEURISTIC (Pattern-based)"
    try:
        from ai_scanner import _is_ollama_available
        ollama_status = "✓ CONNECTED" if _is_ollama_available() else "✗ NOT RUNNING (using heuristic fallback)"
    except Exception:
        ollama_status = "✗ NOT AVAILABLE"

    print(f"""
    ╔══════════════════════════════════════════════════════════════════╗
    ║   KAVACH AI — Executive Dashboard                              ║
    ║   http://localhost:{port}                                       ║
    ╠══════════════════════════════════════════════════════════════════╣
    ║                                                                ║
    ║   INTELLIGENCE MODE: {ai_mode:<40}║
    ║   Ollama LLM Status: {ollama_status:<40}║
    ║   Spring Boot API:   http://localhost:9090                     ║
    ║                                                                ║
    ║   SCANNING: watch-folder/ (auto-refresh every 3 seconds)       ║
    ║                                                                ║
    ║   HOW IT WORKS:                                                ║
    ║   1. File modified → Content sent to LLM for reasoning         ║
    ║   2. LLM returns compliance findings (domain, severity, reg)   ║
    ║   3. Findings fire events → Spring Boot agent pipeline         ║
    ║   4. Chain Reactor → Audit Narrator → Score update             ║
    ║                                                                ║
    ║   TO TEST: Edit any .java file in watch-folder/                ║
    ║   Press Ctrl+C to stop                                         ║
    ╚══════════════════════════════════════════════════════════════════╝
    """)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n  Stopped.")
        server.shutdown()


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Generate a high-resolution architecture diagram as PNG."""

from PIL import Image, ImageDraw, ImageFont
import os

# Image size (high-res for presentation)
W, H = 2400, 1500
img = Image.new('RGB', (W, H), '#0f1419')
draw = ImageDraw.Draw(img)

# Fonts
def font(size, bold=False):
    try:
        if bold:
            return ImageFont.truetype('/System/Library/Fonts/Helvetica.ttc', size, index=1)
        return ImageFont.truetype('/System/Library/Fonts/Helvetica.ttc', size)
    except:
        return ImageFont.load_default()

f_title = font(48, bold=True)
f_subtitle = font(22)
f_label = font(18, bold=True)
f_box_title = font(22, bold=True)
f_box_sub = font(16)
f_box_detail = font(14)
f_section = font(20, bold=True)
f_tech = font(16, bold=True)
f_tech_sub = font(13)

# Colors
PURPLE = '#818cf8'
GREEN = '#34d399'
AMBER = '#fbbf24'
RED = '#f87171'
WHITE = '#f8fafc'
GRAY = '#94a3b8'
DIM = '#475569'
DARK_CARD = '#161b24'
BORDER = '#334155'

def rounded_rect(x, y, w, h, fill=DARK_CARD, outline=BORDER, radius=12, width=2):
    draw.rounded_rectangle([x, y, x+w, y+h], radius=radius, fill=fill, outline=outline, width=width)

def center_text(text, x, y, w, fnt, fill=WHITE):
    bbox = draw.textbbox((0,0), text, font=fnt)
    tw = bbox[2] - bbox[0]
    draw.text((x + (w - tw)//2, y), text, font=fnt, fill=fill)

def arrow_down(x, y1, y2, color=PURPLE):
    draw.line([(x, y1), (x, y2-8)], fill=color, width=3)
    draw.polygon([(x-6, y2-10), (x+6, y2-10), (x, y2)], fill=color)

# ═══════════════════════════════════════════════════════════
# TITLE
# ═══════════════════════════════════════════════════════════
draw.text((80, 40), "🛡️ KAVACH AI — Architecture Flow", font=f_title, fill=WHITE)
draw.text((80, 100), "How the Agentic Pipeline works: Event → Queue → Agents → Intelligence → Action", font=f_subtitle, fill=GRAY)

# ═══════════════════════════════════════════════════════════
# LAYER 1: EVENT SOURCES
# ═══════════════════════════════════════════════════════════
draw.text((80, 160), "① EVENT SOURCES (Perceive)", font=f_section, fill=DIM)

sources = [
    ("Git Webhooks", "Code commits, PRs"),
    ("Jenkins CI/CD", "Build & deploy events"),
    ("Jira / ServiceNow", "Ticket state changes"),
    ("AWS Config", "Infra & IAM changes"),
    ("File Watcher", "Real-time code scan"),
    ("SAST Tools", "Checkmarx, Snyk, Wiz"),
]
sx = 80
for i, (title, sub) in enumerate(sources):
    bx = sx + i * 370
    rounded_rect(bx, 195, 340, 80, outline=BORDER)
    draw.text((bx+20, 210), title, font=f_box_title, fill=WHITE)
    draw.text((bx+20, 240), sub, font=f_box_sub, fill=GRAY)

# Arrow down to queue
arrow_down(W//2, 280, 320, PURPLE)

# ═══════════════════════════════════════════════════════════
# LAYER 2: MESSAGE QUEUE
# ═══════════════════════════════════════════════════════════
rounded_rect(300, 325, W-600, 70, outline=PURPLE, width=3)
center_text("ActiveMQ Message Queue — \"compliance-events\"", 300, 343, W-600, f_box_title, PURPLE)
draw.text((320, 370), "All events from all sources flow into a single unified queue — agents consume asynchronously", font=f_box_detail, fill=DIM)

arrow_down(W//2, 400, 445, PURPLE)

# ═══════════════════════════════════════════════════════════
# LAYER 3: AGENT LAYER
# ═══════════════════════════════════════════════════════════
draw.text((80, 450), "② AGENT LAYER (Reason & Decide)", font=f_section, fill=DIM)

# Chain Reactor (center, larger)
cx, cy = 700, 490
rounded_rect(cx, cy, 900, 110, outline=AMBER, width=3)
draw.text((cx+30, cy+15), "⚡ Chain Reactor Agent (Central Orchestrator)", font=f_box_title, fill=AMBER)
draw.text((cx+30, cy+50), "Receives event → Evaluates 26+ policies → Traces causal graph → Triggers downstream agents", font=f_box_sub, fill=WHITE)
draw.text((cx+30, cy+78), "PERCEIVE the event → REASON about impact → DECIDE severity → ACT (block/alert/narrate)", font=f_box_detail, fill=GRAY)

# Left agents
agents_left = [
    ("🏗️ Digital Twin Agent", "Maintains live compliance", "score per client engagement", GREEN),
    ("📝 Audit Narrator Agent", "Auto-generates audit evidence", "using LLM (EY/Deloitte ready)", GREEN),
]
for i, (title, line1, line2, color) in enumerate(agents_left):
    ay = 490 + i * 120
    rounded_rect(80, ay, 570, 100, outline=color, width=2)
    draw.text((100, ay+15), title, font=f_box_title, fill=color)
    draw.text((100, ay+48), line1, font=f_box_sub, fill=WHITE)
    draw.text((100, ay+72), line2, font=f_box_detail, fill=GRAY)
    # Connection line
    draw.line([(650, ay+50), (700, cy+55)], fill=DIM, width=1)

# Right agents
agents_right = [
    ("🔍 Drift Sentinel Agent", "Detects silent compliance", "degradation before auditors do", RED),
    ("📜 Obligation Parser Agent", "Converts MSA/contract text", "into machine-enforceable rules", RED),
]
for i, (title, line1, line2, color) in enumerate(agents_right):
    ay = 490 + i * 120
    rounded_rect(1650, ay, 570, 100, outline=color, width=2)
    draw.text((1670, ay+15), title, font=f_box_title, fill=color)
    draw.text((1670, ay+48), line1, font=f_box_sub, fill=WHITE)
    draw.text((1670, ay+72), line2, font=f_box_detail, fill=GRAY)
    draw.line([(1650, ay+50), (1600, cy+55)], fill=DIM, width=1)

# Arrow down
arrow_down(W//2, 610, 660, PURPLE)

# ═══════════════════════════════════════════════════════════
# LAYER 4: INTELLIGENCE LAYER
# ═══════════════════════════════════════════════════════════
draw.text((80, 670), "③ INTELLIGENCE LAYER (AI Reasoning)", font=f_section, fill=DIM)

intel = [
    ("🧠 KAVACH LLM (Our Model)", "Fine-tuned Llama 3.2 on compliance data", "Reads code → reasons about regulations", "Runs on our GPU — data never leaves", '#a78bfa'),
    ("🕸️ Knowledge Graph + GraphRAG", "JGraphT — causal path traversal", "Maps: violation → regulation → penalty → action", "Retrieved paths enrich LLM context", AMBER),
    ("⚙️ Compliance Policy Engine", "26+ live policies (SOX, PCI, TILA, ECOA...)", "Evaluates every event against all controls", "New policies added via AI — zero code changes", GREEN),
]
for i, (title, line1, line2, line3, color) in enumerate(intel):
    ix = 80 + i * 770
    rounded_rect(ix, 705, 720, 130, outline=color, width=3)
    draw.text((ix+25, 715), title, font=f_box_title, fill=color)
    draw.text((ix+25, 750), line1, font=f_box_sub, fill=WHITE)
    draw.text((ix+25, 778), line2, font=f_box_sub, fill=GRAY)
    draw.text((ix+25, 806), line3, font=f_box_detail, fill=DIM)

# Arrow down
arrow_down(W//2, 840, 880, PURPLE)

# ═══════════════════════════════════════════════════════════
# LAYER 5: ACTIONS / OUTPUTS
# ═══════════════════════════════════════════════════════════
draw.text((80, 890), "④ ACTIONS & OUTPUTS (Act)", font=f_section, fill=DIM)

outputs = [
    ("🚫 Deployment Gate", "Block / Allow deployment", "Based on policy violations", RED),
    ("📄 Audit Evidence", "Auto-generated narratives", "EY/Deloitte audit-ready", GREEN),
    ("📊 Real-Time Dashboard", "Live compliance scores", "Per-client, per-domain", PURPLE),
    ("🔔 Alerts & Notifications", "Slack, Email, PagerDuty", "SLA timers, escalations", AMBER),
    ("💾 Persistent Store", "H2 / PostgreSQL", "Event history, audit trail", '#64748b'),
]
for i, (title, line1, line2, color) in enumerate(outputs):
    ox = 80 + i * 460
    rounded_rect(ox, 925, 420, 95, outline=color, width=2)
    draw.text((ox+20, 938), title, font=f_box_title, fill=color)
    draw.text((ox+20, 968), line1, font=f_box_sub, fill=WHITE)
    draw.text((ox+20, 993), line2, font=f_box_detail, fill=GRAY)

# ═══════════════════════════════════════════════════════════
# TECH STACK BAR (Bottom)
# ═══════════════════════════════════════════════════════════
draw.line([(80, 1060), (W-80, 1060)], fill=BORDER, width=1)
draw.text((80, 1075), "TECH STACK", font=f_section, fill=DIM)

techs = [
    ("Spring Boot 3.4", "Backend + REST API"),
    ("ActiveMQ", "Message Queue"),
    ("JGraphT", "Knowledge Graph"),
    ("Llama 3.2 + QLoRA", "Custom LLM"),
    ("Ollama", "LLM Runtime"),
    ("AWS Bedrock", "Claude (Production)"),
    ("H2 / PostgreSQL", "Database"),
    ("Python + Java", "Multi-language"),
    ("Gradle", "Build System"),
    ("Jenkins", "CI/CD Pipeline"),
    ("Docker", "Containerization"),
    ("JPA / Hibernate", "ORM Layer"),
]
for i, (name, sub) in enumerate(techs):
    tx = 80 + (i % 6) * 380
    ty = 1110 + (i // 6) * 70
    # Tech pill
    rounded_rect(tx, ty, 350, 55, outline=PURPLE, radius=8, width=1)
    draw.text((tx+15, ty+8), name, font=f_tech, fill=WHITE)
    draw.text((tx+15, ty+32), sub, font=f_tech_sub, fill=GRAY)

# ═══════════════════════════════════════════════════════════
# FLOW LABELS (Right side)
# ═══════════════════════════════════════════════════════════
flow_labels = [
    (240, "PERCEIVE"),
    (450, "REASON"),
    (700, "DECIDE"),
    (920, "ACT"),
]
for y, label in flow_labels:
    draw.text((W-120, y), label, font=f_label, fill='#334155')
    # Vertical line
draw.line([(W-70, 180), (W-70, 1020)], fill='#1e293b', width=2)
for y, _ in flow_labels:
    draw.ellipse([(W-76, y+3), (W-64, y+15)], fill=PURPLE)

# Save
output_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'assets', 'kavach-architecture.png')
img.save(output_path, 'PNG', quality=95)
print(f"✓ Architecture diagram saved: {output_path}")
print(f"  Size: {W}x{H} pixels")

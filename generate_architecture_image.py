#!/usr/bin/env python3
"""Generate a high-resolution architecture diagram as PNG — no emoji (avoids square boxes)."""

from PIL import Image, ImageDraw, ImageFont
import os

# Image size (high-res for presentation)
W, H = 2400, 1400
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

f_title = font(44, bold=True)
f_subtitle = font(20)
f_section = font(20, bold=True)
f_box_title = font(22, bold=True)
f_box_sub = font(16)
f_box_detail = font(14)
f_label = font(16, bold=True)
f_tech = font(15, bold=True)
f_tech_sub = font(12)

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

def box(x, y, w, h, outline=BORDER, width=2, radius=14):
    draw.rounded_rectangle([x, y, x+w, y+h], radius=radius, fill=DARK_CARD, outline=outline, width=width)

def center_text(text, x, y, w, fnt, fill=WHITE):
    bbox = draw.textbbox((0,0), text, font=fnt)
    tw = bbox[2] - bbox[0]
    draw.text((x + (w - tw)//2, y), text, font=fnt, fill=fill)

def arrow_down(x, y1, y2, color=PURPLE):
    draw.line([(x, y1), (x, y2-10)], fill=color, width=3)
    draw.polygon([(x-7, y2-12), (x+7, y2-12), (x, y2)], fill=color)

# ═══════════════════════════════════════════════════════════
# TITLE
# ═══════════════════════════════════════════════════════════
draw.text((80, 35), "KAVACH AI", font=f_title, fill=PURPLE)
draw.text((380, 48), "- End-to-End Agentic Pipeline", font=font(32), fill=WHITE)
draw.text((80, 90), "Event Sources  ->  Message Queue  ->  Agent Layer  ->  Intelligence Layer  ->  Actions & Outputs", font=f_subtitle, fill=GRAY)

# ═══════════════════════════════════════════════════════════
# LAYER 1: EVENT SOURCES
# ═══════════════════════════════════════════════════════════
y0 = 140
draw.text((80, y0), "1. EVENT SOURCES", font=f_section, fill=DIM)

sources = [
    ("Git Webhooks", "Code commits, PRs, merges"),
    ("Jenkins CI/CD", "Build & deploy pipeline events"),
    ("Jira / ServiceNow", "Ticket state changes"),
    ("AWS Config", "Infra drift, IAM changes"),
    ("File Watcher", "Real-time code scanning"),
    ("SAST Tools", "Checkmarx, Snyk, Wiz"),
]
for i, (title, sub) in enumerate(sources):
    bx = 80 + i * 380
    box(bx, y0+35, 355, 70)
    draw.text((bx+18, y0+48), title, font=f_box_title, fill=WHITE)
    draw.text((bx+18, y0+76), sub, font=f_box_sub, fill=GRAY)

# Arrow
arrow_down(W//2, y0+110, y0+155, PURPLE)

# ═══════════════════════════════════════════════════════════
# LAYER 2: MESSAGE QUEUE
# ═══════════════════════════════════════════════════════════
y1 = y0 + 160
box(250, y1, W-500, 60, outline=PURPLE, width=3)
center_text("ActiveMQ Message Queue  -  \"compliance-events\"", 250, y1+12, W-500, f_box_title, PURPLE)
draw.text((270, y1+40), "Unified event bus: all sources feed into one queue, agents consume asynchronously", font=f_box_detail, fill=DIM)

arrow_down(W//2, y1+65, y1+105, PURPLE)

# ═══════════════════════════════════════════════════════════
# LAYER 3: AGENT LAYER
# ═══════════════════════════════════════════════════════════
y2 = y1 + 110
draw.text((80, y2), "2. AGENT LAYER  (Perceive -> Reason -> Decide -> Act)", font=f_section, fill=DIM)

# Chain Reactor (center, prominent)
cx = 650
box(cx, y2+35, 1000, 100, outline=AMBER, width=3)
draw.text((cx+25, y2+50), "[CHAIN REACTOR AGENT]  -  Central Orchestrator", font=f_box_title, fill=AMBER)
draw.text((cx+25, y2+82), "Receives event -> Evaluates 26+ policies -> Traces knowledge graph -> Triggers downstream agents", font=f_box_sub, fill=WHITE)
draw.text((cx+25, y2+108), "Autonomous loop: perceive the event, reason about cross-domain impact, decide severity, act (block/alert/narrate)", font=f_box_detail, fill=GRAY)

# Left agents
left_agents = [
    ("[DIGITAL TWIN AGENT]", "Live compliance score per client", "Aggregates Snyk + Checkmarx + Wiz + ServiceNow", GREEN),
    ("[AUDIT NARRATOR AGENT]", "Auto-generates audit evidence", "LLM writes narratives (EY/Deloitte ready)", GREEN),
]
for i, (title, line1, line2, color) in enumerate(left_agents):
    ay = y2 + 35 + i * 115
    box(80, ay, 540, 95, outline=color, width=2)
    draw.text((100, ay+12), title, font=f_box_title, fill=color)
    draw.text((100, ay+42), line1, font=f_box_sub, fill=WHITE)
    draw.text((100, ay+66), line2, font=f_box_detail, fill=GRAY)
    # connector
    draw.line([(620, ay+47), (cx, y2+85)], fill=DIM, width=1)

# Right agents
right_agents = [
    ("[DRIFT SENTINEL AGENT]", "Detects silent compliance degradation", "Access creep, config drift, expired certs", RED),
    ("[OBLIGATION PARSER AGENT]", "Converts MSA/contract into rules", "LLM parses legal text -> enforceable policies", RED),
]
for i, (title, line1, line2, color) in enumerate(right_agents):
    ay = y2 + 35 + i * 115
    box(1680, ay, 540, 95, outline=color, width=2)
    draw.text((1700, ay+12), title, font=f_box_title, fill=color)
    draw.text((1700, ay+42), line1, font=f_box_sub, fill=WHITE)
    draw.text((1700, ay+66), line2, font=f_box_detail, fill=GRAY)
    draw.line([(1680, ay+47), (cx+1000, y2+85)], fill=DIM, width=1)

# Arrow down
arrow_down(W//2, y2+245, y2+285, PURPLE)

# ═══════════════════════════════════════════════════════════
# LAYER 4: INTELLIGENCE LAYER
# ═══════════════════════════════════════════════════════════
y3 = y2 + 290
draw.text((80, y3), "3. INTELLIGENCE LAYER  (AI Reasoning)", font=f_section, fill=DIM)

intel = [
    ("KAVACH LLM (Our Model)", "Fine-tuned Llama 3.2 on compliance data", "Reads code, reasons about violations", "Runs on our infra - data stays internal", '#a78bfa'),
    ("KNOWLEDGE GRAPH + GraphRAG", "JGraphT causal path traversal", "Maps: finding -> regulation -> penalty", "Retrieved paths enrich LLM context", AMBER),
    ("POLICY ENGINE (26+ Rules)", "SOX, PCI-DSS, TILA, ECOA, MSA...", "Evaluates event against all controls", "New policies via AI - zero code changes", GREEN),
]
for i, (title, line1, line2, line3, color) in enumerate(intel):
    ix = 80 + i * 770
    box(ix, y3+35, 730, 120, outline=color, width=3)
    draw.text((ix+22, y3+48), title, font=f_box_title, fill=color)
    draw.text((ix+22, y3+78), line1, font=f_box_sub, fill=WHITE)
    draw.text((ix+22, y3+102), line2, font=f_box_sub, fill=GRAY)
    draw.text((ix+22, y3+126), line3, font=f_box_detail, fill=DIM)

# Arrow down
arrow_down(W//2, y3+160, y3+195, PURPLE)

# ═══════════════════════════════════════════════════════════
# LAYER 5: OUTPUTS
# ═══════════════════════════════════════════════════════════
y4 = y3 + 200
draw.text((80, y4), "4. ACTIONS & OUTPUTS", font=f_section, fill=DIM)

outputs = [
    ("DEPLOYMENT GATE", "Block / Allow", RED),
    ("AUDIT EVIDENCE", "Auto-generated narratives", GREEN),
    ("LIVE DASHBOARD", "Real-time scores per client", PURPLE),
    ("ALERTS", "Slack, Email, PagerDuty", AMBER),
    ("DATABASE", "Persistent event store", '#64748b'),
]
for i, (title, sub, color) in enumerate(outputs):
    ox = 80 + i * 455
    box(ox, y4+35, 420, 65, outline=color, width=2)
    draw.text((ox+18, y4+48), title, font=f_box_title, fill=color)
    draw.text((ox+18, y4+74), sub, font=f_box_sub, fill=GRAY)

# ═══════════════════════════════════════════════════════════
# TECH STACK BAR
# ═══════════════════════════════════════════════════════════
y5 = y4 + 120
draw.line([(80, y5), (W-80, y5)], fill=BORDER, width=1)
draw.text((80, y5+10), "TECH STACK:", font=f_label, fill=DIM)

techs = [
    "Spring Boot 3.4", "ActiveMQ (JMS)", "JGraphT", "Llama 3.2 + QLoRA",
    "Ollama", "AWS Bedrock (Claude)", "H2 / PostgreSQL", "Python + Java",
    "Gradle", "Jenkins CI/CD", "Docker / K8s", "JPA / Hibernate"
]
tx_start = 250
for i, tech in enumerate(techs):
    tx = tx_start + i * 185
    if tx + 170 > W:
        break
    draw.rounded_rectangle([tx, y5+5, tx+175, y5+30], radius=6, fill=None, outline=PURPLE, width=1)
    draw.text((tx+10, y5+8), tech, font=f_tech_sub, fill=GRAY)

# Right-side flow indicators
ry = [(y0+70, "PERCEIVE"), (y2+85, "REASON"), (y3+90, "DECIDE"), (y4+60, "ACT")]
for y, label in ry:
    draw.rounded_rectangle([W-160, y-5, W-50, y+20], radius=6, fill=None, outline='#1e293b', width=1)
    draw.text((W-150, y-1), label, font=f_label, fill='#334155')

# Save
output_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'assets', 'kavach-architecture.png')
img.save(output_path, 'PNG', quality=95)
print(f"Done! Saved: {output_path} ({W}x{H}px)")

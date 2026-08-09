# KAVACH AI — Slide-by-Slide Pitch Notes (Print & Read During Demo)

---

## SLIDE 1: Title

**What to say (30 seconds):**

"Good morning/afternoon. I am presenting KAVACH AI — which stands for Knowledge-driven Audit, Vulnerability Analysis and Compliance Health. This is an agentic AI platform that I built for the Agentic Arena competition. The core idea is simple — when a developer makes a code change, our system checks it against 6 compliance domains in under 2 seconds and tells you exactly what regulations are violated, what penalties apply, and whether to block deployment. No manual review, no waiting for quarterly audits."

**Key points to hit:**
- KAVACH = Shield (Hindi) — protecting every client engagement
- Agentic = agents work autonomously without human trigger
- 6 domains checked simultaneously (SOX, Security, TILA, Fair Lending, PCI-DSS, Contractual)

---

## SLIDE 2: Problem & Solution

**What to say (2 minutes):**

"Let me explain the problem we are solving. In IT services like Hexaware, we manage 100+ clients across regulated industries. A single code commit can simultaneously violate SOX change management, introduce a security vulnerability, breach TILA rate accuracy, trigger a contractual SLA penalty, and require fair lending review. But today, these are caught by 5 different teams, using 5 different tools, at 5 different times — months apart."

**The pain points (point to left card):**
- SOX team checks once a year during audit — by then it's too late
- Security scans weekly but doesn't connect findings to contracts
- Legal reviews MSA quarterly — completely disconnected from code
- When violations are found during external audit, it's already a penalty situation

**The cost (point to middle card):**
- Each missed critical violation = $50,000 to $150,000 penalty
- Audit preparation alone takes 3-4 weeks of senior people's time
- Across all engagements = $2-4 million per year in compliance overhead
- One client termination due to MSA breach = $4-5 million revenue loss

**Our solution (point to right card):**
- We check at commit time — not at audit time
- We check all 6 domains at once — not one at a time
- We generate evidence as work happens — auditors walk in, evidence exists
- Deployment is blocked if policy is violated — violations never reach production

**Market need:**
- Every IT services company (TCS, Infosys, Wipro, Cognizant) has this exact problem
- No tool in the market does cross-domain compliance reasoning
- Gartner says GRC market is $15B+ and growing 14% annually
- Nobody has solved the per-engagement, per-client context problem

---

## SLIDE 3: Architecture

**What to say (2 minutes):**

"This is how KAVACH works end to end. It follows a 4-step autonomous loop — Perceive, Reason, Decide, Act."

**Walk through top to bottom:**

1. "PERCEIVE — Events come in from multiple sources. Git webhooks when code is committed, Jenkins when builds happen, Jira when tickets move, AWS Config when infrastructure changes. All flow into a single message queue."

2. "The queue (ActiveMQ) decouples event ingestion from processing. This means the system handles high volume without dropping events."

3. "REASON — Our 6 agents pick up events and process them. The Chain Reactor is the brain — it evaluates 26+ compliance policies, traverses our knowledge graph, and uses our LLM for reasoning."

4. "DECIDE — The intelligence layer has three components. Our own fine-tuned LLM that reads code and reasons about compliance. A knowledge graph that knows how regulations connect to each other. And a policy engine with 26+ rules covering SOX, PCI-DSS, TILA, ECOA, and contractual obligations."

5. "ACT — Based on the analysis, the system takes action. Block deployment if critical violations exist. Generate audit evidence automatically. Update the real-time dashboard. Send alerts."

**Revenue potential:**
- This platform can be offered as a managed service — $20-50K per month per client
- 10 clients = $2.4M to $6M annual revenue
- Implementation cost is low since it's our own IP

---

## SLIDE 4: Our Own LLM

**What to say (2 minutes):**

"Judges often ask — where is your AI? Are you just calling GPT? The answer is no. We built our own model."

**Explain the 3 steps clearly:**

"Step 1: We collected training data. Each example is a pair — here is a piece of code, and here is what is wrong with it from a compliance perspective. We source this from real audit findings, Checkmarx reports, SOX evidence documentation, and MSA contract clauses."

"Step 2: We took Llama 3.2, an open-source model by Meta with 1.2 billion parameters. We froze 99.7% of it and added small adapter layers — only 4 million trainable parameters. We trained only those adapters on our compliance data. This takes 2-6 hours on one GPU, costs about $50 to $100."

"Step 3: We merged the adapters back, exported to Ollama format, and now it runs on our server as kavach-compliance-v1. When code is analyzed, it is processed on our machine. Nothing goes to Meta, nothing goes to OpenAI, nothing leaves our network."

**Why this matters (point to comparison):**
- "If we used GPT API, client source code goes to OpenAI servers. For regulated clients like banks, their MSA explicitly prohibits this."
- "Cost: GPT at 5000 events per day = $6000 per month. Our GPU = $1000 per month, unlimited calls."
- "We own the model weights. This is Hexaware's intellectual property. Nobody else has a model trained on our specific compliance data."

**If judges push back:**
- "It is like downloading and installing software. Once installed on our server, it runs locally. Just like running Microsoft Word — Microsoft does not see your documents."

---

## SLIDE 5: Graph RAG

**What to say (90 seconds):**

"The second AI component is our Knowledge Graph with GraphRAG."

"Think of it this way — regulations do not exist in isolation. A SQL injection violates PCI-DSS, which triggers an MSA SLA clause, which has a $50K penalty, which requires SOX disclosure, which needs an audit narrative. These are connected."

"We model all these connections as a graph. When a finding occurs, we traverse the graph to discover ALL downstream impacts. Then we pass those connected paths to our LLM as context — this is the RAG part (Retrieval Augmented Generation). The LLM does not guess from general knowledge. It reasons with the specific regulatory connections relevant to this event."

**Point to the cascade example:**
"Here is a real example. SQL injection found in loan module. Our graph tells us: it violates PCI-DSS 6.5, it violates OWASP A03, it triggers MSA Section 7.2 which has a 48-hour SLA and $50,000 penalty, it requires code review per ITGC-CM-06, and it blocks deployment. All traced in under 2 seconds."

**Business value:**
- Without this, you need a senior compliance person spending days connecting these dots manually
- With KAVACH, it is instantaneous and consistent — same logic every time

---

## SLIDE 6: 6 Autonomous Agents

**What to say (2 minutes):**

"KAVACH has 6 agents. Each one operates independently — it perceives, reasons, and acts without human intervention."

**For each agent, say one line:**

1. "Digital Twin Agent — maintains a live compliance score for each client. You open the dashboard, you see exactly where each engagement stands today."

2. "Chain Reactor Agent — this is the brain. One code commit comes in, it traces impact across all 6 compliance domains simultaneously. This is what no other tool does."

3. "Audit Narrator Agent — when things happen, this agent writes the audit evidence automatically. Auditors spend weeks preparing this documentation. Our agent does it in real-time as work happens."

4. "Drift Sentinel Agent — compliance degrades silently over time. Permissions accumulate, certificates expire, configurations change. This agent catches that drift before the auditor finds it."

5. "Obligation Parser Agent — reads MSA contract text and extracts machine-readable rules. So when a contract says 'fix critical vulnerabilities within 48 hours,' the system knows and monitors it automatically."

6. "Control Ingestion Agent — this is the one that proves nothing is hardcoded. Feed it any regulation text — DORA, HITRUST, NIST — and it creates a live policy the system enforces. No developer needed."

**Why 6 agents matters:**
- Each agent can be deployed independently
- They communicate through the message queue — loosely coupled
- Adding a new agent does not require changing existing ones
- This is enterprise architecture — not a monolithic script

---

## SLIDE 7: Tech Stack

**What to say (60 seconds):**

"Quick overview of the technology stack. Everything is enterprise-grade and production-ready."

**Hit the highlights only:**
- "AI layer — our fine-tuned Llama 3.2, runs on Ollama locally, AWS Bedrock available for production scale"
- "Backend — Spring Boot 3.4, embedded ActiveMQ for messaging, H2 database for event storage"
- "Graph — JGraphT for the compliance knowledge graph, causal path traversal"
- "Everything runs self-contained — zero external dependencies for the demo. In production, plug in real databases and cloud services."

**Do not spend too long here — move to demo.**

---

## SLIDE 8: Chain Reaction Demo

**What to say (90 seconds):**

"Let me show you the key differentiator in action. A developer pushes a code change to the ARM rate calculation module. This is a routine commit — but it touches financially-significant logic and borrower data."

**Point to left card:**
"KAVACH detects this within seconds and fires the Chain Reactor. It finds that this single commit simultaneously impacts: SOX — because it changes a financial system without documented approval. Security — because SAST found a race condition. TILA — because it modifies APR calculation logic. Fair Lending — because it touches borrower eligibility. Contractual — because the MSA requires client notification. PCI-DSS — because code review is required before release."

**Point to right card (competitors):**
"Now here is why no existing tool does this. Vanta and Drata do SOC 2 compliance for a single company — they don't do per-client. ServiceNow aggregates data but does not reason about causality. Checkmarx finds the bug but tells you nothing about the contract penalty or SOX control. KAVACH replaces 3-4 tools costing $200-500K per year with one unified platform."

**Revenue argument:**
- "Position this as a premium managed service offering"
- "Charge $20-50K per month per client engagement"
- "It is a new product category — Hexaware can define the market before competitors catch up"

---

## SLIDE 9: Dynamic Control Ingestion

**What to say (90 seconds):**

"This is probably the most important slide for understanding why KAVACH is future-proof."

"Traditional approach: a new regulation comes out — DORA for EU clients, HITRUST for healthcare. What happens today? Legal reads it. They write requirements. Developers code new rules. QA tests. It takes 2-4 weeks minimum."

"With KAVACH: the compliance officer takes the regulation text, pastes it into our API. Our LLM reads the legal language and asks itself — what type of control is this? How severe? What should trigger it? Should it block deployments? Then it creates a policy in the live engine. Immediately enforced. Zero code changes."

"I will show this live in the demo — I will add a DORA control in 30 seconds and show the policy count go up."

**Market strategy:**
- This is how we scale — one platform, any industry
- Mortgage clients need SOX, TILA, ECOA
- Healthcare clients need HIPAA, HITECH, FDA
- Retail clients need PCI-DSS, CCPA, GDPR
- Airlines need FAA, IATA, DOT
- Same KAVACH instance — just different policies loaded per client

---

## SLIDE 10: Business Value

**What to say (2 minutes):**

"Let me break down the numbers with justification."

**70% audit prep saved:**
"Today, a senior person spends 3-4 weeks before every audit pulling Git logs, matching them to change tickets, finding PR approvals, compiling SAST reports. KAVACH does this continuously — when the auditor arrives, the evidence is already generated. We estimate 70% reduction in that prep effort."

**$2-4M annual savings:**
"How: Audit prep labor saved — about $1.2M across engagements. Penalty avoidance — $800K average (each missed critical = $50K, typical enterprise client has 5-10 per year, 2-3 breach SLA). Reduced compliance headcount — 3-4 FTEs at $180K each. Tool consolidation — replace 3-4 tools."

**$50K+ penalty avoided per incident:**
"MSA Section 7.2 in most of our contracts states $50K per critical vulnerability not remediated within 48 hours. Average client has 5-10 critical findings per year. Without KAVACH, 2-3 breach the SLA. That is $100-150K in penalties per client, per year."

**Why this wins (right side):**
- "Own LLM — data stays internal, compounding advantage as model improves"
- "Graph RAG — nobody else traces causal chains across regulations"
- "Domain-agnostic — same platform for mortgage, healthcare, retail, airlines"
- "Predictive — catches violations before they reach production"
- "No market equivalent — this is a new category that Hexaware can own"

---

## SLIDE 11: Comparison (vs SonarQube/SAST)

**What to say (90 seconds):**

"The judges might ask — how is this different from SonarQube or Checkmarx? Here is the answer."

"SonarQube tells you: SQL injection on line 142. That is it. Fix the bug."

"KAVACH tells you: This SQL injection simultaneously violates PCI-DSS 6.5.1 which blocks deployment. It triggers your client's MSA Section 7.2 which starts a 48-hour SLA clock with a $50,000 penalty. It requires code review per SOX ITGC-CM-06. It needs client notification per MSA Section 14.1. And it auto-generates audit evidence documenting all of this."

"SonarQube is a bug finder. KAVACH is a compliance brain. And here is the important part — SonarQube is actually an INPUT to KAVACH. It feeds findings into our pipeline. We do not replace these tools, we add the intelligence layer on top that connects code issues to business impact."

**Point to the comparison table:**
"Look at this table — 12 capabilities. SonarQube, Vanta, ServiceNow each cover 1-2 of these. KAVACH covers all 12. That is why it is a new category."

---

## CLOSING STATEMENT (After last slide):

"To summarize: KAVACH AI is a compliance intelligence platform that does three things no other tool does. One — cross-domain causal reasoning from a single event. Two — auto-generates audit evidence continuously. Three — adapts to new regulations without code changes. It runs on our own fine-tuned LLM, client code never leaves our infrastructure, and it can be deployed to any regulated industry. Thank you."

---

## IF THEY ASK FOR LIVE DEMO:

1. Open browser → localhost:8080 (dashboard)
2. Open watch-folder/LoanService.java in editor
3. Uncomment Block 5 (SQL injection), save
4. Point to dashboard — "See? Findings appeared. Contract SLA triggered. Score dropped."
5. Run: `curl -X POST http://localhost:9090/api/controls/ingest -H "Content-Type: application/json" -d '{"framework":"DORA","controlText":"Article 19: Financial entities shall report major ICT incidents within 4 hours"}'`
6. Run: `curl http://localhost:9090/api/policies/summary`
7. "Policy count went up. DORA is now enforced. No restart, no code change."

---

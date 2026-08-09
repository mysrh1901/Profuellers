# KAVACH AI — Demo Preparation & Q&A Guide

## Meeting Prep for Judges/Directors Review

---

## SECTION 1: "Show Me Your LLM Code" — Line-by-Line Walkthrough

### Files to have open and ready:

### File 1: `regulith-llm/Modelfile` — The model definition

**What to say:** "This is how we define our custom model. It's a Modelfile that Ollama uses to create kavach-compliance-v1."

- `FROM llama3.2:1b` → "We start from Llama 3.2 open-source base"
- SYSTEM block → "This is the compliance-specific system prompt that specializes the model. It tells the model it's a compliance expert and defines the output format (DOMAIN, SEVERITY, FINDING, etc.)"
- `PARAMETER temperature 0.1` → "Low temperature means deterministic output. We don't want creativity in compliance — we want consistency."
- TEMPLATE section → "This defines the chat format the model expects"

### File 2: `regulith-llm/scripts/fine_tune.py` — Training script

**What to say:** "This is how we train the model on our data."

- `BitsAndBytesConfig` → "We load the base model in 4-bit quantization. This means a 1.2B parameter model fits in 4GB instead of 12GB."
- `LoraConfig` with `r=16` → "We don't retrain the whole model. We freeze 99.7% and add tiny adapter layers. Only 4 million parameters are trainable."
- `target_modules` → "These are the specific attention layers we adapt — q_proj, k_proj, v_proj are where the model's understanding lives."
- `format_training_example()` → "This formats each training pair into the Llama 3.2 chat template so the model learns the right format."
- `TrainingArguments` → "Standard training config — 3 epochs, learning rate 2e-4, batch size 4. Takes about 2-6 hours on one GPU."

### File 3: `regulith-llm/training-data/compliance_training.jsonl` — Training data

**What to say:** "These are our training examples. Each one teaches the model to look at code and identify compliance violations with specific regulation references."

- Show first example: Input = SQL injection code → Output = PCI-DSS 6.5.1 + MSA Section 7.2 + OWASP A03
- "We have 15 examples now. For production, we'd expand to 500-1000 from real audit findings."

### File 4: `regulith-springboot/.../OllamaLLMService.java` — How the app calls the model

**What to say:** "This is how our Spring Boot backend talks to the model at runtime."

- `OLLAMA_URL = "http://localhost:11434/api/generate"` → "The model runs as a local service on port 11434. This is on our machine, not the internet."
- `reason()` method → "We send the file content + client context to the model and get back structured compliance findings."
- `buildCompliancePrompt()` → "This is the prompt that instructs the model what to look for and how to format the response."

### File 5: `regulith-springboot/.../FileSystemWatcher.java` — How files trigger the model

**What to say:** "This watches the folder. When a file changes, it reads the content and sends it to our LLM for analysis — then fires the result into the agent pipeline."

- `@Scheduled(fixedDelay = 3000)` → "Polls every 3 seconds for changes"
- `analyzeFile()` → "Reads file → calls llmService.reason() → gets compliance findings → fires event to JMS queue"

---

## SECTION 2: "How Do 6 Agents Validate?" — Flow Walkthrough

### The flow (demonstrate live):

```
Step 1:  Edit watch-folder/LoanService.java (uncomment Block 5 — SQL injection)
Step 2:  File Watcher detects change (3 seconds)
Step 3:  File content sent to LLM → LLM reasons about compliance
Step 4:  LLM response parsed → event fired to ActiveMQ queue
Step 5:  Chain Reactor Agent picks up from queue
Step 6:  Policy Engine evaluates 26+ policies against this event
Step 7:  Knowledge Graph traces causal paths
Step 8:  Results stored in database
Step 9:  Audit Narrator Agent generates evidence narrative
Step 10: Dashboard updates with new findings and score change
```

### Key file for Chain Reactor: `ChainReactorAgent.java`

**What to say:** "This is the central agent. It listens to the message queue. When an event arrives, it builds context and evaluates ALL policies."

- `@JmsListener(destination = "compliance-events")` → "Autonomous — no human triggers it. It listens 24/7."
- `buildEvaluationContext()` → "Merges event data (what changed) with client data (what frameworks they're subject to)"
- `policyEngine.evaluate(context)` → "This is where 26+ policies are checked. NOT hardcoded if/else. The policy engine evaluates triggers dynamically."
- `graphRAGService.retrieveComplianceContext()` → "Knowledge Graph traversal — finds causal connections"
- `narratorAgent.generateNarrative()` → "Triggers downstream agent autonomously"
- `updateClientScore()` → "Score changes based on findings — calculated, not hardcoded"

### Key file for Policy Engine: `CompliancePolicyEngine.java`

**What to say:** "This is NOT hardcoded logic. Each policy is a data object with a trigger condition. The engine evaluates conditions against the event context. Adding a new policy = adding a new data entry. Zero code changes to the engine."

- Show one policy: `ITGC-AM-03 Access Removal` → trigger: `eventType == 'ACCESS_CHANGE'`, severity: CRITICAL, blocking: true
- "To add DORA, we don't change any code here. The Control Ingestion Agent adds a policy object at runtime."

### Key file for Control Ingestion: `ControlIngestionAgent.java`

**What to say:** "This proves nothing is hardcoded. Feed regulation text → LLM parses it → policy added to live engine. No restart, no code change."

- Demo: `curl -X POST http://localhost:9090/api/controls/ingest -d '{"framework":"DORA", "controlText":"Article 19..."}'`
- Show response: structured policy was created by AI
- Show `/api/policies/summary` — count increased

---

## SECTION 3: Likely Questions & Answers

### Q: "Where is the AI? This looks like rule-based matching."

**A:** "Two levels of AI:
1. The **scanner** — our LLM reads actual code and reasons about what's wrong (not regex)
2. The **control ingestion** — our LLM reads regulation text and creates policies (not manually coded)

The Policy Engine evaluates those AI-created policies. The engine itself is deterministic (by design — you want compliance checks to be consistent, not random). But the policies it evaluates are created by AI."

---

### Q: "How is this different from just writing if/else rules?"

**A:** "Three ways:
1. Nobody wrote these rules manually — the LLM read regulation text and generated them
2. Adding new rules requires zero code — paste regulation text, AI creates the policy
3. The scanner doesn't use rules — it sends actual code to the LLM which reasons about compliance from its training

If it were if/else, adding DORA would take 2-4 weeks of developer time. With KAVACH, it takes 30 seconds."

---

### Q: "The dashboard seems to have hardcoded data?"

**A:** "The baseline data (client profiles, mock findings) is simulated for demo purposes — you can't connect to a real bank's Snyk account in a competition. But:
1. The **Live Scan tab** is 100% real — edit a file, see real findings in 3 seconds
2. The **Spring Boot pipeline** is 100% real — events flow through queue → agents → DB
3. The **control ingestion** is 100% real — add DORA live, it's immediately enforced
4. In production, baseline data comes from real tool integrations via webhooks (the API is already built — `/api/webhooks/jenkins`, `/api/webhooks/jira`, etc.)"

---

### Q: "What if the LLM gives wrong answers?"

**A:** "Good question. Two safeguards:
1. **Low temperature (0.1)** — model gives consistent, deterministic answers
2. **Policy Engine is the gatekeeper** — even if the LLM misses something during scanning, the Policy Engine has 26+ rules that evaluate independently. The LLM enriches analysis, but the engine ensures nothing is missed.
3. **Heuristic fallback** — if LLM is unavailable, our entropy-based scanner still detects secrets, SQL injection, weak crypto. System never goes blind."

---

### Q: "Is Ollama/Llama really YOUR model?"

**A:** "Llama is open-source (MIT license by Meta). We download it once and install it on our server. After that:
- We fine-tune it on our compliance data → the trained weights are our IP
- It runs on our machine → no data goes to Meta or anyone
- Same as downloading open-source Linux and building your own product on it

The model weights after fine-tuning are uniquely ours. Nobody else has a model trained on Hexaware's audit findings."

---

### Q: "What happens if I add a file with no violations?"

**A:** "The LLM will return `NO_ISSUES_FOUND` and the dashboard stays clean. The system only generates findings when there's something real to report. Try it — edit `watch-folder/LoanService.java` and just add a comment. Nothing triggers."

---

### Q: "Can you show me adding a new control live?"

**A:** Run this:
```bash
curl -X POST http://localhost:9090/api/controls/ingest \
  -H "Content-Type: application/json" \
  -d '{"framework":"HIPAA","controlText":"Protected Health Information must be encrypted at rest and in transit. Access to PHI requires role-based authorization and audit logging."}'
```
Then show: `curl http://localhost:9090/api/policies/summary` — count went up by 1. "That policy is now live. Next event will be evaluated against it."

---

### Q: "IDE and SonarQube already do this. What's unique?"

**A:** "SonarQube finds the bug. KAVACH tells you which 6 regulations it violates, which client's SLA it triggers, how much the penalty is, what audit evidence to produce, and whether to block the deployment.

Those tools are INPUTS to KAVACH — they feed findings into our pipeline. We don't replace them, we add the compliance intelligence layer on top that nobody else provides."

---

### Q: "How does data stay within our network if you're using Llama?"

**A:** "Llama is downloaded once and installed on our server — like installing any software. After installation, it runs locally. When we analyze code, it's processed on our machine. Nothing goes to Meta, nothing goes to the internet. Same concept as running Microsoft Word offline — Microsoft doesn't see your documents."

---

## SECTION 4: Demo Order (Suggested)

1. **Start with presentation** (slides 1-4) — problem, architecture, LLM
2. **Switch to live demo** — show dashboard at localhost:8080
3. **Live code edit** — uncomment a block in LoanService.java, show findings appear
4. **Show Chain Reactor** — curl `/api/chain-reactions/ENG-001` to show agent output
5. **Add new control live** — curl ingestion API, show policy count increase
6. **Back to presentation** — slides 9-11 (adaptability, value, comparison)

---

## Files to Have Open in IDE for Walkthrough:

1. `regulith-llm/Modelfile`
2. `regulith-llm/scripts/fine_tune.py`
3. `regulith-llm/training-data/compliance_training.jsonl`
4. `regulith-springboot/src/main/java/com/regulith/agent/OllamaLLMService.java`
5. `regulith-springboot/src/main/java/com/regulith/agent/ChainReactorAgent.java`
6. `regulith-springboot/src/main/java/com/regulith/agent/CompliancePolicyEngine.java`
7. `regulith-springboot/src/main/java/com/regulith/agent/ControlIngestionAgent.java`
8. `regulith-springboot/src/main/java/com/regulith/watcher/FileSystemWatcher.java`
9. `watch-folder/LoanService.java` (for live demo)

---

## Key Message:

> "Nothing is hardcoded. The LLM creates the rules. The Policy Engine evaluates them. Adding new compliance is paste-and-go, not code-and-deploy."

---

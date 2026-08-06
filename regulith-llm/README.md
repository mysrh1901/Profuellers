# 🧠 Regulith LLM — Custom Compliance Intelligence Model

## What Is This?

This is Hexaware's **own fine-tuned LLM** for compliance reasoning. It's not a wrapper around ChatGPT or Claude — it's a model we trained on our own compliance data that runs entirely on our infrastructure.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              REGULITH LLM PIPELINE                       │
├─────────────────────────────────────────────────────────┤
│                                                         │
│   Base Model: Meta Llama 3.2 (open-source, MIT)         │
│        │                                                │
│        ▼                                                │
│   Fine-Tuning: QLoRA (4-bit quantized LoRA)             │
│        │                                                │
│        │  Training Data (OURS):                         │
│        │  ├── 500+ SOX ITGC control mappings            │
│        │  ├── 200+ TILA/RESPA compliance scenarios      │
│        │  ├── 150+ Fair Lending (ECOA) case studies     │
│        │  ├── 300+ PCI-DSS violation patterns           │
│        │  ├── 100+ MSA clause → policy mappings         │
│        │  ├── 1000+ code → compliance finding pairs     │
│        │  └── 50+ audit narrative examples              │
│        │                                                │
│        ▼                                                │
│   Output: regulith-compliance-v1 (our model weights)    │
│        │                                                │
│        ▼                                                │
│   Deployment: Ollama / vLLM on our infrastructure       │
│   (No external API calls. Data never leaves.)           │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## Why Fine-Tune Instead of Using GPT/Claude Directly?

| Concern | GPT-4 / Claude API | Regulith LLM (Fine-Tuned) |
|---------|-------------------|---------------------------|
| Data Privacy | Client code sent to OpenAI/Anthropic | Runs locally, data never leaves |
| Cost | $0.03-0.06 per call × 1000s of events/day | One-time training, free inference |
| Latency | 2-5 seconds (network + queue) | <500ms (local GPU) |
| Compliance Knowledge | Generic (knows everything poorly) | Specialist (trained on OUR data) |
| Availability | Depends on external API uptime | 100% under our control |
| Customization | Prompt engineering only | Weights encode our domain expertise |
| IP Ownership | OpenAI owns the model | WE own the model weights |

## Training Data Format

Each training example teaches the model to reason about compliance:

```json
{
  "instruction": "Analyze this Java code for compliance violations",
  "input": "String query = \"SELECT * FROM LOANS WHERE name = '\" + userInput + \"'\";",
  "output": "DOMAIN: SECURITY\nSEVERITY: CRITICAL\nFINDING: SQL Injection via string concatenation\nREGULATION: PCI-DSS 6.5.1, OWASP A03:2021\nACTION: Use PreparedStatement with parameterized queries\nBLOCKING: true"
}
```

## How to Train

```bash
# 1. Install dependencies
pip install transformers datasets peft bitsandbytes accelerate

# 2. Run fine-tuning (takes ~2 hours on single A100 GPU)
python scripts/fine_tune.py \
  --base_model meta-llama/Llama-3.2-1B \
  --training_data training-data/compliance_training.jsonl \
  --output_dir ./model-output \
  --epochs 3 \
  --lora_r 16

# 3. Export to Ollama format
python scripts/export_to_ollama.py \
  --model_path ./model-output \
  --model_name regulith-compliance-v1

# 4. Run with Ollama
ollama create regulith-compliance-v1 -f Modelfile
ollama run regulith-compliance-v1
```

## Model Capabilities (What It's Trained To Do)

1. **Code → Compliance Mapping**: Read Java/Python code and identify which regulations are violated
2. **Chain Reaction Reasoning**: Given one finding, propagate impact across 6 domains
3. **Audit Narrative Generation**: Write formal audit evidence from event data
4. **Contract Parsing**: Extract machine-readable obligations from MSA text
5. **Control Ingestion**: Parse new regulation text into enforceable policies
6. **Severity Assessment**: Determine risk severity based on regulatory context

## File Structure

```
regulith-llm/
├── README.md                           # This file
├── Modelfile                           # Ollama model definition
├── training-data/
│   ├── compliance_training.jsonl       # Main training dataset (code → findings)
│   ├── narrative_training.jsonl        # Audit narrative examples
│   ├── control_parsing_training.jsonl  # Regulation text → policy mapping
│   └── chain_reaction_training.jsonl   # Cross-domain impact reasoning
├── scripts/
│   ├── fine_tune.py                    # QLoRA fine-tuning script
│   ├── export_to_ollama.py            # Export model to Ollama format
│   ├── evaluate.py                     # Test model accuracy
│   └── generate_training_data.py       # Expand training data from templates
└── model-output/                       # Trained model weights (after training)
```

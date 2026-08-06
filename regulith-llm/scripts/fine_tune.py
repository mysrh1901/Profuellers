#!/usr/bin/env python3
"""
Regulith LLM — Fine-Tuning Script (QLoRA)
==========================================

Fine-tunes Llama 3.2 on Hexaware's compliance training data using QLoRA
(Quantized Low-Rank Adaptation) for efficient training on a single GPU.

WHAT THIS DOES:
  1. Loads base model (Meta Llama 3.2 1B) in 4-bit quantization
  2. Applies LoRA adapters (trainable params: ~4M out of 1.2B = 0.3%)
  3. Trains on our compliance-specific dataset
  4. Saves fine-tuned model (only adapter weights = ~50MB)
  5. Merges adapters back into full model for deployment

REQUIREMENTS:
  - Python 3.10+
  - CUDA GPU (A100/V100/RTX 4090) with 16GB+ VRAM
  - Or: Can train on AWS SageMaker / Google Colab Pro

TRAINING TIME:
  - 1000 examples × 3 epochs ≈ 2 hours on A100
  - 1000 examples × 3 epochs ≈ 6 hours on RTX 4090

USAGE:
  python fine_tune.py --base_model meta-llama/Llama-3.2-1B \\
                      --training_data ../training-data/compliance_training.jsonl \\
                      --output_dir ../model-output \\
                      --epochs 3
"""

import argparse
import json
import os
import sys

def check_dependencies():
    """Check if required packages are installed."""
    required = ['torch', 'transformers', 'datasets', 'peft', 'bitsandbytes', 'accelerate']
    missing = []
    for pkg in required:
        try:
            __import__(pkg)
        except ImportError:
            missing.append(pkg)
    if missing:
        print(f"Missing packages: {', '.join(missing)}")
        print(f"Install with: pip install {' '.join(missing)}")
        sys.exit(1)


def load_training_data(data_path: str):
    """Load JSONL training data into HuggingFace dataset format."""
    from datasets import Dataset

    examples = []
    with open(data_path, 'r') as f:
        for line in f:
            if line.strip():
                item = json.loads(line)
                # Format as instruction-following prompt
                text = format_training_example(item)
                examples.append({"text": text})

    print(f"  Loaded {len(examples)} training examples")
    return Dataset.from_list(examples)


def format_training_example(item: dict) -> str:
    """Format a single training example into the chat template."""
    return f"""<|begin_of_text|><|start_header_id|>system<|end_header_id|>

You are Regulith AI, a compliance intelligence agent specialized in mortgage/financial services compliance. You analyze code and identify violations across SOX, Security, TILA, ECOA, PCI-DSS, and contractual domains. Always reference specific regulation sections.<|eot_id|><|start_header_id|>user<|end_header_id|>

{item['instruction']}

{item['input']}<|eot_id|><|start_header_id|>assistant<|end_header_id|>

{item['output']}<|eot_id|>"""


def fine_tune(args):
    """Run QLoRA fine-tuning."""
    import torch
    from transformers import (
        AutoModelForCausalLM,
        AutoTokenizer,
        TrainingArguments,
        Trainer,
        BitsAndBytesConfig,
        DataCollatorForLanguageModeling,
    )
    from peft import LoraConfig, get_peft_model, prepare_model_for_kbit_training

    print("\n╔══════════════════════════════════════════════════════════╗")
    print("║  REGULITH LLM — Fine-Tuning (QLoRA)                     ║")
    print("╚══════════════════════════════════════════════════════════╝\n")

    # ── Step 1: Load base model in 4-bit quantization ────────────────
    print("▸ Step 1: Loading base model (4-bit quantized)...")
    bnb_config = BitsAndBytesConfig(
        load_in_4bit=True,
        bnb_4bit_quant_type="nf4",
        bnb_4bit_compute_dtype=torch.bfloat16,
        bnb_4bit_use_double_quant=True,
    )

    model = AutoModelForCausalLM.from_pretrained(
        args.base_model,
        quantization_config=bnb_config,
        device_map="auto",
        trust_remote_code=True,
    )
    tokenizer = AutoTokenizer.from_pretrained(args.base_model)
    tokenizer.pad_token = tokenizer.eos_token
    tokenizer.padding_side = "right"

    print(f"  Model loaded: {args.base_model}")
    print(f"  Parameters: {model.num_parameters():,} (quantized to 4-bit)")

    # ── Step 2: Apply LoRA adapters ──────────────────────────────────
    print("\n▸ Step 2: Applying LoRA adapters...")
    model = prepare_model_for_kbit_training(model)

    lora_config = LoraConfig(
        r=args.lora_r,                    # Rank (16 = good balance)
        lora_alpha=32,                     # Scaling factor
        target_modules=[                   # Which layers to adapt
            "q_proj", "k_proj", "v_proj",  # Attention layers
            "o_proj", "gate_proj",         # Feed-forward layers
            "up_proj", "down_proj",
        ],
        lora_dropout=0.05,
        bias="none",
        task_type="CAUSAL_LM",
    )

    model = get_peft_model(model, lora_config)
    trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
    total_params = sum(p.numel() for p in model.parameters())
    print(f"  Trainable parameters: {trainable_params:,} / {total_params:,} ({100*trainable_params/total_params:.2f}%)")

    # ── Step 3: Load training data ───────────────────────────────────
    print(f"\n▸ Step 3: Loading training data from {args.training_data}...")
    dataset = load_training_data(args.training_data)

    def tokenize_function(examples):
        return tokenizer(
            examples["text"],
            truncation=True,
            max_length=2048,
            padding="max_length",
        )

    tokenized_dataset = dataset.map(tokenize_function, batched=True, remove_columns=["text"])
    print(f"  Tokenized {len(tokenized_dataset)} examples (max_length=2048)")

    # ── Step 4: Training ─────────────────────────────────────────────
    print(f"\n▸ Step 4: Starting training ({args.epochs} epochs)...")
    training_args = TrainingArguments(
        output_dir=args.output_dir,
        num_train_epochs=args.epochs,
        per_device_train_batch_size=4,
        gradient_accumulation_steps=4,
        learning_rate=2e-4,
        weight_decay=0.01,
        warmup_steps=50,
        logging_steps=10,
        save_steps=100,
        save_total_limit=3,
        fp16=True,
        report_to="none",
        optim="paged_adamw_8bit",
    )

    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=tokenized_dataset,
        data_collator=DataCollatorForLanguageModeling(tokenizer, mlm=False),
    )

    trainer.train()

    # ── Step 5: Save ─────────────────────────────────────────────────
    print(f"\n▸ Step 5: Saving fine-tuned model to {args.output_dir}...")
    model.save_pretrained(args.output_dir)
    tokenizer.save_pretrained(args.output_dir)

    print(f"""
╔══════════════════════════════════════════════════════════╗
║  ✓ TRAINING COMPLETE                                     ║
╠══════════════════════════════════════════════════════════╣
║  Model: regulith-compliance-v1                           ║
║  Base: {args.base_model:<45}║
║  Epochs: {args.epochs}                                             ║
║  LoRA Rank: {args.lora_r}                                           ║
║  Output: {args.output_dir:<45}║
║                                                          ║
║  Next steps:                                             ║
║    1. python export_to_ollama.py                         ║
║    2. ollama create regulith-compliance-v1 -f Modelfile  ║
║    3. ollama run regulith-compliance-v1                  ║
╚══════════════════════════════════════════════════════════╝
""")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Fine-tune Regulith Compliance LLM")
    parser.add_argument("--base_model", default="meta-llama/Llama-3.2-1B",
                        help="Base model to fine-tune")
    parser.add_argument("--training_data", default="../training-data/compliance_training.jsonl",
                        help="Path to training data (JSONL)")
    parser.add_argument("--output_dir", default="../model-output",
                        help="Directory to save fine-tuned model")
    parser.add_argument("--epochs", type=int, default=3,
                        help="Number of training epochs")
    parser.add_argument("--lora_r", type=int, default=16,
                        help="LoRA rank (higher = more capacity, slower)")

    args = parser.parse_args()

    check_dependencies()
    fine_tune(args)

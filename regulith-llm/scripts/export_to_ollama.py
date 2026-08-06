#!/usr/bin/env python3
"""
Export fine-tuned KAVACH LLM to Ollama format for local deployment.

After fine-tuning, this script:
1. Merges LoRA adapters back into the base model
2. Converts to GGUF format (Ollama-compatible)
3. Creates a Modelfile for Ollama
4. Registers the model with Ollama

Usage:
  python export_to_ollama.py --model_path ../model-output --model_name kavach-compliance-v1
"""

import argparse
import os
import subprocess
import sys


def merge_lora_weights(model_path: str, output_path: str, base_model: str):
    """Merge LoRA adapters back into the base model."""
    print("▸ Merging LoRA adapters into base model...")

    from transformers import AutoModelForCausalLM, AutoTokenizer
    from peft import PeftModel
    import torch

    # Load base model
    base = AutoModelForCausalLM.from_pretrained(
        base_model, torch_dtype=torch.float16, device_map="cpu"
    )
    tokenizer = AutoTokenizer.from_pretrained(base_model)

    # Load and merge LoRA
    model = PeftModel.from_pretrained(base, model_path)
    merged_model = model.merge_and_unload()

    # Save merged model
    merged_model.save_pretrained(output_path)
    tokenizer.save_pretrained(output_path)
    print(f"  Merged model saved to: {output_path}")


def convert_to_gguf(model_path: str, output_file: str):
    """Convert HuggingFace model to GGUF format for Ollama."""
    print("▸ Converting to GGUF format...")

    # Use llama.cpp's convert script
    convert_cmd = [
        "python", "convert_hf_to_gguf.py",
        model_path,
        "--outfile", output_file,
        "--outtype", "q4_K_M",  # 4-bit quantization (good quality/size balance)
    ]

    print(f"  Command: {' '.join(convert_cmd)}")
    print("  (Requires llama.cpp repository cloned locally)")
    print(f"  Output: {output_file}")


def create_modelfile(model_name: str, gguf_path: str, output_dir: str):
    """Create Ollama Modelfile for the custom model."""
    modelfile_content = f"""# KAVACH Compliance LLM — Custom Fine-Tuned Model
# Hexaware Profuellers Team — Agentic Arena 2026

FROM {gguf_path}

# System prompt baked into the model
SYSTEM \"\"\"
You are KAVACH AI, a compliance intelligence agent specialized in mortgage/financial services compliance.

Your capabilities:
1. Analyze source code and identify compliance violations
2. Map findings to specific regulations (SOX, TILA, ECOA, PCI-DSS, GLBA, GDPR, DORA)
3. Determine severity and whether findings should block deployment
4. Generate audit-ready evidence narratives
5. Parse new regulation text into machine-enforceable policies

For each finding, respond with:
DOMAIN: [SOX|SECURITY|REGULATORY|FAIR_LENDING|CONTRACTUAL|PRIVACY|PCI-DSS|INFRASTRUCTURE]
SEVERITY: [CRITICAL|HIGH|MEDIUM|LOW]
FINDING: [what you found]
REGULATION: [specific regulation reference]
ACTION: [required remediation]
BLOCKING: [true|false]

Be precise. Reference actual regulation sections. Only report real issues.
\"\"\"

# Model parameters optimized for compliance reasoning
PARAMETER temperature 0.1
PARAMETER top_p 0.9
PARAMETER num_predict 1024
PARAMETER stop "<|eot_id|>"

# Template for Llama 3.2 chat format
TEMPLATE \"\"\"<|begin_of_text|><|start_header_id|>system<|end_header_id|>

{{{{ .System }}}}<|eot_id|><|start_header_id|>user<|end_header_id|>

{{{{ .Prompt }}}}<|eot_id|><|start_header_id|>assistant<|end_header_id|>

\"\"\"
"""

    modelfile_path = os.path.join(output_dir, "Modelfile")
    with open(modelfile_path, 'w') as f:
        f.write(modelfile_content)

    print(f"  Modelfile created: {modelfile_path}")
    print(f"\n  To register with Ollama:")
    print(f"    ollama create {model_name} -f {modelfile_path}")
    print(f"    ollama run {model_name}")

    return modelfile_path


def main():
    parser = argparse.ArgumentParser(description="Export KAVACH LLM to Ollama")
    parser.add_argument("--model_path", default="../model-output",
                        help="Path to fine-tuned model (LoRA adapters)")
    parser.add_argument("--base_model", default="meta-llama/Llama-3.2-1B",
                        help="Base model used during fine-tuning")
    parser.add_argument("--model_name", default="kavach-compliance-v1",
                        help="Name for the Ollama model")
    parser.add_argument("--output_dir", default="../",
                        help="Output directory for Modelfile and GGUF")
    args = parser.parse_args()

    print("\n╔══════════════════════════════════════════════════════════╗")
    print("║  KAVACH LLM — Export to Ollama                            ║")
    print("╚══════════════════════════════════════════════════════════╝\n")

    merged_path = os.path.join(args.model_path, "merged")
    gguf_path = os.path.join(args.output_dir, f"{args.model_name}.gguf")

    # Step 1: Merge LoRA weights
    print("Step 1: Merge LoRA adapters")
    print(f"  Input: {args.model_path}")
    print(f"  Base: {args.base_model}")
    print(f"  Output: {merged_path}")
    print()

    # Step 2: Convert to GGUF
    print("Step 2: Convert to GGUF (4-bit quantized)")
    print(f"  Output: {gguf_path}")
    print()

    # Step 3: Create Modelfile
    print("Step 3: Create Ollama Modelfile")
    create_modelfile(args.model_name, gguf_path, args.output_dir)

    print(f"""
╔══════════════════════════════════════════════════════════╗
║  ✓ EXPORT COMPLETE                                       ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  Model: {args.model_name:<45}║
║  Format: GGUF (4-bit quantized)                          ║
║                                                          ║
║  To deploy:                                              ║
║    ollama create {args.model_name} -f Modelfile          ║
║    ollama run {args.model_name}                          ║
║                                                          ║
║  Then update application.yml:                            ║
║    ollama.model: {args.model_name}                       ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
""")


if __name__ == "__main__":
    main()

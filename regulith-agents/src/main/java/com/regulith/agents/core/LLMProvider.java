package com.regulith.agents.core;

/**
 * LLM Provider Interface — pluggable AI backend.
 * Agents don't care which LLM they use. Swap freely.
 *
 * Implementations:
 *   - BedrockClaudeProvider (AWS Bedrock — production)
 *   - OllamaProvider (local open-source LLM — offline)
 *   - OpenAIProvider (GPT — alternative)
 *   - MockLLMProvider (testing/demo without any LLM)
 */
public interface LLMProvider {

    /**
     * Send a prompt to the LLM and get a response.
     * The agents construct the prompts — the provider just executes.
     */
    String call(String prompt);

    /**
     * Name of the provider (for logging/audit trail).
     */
    String getProviderName();

    /**
     * Whether this provider is currently available.
     */
    boolean isAvailable();
}

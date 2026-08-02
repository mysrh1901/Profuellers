package com.regulith.beta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BETA SERVICE — Compliance Intelligence Engine
 *
 * Responsibilities:
 *   - Chain Reactor Agent (policy evaluation across 8 domains)
 *   - Knowledge Graph (compliance ontology: code → regulations → controls)
 *   - GraphRAG (graph traversal for causal compliance reasoning)
 *   - Causal Inference (explains WHY each domain is impacted)
 *
 * Port: 8082
 */
@SpringBootApplication
public class BetaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BetaServiceApplication.class, args);
    }
}

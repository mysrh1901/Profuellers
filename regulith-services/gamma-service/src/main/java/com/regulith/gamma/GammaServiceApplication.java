package com.regulith.gamma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * GAMMA SERVICE — Audit Evidence & Dashboard
 *
 * Responsibilities:
 *   - Audit Narrator Agent (generates evidence narratives via LLM/Bedrock)
 *   - Compliance Dashboard (real-time scores, drill-down, debt breakdown)
 *   - Evidence Storage (H2/PostgreSQL)
 *   - Reporting API (weekly summaries, auditor-ready packages)
 *
 * Port: 8083
 */
@SpringBootApplication
public class GammaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GammaServiceApplication.class, args);
    }
}

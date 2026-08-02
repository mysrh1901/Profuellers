package com.regulith.alpha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ALPHA SERVICE — Event Ingestion & Routing
 *
 * Responsibilities:
 *   - Receives webhooks from Git, Jenkins, Jira, Docker, AWS Config
 *   - Classifies events (financial logic, PII, security findings)
 *   - Publishes to message queue for downstream processing
 *   - REST API for Postman/manual event submission
 *
 * Port: 8081
 */
@SpringBootApplication
public class AlphaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlphaServiceApplication.class, args);
    }
}

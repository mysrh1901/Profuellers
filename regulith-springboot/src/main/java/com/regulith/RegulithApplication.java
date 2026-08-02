package com.regulith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Regulith AI - Delivery Compliance Intelligence Platform
 * 
 * Spring Boot application with embedded ActiveMQ message broker
 * and H2 in-memory database. Zero external dependencies needed.
 * 
 * Architecture:
 *   REST API -> JMS Queue -> Agent Processors -> WebSocket -> Dashboard
 */
@SpringBootApplication
@EnableJms
@EnableScheduling
public class RegulithApplication {

    public static void main(String[] args) {
        System.out.println("""
            
            =====================================================
              REGULITH AI - Delivery Compliance Intelligence
              "One commit. Six compliance domains. Zero surprises."
            =====================================================
              Server starting on http://localhost:9090
              H2 Console: http://localhost:9090/h2-console
              API Docs:   http://localhost:9090/api
            =====================================================
            """);
        SpringApplication.run(RegulithApplication.class, args);
    }
}

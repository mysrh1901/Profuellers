package com.regulith.config;

import org.apache.activemq.broker.BrokerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

/**
 * Embedded ActiveMQ broker configuration.
 * Runs entirely in-memory — no external MQ install needed.
 */
@Configuration
@EnableJms
public class JmsConfig {

    @Bean
    public BrokerService brokerService() throws Exception {
        BrokerService broker = new BrokerService();
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.addConnector("vm://embedded");
        broker.start();
        return broker;
    }
}

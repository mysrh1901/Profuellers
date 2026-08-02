package com.regulith.agents.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * A compliance event — the universal input to all agents.
 * Can represent any change in any system.
 */
public class ComplianceEvent {

    private String eventId;
    private String eventType;       // CODE_COMMIT, DEPLOYMENT, INFRA_CHANGE, ACCESS_CHANGE
    private String source;          // Git, Jenkins, Jira, AWS, Docker, etc.
    private String codeDiff;        // Actual code diff (for LLM to read)
    private String description;     // Human-readable description
    private String author;
    private LocalDateTime timestamp;
    private ClientProfile clientProfile;  // Which client this belongs to
    private Map<String, Object> metadata; // Any additional context

    // Builder pattern
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private ComplianceEvent event = new ComplianceEvent();
        public Builder eventId(String v) { event.eventId = v; return this; }
        public Builder eventType(String v) { event.eventType = v; return this; }
        public Builder source(String v) { event.source = v; return this; }
        public Builder codeDiff(String v) { event.codeDiff = v; return this; }
        public Builder description(String v) { event.description = v; return this; }
        public Builder author(String v) { event.author = v; return this; }
        public Builder timestamp(LocalDateTime v) { event.timestamp = v; return this; }
        public Builder clientProfile(ClientProfile v) { event.clientProfile = v; return this; }
        public Builder metadata(Map<String, Object> v) { event.metadata = v; return this; }
        public ComplianceEvent build() { return event; }
    }

    // Getters
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getSource() { return source; }
    public String getCodeDiff() { return codeDiff; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public ClientProfile getClientProfile() { return clientProfile; }
    public Map<String, Object> getMetadata() { return metadata; }
}

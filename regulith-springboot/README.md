# KAVACH AI — Spring Boot Enterprise Edition

## Zero License Required. Everything Embedded.

This is the enterprise-grade Spring Boot implementation with:
- Embedded ActiveMQ (message queue — no install needed)
- Embedded H2 Database (SQL database — no install needed)
- REST APIs for event submission and dashboard data
- JMS-based agent pipeline (event-driven architecture)

## Prerequisites

- Java 17+ (you have Java 18)
- Gradle (you have Gradle 9.3)

That's it. No Docker, no external database, no message queue license.

## How to Run

```bash
cd regulith-springboot
gradle bootRun
```

Server starts at: http://localhost:9090

## Architecture

```
  HTTP Request (code commit event)
       |
       v
  [REST Controller] -- saves to H2 DB
       |
       v
  [JMS Queue: "compliance-events"] -- ActiveMQ (embedded)
       |
       v
  [Chain Reactor Agent] -- listens to queue, analyzes cross-domain impact
       |
       v
  [Audit Narrator Agent] -- auto-generates evidence narrative
       |
       v
  [H2 Database] -- stores results, narratives, updated scores
       |
       v
  [Dashboard API] -- serves data to frontend
```

## API Endpoints

### Submit Events
- POST /api/events — Submit any compliance event
- POST /api/events/simulate/code-commit — Simulate a mortgage rate code change
- POST /api/events/simulate/infra-change — Simulate an infra change (GDPR violation)

### Query Data
- GET /api/twins — All client engagement compliance twins
- GET /api/twins/{id} — Single client twin
- GET /api/chain-reactions/{id} — Chain reaction results for a client
- GET /api/narratives/{id} — Audit narratives for a client
- GET /api/events/{id} — Event history for a client
- GET /api/summary — Aggregate metrics

### Utilities
- GET /h2-console — Database console (see raw data)
- GET /actuator/health — Health check

## Demo Flow

1. Start the app: `gradle bootRun`
2. Simulate a code commit:
   ```bash
   curl -X POST http://localhost:9090/api/events/simulate/code-commit
   ```
3. Check the chain reaction:
   ```bash
   curl http://localhost:9090/api/chain-reactions/ENG-001
   ```
4. View the auto-generated audit narrative:
   ```bash
   curl http://localhost:9090/api/narratives/ENG-001
   ```
5. See the updated compliance twin:
   ```bash
   curl http://localhost:9090/api/twins/ENG-001
   ```

## What This Demonstrates

- Event-driven architecture (JMS message queue)
- Multi-agent pipeline (Chain Reactor -> Audit Narrator)
- Persistent storage (H2 relational database)
- RESTful APIs (Spring Boot)
- Enterprise patterns (Repository, Service, Controller layers)
- Zero external dependencies (everything embedded)

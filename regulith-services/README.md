# KAVACH Services — Monorepo with Selective Deployment

## Architecture

```
regulith-services/          (one repository)
├── alpha-service/          (Event Ingestion & Routing — port 8081)
├── beta-service/           (Compliance Intelligence — port 8082)
├── gamma-service/          (Audit Evidence & Dashboard — port 8083)
├── Jenkinsfile             (Selective deployment pipeline)
├── build.gradle            (Root build — shared config)
└── settings.gradle         (Includes all 3 subprojects)
```

## Selective Deployment

The Jenkins pipeline provides parameterized builds:
- Checkbox: Deploy Alpha? Deploy Beta? Deploy Gamma?
- Dropdown: Environment (dev / staging / production)
- Toggle: Run KAVACH compliance check before deploy?

You can deploy any combination: just Alpha, Alpha + Gamma, all three, etc.

## Build Commands

```bash
# Build all services
./gradlew build

# Build only alpha
./gradlew :alpha-service:build

# Build only beta
./gradlew :beta-service:build

# Build only gamma
./gradlew :gamma-service:build

# Run individual service
./gradlew :alpha-service:bootRun
./gradlew :beta-service:bootRun
./gradlew :gamma-service:bootRun
```

## Service Roles

| Service | Port | Role |
|---------|------|------|
| Alpha | 8081 | Event ingestion (Git, Jenkins, Jira, Docker, AWS webhooks) |
| Beta | 8082 | Compliance intelligence (Knowledge Graph, GraphRAG, Policy Engine) |
| Gamma | 8083 | Audit evidence (Narrator Agent, Dashboard, Reporting) |

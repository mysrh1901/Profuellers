# Compliance Configs — External Policy Configuration

## What This Is

These YAML files define compliance policies **externally** — not hardcoded in Java. The Policy Engine reads these configs and enforces them.

## Architecture

```
compliance-configs/
├── sox-itgc.yml          # SOX IT General Controls (7 controls)
├── pci-dss.yml           # PCI-DSS v4.0 (8 controls)
├── tila-ecoa.yml         # TILA/Reg Z + ECOA/Reg B (7 controls)
├── contractual-msa.yml   # Client MSA/SLA (7 controls)
└── README.md             # This file
```

## How It Works

1. **Policy Engine loads these files at startup** (or reloads on API call)
2. **Each control has a `validation_rule`** — a logical expression checked against the event
3. **Each control has commented `# production_api`** — the real API call to make in production
4. **To go live:** Uncomment the `api_integration` section and add real credentials

## Per-Control Structure

```yaml
- id: ITGC-CM-01                          # Unique control ID
  domain: CHANGE_MANAGEMENT               # Compliance domain
  name: "Normal Change Approval"          # Human-readable name
  description: "..."                      # What this control requires
  trigger:
    event_types: [CODE_COMMIT, DEPLOYMENT] # Which events trigger this check
    condition: "..."                       # When to evaluate
  severity: HIGH                          # CRITICAL/HIGH/MEDIUM/LOW
  blocking: true                          # Should it block deployment?
  sla: "Before deployment"               # Time requirement
  evidence_required: [...]               # What auditor needs to see
  validation_rule: "..."                  # Logic to check (mock in POC)
  # production_api: GET /api/...          # Real API to call in production
```

## Mock vs Production

| POC (Now) | Production (Future) |
|-----------|-------------------|
| Policies loaded from YAML | Same YAML + real API calls |
| Validation via heuristic scanner | Validation via real tool APIs |
| `validation_rule` checked locally | `production_api` called remotely |
| Evidence simulated | Evidence from ServiceNow/Jira/Git |

## To Add a New Framework

1. Create `new-framework.yml` in this folder
2. Follow the same YAML structure
3. Restart the service OR call `/api/controls/reload`
4. New policies are immediately enforced

## Why This Matters

- **Not hardcoded** — policies are config, not code
- **Client-specific** — each engagement can have its own MSA config
- **Auditable** — YAML files are version-controlled in Git
- **Extensible** — add HIPAA, DORA, HITRUST by dropping a new YAML file
- **Production-ready** — just uncomment the API integration lines and add credentials

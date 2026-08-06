# Sample Mortgage Application
# Used to demonstrate KAVACH AI Git integration

This is a sample mortgage origination app. When you commit code here,
the Git post-commit hook automatically sends events to KAVACH AI
for cross-domain compliance analysis.

## Try these commits to trigger different agents:

1. Modify rate_calculator.py -> triggers SOX + TILA + Security
2. Modify borrower_eligibility.py -> triggers Fair Lending + PII + SOX
3. Add a hardcoded password -> triggers Security (SAST finding)

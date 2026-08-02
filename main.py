#!/usr/bin/env python3
"""
╔══════════════════════════════════════════════════════════════════════════╗
║                                                                          ║
║   🛡️ COMPLIANCE TWIN                                                     ║
║   Delivery Compliance Digital Twin Platform                              ║
║                                                                          ║
║   Autonomous Security, Compliance & Audit Intelligence                   ║
║   For: Hexaware Agentic Arena Competition 2026                          ║
║                                                                          ║
╚══════════════════════════════════════════════════════════════════════════╝

An Agentic AI platform that creates real-time compliance digital twins
for each client engagement. It reasons across security, SOX, regulatory,
and contractual domains — predicting compliance impact before code ships.

Usage:
    python3 main.py              # Run full demo
    python3 main.py --quick      # Run quick summary only
    python3 main.py --scenario   # Run specific scenario

Author: Hexaware Profuellers Team
Competition: Agentic Arena 2026
Category: Autonomous Security, Compliance & Audit Intelligence
"""

import sys
import os

# Add project root to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from agents.orchestrator import ComplianceTwinOrchestrator


def main():
    """Entry point for Compliance Twin platform demo."""

    args = sys.argv[1:] if len(sys.argv) > 1 else []

    orchestrator = ComplianceTwinOrchestrator()

    if "--quick" in args:
        run_quick_demo(orchestrator)
    elif "--scenario" in args:
        run_scenario_menu(orchestrator)
    else:
        # Full demo
        orchestrator.run_full_demo()


def run_quick_demo(orchestrator):
    """Run a condensed version showing key capabilities."""

    print("""
╔══════════════════════════════════════════════════════════════════════════╗
║   🛡️ COMPLIANCE TWIN — Quick Demo                                       ║
╚══════════════════════════════════════════════════════════════════════════╝
""")
    from simulators.mock_data import get_mock_clients, get_mock_code_change

    # Build twins
    clients = get_mock_clients()
    twins = []
    for client in clients:
        twin = orchestrator.twin_agent.build_twin(client)
        twins.append(twin)

    # Show command center
    orchestrator._print_multi_client_dashboard(twins)

    # Chain reaction
    code_change = get_mock_code_change()
    reaction = orchestrator.reactor_agent.analyze_code_change(code_change, clients[0])
    print(orchestrator.reactor_agent.format_chain_reaction(reaction))


def run_scenario_menu(orchestrator):
    """Run specific scenarios for targeted demonstration."""

    print("""
╔══════════════════════════════════════════════════════════════════════════╗
║   🛡️ COMPLIANCE TWIN — Scenario Selection                               ║
╠══════════════════════════════════════════════════════════════════════════╣
║                                                                          ║
║   [1] Build Digital Twins for all client engagements                    ║
║   [2] Cross-Domain Chain Reaction (code commit analysis)                ║
║   [3] Regulatory Change Propagation (new CFPB guidance)                 ║
║   [4] Autonomous Audit Narrative Generation                             ║
║   [5] Compliance Drift Detection                                        ║
║   [6] Inherited Risk Detection (cross-engagement)                       ║
║   [7] Contract Obligation Parsing                                       ║
║   [8] Full Demo (all scenarios)                                         ║
║                                                                          ║
╚══════════════════════════════════════════════════════════════════════════╝
""")

    from simulators.mock_data import get_mock_clients, get_mock_code_change

    # For non-interactive demo, run all
    print("  Running all scenarios sequentially...\n")
    orchestrator.run_full_demo()


if __name__ == "__main__":
    main()

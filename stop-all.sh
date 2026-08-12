#!/bin/bash
# KAVACH AI — Stop All Services
# Usage: ./stop-all.sh

echo "╔══════════════════════════════════════════════╗"
echo "║   KAVACH AI — Stopping All Services         ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# Stop Dashboard
echo "▸ Stopping Dashboard..."
if [ -f /tmp/kavach-dashboard.pid ]; then
    kill $(cat /tmp/kavach-dashboard.pid) 2>/dev/null
    rm -f /tmp/kavach-dashboard.pid
fi
pkill -f "python3 dashboard.py" 2>/dev/null
lsof -ti :8080 | xargs kill -9 2>/dev/null
echo "  Done"

# Stop Spring Boot
echo "▸ Stopping Spring Boot..."
if [ -f /tmp/kavach-spring.pid ]; then
    kill $(cat /tmp/kavach-spring.pid) 2>/dev/null
    rm -f /tmp/kavach-spring.pid
fi
pkill -f "gradlew bootRun" 2>/dev/null
pkill -f "kavach-ai" 2>/dev/null
lsof -ti :9090 | xargs kill -9 2>/dev/null
echo "  Done"

# Stop Ollama
echo "▸ Stopping Ollama LLM..."
if [ -f /tmp/kavach-ollama.pid ]; then
    kill $(cat /tmp/kavach-ollama.pid) 2>/dev/null
    rm -f /tmp/kavach-ollama.pid
fi
pkill -f "ollama serve" 2>/dev/null
echo "  Done"

# Clear Python cache
find "$(dirname "$0")" -name "__pycache__" -exec rm -rf {} + 2>/dev/null

echo ""
echo "╔══════════════════════════════════════════════╗"
echo "║   All Services Stopped                      ║"
echo "╚══════════════════════════════════════════════╝"

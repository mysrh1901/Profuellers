#!/bin/bash
# KAVACH AI — Start All Services
# Usage: ./start-all.sh

cd "$(dirname "$0")"

echo "╔══════════════════════════════════════════════╗"
echo "║   KAVACH AI — Starting All Services         ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# 0. Start Ollama LLM server (port 11434)
echo "▸ Starting Ollama LLM server (port 11434)..."
ollama serve > /tmp/kavach-ollama.log 2>&1 &
OLLAMA_PID=$!
echo "  PID: $OLLAMA_PID"
sleep 2

# 1. Start Spring Boot backend (port 9090)
echo "▸ Starting Spring Boot backend (port 9090)..."
cd regulith-springboot
./gradlew bootRun > /tmp/kavach-springboot.log 2>&1 &
SPRING_PID=$!
echo "  PID: $SPRING_PID"
cd ..

# 2. Start Dashboard UI (port 8080)
echo "▸ Starting Dashboard UI (port 8080)..."
python3 dashboard.py > /tmp/kavach-dashboard.log 2>&1 &
DASH_PID=$!
echo "  PID: $DASH_PID"

# Save PIDs for stop script
echo "$OLLAMA_PID" > /tmp/kavach-ollama.pid
echo "$SPRING_PID" > /tmp/kavach-spring.pid
echo "$DASH_PID" > /tmp/kavach-dashboard.pid

echo ""
echo "╔══════════════════════════════════════════════╗"
echo "║   All Services Started                      ║"
echo "║                                             ║"
echo "║   Ollama LLM:   http://localhost:11434      ║"
echo "║   Spring Boot:  http://localhost:9090       ║"
echo "║   Dashboard:    http://localhost:8080       ║"
echo "║   Pipeline Trace: http://localhost:8080/trace ║"
echo "║   H2 Console:   http://localhost:9090/h2-console ║"
echo "║                                             ║"
echo "║   Model: kavach-compliance-v1               ║"
echo "║                                             ║"
echo "║   Logs:                                     ║"
echo "║     /tmp/kavach-ollama.log                  ║"
echo "║     /tmp/kavach-springboot.log              ║"
echo "║     /tmp/kavach-dashboard.log               ║"
echo "║                                             ║"
echo "║   Stop all: ./stop-all.sh                   ║"
echo "╚══════════════════════════════════════════════╝"

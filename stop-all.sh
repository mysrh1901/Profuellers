#!/bin/bash
# Regulith AI — Stop All Services
# Usage: ./stop-all.sh

echo "╔══════════════════════════════════════════════╗"
echo "║   Regulith AI — Stopping All Services       ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# Stop Dashboard
echo "▸ Stopping Dashboard..."
if [ -f /tmp/regulith-dashboard.pid ]; then
    kill $(cat /tmp/regulith-dashboard.pid) 2>/dev/null
    rm -f /tmp/regulith-dashboard.pid
fi
pkill -f "python3 dashboard.py" 2>/dev/null
echo "  Done"

# Stop Spring Boot
echo "▸ Stopping Spring Boot..."
if [ -f /tmp/regulith-spring.pid ]; then
    kill $(cat /tmp/regulith-spring.pid) 2>/dev/null
    rm -f /tmp/regulith-spring.pid
fi
pkill -f "gradlew bootRun" 2>/dev/null
pkill -f "regulith-ai" 2>/dev/null
echo "  Done"

echo ""
echo "╔══════════════════════════════════════════════╗"
echo "║   All Services Stopped                      ║"
echo "╚══════════════════════════════════════════════╝"

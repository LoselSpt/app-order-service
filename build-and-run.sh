#!/usr/bin/env bash
# Sobe toda a stack (bancos + serviços).
# O build Maven acontece dentro dos containers, então só é necessário ter Docker.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

if docker compose version >/dev/null 2>&1; then
    COMPOSE="docker compose"
else
    COMPOSE="docker-compose"
fi

echo "=== Building and starting Order System ==="
$COMPOSE up --build -d

echo ""
echo "=== Services are up! ==="
echo "customer-service : http://localhost:8081"
echo "  Swagger UI     : http://localhost:8081/swagger-ui.html"
echo "order-service    : http://localhost:8082"
echo "  Swagger UI     : http://localhost:8082/swagger-ui.html"

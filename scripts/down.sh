#!/usr/bin/env bash
# Para a infraestrutura local. Use --volumes para apagar dados.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ "${1:-}" == "--volumes" || "${1:-}" == "-v" ]]; then
  echo "🗑️  Removendo containers E volumes (dados serão apagados)..."
  docker compose -f infra/docker-compose.yml down -v
else
  docker compose -f infra/docker-compose.yml down
fi

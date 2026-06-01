#!/usr/bin/env bash
# Sobe toda a infraestrutura local (Postgres, Redis, RabbitMQ, MailHog)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "⚠️  .env não encontrado — usando defaults. Crie um com: cp .env.example .env"
fi

docker compose -f infra/docker-compose.yml up -d
echo
docker compose -f infra/docker-compose.yml ps
echo
echo "✅ Infra no ar:"
echo "   RabbitMQ UI → http://localhost:${RABBITMQ_UI_PORT:-15672}"
echo "   MailHog UI  → http://localhost:${MAILHOG_UI_PORT:-8025}"

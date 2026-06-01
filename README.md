# rabbitride
Sistema de aluguel de carros event-driven com Spring Boot, microsserviços e RabbitMQ

## Configuração local

1. Copie as variáveis de ambiente:

```bash
   cp .env.example .env
```

2. (Opcional) ajuste credenciais e portas no `.env`.

3. Suba a infraestrutura:

```bash
   ./scripts/up.sh
```

Serviços e portas (defaults):

| Serviço   | Porta(s)       | UI / acesso                     |
|-----------|----------------|---------------------------------|
| Postgres  | 5432           | `psql -h localhost -U rabbitride` |
| Redis     | 6379           | `redis-cli`                     |
| RabbitMQ  | 5672, 15672    | http://localhost:15672          |
| MailHog   | 1025, 8025     | http://localhost:8025           |

**Parar:** `./scripts/down.sh`
**Limpar tudo (apaga dados):** `./scripts/down.sh --volumes`

⚠️ Se mudar `POSTGRES_PASSWORD` no `.env` depois de já ter subido, é necessário recriar o volume:
`./scripts/down.sh --volumes && ./scripts/up.sh`

#!/usr/bin/env bash
# =============================================================================
# RabbitRide — Setup automático no GitHub
# =============================================================================
# Cria o repositório, labels, milestones e issues do projeto RabbitRide.
#
# Pré-requisitos:
#   1. GitHub CLI instalado:  brew install gh
#   2. Autenticado:            gh auth login
#
# Uso:
#   chmod +x setup-github.sh
#   ./setup-github.sh
#
# Rode apenas UMA vez por repositório. Para refazer, delete o repo no GitHub
# (gh repo delete <user>/rabbitride --yes) e rode de novo.
# =============================================================================

set -euo pipefail

# ----------------------------------------------------------------------------
# Configuração — edite se quiser
# ----------------------------------------------------------------------------
REPO_NAME="${REPO_NAME:-rabbitride}"
REPO_DESCRIPTION="Sistema de aluguel de carros event-driven com Spring Boot, microsserviços e RabbitMQ"
VISIBILITY="${VISIBILITY:-public}"   # public ou private

# ----------------------------------------------------------------------------
# Pré-flight
# ----------------------------------------------------------------------------
command -v gh >/dev/null 2>&1 || {
  echo "❌ GitHub CLI (gh) não encontrado. Instale com: brew install gh"
  exit 1
}

gh auth status >/dev/null 2>&1 || {
  echo "❌ Não autenticado no GitHub CLI. Rode: gh auth login"
  exit 1
}

GITHUB_USER="${GITHUB_USER:-$(gh api user --jq .login)}"
REPO="$GITHUB_USER/$REPO_NAME"

echo "================================================================"
echo " RabbitRide — setup GitHub"
echo "================================================================"
echo " Repositório: $REPO"
echo " Visibilidade: $VISIBILITY"
echo "================================================================"
read -p "Continuar? [y/N] " -n 1 -r
echo
[[ $REPLY =~ ^[Yy]$ ]] || { echo "Cancelado."; exit 0; }

# ----------------------------------------------------------------------------
# 1. Cria repositório (se não existir)
# ----------------------------------------------------------------------------
if gh repo view "$REPO" >/dev/null 2>&1; then
  echo "ℹ️  Repositório $REPO já existe — pulando criação."
else
  echo "📦 Criando repositório $REPO..."
  gh repo create "$REPO" \
    --"$VISIBILITY" \
    --description "$REPO_DESCRIPTION" \
    --add-readme
fi

# ----------------------------------------------------------------------------
# 2. Labels
# ----------------------------------------------------------------------------
echo "🏷️  Criando labels..."
create_label() {
  gh label create "$1" --color "$2" --description "$3" --repo "$REPO" --force >/dev/null 2>&1 || true
  echo "   • $1"
}

create_label "infra"                "0e8a16" "Docker, CI, build, configuração de infraestrutura"
create_label "messaging"            "fbca04" "RabbitMQ, eventos, filas, DLQ"
create_label "security"             "d93f0b" "Autenticação, autorização, JWT"
create_label "docs"                 "5319e7" "Documentação"
create_label "tests"                "c5def5" "Testes unitários e de integração"
create_label "user-service"         "1d76db" "Microsserviço user-service"
create_label "car-service"          "1d76db" "Microsserviço car-service"
create_label "rental-service"       "1d76db" "Microsserviço rental-service"
create_label "analysis-service"     "1d76db" "Microsserviço analysis-service"
create_label "notification-service" "1d76db" "Microsserviço notification-service"

# ----------------------------------------------------------------------------
# 3. Helpers para milestones e issues
# ----------------------------------------------------------------------------
mk_milestone() {
  local title="$1"
  local description="$2"
  gh api "repos/$REPO/milestones" \
    -f title="$title" \
    -f description="$description" \
    -f state="open" \
    --jq '.number'
}

# Lê o body da issue via stdin (heredoc).
mk_issue() {
  local milestone="$1"
  local labels="$2"
  local title="$3"
  local body
  body=$(cat)
  gh issue create \
    --repo "$REPO" \
    --title "$title" \
    --body "$body" \
    --milestone "$milestone" \
    --label "$labels" >/dev/null
  printf "   ✓ %s\n" "$title"
}

# ----------------------------------------------------------------------------
# 4. Milestones + Issues
# ----------------------------------------------------------------------------

# ============================================================================
# M1 — Infraestrutura & Docker Compose
# ============================================================================
echo "🎯 Milestone 1 — Infraestrutura & Docker Compose"
M1=$(mk_milestone "M1 — Infraestrutura & Docker Compose" \
  "Estrutura do mono-repo, parent POM, Docker Compose com Postgres, Redis, RabbitMQ e MailHog, CI básico.")

mk_issue "$M1" "infra" "Estrutura inicial do mono-repo" <<'EOF'
**Objetivo:** Criar a estrutura de pastas do mono-repo.

**Critérios de aceite:**
- [ ] Pasta raiz com `README.md` mínimo descrevendo o projeto
- [ ] `services/` para os 4 microsserviços (`user-service`, `car-service`, `rental-service`, `analysis-service`, `notification-service`)
- [ ] `commons/` para módulo compartilhado (configuração AMQP, DTOs de eventos)
- [ ] `infra/` para `docker-compose.yml` e scripts de bootstrap

**Notas:** Mono-repo facilita compartilhar o módulo `commons` e padronizar versões de dependências.
EOF

mk_issue "$M1" "infra" "Parent POM Maven com módulos" <<'EOF'
**Objetivo:** Configurar `pom.xml` raiz que agrega todos os módulos.

**Critérios de aceite:**
- [ ] `parent` POM com Spring Boot 3.x e Java 17
- [ ] Lista de `<modules>` com `commons` + 5 serviços
- [ ] `dependencyManagement` centralizando versões (Lombok, MapStruct, Testcontainers)
- [ ] `mvn -pl services/user-service -am package` funciona
EOF

mk_issue "$M1" "infra" "docker-compose.yml — Postgres, Redis, RabbitMQ, MailHog" <<'EOF'
**Objetivo:** Subir toda a infraestrutura local com um comando.

**Critérios de aceite:**
- [ ] Postgres na porta 5432 com volume nomeado
- [ ] Script de init criando databases separados por serviço (`user_db`, `car_db`, `rental_db`, `analysis_db`, `notification_db`)
- [ ] Redis na porta 6379
- [ ] RabbitMQ (`rabbitmq:3-management`) nas portas 5672 e 15672 (UI)
- [ ] MailHog nas portas 1025 (SMTP) e 8025 (UI)
- [ ] Healthchecks em todos os serviços
- [ ] `docker compose up -d` sobe tudo limpo
EOF

mk_issue "$M1" "infra" ".gitignore, .editorconfig, .env.example, scripts utilitários" <<'EOF'
**Objetivo:** Higiene do repositório.

**Critérios de aceite:**
- [ ] `.gitignore` para Java + Maven + IDEs (IntelliJ, VSCode)
- [ ] `.editorconfig` com indentação consistente (4 espaços Java, 2 YAML)
- [ ] `.env.example` listando variáveis esperadas (JWT secret, credenciais DB, etc)
- [ ] `scripts/up.sh` e `scripts/down.sh` para conveniência
EOF

mk_issue "$M1" "infra,tests" "GitHub Actions: build e testes em PR" <<'EOF'
**Objetivo:** CI básico rodando em cada PR.

**Critérios de aceite:**
- [ ] Workflow `.github/workflows/ci.yml` disparado em PR para `main`
- [ ] Job que roda `mvn verify` em cada módulo
- [ ] Cache de dependências Maven
- [ ] Badge de build no README

**Por que vale:** mostra ao recrutador que você se preocupa com qualidade desde o início.
EOF

# ============================================================================
# M2 — User Service (auth)
# ============================================================================
echo "🎯 Milestone 2 — User Service"
M2=$(mk_milestone "M2 — User Service (auth + JWT)" \
  "Cadastro, login e emissão de JWT stateless. Base de autenticação para os demais serviços.")

mk_issue "$M2" "user-service,infra" "Bootstrap do user-service" <<'EOF'
**Objetivo:** Esqueleto do serviço com Spring Boot, Postgres e Flyway.

**Critérios de aceite:**
- [ ] `pom.xml` com Spring Web, Spring Data JPA, Postgres driver, Flyway, Lombok, Validation
- [ ] `application.yml` apontando para `user_db`
- [ ] Dockerfile simples
- [ ] Aplicação sobe na porta 8081 e responde a `/actuator/health`
EOF

mk_issue "$M2" "user-service" "Entidade User + migration V1" <<'EOF'
**Objetivo:** Modelar usuário e criar tabela.

**Critérios de aceite:**
- [ ] Entidade `User` com: `id` (UUID), `nome`, `email` (único), `senha` (hash), `cpf` (único), `criadoEm`
- [ ] Migration Flyway `V1__create_users.sql`
- [ ] Repositório `UserRepository extends JpaRepository`
- [ ] Método `findByEmail`

**Notas:** senha sempre como hash BCrypt. CPF útil para o `analysis-service` simular regra de análise.
EOF

mk_issue "$M2" "user-service" "POST /auth/register com Bean Validation" <<'EOF'
**Objetivo:** Endpoint público de cadastro.

**Critérios de aceite:**
- [ ] DTO `RegisterRequest` com `@NotBlank`, `@Email`, `@CPF` (Hibernate Validator), `@Size` na senha
- [ ] Service criptografa senha com BCrypt
- [ ] Retorna 201 com `id` e `email`
- [ ] 409 se email/CPF já existir
EOF

mk_issue "$M2" "user-service,security" "Spring Security + JWT stateless" <<'EOF'
**Objetivo:** Configurar filtro JWT e autenticação stateless.

**Critérios de aceite:**
- [ ] `SecurityConfig` com `SessionCreationPolicy.STATELESS`
- [ ] `JwtTokenProvider` que gera e valida tokens (HS256, secret via env)
- [ ] `JwtAuthenticationFilter` no `OncePerRequestFilter`
- [ ] Rotas `/auth/**` e `/actuator/**` públicas; resto autenticado

**Notas:** Use a mesma estrutura de validação em todos os serviços — vou extrair para `commons` em milestone futura.
EOF

mk_issue "$M2" "user-service,security" "POST /auth/login retornando JWT" <<'EOF'
**Objetivo:** Login com email + senha.

**Critérios de aceite:**
- [ ] DTO `LoginRequest` com email e senha
- [ ] Service valida senha com BCrypt
- [ ] Retorna `{ token, expiresIn, userId }`
- [ ] 401 com mensagem genérica em caso de falha (não vaza se foi email ou senha)
EOF

mk_issue "$M2" "user-service,docs" "Tratamento global de exceções (RFC 7807)" <<'EOF'
**Objetivo:** Respostas de erro padronizadas.

**Critérios de aceite:**
- [ ] `@RestControllerAdvice` com handlers para `MethodArgumentNotValidException`, `ResponseStatusException`, `Exception`
- [ ] Resposta no formato `ProblemDetail` (RFC 7807)
- [ ] Erros de validação listam cada campo + mensagem

**Por que vale:** RFC 7807 é padrão moderno do Spring 6 — bom argumento em entrevista.
EOF

mk_issue "$M2" "user-service,tests" "Testes unitários e de integração" <<'EOF'
**Objetivo:** Cobertura mínima de testes.

**Critérios de aceite:**
- [ ] Testes unitários para `UserService` (mockando repository)
- [ ] Teste de integração do fluxo register → login com Testcontainers (Postgres)
- [ ] Teste de filtro JWT (token válido, inválido, expirado)
EOF

# ============================================================================
# M3 — Car Service
# ============================================================================
echo "🎯 Milestone 3 — Car Service"
M3=$(mk_milestone "M3 — Car Service (catálogo + Redis)" \
  "Catálogo de carros com cache Redis e endpoints internos de reserva/liberação.")

mk_issue "$M3" "car-service,infra" "Bootstrap do car-service" <<'EOF'
**Objetivo:** Esqueleto do serviço.

**Critérios de aceite:**
- [ ] Dependências: Spring Web, JPA, Postgres, Flyway, Spring Data Redis, Validation
- [ ] Aponta para `car_db` e Redis local
- [ ] Sobe na porta 8082 + `/actuator/health`
EOF

mk_issue "$M3" "car-service" "Entidade Carro + enum StatusCarro com descricao()" <<'EOF'
**Objetivo:** Modelar carro do catálogo.

**Critérios de aceite:**
- [ ] Entidade `Carro` com: `id`, `placa` (único), `modelo`, `marca`, `cor`, `ano`, `valorDiaria`, `status`, `versao` (`@Version` para optimistic locking)
- [ ] Enum `StatusCarro { DISPONIVEL, RESERVADO, ALUGADO, MANUTENCAO }` com método `descricao()` retornando label legível
- [ ] Repositório com `findByStatus` e `findByPlaca`

**Notas:** `@Version` é crítico — evita race condition se duas reservas chegarem ao mesmo carro simultaneamente.
EOF

mk_issue "$M3" "car-service" "Migration + seed de 15 carros" <<'EOF'
**Objetivo:** Dados iniciais para testes manuais.

**Critérios de aceite:**
- [ ] `V1__create_carros.sql` com a estrutura
- [ ] `V2__seed_carros.sql` com 15 carros variados (marcas, cores, anos, valores diferentes)
- [ ] Pelo menos 1 carro em cada status (não só `DISPONIVEL`)
EOF

mk_issue "$M3" "car-service" "GET /cars com cache Redis" <<'EOF'
**Objetivo:** Listar carros disponíveis com cache.

**Critérios de aceite:**
- [ ] `GET /cars?status=DISPONIVEL` retorna lista paginada
- [ ] `GET /cars/{id}` retorna detalhe
- [ ] `@Cacheable("cars")` nos métodos de leitura
- [ ] `@CacheEvict` nos endpoints internos de reserva/liberação
- [ ] TTL do cache configurável (default 5 min)

**Notas:** documentar no README a estratégia de invalidação — esse tipo de detalhe impressiona tech lead.
EOF

mk_issue "$M3" "car-service,security" "CRUD admin + endpoints internos de reserva/liberação" <<'EOF'
**Objetivo:** Operações administrativas e endpoints chamados pelo rental-service.

**Critérios de aceite:**
- [ ] `POST /cars`, `PUT /cars/{id}`, `DELETE /cars/{id}` protegidos por role `ADMIN`
- [ ] `PATCH /internal/cars/{id}/reserve` muda status `DISPONIVEL → RESERVADO` (409 se não disponível)
- [ ] `PATCH /internal/cars/{id}/release` muda status `RESERVADO → DISPONIVEL`
- [ ] Endpoints internos protegidos por header `X-Internal-Token` (segredo compartilhado entre serviços)

**Notas:** evite expor /internal publicamente — em produção iria atrás de um service mesh.
EOF

mk_issue "$M3" "car-service,tests" "Testes do car-service" <<'EOF'
**Objetivo:** Cobertura básica.

**Critérios de aceite:**
- [ ] Teste unitário do `CarroService` para reserva (sucesso e falha por status)
- [ ] Teste de integração do cache Redis com Testcontainers
- [ ] Teste de concorrência: 2 reservas simultâneas no mesmo carro — só uma vence (`@Version`)
EOF

# ============================================================================
# M4 — RabbitMQ: topologia base
# ============================================================================
echo "🎯 Milestone 4 — RabbitMQ: topologia base"
M4=$(mk_milestone "M4 — RabbitMQ: topologia base" \
  "Módulo commons com configuração de exchanges, queues, bindings e DLQ compartilhada por todos os serviços.")

mk_issue "$M4" "messaging,infra" "Módulo commons com configuração AMQP" <<'EOF'
**Objetivo:** Centralizar a configuração de mensageria.

**Critérios de aceite:**
- [ ] Módulo Maven `commons` com `spring-boot-starter-amqp`
- [ ] Pacote `events` com DTOs: `RentalRequestedEvent`, `AnalysisCompletedEvent`, `RentalConfirmedEvent`, `RentalFailedEvent`
- [ ] Pacote `messaging` com constantes (`Exchanges`, `Queues`, `RoutingKeys`)
- [ ] `MessageConverter` Jackson com `ObjectMapper` que suporta `java.time`

**Notas:** todo evento carrega `eventId` (UUID), `occurredAt` (Instant) e `rentalId` para idempotência.
EOF

mk_issue "$M4" "messaging" "Declarar exchange rental.exchange (topic)" <<'EOF'
**Objetivo:** Exchange principal do sistema.

**Critérios de aceite:**
- [ ] Bean `TopicExchange("rental.exchange", durable=true, autoDelete=false)`
- [ ] Declaração ativa via `RabbitAdmin` em todos os serviços que publicam ou consomem
- [ ] Constante `Exchanges.RENTAL = "rental.exchange"` em `commons`
EOF

mk_issue "$M4" "messaging" "Declarar queues + bindings com routing keys" <<'EOF'
**Objetivo:** Roteamento dos eventos.

**Critérios de aceite:**
- [ ] Queue `analysis.requested.queue` ← routing key `rental.requested`
- [ ] Queue `rental.analysis.completed.queue` ← routing key `rental.analysis.completed`
- [ ] Queue `notification.queue` ← routing keys `rental.confirmed` E `rental.failed`
- [ ] Todas as queues durables, com argumento `x-dead-letter-exchange = rental.dlx`
- [ ] Constantes em `commons` (`Queues.*`, `RoutingKeys.*`)

**Notas:** topic exchange permite adicionar novos consumers depois sem mexer nos publishers — fundamento de event-driven.
EOF

mk_issue "$M4" "messaging" "Configurar DLX e DLQ" <<'EOF'
**Objetivo:** Mensagens que falham N vezes vão para a DLQ.

**Critérios de aceite:**
- [ ] Exchange `rental.dlx` (topic)
- [ ] Queue `rental.dlq` bindada a `#` (captura tudo)
- [ ] `SimpleRetryPolicy` com 3 tentativas e backoff exponencial (1s, 3s, 9s)
- [ ] Após esgotar retries, mensagem vai para `rental.dlq` com headers preservados
- [ ] Documentar no README como consumir a DLQ manualmente

**Por que vale:** DLQ é a primeira pergunta de tech lead sobre filas. Tê-la funcionando bota você em outro patamar.
EOF

mk_issue "$M4" "messaging,docs" "Documentar topologia no README com Mermaid" <<'EOF'
**Objetivo:** Diagrama claro da topologia.

**Critérios de aceite:**
- [ ] Diagrama Mermaid no `README.md` mostrando exchange → queues → consumers
- [ ] Tabela de eventos com `routing key`, `publisher`, `consumer(s)`, `payload resumido`
- [ ] Seção explicando DLQ e retry
EOF

# ============================================================================
# M5 — Rental Service (orquestrador)
# ============================================================================
echo "🎯 Milestone 5 — Rental Service"
M5=$(mk_milestone "M5 — Rental Service (orquestrador)" \
  "Serviço central que recebe a solicitação, publica eventos, consome resultado da análise e coordena a reserva do carro.")

mk_issue "$M5" "rental-service,infra" "Bootstrap do rental-service" <<'EOF'
**Objetivo:** Esqueleto do orquestrador.

**Critérios de aceite:**
- [ ] Dependências: Web, JPA, Postgres, Flyway, Spring Security (validar JWT), AMQP, OpenFeign
- [ ] Importa módulo `commons`
- [ ] Aponta para `rental_db`
- [ ] Sobe na porta 8083 + `/actuator/health`
EOF

mk_issue "$M5" "rental-service" "Entidade Rental + StatusRental + migration" <<'EOF'
**Objetivo:** Modelar o aluguel.

**Critérios de aceite:**
- [ ] Entidade `Rental`: `id`, `userId`, `userEmail`, `carroId`, `status`, `motivoFalha`, `criadoEm`, `atualizadoEm`
- [ ] Enum `StatusRental { PENDENTE, EM_ANALISE, APROVADO, REJEITADO, CONFIRMADO, FALHOU }` com `descricao()`
- [ ] Migration `V1__create_rentals.sql`
- [ ] Repositório com `findByUserId` paginado

**Notas:** guardar `userEmail` na própria tabela evita ter que chamar user-service no consumer de notification.
EOF

mk_issue "$M5" "rental-service,messaging" "POST /rentals publica RentalRequested" <<'EOF'
**Objetivo:** Endpoint que inicia o fluxo.

**Critérios de aceite:**
- [ ] `POST /rentals { carroId }` autenticado (extrai `userId` do JWT)
- [ ] Salva `Rental` com status `PENDENTE` e publica `RentalRequested` em transação
- [ ] Retorna 202 Accepted com `rentalId` e status
- [ ] Validação: carroId obrigatório, usuário não pode ter rental ativo

**Notas:** persistência + publicação em transação é a base do *transactional outbox* — versão simplificada por enquanto.
EOF

mk_issue "$M5" "rental-service" "OpenFeign client tipado para car-service" <<'EOF'
**Objetivo:** Comunicação síncrona para reservar o carro.

**Critérios de aceite:**
- [ ] `@FeignClient("car-service")` com métodos `reservar(carroId)` e `liberar(carroId)`
- [ ] Header `X-Internal-Token` injetado via `RequestInterceptor`
- [ ] `ErrorDecoder` customizado mapeando 409 para exceção tipada `CarroIndisponivelException`
- [ ] Timeout configurável (default 3s)

**Por que vale:** mostrar que você sabe quando usar sync (precisa de resposta imediata, vai abortar transação) vs async (pode esperar).
EOF

mk_issue "$M5" "rental-service,messaging" "Consumer de AnalysisCompleted" <<'EOF'
**Objetivo:** Reagir ao resultado da análise.

**Critérios de aceite:**
- [ ] `@RabbitListener` na queue `rental.analysis.completed.queue`
- [ ] Se `REJECTED`: atualiza status para `REJEITADO`, publica `RentalFailed`
- [ ] Se `APPROVED`: chama car-service para reservar
  - sucesso → status `CONFIRMADO`, publica `RentalConfirmed`
  - falha (carro já reservado) → status `FALHOU`, publica `RentalFailed`
- [ ] Toda transição grava `atualizadoEm`
EOF

mk_issue "$M5" "rental-service,messaging" "Idempotência via tabela processed_event" <<'EOF'
**Objetivo:** Consumer não pode processar o mesmo evento duas vezes.

**Critérios de aceite:**
- [ ] Tabela `processed_event(event_id PK, consumer, processed_at)`
- [ ] Antes de processar, verifica se `eventId` já está na tabela
- [ ] Se já existe: faz ACK e ignora
- [ ] Insere `eventId` na mesma transação do efeito colateral

**Notas:** RabbitMQ garante *at least once*, não *exactly once*. Idempotência é responsabilidade do consumer. Esse é o conceito #1 que separa júnior de pleno em entrevista de mensageria.
EOF

mk_issue "$M5" "rental-service,tests" "Testes do rental-service" <<'EOF'
**Objetivo:** Cobrir os caminhos críticos.

**Critérios de aceite:**
- [ ] Teste unitário do orquestrador para os 3 cenários (reject, approve+sucesso, approve+falha de reserva)
- [ ] Teste de integração com Testcontainers (Postgres + RabbitMQ)
- [ ] Teste de idempotência: enviar mesmo evento 2x e verificar que só processa 1x
EOF

# ============================================================================
# M6 — Analysis Service
# ============================================================================
echo "🎯 Milestone 6 — Analysis Service"
M6=$(mk_milestone "M6 — Analysis Service" \
  "Consumer da solicitação que faz análise simulada e publica resultado. Showcase de retry + DLQ.")

mk_issue "$M6" "analysis-service,infra" "Bootstrap do analysis-service" <<'EOF'
**Objetivo:** Esqueleto do serviço.

**Critérios de aceite:**
- [ ] Dependências: Web, JPA, Postgres, Flyway, AMQP
- [ ] Importa `commons`
- [ ] Aponta para `analysis_db`
- [ ] Sobe na porta 8084 + `/actuator/health`
EOF

mk_issue "$M6" "analysis-service,messaging" "Consumer de RentalRequested" <<'EOF'
**Objetivo:** Receber a solicitação de análise.

**Critérios de aceite:**
- [ ] `@RabbitListener` na queue `analysis.requested.queue`
- [ ] Deserializa `RentalRequestedEvent`
- [ ] Loga `rentalId` e `userId`
EOF

mk_issue "$M6" "analysis-service" "Lógica de análise simulada" <<'EOF'
**Objetivo:** Decidir aprovar ou rejeitar.

**Critérios de aceite:**
- [ ] Tabela `blacklist(cpf, motivo)` populada via seed com alguns CPFs
- [ ] Regra: se CPF está na blacklist → rejeita com motivo
- [ ] Senão: aprova
- [ ] (Opcional) latência artificial de 2-5s com `Thread.sleep` para simular processamento real

**Notas:** mantemos simples porque o foco é mensageria, não regra de negócio. O importante é o evento de resposta.
EOF

mk_issue "$M6" "analysis-service,messaging" "Publica AnalysisCompleted" <<'EOF'
**Objetivo:** Devolver o resultado da análise.

**Critérios de aceite:**
- [ ] Publica `AnalysisCompletedEvent` com routing key `rental.analysis.completed`
- [ ] Payload: `rentalId`, `resultado` (`APPROVED`/`REJECTED`), `motivo` (opcional), `eventId`, `occurredAt`
- [ ] Publicação ocorre na mesma transação da gravação no banco local (registro de auditoria)
EOF

mk_issue "$M6" "analysis-service,messaging" "Retry com backoff exponencial + DLQ" <<'EOF'
**Objetivo:** Tolerância a falhas transitórias.

**Critérios de aceite:**
- [ ] `application.yml` configurando `spring.rabbitmq.listener.simple.retry` (max-attempts 3, backoff inicial 1s, multiplier 3)
- [ ] Exception customizada `RetryableException` para erros que devem retentar
- [ ] `AmqpRejectAndDontRequeueException` para erros definitivos (vai direto pra DLQ)
- [ ] Teste manual: simular falha e verificar que após 3 tentativas mensagem aparece na DLQ
EOF

mk_issue "$M6" "analysis-service,messaging,tests" "Idempotência + testes" <<'EOF'
**Objetivo:** Garantir consistência.

**Critérios de aceite:**
- [ ] Tabela `processed_event` reutilizada
- [ ] Teste de idempotência: mesmo `RentalRequested` enviado 2x gera só 1 `AnalysisCompleted`
- [ ] Teste de integração end-to-end (publish → consume → publish) com Testcontainers
EOF

# ============================================================================
# M7 — Notification Service
# ============================================================================
echo "🎯 Milestone 7 — Notification Service"
M7=$(mk_milestone "M7 — Notification Service" \
  "Consumer que dispara e-mail ao usuário. Demonstra desacoplamento total — não conhece nenhum outro serviço.")

mk_issue "$M7" "notification-service,infra" "Bootstrap do notification-service" <<'EOF'
**Objetivo:** Esqueleto do serviço.

**Critérios de aceite:**
- [ ] Dependências: AMQP, Spring Mail, Thymeleaf (templates), JPA + Postgres (só para idempotência)
- [ ] Importa `commons`
- [ ] Sobe na porta 8085 + `/actuator/health`
EOF

mk_issue "$M7" "notification-service,messaging" "Consumer de RentalConfirmed" <<'EOF'
**Objetivo:** Disparar e-mail de confirmação.

**Critérios de aceite:**
- [ ] `@RabbitListener` na queue `notification.queue` com filtro por routing key `rental.confirmed`
- [ ] Extrai `userEmail` e `rentalId` do evento
- [ ] Aciona service de envio de e-mail
EOF

mk_issue "$M7" "notification-service,messaging" "Consumer de RentalFailed" <<'EOF'
**Objetivo:** Notificar quando o aluguel falhou.

**Critérios de aceite:**
- [ ] Mesma queue, routing key `rental.failed`
- [ ] Inclui `motivo` da falha no e-mail
- [ ] Logs diferenciados de confirmado vs falhou
EOF

mk_issue "$M7" "notification-service" "JavaMailSender configurado para MailHog" <<'EOF'
**Objetivo:** Envio real (em ambiente local).

**Critérios de aceite:**
- [ ] `spring.mail.host=mailhog` e porta 1025
- [ ] `JavaMailSender` injetado no service
- [ ] E-mails aparecem na UI do MailHog em `http://localhost:8025`
EOF

mk_issue "$M7" "notification-service" "Templates de e-mail com Thymeleaf" <<'EOF'
**Objetivo:** E-mails com formatação decente.

**Critérios de aceite:**
- [ ] Template `rental-confirmed.html` com `${userName}`, `${rentalId}`, `${carroModelo}`
- [ ] Template `rental-failed.html` com `${motivo}`
- [ ] Service renderiza template com `TemplateEngine`
- [ ] E-mail enviado em HTML (`setText(html, true)`)

**Notas:** mesmo sem CSS sofisticado, ter HTML > texto puro. Recrutador valoriza.
EOF

mk_issue "$M7" "notification-service,messaging,tests" "Idempotência + testes com Greenmail" <<'EOF'
**Objetivo:** Não enviar e-mail duplicado.

**Critérios de aceite:**
- [ ] Tabela `processed_event` no `notification_db`
- [ ] Teste de integração com Greenmail (servidor SMTP em memória)
- [ ] Verificar que mesmo `RentalConfirmed` recebido 2x dispara só 1 e-mail
- [ ] Teste do conteúdo HTML do e-mail
EOF

# ============================================================================
# M8 — Documentação & demo
# ============================================================================
echo "🎯 Milestone 8 — Documentação & demo"
M8=$(mk_milestone "M8 — Documentação & demo" \
  "README mestre, Swagger, Postman collection, vídeo de demo e release v1.0.0.")

mk_issue "$M8" "docs" "README mestre com diagrama Mermaid" <<'EOF'
**Objetivo:** Landing page do projeto.

**Critérios de aceite:**
- [ ] Visão geral do que é RabbitRide e por que existe
- [ ] Diagrama Mermaid de arquitetura (5 serviços + RabbitMQ + bancos)
- [ ] Diagrama Mermaid do fluxo de eventos
- [ ] Tabela com porta, banco e responsabilidade de cada serviço
- [ ] Instruções claras: `docker compose up -d` + como rodar cada serviço
- [ ] Stack list com links

**Por que vale:** recrutador olha o README primeiro. Um README excelente vale mais que 100 commits.
EOF

mk_issue "$M8" "docs" "Swagger/OpenAPI em cada serviço" <<'EOF'
**Objetivo:** Documentação interativa das APIs.

**Critérios de aceite:**
- [ ] `springdoc-openapi-starter-webmvc-ui` em cada serviço
- [ ] `/swagger-ui.html` acessível em cada um
- [ ] DTOs anotados com `@Schema(description=...)`
- [ ] Endpoints anotados com `@Operation` e `@ApiResponse`
EOF

mk_issue "$M8" "docs" "Postman/Insomnia collection" <<'EOF'
**Objetivo:** Facilitar testes manuais.

**Critérios de aceite:**
- [ ] Collection cobrindo: register, login, criar rental, consultar rental, listar carros
- [ ] Variável de ambiente para token JWT (capturado automaticamente do login)
- [ ] Arquivo commitado em `docs/postman/`
EOF

mk_issue "$M8" "docs,messaging" "Documentar topologia RabbitMQ e DLQ" <<'EOF'
**Objetivo:** Página dedicada à mensageria.

**Critérios de aceite:**
- [ ] `docs/messaging.md` com diagrama de exchanges e queues
- [ ] Lista completa de eventos com publisher e consumer
- [ ] Seção sobre idempotência (por que e como)
- [ ] Seção sobre DLQ (como inspecionar, como reprocessar)
EOF

mk_issue "$M8" "docs" "Tag v1.0.0 + release notes" <<'EOF'
**Objetivo:** Marco do MVP.

**Critérios de aceite:**
- [ ] Quando todas as outras milestones estiverem fechadas: criar tag `v1.0.0`
- [ ] Release no GitHub com notas listando funcionalidades, stack e como rodar
- [ ] (Opcional) GIF de demo no release

**Como gerar:** `gh release create v1.0.0 --generate-notes`
EOF

# ----------------------------------------------------------------------------
# 5. Resumo final
# ----------------------------------------------------------------------------
echo
echo "================================================================"
echo " ✅ RabbitRide pronto no GitHub!"
echo "================================================================"
echo " Repositório: https://github.com/$REPO"
echo " Milestones:  https://github.com/$REPO/milestones"
echo " Issues:      https://github.com/$REPO/issues"
echo "================================================================"
echo " Próximo passo sugerido:"
echo "   git clone https://github.com/$REPO.git"
echo "   cd $REPO_NAME"
echo "   # Começar pela primeira issue da M1"
echo "================================================================"

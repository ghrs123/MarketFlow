# MarketFlow Senior Java Cloud Lab — Instruction Principal Atualizada

> Documento atualizado para orientar a criação de um curso prático + aplicação real de estudo para Senior Java/Spring Boot, estruturado em fases incrementais, cada uma com uma funcionalidade completa, branch própria, documentação, testes e critérios de conclusão.

---

## 0. Princípio central do projeto

Este projeto não deve ser tratado como uma aplicação gerada de uma vez.  
Ele deve ser construído como um **curso prático incremental**, onde cada fase:

1. Entrega uma funcionalidade utilizável.
2. Pode ser executada localmente.
3. Pode ser testada.
4. Tem documentação própria.
5. Tem uma branch própria.
6. Tem commits semânticos.
7. Ensina conceitos técnicos relevantes.
8. Prepara conteúdo para entrevista Senior Java.
9. Mantém a aplicação funcional ao final da fase.
10. Não avança para tópicos futuros antes de fechar o objetivo atual.

A aplicação será chamada:

```text
MarketFlow Senior Java Cloud Lab
```

Ela simula um sistema financeiro/event-driven para processamento de ordens de compra/venda, inspirado em contextos bancários, microservices, mensageria, observabilidade, Kubernetes, cloud e performance.

---

## 1. Objetivo geral do curso

Criar uma aplicação Java 21 + Spring Boot que simule o processamento de ordens financeiras.

A aplicação deve evoluir de forma incremental até suportar:

- Criação de ordens via REST API.
- Validação de entrada.
- Tratamento profissional de erros.
- Persistência em PostgreSQL.
- JPA/Hibernate avançado.
- Transações.
- Histórico/auditoria.
- Estruturas de dados reais.
- Order book com prioridade de preço.
- Processamento assíncrono e concorrente.
- Simulação de mensagem estilo FIX Protocol.
- Event-driven architecture.
- Retry.
- Dead Letter Queue.
- Idempotência.
- Logs estruturados.
- Correlation ID.
- Métricas com Micrometer/Prometheus.
- Dashboards Grafana.
- Segurança com JWT/Keycloak.
- Resilience patterns.
- Cache.
- Scheduling.
- Async.
- Docker Compose.
- Kubernetes local.
- Testes de carga com k6.
- Preparação para cloud.
- CI/CD com GitHub Actions.
- Documentação profissional e guia de entrevista.

No final, eu devo conseguir explicar este projeto em entrevista como um exemplo real de:

```text
Senior Java Backend / Backend-focused Full Stack Engineering
```

---

## 2. Alinhamento com vagas reais e entrevista técnica

Este projeto deve preparar para vagas como:

- Senior Java Developer.
- Senior Backend Engineer.
- Backend-focused Full Stack Developer.
- Java + Angular Developer.
- Key Engineer.
- Java Microservices Developer.
- Banking/Financial Services Developer.
- EPAM / BNP Paribas style interview.

Tópicos esperados em entrevista:

### Java Core

- Collections.
- HashMap.
- ConcurrentHashMap.
- equals/hashCode.
- Streams.
- Optional.
- Exceptions.
- Immutability.
- Generics.
- Records.
- Multithreading.
- ExecutorService.
- CompletableFuture.
- Java Memory Model.
- volatile.
- synchronized.
- ReentrantLock.
- AtomicInteger.
- BlockingQueue.

### Spring Boot

- Dependency Injection.
- @Component.
- @Service.
- @Repository.
- @RestController.
- @Configuration.
- @Bean.
- @ConfigurationProperties.
- @Transactional.
- Validation.
- Exception Handling.
- Actuator.
- Profiles.

### JPA / Hibernate / SQL

- @Entity.
- @Table.
- @Id.
- @GeneratedValue.
- @Column.
- @ManyToOne.
- @OneToMany.
- Lazy vs Eager.
- N+1 problem.
- Fetch join.
- Cascade.
- Orphan removal.
- Dirty checking.
- First-level cache.
- Optimistic locking.
- Pessimistic locking.
- Transactions.
- Indexes.
- Joins.
- Query optimization.
- T-SQL / PL-SQL basics.

### Microservices

- Service boundaries.
- REST contracts.
- API versioning.
- Synchronous vs asynchronous communication.
- Database per service.
- Configuration.
- Fault tolerance.
- Resilience.
- Observability.
- Idempotency.
- Backpressure.

### Messaging / Event-driven

- Producer.
- Consumer.
- Queue.
- Topic.
- Kafka concepts.
- ActiveMQ/Artemis concepts.
- Retry.
- DLQ.
- Consumer group.
- Partition.
- Offset.
- Ordering.
- At-least-once.
- Duplicate messages.
- Idempotent consumers.

### DevOps / Cloud Native

- Docker.
- Docker Compose.
- Jenkins.
- GitHub Actions.
- Linux.
- Bash.
- Git.
- Kubernetes.
- OpenShift.
- Pod.
- Deployment.
- Service.
- ConfigMap.
- Secret.
- Ingress.
- Route.
- Readiness probe.
- Liveness probe.
- Requests/limits.
- HPA.
- Logs.
- Rollout.
- Rollback.

### Monitoring / Logs / Troubleshooting

- Structured logs.
- Correlation ID.
- MDC.
- Business logs.
- Technical logs.
- Audit logs.
- Error logs.
- Actuator.
- Micrometer.
- Prometheus.
- Grafana.
- Health checks.
- SLI.
- SLO.
- SLA.
- RED metrics.
- USE metrics.
- Troubleshooting production issues.

### Code quality / Security / AI tools

- Code review.
- Clean code.
- Testing.
- Sonar-style quality checks.
- Vulnerability awareness.
- OWASP basics.
- Secure logging.
- Dependency vulnerabilities.
- AI tools in SDLC.
- AI for documentation, tests, refactoring and code review support.
- AI risks: hallucination, security, sensitive data leakage, over-reliance.

---

## 3. Stack principal

Usar obrigatoriamente:

- Java 21.
- Spring Boot.
- Maven.
- Spring Web.
- Spring Validation.
- Spring Data JPA.
- PostgreSQL.
- Hibernate.
- Spring Boot Actuator.
- Micrometer.
- Prometheus.
- Grafana.
- Docker.
- Docker Compose.
- Kubernetes.
- JUnit 5.
- Mockito.
- AssertJ.
- MockMvc.
- Testcontainers.
- GitHub Actions.
- k6.

Usar em fases apropriadas:

- Spring Security.
- OAuth2 Resource Server.
- JWT.
- Keycloak.
- RabbitMQ ou Kafka.
- Resilience4j.
- OpenAPI/Swagger.
- Liquibase ou Flyway.
- OpenTelemetry como evolução futura.
- Loki/Promtail como evolução futura.

---

## 4. Domínio funcional da aplicação

A aplicação simula um sistema de ordens financeiras.

### Fluxo principal final

1. Cliente envia uma ordem de compra/venda.
2. API valida o request.
3. Sistema cria uma `Order`.
4. Sistema evita duplicidade usando `idempotencyKey`.
5. Ordem é persistida no PostgreSQL.
6. Sistema registra histórico/auditoria.
7. Sistema gera mensagem estilo FIX.
8. Ordem é colocada numa fila.
9. Workers processam ordens em paralelo.
10. Ordem pode ser executada, falhar, ser retentada ou ir para DLQ.
11. Eventos internos ou externos são publicados.
12. Logs estruturados são gerados com correlation ID.
13. Métricas são expostas.
14. Dashboards acompanham a saúde do sistema.
15. Testes de carga validam comportamento sob pressão.
16. Docker Compose sobe ambiente completo.
17. Kubernetes executa a aplicação em cluster local.
18. CI valida build, testes e package.

### Exemplo de request

```json
{
  "clientId": "C001",
  "symbol": "AAPL",
  "side": "BUY",
  "quantity": 100,
  "price": 150.25,
  "idempotencyKey": "REQ-123"
}
```

### Exemplo de mensagem FIX simulada

```text
8=FIX.4.4|35=D|49=MARKETFLOW|56=SIMULATED_BROKER|11={orderId}|55=AAPL|54=1|38=100|40=2|44=150.25|52=2026-01-01T10:00:00Z
```

---

## 5. Arquitetura

Começar como **modular monolith**.

Não começar com microservices reais.

Motivo:

- Facilita aprendizado incremental.
- Mantém deploy simples.
- Permite aprender camadas, domínio, testes e transações antes de distribuir complexidade.
- Evita criar complexidade operacional prematura.
- Permite evoluir para microservices depois com melhor fundamento.

### Package base

```text
com.gustavo.marketflow
```

### Estrutura sugerida

```text
src/main/java/com/gustavo/marketflow
├── MarketFlowApplication.java
├── order
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── fix
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── execution
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── event
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── monitoring
│   ├── api
│   ├── application
│   └── infrastructure
├── security
│   ├── config
│   └── application
├── resilience
│   ├── application
│   └── infrastructure
├── learning
│   ├── spring
│   ├── rest
│   ├── data
│   ├── concurrency
│   ├── messaging
│   ├── performance
│   ├── monitoring
│   ├── security
│   ├── resilience
│   └── kubernetes
└── shared
    ├── config
    ├── exception
    ├── audit
    ├── logging
    ├── mapper
    └── util
```

---

## 6. Estratégia Git

Usar uma branch por fase.

A regra é:

```text
uma fase = uma funcionalidade completa = uma branch = um PR = uma tag
```

### Branch principal

```text
main
```

### Branches

```text
feature/01-foundation-order-api
feature/02-persistence-jpa-transactions
feature/03-testing-quality-api-docs
feature/04-data-structures-order-book
feature/05-concurrency-processing-engine
feature/06-fix-message-engine
feature/07-event-driven-retry-dlq-idempotency
feature/08-external-messaging-kafka-rabbitmq
feature/09-monitoring-logging-observability
feature/10-security-jwt-keycloak
feature/11-resilience-external-services
feature/12-caching-scheduling-async
feature/13-docker-compose-environment
feature/14-kubernetes-local-deployment
feature/15-performance-jvm-load-tests
feature/16-cloud-readiness-ci-cd
feature/17-final-documentation-interview-guide
```

### Tags

```text
v0.1-foundation-order-api
v0.2-persistence-jpa-transactions
v0.3-testing-quality-api-docs
v0.4-data-structures-order-book
v0.5-concurrency-processing-engine
v0.6-fix-message-engine
v0.7-event-driven-retry-dlq-idempotency
v0.8-external-messaging
v0.9-monitoring-logging-observability
v1.0-security-jwt-keycloak
v1.1-resilience-external-services
v1.2-caching-scheduling-async
v1.3-docker-compose
v1.4-kubernetes-local
v1.5-performance-jvm-load-tests
v1.6-cloud-readiness-ci-cd
v2.0-final-documentation
```

---

## 7. Regras obrigatórias para cada fase

Para cada fase, o assistente de código deve entregar:

1. Planeamento da fase antes de codar.
2. Código funcional.
3. Testes.
4. Documentação da fase.
5. Atualização do README quando necessário.
6. Endpoints novos documentados.
7. Exemplos curl.
8. Explicação dos conceitos estudados.
9. Explicação dos trade-offs.
10. Perguntas de entrevista.
11. Exercícios práticos.
12. Checklist de conclusão.
13. Sugestão de commits semânticos.
14. Descrição de Pull Request.
15. Garantia de que a aplicação continua executável.

### Definition of Done global

Uma fase só está concluída quando:

- A aplicação compila.
- Os testes passam.
- A funcionalidade principal da fase pode ser demonstrada.
- O README ou documento da fase foi atualizado.
- Não há dados sensíveis hardcoded.
- Erros são tratados corretamente.
- Logs não expõem dados sensíveis.
- O código está organizado por responsabilidade.
- Há uma narrativa clara para explicar em entrevista.

---

# 8. Fases funcionais do curso

---

## Fase 1 — Foundation + Order API em memória

### Branch

```text
feature/01-foundation-order-api
```

### Objetivo

Criar a base do projeto e entregar a primeira funcionalidade completa: **criar, consultar e listar ordens em memória**.

Esta fase deve juntar fundação do projeto, modelagem inicial, Spring Core, REST, validação e tratamento de erros.  
Não usar banco ainda. A persistência será em memória para manter o primeiro vertical slice simples e executável.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Subir a aplicação.
2. Criar uma ordem via REST.
3. Consultar uma ordem por ID.
4. Listar ordens.
5. Validar requests inválidos.
6. Receber erro padronizado.
7. Ver health check.
8. Executar testes.

### Implementar

- Projeto Spring Boot Java 21.
- Maven.
- Package base `com.gustavo.marketflow`.
- Estrutura modular inicial.
- `Order` domain model.
- `OrderSide`.
- `OrderStatus`.
- `CreateOrderRequest`.
- `OrderResponse`.
- `OrderInMemoryRepository`.
- `OrderApplicationService`.
- `OrderController`.
- `GlobalExceptionHandler`.
- `ErrorResponse`.
- `CustomHealthController`.
- Actuator básico.
- README inicial.
- Documentação da fase.

### Campos da Order

- id: UUID.
- clientId: String.
- symbol: String.
- side: OrderSide.
- quantity: BigDecimal.
- price: BigDecimal.
- status: OrderStatus.
- createdAt: Instant.
- updatedAt: Instant.

### Endpoints

```text
GET  /health/custom
POST /orders
GET  /orders/{id}
GET  /orders
GET  /learning/spring/beans
GET  /learning/rest
GET  /learning/validation
GET  /learning/exception-handling
```

### Conceitos ensinados

- Spring Boot.
- Maven.
- ApplicationContext.
- Dependency Injection.
- Constructor injection.
- @Service.
- @RestController.
- @Configuration.
- @Bean.
- @ConfigurationProperties.
- @Component vs @Bean.
- REST.
- DTO.
- Validation.
- @Valid.
- @NotBlank.
- @NotNull.
- @Positive.
- Exception handling.
- @RestControllerAdvice.
- ResponseEntity.
- HTTP status codes.
- In-memory repository.
- Modular monolith.

### Testes

- Application context test.
- OrderApplicationServiceTest.
- OrderControllerTest com MockMvc.
- Validation error test.
- GlobalExceptionHandlerTest.

### Documentação

Criar:

```text
docs/phase-01-foundation-order-api.md
```

Deve explicar:

- O que foi criado.
- Por que começar em memória.
- O que é vertical slice.
- O que é Spring Boot.
- O que é DI.
- Por que usar DTO.
- Por que não expor entidade diretamente.
- Como testar com curl.
- Perguntas de entrevista.
- Exercícios.

### Commits sugeridos

```text
chore: initialize Spring Boot project
feat: add order domain model
feat: add in-memory order repository
feat: add order REST API
feat: add validation and global error handling
test: add order API tests
docs: add phase 01 documentation
```

### Critério de conclusão

A fase está concluída quando:

- `POST /orders` cria uma ordem.
- `GET /orders/{id}` retorna a ordem.
- `GET /orders` lista ordens.
- Validação retorna 400.
- Ordem inexistente retorna 404.
- Testes passam.
- Documentação existe.

---

## Fase 2 — PostgreSQL + JPA + Transactions + History

### Branch

```text
feature/02-persistence-jpa-transactions
```

### Objetivo

Substituir persistência em memória por PostgreSQL usando JPA/Hibernate e adicionar transações, migrations, histórico e auditoria básica.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Criar ordens persistidas em banco.
2. Reiniciar a aplicação e manter dados.
3. Filtrar ordens por cliente/status.
4. Consultar histórico da ordem.
5. Persistir eventos de histórico.
6. Executar migrations.
7. Demonstrar transações e rollback.

### Implementar

- PostgreSQL config.
- `OrderEntity`.
- `OrderRepository` com Spring Data JPA.
- Mapper domain/entity/response.
- `OrderHistoryEntity`.
- `OrderHistoryRepository`.
- Migration com Liquibase ou Flyway.
- Índices para:
  - clientId.
  - status.
  - createdAt.
- `@Transactional` no service.
- Paginação e filtros.
- Versionamento com `@Version`.
- Endpoint de histórico.
- Endpoints didáticos de JPA/transactions.

### Endpoints

```text
POST /orders
GET  /orders/{id}
GET  /orders?clientId=&status=&page=&size=
GET  /orders/{id}/history
GET  /learning/jpa/lazy-vs-eager
GET  /learning/jpa/n-plus-one
GET  /learning/transactions
GET  /learning/transactions/self-invocation
GET  /learning/hibernate/dirty-checking
```

### Conceitos ensinados

- JPA.
- Hibernate.
- @Entity.
- @Table.
- @Id.
- @GeneratedValue.
- @Column.
- @ManyToOne.
- @OneToMany.
- @Version.
- Lazy vs Eager.
- N+1 problem.
- Fetch join.
- Cascade.
- Orphan removal.
- Dirty checking.
- First-level cache.
- @Transactional.
- Rollback.
- rollbackFor.
- Self-invocation problem.
- HikariCP.
- Indexes.
- Query optimization.
- Migrations.

### Testes

- Repository test com Testcontainers PostgreSQL.
- Service transaction test.
- Controller integration test.
- History persistence test.
- Validation + DB persistence test.
- Optimistic locking test simples.

### Documentação

Criar:

```text
docs/phase-02-persistence-jpa-transactions.md
```

### Commits sugeridos

```text
feat: replace in-memory repository with JPA persistence
feat: add order history entity and repository
feat: add database migrations
feat: add transactional order creation
feat: add order filters and pagination
test: add PostgreSQL integration tests with Testcontainers
docs: add phase 02 persistence documentation
```

### Critério de conclusão

- Dados persistem após restart.
- Histórico da ordem é salvo.
- Filtros funcionam.
- Migration cria tabelas e índices.
- Testcontainers executa testes.
- Documentação cobre JPA e transactions.

---

## Fase 3 — Testing, Code Quality, OpenAPI e CI básico

### Branch

```text
feature/03-testing-quality-api-docs
```

### Objetivo

Transformar a aplicação numa base profissional com estratégia de testes, documentação de API e qualidade mínima automatizada.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Ver documentação OpenAPI/Swagger.
2. Rodar testes unitários e integração.
3. Validar pipeline CI no GitHub Actions.
4. Ter estrutura clara de testes por camada.
5. Entender o que cada tipo de teste cobre.

### Implementar

- OpenAPI/Swagger.
- Test strategy document.
- Mais testes unitários.
- Mais testes de controller.
- Mais testes JPA.
- Testcontainers documentado.
- GitHub Actions inicial:
  - checkout.
  - setup Java 21.
  - cache Maven.
  - mvn test.
  - mvn package.
- PR template.
- CONTRIBUTING.
- docs/git-workflow.md.
- docs/testing-strategy.md.

### Endpoints

```text
GET /swagger-ui.html
GET /v3/api-docs
GET /learning/testing
GET /learning/code-quality
```

### Conceitos ensinados

- Unit test.
- Integration test.
- MockMvc.
- Mockito.
- AssertJ.
- @SpringBootTest.
- @WebMvcTest.
- @DataJpaTest.
- @MockBean.
- Testcontainers.
- OpenAPI.
- CI.
- Pull Request workflow.
- Conventional commits.
- Code review basics.

### Testes

Esta fase deve elevar a cobertura dos fluxos principais.

### Documentação

Criar:

```text
docs/phase-03-testing-quality-api-docs.md
docs/testing-strategy.md
docs/git-workflow.md
```

### Commits sugeridos

```text
test: expand order service and controller tests
test: add repository integration tests with Testcontainers
docs: add testing strategy guide
feat: add OpenAPI documentation
ci: add GitHub Actions build pipeline
docs: add pull request template and contributing guide
```

### Critério de conclusão

- Swagger acessível.
- CI configurado.
- Testes passam localmente.
- PR template criado.
- Estratégia de testes documentada.

---

## Fase 4 — Estruturas de Dados + Order Book

### Branch

```text
feature/04-data-structures-order-book
```

### Objetivo

Adicionar estruturas de dados reais ao domínio, criando um **order book funcional**.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Criar ordens.
2. Adicionar ordens ao order book.
3. Consultar melhor BUY.
4. Consultar melhor SELL.
5. Consultar estado do order book.
6. Usar cache LRU para ordens recentes.
7. Consultar sessões de clientes em memória.

### Implementar

- `OrderTask`.
- `OrderBook`.
- PriorityQueue para BUY.
- PriorityQueue para SELL.
- `RecentOrderCache` com LinkedHashMap LRU.
- `ClientSession`.
- `ClientSessionRegistry` com ConcurrentHashMap.
- `OrderBookController`.
- Endpoints didáticos.

### Endpoints

```text
POST /orders/{id}/book
GET  /order-book
GET  /order-book/best-buy
GET  /order-book/best-sell
GET  /orders/recent/{id}
POST /clients/{clientId}/sessions
DELETE /clients/{clientId}/sessions
GET  /clients/sessions
GET  /learning/data-structures
GET  /learning/data-structures/order-book
GET  /learning/data-structures/cache
GET  /learning/data-structures/concurrent-map
```

### Conceitos ensinados

- Array.
- List.
- Set.
- Map.
- HashMap.
- TreeMap.
- LinkedHashMap.
- Queue.
- Deque.
- Stack.
- BlockingQueue.
- PriorityQueue.
- ConcurrentHashMap.
- CopyOnWriteArrayList.
- ConcurrentLinkedQueue.
- Big O.
- Cache LRU.
- Trade-off memória/performance.

### Testes

- OrderBookTest.
- RecentOrderCacheTest.
- ClientSessionRegistryTest.
- Controller tests.

### Documentação

Criar:

```text
docs/phase-04-data-structures-order-book.md
```

### Critério de conclusão

- Order book ordena BUY pelo maior preço.
- Order book ordena SELL pelo menor preço.
- Cache remove item mais antigo quando excede capacidade.
- ConcurrentHashMap controla sessões.
- Testes provam comportamento.

---

## Fase 5 — Concorrência + Processing Engine

### Branch

```text
feature/05-concurrency-processing-engine
```

### Objetivo

Criar motor de processamento concorrente de ordens.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Enfileirar ordens.
2. Iniciar workers.
3. Processar ordens em paralelo.
4. Parar workers.
5. Ver estatísticas de execução.
6. Simular sucesso/falha.
7. Demonstrar race condition e correções.

### Implementar

- `OrderQueue` com BlockingQueue.
- `OrderProcessingEngine`.
- `ExecutionWorker`.
- `ExecutionResult`.
- `WorkerPoolConfiguration`.
- `ExecutionController`.
- `RaceConditionDemo`.
- `SynchronizedCounterDemo`.
- `AtomicCounterDemo`.
- `ReentrantLockCounterDemo`.
- `DeadlockDemo`.
- `StarvationDemo`.
- `CompletableFutureDemo`.

### Endpoints

```text
POST /orders/{id}/queue
POST /execution/start
POST /execution/stop
GET  /execution/stats
GET  /learning/concurrency/race-condition
GET  /learning/concurrency/deadlock
GET  /learning/concurrency/completable-future
GET  /learning/concurrency/thread-pool
```

### Conceitos ensinados

- Thread.
- Runnable.
- Callable.
- Future.
- CompletableFuture.
- ExecutorService.
- ThreadPoolExecutor.
- ScheduledExecutorService.
- BlockingQueue.
- Producer/consumer.
- AtomicInteger.
- AtomicLong.
- AtomicReference.
- synchronized.
- volatile.
- Lock.
- ReentrantLock.
- Semaphore.
- CountDownLatch.
- CyclicBarrier.
- Race condition.
- Deadlock.
- Starvation.
- Java Memory Model.
- CPU-bound vs IO-bound.
- Thread pool sizing.

### Testes

- OrderProcessingEngineTest.
- RaceConditionDemoTest.
- AtomicCounterDemoTest.
- CompletableFutureDemoTest.
- Concurrency-safe stats test.

### Documentação

Criar:

```text
docs/phase-05-concurrency-processing-engine.md
```

### Critério de conclusão

- Ordem enfileirada é processada.
- Estatísticas são atualizadas.
- Workers podem iniciar/parar.
- Testes de concorrência passam.
- Race condition é demonstrada e corrigida.

---

## Fase 6 — FIX Message Engine

### Branch

```text
feature/06-fix-message-engine
```

### Objetivo

Criar motor didático de geração, parsing e explicação de mensagens estilo FIX Protocol.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Gerar mensagem FIX para uma ordem.
2. Persistir a mensagem gerada.
3. Consultar mensagem FIX da ordem.
4. Explicar cada tag.
5. Fazer parse de uma mensagem raw enviada pelo utilizador.

### Implementar

- `FixMessage`.
- `FixMessageEntity`.
- `FixTag`.
- `FixMessageGenerator`.
- `FixMessageParser`.
- `FixMessageExplainer`.
- `FixMessageRepository`.
- `FixController`.

### Tags

```text
8  = BeginString
35 = MsgType
49 = SenderCompID
56 = TargetCompID
11 = ClOrdID
55 = Symbol
54 = Side
38 = OrderQty
40 = OrdType
44 = Price
52 = SendingTime
```

### Endpoints

```text
POST /orders/{id}/fix-message
GET  /orders/{id}/fix-message
GET  /orders/{id}/fix-explanation
POST /fix/explain
GET  /learning/fix
```

### Conceitos ensinados

- FIX Protocol.
- Message tags.
- New Order Single.
- Execution Report.
- Heartbeat.
- Sequence Number.
- Cancel Request.
- Latência em sistemas financeiros.
- Diferença entre simulação e implementação real.
- QuickFIX/J como evolução futura.

### Testes

- FixMessageGeneratorTest.
- FixMessageParserTest.
- FixMessageExplainerTest.
- FixControllerTest.
- Persistence test.

### Documentação

Criar:

```text
docs/phase-06-fix-message-engine.md
```

### Critério de conclusão

- BUY gera `54=1`.
- SELL gera `54=2`.
- Parser transforma raw FIX em mapa.
- Explainer retorna tag, nome, valor e descrição.
- Testes passam.

---

## Fase 7 — Event-driven + Retry + DLQ + Idempotência

### Branch

```text
feature/07-event-driven-retry-dlq-idempotency
```

### Objetivo

Adicionar arquitetura orientada a eventos interna com retry, DLQ e idempotência.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Publicar eventos internos.
2. Ver eventos publicados.
3. Criar ordem com idempotency key.
4. Evitar criação duplicada.
5. Simular falha.
6. Executar retry.
7. Enviar ordem para DLQ após limite.
8. Reprocessar item da DLQ.

### Implementar

- `DomainEvent`.
- `OrderCreatedEvent`.
- `OrderValidatedEvent`.
- `OrderQueuedEvent`.
- `FixMessageGeneratedEvent`.
- `OrderExecutedEvent`.
- `OrderFailedEvent`.
- `OrderRetriedEvent`.
- `OrderMovedToDeadLetterEvent`.
- `InMemoryEventBus`.
- `RetryPolicy`.
- `RetryRegistry`.
- `DeadLetterMessage`.
- `DeadLetterQueue`.
- `IdempotencyRegistry`.

### Endpoints

```text
GET  /events
GET  /events/{type}
GET  /execution/dlq
POST /execution/dlq/{orderId}/reprocess
GET  /learning/event-driven
GET  /learning/idempotency
GET  /learning/dlq
```

### Conceitos ensinados

- Event-driven architecture.
- Producer.
- Consumer.
- Queue.
- Topic.
- Retry.
- Dead Letter Queue.
- Idempotência.
- Ordering.
- Backpressure.
- At-least-once.
- Exactly-once.
- Duplicação de mensagens.
- Falha antes/depois do ACK.
- Evento interno vs broker externo.

### Testes

- InMemoryEventBusTest.
- RetryRegistryTest.
- DeadLetterQueueTest.
- IdempotencyRegistryTest.
- Order duplicate request test.
- DLQ after max attempts test.

### Documentação

Criar:

```text
docs/phase-07-event-driven-retry-dlq-idempotency.md
```

### Critério de conclusão

- Eventos aparecem em `/events`.
- Mesmo `idempotencyKey` não cria ordem duplicada.
- Falha executa retry.
- Após max attempts, ordem vai para DLQ.
- DLQ pode ser consultada.
- Reprocessamento existe.

---

## Fase 8 — Messaging Externo com Kafka ou RabbitMQ

### Branch

```text
feature/08-external-messaging-kafka-rabbitmq
```

### Objetivo

Adicionar integração com broker externo para aproximar o projeto de sistemas reais.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Subir broker via Docker Compose.
2. Publicar evento de ordem criada num tópico/fila.
3. Consumir evento.
4. Processar mensagem com consumidor.
5. Simular duplicação.
6. Demonstrar idempotent consumer.
7. Configurar DLQ ou tópico de erro.

### Implementar

Escolher preferencialmente Kafka para alinhamento com vagas bancárias, mas RabbitMQ também é aceitável se for mais simples.

#### Kafka

- `OrderEventProducer`.
- `OrderEventConsumer`.
- Topic `marketflow.orders.created`.
- Topic `marketflow.orders.executed`.
- Topic `marketflow.orders.failed`.
- Topic/handler de erro.
- Consumer group.
- Offset basics.

#### RabbitMQ alternativa

- Exchange.
- Queue.
- Routing key.
- DLQ.
- Producer.
- Consumer.

### Endpoints

```text
POST /orders/{id}/publish
GET  /messaging/status
GET  /learning/messaging
GET  /learning/kafka
GET  /learning/queue-vs-topic
```

### Conceitos ensinados

- Broker externo.
- Kafka topic.
- Partition.
- Consumer group.
- Offset.
- Producer.
- Consumer.
- Retry.
- DLQ.
- Idempotent consumer.
- ActiveMQ/Artemis vs Kafka.
- Queue vs Topic.
- Ordering.
- Backpressure.

### Testes

- Producer test.
- Consumer test.
- Idempotent consumer test.
- Integration test com Testcontainers Kafka/RabbitMQ.

### Documentação

Criar:

```text
docs/phase-08-external-messaging-kafka-rabbitmq.md
```

### Critério de conclusão

- Evento é publicado no broker.
- Consumidor processa mensagem.
- Duplicação não causa efeito duplicado.
- Testcontainers valida integração.
- Conceitos Kafka/RabbitMQ documentados.

---

## Fase 9 — Monitoring, Logs e Observability

### Branch

```text
feature/09-monitoring-logging-observability
```

### Objetivo

Criar camada profissional de observabilidade.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Enviar request com `X-Correlation-Id`.
2. Ver logs com correlation ID.
3. Ver logs de negócio.
4. Ver logs técnicos.
5. Ver audit logs.
6. Consultar métricas.
7. Consultar health checks.
8. Ver resumo operacional.
9. Ter dashboard Grafana preparado.
10. Ter troubleshooting guide.

### Implementar

- Actuator completo.
- Micrometer.
- Prometheus endpoint.
- `CorrelationIdFilter`.
- MDC.
- Request logging.
- Structured logging.
- Business logs.
- Technical logs.
- Error logs.
- `AuditLogService`.
- Health indicators:
  - database.
  - order queue.
  - DLQ.
  - processing engine.
- `OrderMetricsService`.
- `MonitoringSummaryController`.
- Grafana dashboard JSON.
- Prometheus config.
- Alerts documentation.
- Troubleshooting documentation.

### Endpoints

```text
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
GET /actuator/prometheus
GET /monitoring/summary
GET /learning/logging
GET /learning/monitoring
GET /learning/performance/jvm
```

### Métricas customizadas

Counters:

```text
marketflow_orders_created_total
marketflow_orders_validated_total
marketflow_orders_rejected_total
marketflow_orders_queued_total
marketflow_orders_executed_total
marketflow_orders_failed_total
marketflow_orders_retried_total
marketflow_orders_dlq_total
marketflow_fix_messages_generated_total
```

Gauges:

```text
marketflow_order_queue_size
marketflow_dead_letter_queue_size
marketflow_active_workers
marketflow_active_client_sessions
```

Timers:

```text
marketflow_order_processing_duration
marketflow_fix_generation_duration
marketflow_order_creation_duration
```

### Conceitos ensinados

- Observability.
- Logging.
- Monitoring.
- Tracing basics.
- Structured logs.
- Correlation ID.
- MDC.
- Audit logs.
- Actuator.
- Micrometer.
- Prometheus.
- Grafana.
- Health checks.
- Readiness.
- Liveness.
- SLI.
- SLO.
- SLA.
- RED metrics.
- USE metrics.
- Troubleshooting.
- Sensitive data in logs.

### Documentação

Criar:

```text
docs/phase-09-monitoring-logging-observability.md
docs/troubleshooting.md
docs/alerts.md
docs/kubernetes-logging-monitoring.md
```

### Critério de conclusão

- Logs têm correlation ID.
- Erros retornam correlation ID.
- Métricas aparecem em `/actuator/prometheus`.
- `/monitoring/summary` retorna dados operacionais.
- Health customizado funciona.
- Documentação de troubleshooting existe.

---

## Fase 10 — Security com JWT e Keycloak

### Branch

```text
feature/10-security-jwt-keycloak
```

### Objetivo

Adicionar segurança realista à API.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Subir aplicação em perfil seguro.
2. Exigir JWT nos endpoints protegidos.
3. Validar roles.
4. Proteger endpoints administrativos.
5. Manter endpoints learning públicos em local.
6. Documentar Keycloak/JWT.

### Implementar

- Spring Security.
- SecurityFilterChain.
- OAuth2 Resource Server.
- JWT validation.
- Keycloak local/simulado.
- Roles:
  - USER.
  - ADMIN.
- @PreAuthorize.
- Security profiles:
  - local.
  - secure.
- Secure actuator config.

### Endpoints

```text
GET /learning/security
GET /learning/jwt
GET /learning/keycloak
```

### Regras

- `POST /orders` exige USER.
- `GET /orders` exige USER.
- `/monitoring/**` exige ADMIN.
- `/learning/**` pode ser público em perfil local.
- Actuator completo não deve ficar exposto publicamente em produção.

### Conceitos ensinados

- Authentication.
- Authorization.
- JWT.
- OAuth2.
- Resource Server.
- Keycloak.
- SecurityFilterChain.
- @PreAuthorize.
- Roles vs authorities.
- CORS.
- CSRF.
- Stateless authentication.
- Token safety.
- Security logs.

### Testes

- Unauthorized request test.
- Forbidden request test.
- Authorized USER request test.
- ADMIN endpoint test.
- Method security test.

### Documentação

Criar:

```text
docs/phase-10-security-jwt-keycloak.md
```

### Critério de conclusão

- Endpoints protegidos rejeitam chamadas sem token.
- Role USER acessa recursos de usuário.
- Role ADMIN acessa monitoria.
- Testes de segurança passam.

---

## Fase 11 — Resilience + External Service Simulation

### Branch

```text
feature/11-resilience-external-services
```

### Objetivo

Simular chamadas externas e aplicar padrões de resiliência.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Consultar serviço externo simulado.
2. Simular timeout.
3. Simular falha.
4. Aplicar retry controlado.
5. Aplicar circuit breaker.
6. Aplicar bulkhead.
7. Aplicar rate limit.
8. Executar fallback.

### Implementar

- Resilience4j.
- `MarketDataClient`.
- `BrokerClient`.
- `NotificationClient`.
- Timeout.
- Retry.
- Circuit breaker.
- Bulkhead.
- Rate limiter.
- Fallback.
- Métricas de resilience.

### Endpoints

```text
POST /orders/{id}/execute-with-broker
GET  /external/market-data/{symbol}
GET  /learning/resilience
GET  /learning/circuit-breaker
GET  /learning/rate-limit
GET  /learning/bulkhead
```

### Conceitos ensinados

- Timeout.
- Retry.
- Circuit breaker.
- Bulkhead.
- Rate limiter.
- Fallback.
- Graceful degradation.
- Retry storm.
- Dependency failure.
- Resilience metrics.

### Testes

- Circuit breaker test.
- Retry test.
- Timeout test.
- Fallback test.
- Rate limiter test.

### Documentação

Criar:

```text
docs/phase-11-resilience-external-services.md
```

### Critério de conclusão

- Serviço externo simulado falha.
- Circuit breaker abre.
- Fallback responde.
- Retry não explode chamadas.
- Métricas e docs existem.

---

## Fase 12 — Caching, Scheduling e Async

### Branch

```text
feature/12-caching-scheduling-async
```

### Objetivo

Adicionar cache, jobs agendados e execução assíncrona com Spring.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Consultar dados cacheados.
2. Invalidar cache.
3. Rodar scheduler de ordens pendentes.
4. Executar tarefa assíncrona.
5. Usar executor customizado.
6. Monitorar comportamento.

### Implementar

- Spring Cache.
- @EnableCaching.
- @Cacheable.
- @CacheEvict.
- @CachePut.
- Scheduler para ordens pendentes.
- @EnableScheduling.
- @Scheduled.
- @EnableAsync.
- @Async.
- Executor customizado.
- Rejection policy.
- Async exception handling.

### Endpoints

```text
GET    /cache/orders/{id}
DELETE /cache/orders/{id}
POST   /orders/{id}/process-async
GET    /learning/cache
GET    /learning/scheduling
GET    /learning/async
```

### Conceitos ensinados

- Cache local.
- Cache distribuído.
- Cache stale.
- Cache em Kubernetes com múltiplas réplicas.
- fixedRate.
- fixedDelay.
- cron.
- Scheduler em múltiplas instâncias.
- @Async.
- Proxy problem.
- Executor default vs customizado.
- Queue capacity.
- Rejection policy.

### Testes

- Cache behavior test.
- Cache eviction test.
- Scheduler logic test.
- Async execution test.
- Executor config test.

### Documentação

Criar:

```text
docs/phase-12-caching-scheduling-async.md
```

### Critério de conclusão

- Cache funciona.
- Eviction funciona.
- Async processa fora da thread principal.
- Scheduler é documentado.
- Riscos de múltiplas instâncias são explicados.

---

## Fase 13 — Docker Compose Environment

### Branch

```text
feature/13-docker-compose-environment
```

### Objetivo

Criar ambiente local completo com Docker Compose.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Subir app + banco + broker + Prometheus + Grafana com um comando.
2. Criar ordem via API.
3. Processar ordem.
4. Ver logs.
5. Ver métricas.
6. Ver dashboard.
7. Derrubar ambiente.

### Implementar

- Dockerfile multi-stage.
- .dockerignore.
- docker-compose.yml.
- application-docker.yml.
- PostgreSQL container.
- Kafka/RabbitMQ container.
- Prometheus container.
- Grafana container.
- Grafana dashboard provisioning.
- Prometheus config.
- Volume para Postgres.
- Health checks no compose se fizer sentido.

### Comandos

```bash
docker compose up --build
docker compose down
docker compose logs -f marketflow-app
docker compose ps
```

### Conceitos ensinados

- Docker.
- Image.
- Container.
- Dockerfile.
- Multi-stage build.
- Docker Compose.
- Networking.
- Volumes.
- Environment variables.
- Logs em container.
- Prometheus scrape.
- Grafana provisioning.

### Testes

- Build docker image.
- Manual smoke test documentado.
- Optional script `scripts/smoke-test.sh`.

### Documentação

Criar:

```text
docs/phase-13-docker-compose-environment.md
```

### Critério de conclusão

- `docker compose up --build` sobe ambiente.
- App conecta ao Postgres.
- App conecta ao broker.
- Prometheus coleta métricas.
- Grafana abre dashboard.
- Logs aparecem no stdout.

---

## Fase 14 — Kubernetes Local Deployment

### Branch

```text
feature/14-kubernetes-local-deployment
```

### Objetivo

Criar manifests Kubernetes para rodar aplicação em kind/minikube.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Criar namespace.
2. Subir PostgreSQL.
3. Subir broker se aplicável.
4. Subir aplicação com 2 réplicas.
5. Expor app por Service.
6. Fazer port-forward.
7. Ver logs.
8. Ver probes.
9. Aplicar HPA.
10. Executar smoke test.

### Implementar

Pasta:

```text
infra/kubernetes
```

Arquivos:

```text
namespace.yaml
configmap.yaml
secret.yaml
postgres-deployment.yaml
postgres-service.yaml
broker-deployment.yaml
broker-service.yaml
app-deployment.yaml
app-service.yaml
ingress.yaml
hpa.yaml
```

### Configurar

- replicas: 2.
- readinessProbe.
- livenessProbe.
- resources requests/limits.
- envFrom configmap/secret.
- service.
- ingress.
- HPA.

### Comandos

```bash
kubectl apply -f infra/kubernetes
kubectl get pods -n marketflow
kubectl get svc -n marketflow
kubectl logs -n marketflow deployment/marketflow-app
kubectl describe pod -n marketflow {pod-name}
kubectl port-forward -n marketflow svc/marketflow-app 8080:8080
kubectl delete namespace marketflow
```

### Conceitos ensinados

- Kubernetes.
- Cluster.
- Node.
- Pod.
- Deployment.
- ReplicaSet.
- Service.
- Ingress.
- Route no OpenShift.
- ConfigMap.
- Secret.
- Readiness.
- Liveness.
- Requests.
- Limits.
- HPA.
- Rollout.
- Rollback.
- Logs.
- Describe.
- Port-forward.
- CrashLoopBackOff.
- ImagePullBackOff.

### Documentação

Criar:

```text
docs/phase-14-kubernetes-local-deployment.md
docs/kubernetes-logging-monitoring.md
```

### Critério de conclusão

- Pods ficam Running.
- Readiness fica OK.
- Service responde via port-forward.
- Logs podem ser consultados.
- HPA existe.
- Documentação inclui troubleshooting.

---

## Fase 15 — Performance, JVM, GC e Load Tests

### Branch

```text
feature/15-performance-jvm-load-tests
```

### Objetivo

Medir performance da aplicação e estudar JVM/GC.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Rodar load test.
2. Rodar stress test.
3. Medir throughput.
4. Medir latência.
5. Observar P95/P99.
6. Observar fila crescendo.
7. Observar CPU/memória.
8. Observar métricas JVM.
9. Gerar relatório de análise.

### Implementar

Pasta:

```text
performance/k6
```

Scripts:

```text
create-orders.js
order-flow.js
stress-test.js
spike-test.js
```

Documentos:

```text
docs/performance-checklist.md
docs/jvm-performance.md
docs/phase-15-performance-jvm-load-tests.md
```

### Conceitos ensinados

- Performance.
- Escalabilidade.
- Throughput.
- Latency.
- P95.
- P99.
- Bottleneck.
- CPU-bound.
- IO-bound.
- Heap.
- Stack.
- Young generation.
- Old generation.
- G1 GC.
- ZGC.
- GC logs.
- Memory leak.
- Thread dump.
- Heap dump.
- CPU profiling.
- Object allocation.
- Thread pool saturation.
- Queue growth.
- Backpressure.
- Database bottleneck.

### Comandos

```bash
k6 run performance/k6/create-orders.js
k6 run performance/k6/order-flow.js
k6 run performance/k6/stress-test.js
k6 run performance/k6/spike-test.js
```

### Critério de conclusão

- k6 executa scripts.
- Métricas são observáveis.
- Documento explica como interpretar resultados.
- Checklist de performance existe.
- Gargalos prováveis são documentados.

---

## Fase 16 — Cloud Readiness + CI/CD

### Branch

```text
feature/16-cloud-readiness-ci-cd
```

### Objetivo

Preparar aplicação para produção/cloud e automatizar validações.

### Funcionalidade completa da fase

Ao final da fase, deve ser possível:

1. Rodar pipeline CI.
2. Gerar package.
3. Opcionalmente gerar Docker image.
4. Ter checklist de produção.
5. Comparar opções cloud.
6. Entender o que falta para deploy real.

### Implementar

- GitHub Actions robusto.
- Pipeline:
  - checkout.
  - setup Java 21.
  - cache Maven.
  - mvn test.
  - mvn package.
  - optional docker build.
- docs/ci-cd.md.
- docs/cloud-readiness.md.
- docs/cloud-deployment-options.md.
- docs/production-readiness-checklist.md.

### Comparar

- AWS EKS.
- Azure AKS.
- Google GKE.
- DigitalOcean Kubernetes.
- Render.
- Fly.io.
- Railway.

### Conceitos ensinados

- CI.
- CD.
- Artifact.
- Docker image.
- Registry.
- Managed database.
- Secret manager.
- Load balancer.
- Autoscaling.
- Cloud logging.
- Cloud monitoring.
- Security.
- Backups.
- Disaster recovery.
- Cost control.
- Production readiness.

### Critério de conclusão

- Pipeline funciona.
- Docs de cloud estão claros.
- Checklist de produção existe.
- README aponta para CI/CD e cloud readiness.

---

## Fase 17 — Documentação Final + Guia de Entrevista

### Branch

```text
feature/17-final-documentation-interview-guide
```

### Objetivo

Transformar o projeto em portfólio profissional e material de entrevista.

### Funcionalidade completa da fase

Ao final da fase, deve existir documentação suficiente para:

1. Apresentar o projeto numa entrevista.
2. Explicar arquitetura.
3. Explicar system design.
4. Explicar decisões técnicas.
5. Explicar trade-offs.
6. Estudar perguntas Senior Java.
7. Demonstrar o projeto no GitHub.

### Criar/atualizar

```text
README.md
docs/course-overview.md
docs/architecture.md
docs/system-design.md
docs/learning-map.md
docs/interview-guide.md
docs/project-story.md
docs/troubleshooting.md
docs/alerts.md
docs/performance-checklist.md
docs/production-readiness-checklist.md
docs/cloud-deployment-options.md
docs/ci-cd.md
```

### README deve conter

- Título.
- Descrição curta.
- Arquitetura.
- Tecnologias.
- Funcionalidades principais.
- Conceitos estudados.
- Como rodar localmente.
- Como rodar com Docker.
- Como rodar testes.
- Como rodar k6.
- Como subir em Kubernetes local.
- Endpoints principais.
- Dashboards.
- Roadmap.
- Screenshots placeholders.
- Perguntas de entrevista.

### docs/project-story.md

Criar narrativa profissional:

```text
Construí o MarketFlow Senior Java Cloud Lab, uma aplicação Java 21 com Spring Boot que simula processamento de ordens financeiras. O objetivo foi estudar sistemas backend enterprise de alta concorrência, arquitetura event-driven, performance, observabilidade e escalabilidade.

A aplicação usa REST APIs, JPA/Hibernate, PostgreSQL, estruturas de dados como BlockingQueue, ConcurrentHashMap, PriorityQueue e LRU Cache. Também implementei processamento concorrente com ExecutorService e CompletableFuture, retry, DLQ, idempotência, geração de mensagens estilo FIX, métricas com Micrometer/Prometheus, dashboards Grafana, logs estruturados com correlation id, Docker, Kubernetes e CI/CD.
```

Expandir com:

- contexto.
- problema.
- arquitetura.
- decisões.
- trade-offs.
- melhorias futuras.
- como eu explicaria numa entrevista.

### docs/interview-guide.md deve conter perguntas sobre

- Java Core.
- Collections.
- Concurrency.
- Spring Core.
- REST.
- Validation.
- Exception handling.
- JPA/Hibernate.
- Transactions.
- Testing.
- Messaging.
- Kafka.
- ActiveMQ/Artemis.
- Monitoring.
- Logs.
- Security.
- Kubernetes.
- Docker.
- Performance.
- JVM/GC.
- Cloud.
- CI/CD.
- Code quality.
- Debugging.
- AI tools in SDLC.

### Critério de conclusão

- README parece portfólio profissional.
- Guia de entrevista cobre principais tópicos.
- História do projeto está pronta.
- Learning map conecta funcionalidade -> conceito.
- Projeto pode ser mostrado no GitHub.

---

# 9. Instruções para o assistente de código

O assistente deve agir como:

```text
Senior Java Architect + Technical Teacher + Code Reviewer
```

Não deve apenas gerar código.

Para cada fase, deve:

1. Explicar o plano.
2. Implementar incrementalmente.
3. Explicar o código criado.
4. Explicar por que cada decisão foi tomada.
5. Comparar alternativas.
6. Mostrar trade-offs.
7. Criar testes.
8. Criar documentação.
9. Sugerir commits.
10. Criar descrição de PR.
11. Listar perguntas de entrevista.
12. Listar exercícios para praticar.
13. Não avançar para a próxima fase sem fechar a atual.

Se houver várias opções possíveis, deve recomendar uma e explicar porquê.

Se encontrar código ruim, deve refatorar e explicar.

Se uma decisão for perigosa para produção, deve avisar.

Não usar IA para esconder falta de entendimento.  
Toda implementação deve ser explicável.

---

# 10. Pull Request template

Criar:

```text
.github/pull_request_template.md
```

Conteúdo:

```markdown
## Objetivo

## Funcionalidade entregue

## O que foi implementado

## Conceitos estudados

## Como executar

## Como testar

## Exemplos curl

## Logs esperados

## Métricas esperadas

## Trade-offs técnicos

## Riscos conhecidos

## Screenshots ou evidências

## Perguntas de entrevista relacionadas

## Checklist

- [ ] Código compila
- [ ] Testes passam
- [ ] Funcionalidade principal pode ser demonstrada
- [ ] Documentação da fase foi criada/atualizada
- [ ] README atualizado se necessário
- [ ] Sem dados sensíveis
- [ ] Sem stacktrace exposto para cliente
- [ ] Logs incluem correlationId quando aplicável
- [ ] Métricas atualizadas quando aplicável
- [ ] Perguntas de entrevista adicionadas
- [ ] Próxima fase está claramente definida

## Próximos passos
```

---

# 11. Padrão de commits

Usar Conventional Commits.

Tipos:

```text
chore
feat
fix
test
docs
refactor
perf
build
ci
```

Exemplos:

```text
chore: initialize Spring Boot project
feat: add order creation API
feat: add global exception handling
test: add order controller tests
docs: add phase 01 documentation
feat: add JPA persistence for orders
feat: add order history tracking
test: add PostgreSQL Testcontainers tests
feat: add order processing engine
feat: add FIX message generator
feat: add correlation id filter
feat: add Prometheus metrics
ci: add GitHub Actions workflow
```

Não criar um único commit gigante.

---

# 12. Critério final de sucesso

O curso/projeto estará completo quando eu conseguir:

1. Rodar a aplicação localmente.
2. Criar ordens via API.
3. Persistir ordens no PostgreSQL.
4. Consultar histórico de ordens.
5. Adicionar ordens ao order book.
6. Gerar mensagem FIX.
7. Enfileirar ordens.
8. Processar ordens em paralelo.
9. Simular falhas.
10. Aplicar retry.
11. Enviar mensagens para DLQ.
12. Reprocessar DLQ.
13. Evitar duplicação com idempotency key.
14. Publicar/consumir eventos.
15. Ver logs estruturados com correlation ID.
16. Ver audit logs.
17. Ver métricas no Prometheus.
18. Ver dashboards no Grafana.
19. Proteger endpoints com JWT/Keycloak.
20. Simular falhas externas com Resilience4j.
21. Usar cache.
22. Executar jobs agendados.
23. Subir tudo com Docker Compose.
24. Subir em Kubernetes local.
25. Rodar testes unitários e integração.
26. Rodar testes de carga com k6.
27. Executar pipeline CI.
28. Explicar todos os principais tópicos em entrevista.
29. Mostrar o projeto como portfólio técnico Senior Java.

---

# 13. Primeiro prompt para iniciar o projeto

Quando for começar no Copilot/Cursor, usar:

```text
Vamos iniciar o projeto MarketFlow Senior Java Cloud Lab.

Usa este documento como instruction principal.

Começa apenas pela Fase 1 — Foundation + Order API em memória, na branch feature/01-foundation-order-api.

Antes de escrever código, apresenta o plano da fase, a estrutura que vais criar, os endpoints, os testes e a documentação que será produzida.

Depois implementa somente a Fase 1.

Não avances para JPA, PostgreSQL, Docker, Kafka, Kubernetes ou Security nesta fase.

No final, gera:
1. resumo do que foi implementado;
2. como executar;
3. como testar;
4. exemplos curl;
5. commits semânticos sugeridos;
6. descrição de PR;
7. perguntas de entrevista;
8. exercícios antes da próxima fase.
```

---

# 14. Prompt de revisão antes do merge

Usar antes de fechar qualquer fase:

```text
Faz uma revisão completa da fase atual antes do merge.

Verifica:

1. O código compila?
2. Todos os testes passam?
3. A funcionalidade principal da fase funciona?
4. Existem classes duplicadas?
5. Os packages seguem a arquitetura definida?
6. Os nomes das classes estão claros?
7. Existem responsabilidades misturadas?
8. Os controllers estão finos?
9. Os services concentram regras de aplicação?
10. O domínio está independente sempre que possível?
11. Existem validações suficientes?
12. Os erros são tratados corretamente?
13. Os testes cobrem os principais cenários?
14. A documentação da fase foi criada/atualizada?
15. O README foi atualizado se necessário?
16. Existem exemplos curl?
17. Existem decisões técnicas explicadas?
18. Existem trade-offs documentados?
19. Há código morto ou comentários inúteis?
20. Há dados sensíveis hardcoded?
21. Logs expõem dados sensíveis?
22. A fase está pronta para Pull Request?

Se encontrares problemas, corrige.
Se forem melhorias opcionais, lista como future improvements.

No final, gera uma descrição de Pull Request.
```

---

# 15. Prompt de explicação didática após cada fase

Usar depois da implementação:

```text
Agora explica todo o código criado nesta fase de forma didática.

Quero entender:

1. Qual problema esta fase resolve.
2. Qual funcionalidade completa foi entregue.
3. Quais classes foram criadas.
4. Qual a responsabilidade de cada classe.
5. Como o fluxo funciona do endpoint até a camada final.
6. Quais conceitos técnicos foram aplicados.
7. Quais estruturas de dados foram usadas e por quê.
8. Quais riscos existem.
9. Quais trade-offs foram aceitos.
10. Como testar manualmente.
11. Como testar automaticamente.
12. Como eu poderia explicar isso numa entrevista.
13. Que perguntas técnicas podem surgir sobre esta fase.
14. O que devo estudar antes de avançar para a próxima fase.

Não expliques de forma superficial.
Explica como se eu fosse apresentar este projeto para um arquiteto ou entrevistador Java Senior.
```

---

# 16. Nota final

Este projeto deve ser construído para aprender de verdade.

A prioridade é:

```text
funcionalidade completa por fase
clareza arquitetural
testes
documentação
explicação técnica
capacidade de apresentar em entrevista
```

Não é prioridade:

```text
gerar tudo rápido
adicionar tecnologia sem necessidade
criar microservices prematuramente
esconder complexidade
copiar código sem entender
```

O objetivo final é transformar o projeto num laboratório técnico e num portfólio real de Senior Java Backend / Backend-focused Full Stack Developer.

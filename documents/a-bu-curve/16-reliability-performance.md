# 16 - Reliability and Performance Review

## 1) Scope
Static architecture and code review focused on reliability patterns, bottlenecks, fault tolerance, and performance characteristics.

## 2) Reliability architecture patterns observed
- Asynchronous job execution for grading and downstream updates (SQS/queue workers).
- Event-driven side effects for status sync and passback.
- Caching used across services to reduce repeated remote/DB calls.
- Retry patterns for selected token and external API interactions.

## 3) Performance-critical paths

## 3.1 Assessment runtime hot path (Apollo)
- Read-heavy payload assembly:
  - instance -> item instances -> interaction instances -> assessment item metadata.
- Write-heavy during test-taking:
  - frequent interaction updates and periodic status changes.
- Submit path:
  - grading dispatch and status/event propagation.

## 3.2 Telemetry hot path (Hermes)
- Frequent write path for xAPI statements/state.
- JSON document persistence with indexed lookup keys (agent/registration).

## 3.3 Identity/auth hot path (Atlantis)
- Token parsing and claim-based user context hydration.
- Caching of user/session/sku/class-list data reduces DB pressure.

## 4) Reliability controls identified
- Queue fallback patterns and listener/job separation.
- Multiple queue connections (apollo, assignments, raw-json-sqs, autograde response).
- Cached token retrieval with refresh/recreate fallback in service helpers.
- Job retry configuration (example: GradeTestInstance has retries).

## 5) Bottlenecks and risk areas

## 5.1 Cross-service synchronous dependency chain
- User-facing requests often depend on multiple downstream services.
- Potential impact:
  - latency amplification
  - cascading failures

## 5.2 Heavy payload processing in request cycle
- XML parsing and deep relationship loading in assessment flows.
- Risk:
  - elevated response times under high concurrent test-taking.

## 5.3 Eventual-consistency windows
- Status/passback/reporting updates happen after primary state update.
- Risk:
  - temporary mismatch between learner view and downstream systems.

## 5.4 Cache coherence complexity
- Extensive cache usage with different keys/TTLs/services.
- Risk:
  - stale reads or inconsistent behavior after updates without robust invalidation discipline.

## 6) Database and indexing posture
- Apollo schema includes relationship indexes and status/time indexes on core tables.
- Hermes state/statement tables include registration and agent-registration indexes.
- Atlantis identity schema includes uniqueness and lookup indexes around user, realm, token records.

## 7) Recommended reliability improvements
1. Introduce explicit timeout budgets and circuit-breaker patterns for high-fanout endpoints.
2. Standardize retry + dead-letter queue handling across all external call paths.
3. Expand idempotency keys for submit/status endpoints where duplicate requests are possible.
4. Add distributed tracing across sync + async boundaries.
5. Define SLOs for assessment submit, grading completion, and status propagation latency.

## 8) Recommended performance improvements
1. Precompute/cache expensive assessment metadata transformations.
2. Add targeted load tests around interaction update and submit workflows.
3. Review ORM eager-loading plans for item/interaction graph assembly.
4. Evaluate pagination/windowing on heavy history/summary endpoints.
5. Instrument queue lag dashboards for grading and reporting consumers.

## 9) Interview-ready narrative
- Reliability is achieved by separating primary user writes from downstream side effects.
- Performance hinges on assessment graph retrieval efficiency and queue health.
- Main maturity step is stronger observability and resilience standardization across service boundaries.

## 10) Evidence files reviewed
- apollo/config/queue.php
- apollo/app/Jobs/GradeTestInstance.php
- apollo/app/Listeners/ProcessBUTestInstanceUpdates.php
- apollo/app/Http/Controllers/TestInstanceController.php
- apollo/app/Http/Controllers/TestItemInteractionInstanceController.php
- apollo/app/Services/MetisApi/MetisApiService.php
- apollo/app/Utils/Helper/AuthNToken.php
- atlantis/src/main/java/atlantis/config/MemcachedConfiguration.java
- atlantis/src/main/java/atlantis/config/JwtAuthorizationFilter.java
- atlantis/src/main/java/atlantis/service/UserCacheService.java
- atlantis/src/main/java/atlantis/service/ClassListCacheService.java
- hermes/backend/lrs-app/src/main/resources/db/migration/V1__Initial.sql
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/service/impl/StatementServiceImpl.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/service/impl/StateServiceImpl.java

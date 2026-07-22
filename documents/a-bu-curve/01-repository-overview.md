# 01 - Repository Overview

## 1) What this workspace is
BU-Curve is a platform-level multi-repository workspace powering Benchmark Universe learning, assessment, tracking, and reporting flows. It is not a single service. The most interview-critical path is:

Athena/Learner Profile frontend -> Atlantis (identity, user/collective APIs) -> Apollo (assessment lifecycle) -> Hermes (xAPI/LRS) -> Metis/Bubba and downstream systems.

## 2) Top-level repositories and likely product responsibilities
- apollo: assessment delivery, submission, grading workflows, ORR flows, and assignment-test-instance lifecycle.
- atlantis: identity/session JWT APIs, OAuth2 authorization server/resource server behavior, user/collective/term/school/class/group APIs.
- hermes: xAPI statement and state storage/retrieval, optional Kinesis streaming.
- learner-profile: learner profile backend + frontend (student profile analytics and skill progression views).
- athena: cmi5 player frontend and interaction player frontend.
- metis: assignment orchestration frontend/server components.
- bubba, supervisor, visual-glossary: additional product modules and supporting services/apps.

## 3) Primary interview architecture slice
### Identity and access
- Atlantis exposes custom JWT login at /api/v1/auth/create-token and OAuth2 token endpoint at /oauth/token.
- Atlantis supports signed JWT bearer processing and opaque-token introspection paths.
- Learner-profile backend also validates bearer tokens and can call external AuthN service for token status.

### Assessment execution
- Apollo routes include instance submission, status update, grading, and interaction-level manual scoring.
- Apollo dispatches grading/background jobs and emits TestInstanceChanged/TestInstanceGraded events for side effects.

### Learning telemetry
- Athena cmi5 player posts statements and activity state to Hermes xAPI endpoints.
- Hermes persists statements/state in MySQL JSON columns and can stream enriched events to Kinesis.

### Student profile and analytics
- Learner-profile frontend fetches school/class/student context from Atlantis, statements from Hermes, assignment summaries from Metis, and skills data from learner-profile backend.

## 4) Technology stack by major service

## Atlantis
- Runtime: Spring Boot 3.4.x, Java 21, WAR packaging.
- Security: spring-security, oauth2-authorization-server, oauth2-resource-server, jjwt.
- Persistence: Spring Data JPA + Flyway + MySQL.
- Infra: OpenFeign, AWS parameter store, Memcached, Micrometer/OTel.

## Apollo
- Runtime: Lumen 10, PHP >= 8.2.
- Security: Lumen Passport and JWT verification path.
- Persistence/queues: MySQL, Redis, SQS queue connections.
- Domain: test assignment/instance/interactions, ORR, AGCR integrations.

## Hermes
- Runtime: Spring Boot 3.3.x, Java 21.
- Security: JWT filter + optional authz authorization manager.
- Persistence: Flyway + MySQL statements/state tables.
- Messaging/reliability: AWS Kinesis SDK, Resilience4j, Memcached.

## Learner Profile backend
- Runtime: Spring Boot 3.3.x, Java 21.
- Security: JWT auth filters + authz manager.
- Integrations: Atlantis/AuthN/AuthZ/Bubba via Feign.
- Data/cache: Redshift connector, Memcached.

## Frontends (Athena / Learner Profile / Metis)
- Runtime: React + Redux + custom webpack pipelines.
- API clients: axios/fetch with bearer token injection.
- Routing: HashRouter in cmi5 player; BrowserRouter in learner-profile frontend.

## 5) Key cross-service dependencies in code
- Athena cmi5 constants map to Atlantis, Bubba, Hermes xAPI/state, Metis, Apollo, Authz URLs.
- Athena middleware obtains JWT by SID from Bubba and then pulls user data from Atlantis.
- Learner-profile frontend URLs map Atlantis lists, Hermes statements, Metis assignments summary, and learner-profile standards/progression endpoints.
- Apollo emits events/listeners that update BU assignment status and perform grade passback.

## 6) Data stores and messaging surfaces
- MySQL-backed domain data in Atlantis/Apollo/Hermes.
- Hermes statement/state payloads persisted in JSON columns.
- SQS-backed async jobs in Apollo (grading/autograde/assignment side effects).
- Optional Kinesis delivery stream ingestion in Hermes for telemetry forwarding.
- Memcached usage across Atlantis, Hermes, learner-profile for performance/caching.

## 7) External integration surface (high level)
- AuthN/AuthZ services for token and policy decisions.
- Bubba for user/session and component/resource related calls.
- Metis for assignment status/details flows.
- LMS grade passback paths in Apollo listeners.
- AWS services: Parameter Store, Kinesis, S3/SES (service-specific usage).

## 8) What to emphasize in interviews
- Platform integration depth over single-service coding.
- Strong event-driven assessment side effects (status propagation + passback).
- Clear separation between identity APIs, assessment engine, telemetry/LRS, and learner analytics.
- Hybrid auth model (custom JWT + OAuth2 client credentials + opaque token introspection).

## 9) Key evidence files reviewed
- atlantis/build.gradle
- atlantis/src/main/java/atlantis/config/WebSecurity.java
- atlantis/src/main/java/atlantis/config/BecResourceServerConfiguration.java
- atlantis/src/main/java/atlantis/config/AuthorizationServerConfig.java
- atlantis/src/main/java/atlantis/config/JwtAuthenticationFilter.java
- atlantis/src/main/java/atlantis/config/JwtAuthorizationFilter.java
- atlantis/src/main/java/atlantis/controller/UserController.java
- atlantis/src/main/java/atlantis/controller/SchoolController.java
- atlantis/src/main/java/atlantis/controller/ClassController.java
- atlantis/src/main/java/atlantis/controller/DistrictTermController.java
- apollo/composer.json
- apollo/routes/web.php
- apollo/bootstrap/app.php
- apollo/app/Http/Middleware/Authenticate.php
- apollo/app/Http/Middleware/CheckOauthOrJwt.php
- apollo/app/Http/Middleware/OAuthClient.php
- apollo/app/Http/Controllers/TestInstanceController.php
- apollo/app/Http/Controllers/TestItemInteractionInstanceController.php
- apollo/app/Listeners/ProcessBUTestInstanceUpdates.php
- apollo/app/Listeners/PushGradePassback.php
- apollo/app/Jobs/GradeTestInstance.php
- hermes/backend/lrs-app/build.gradle
- hermes/backend/lrs-app/src/main/resources/application.properties
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/controller/StatementExtensionController.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/controller/StateController.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/service/impl/StatementServiceImpl.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/service/impl/StateServiceImpl.java
- learner-profile/backend/learner-profile-app/build.gradle
- learner-profile/backend/learner-profile-app/src/main/resources/application.properties
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/security/LearnerProfileSecurityConfiguration.java
- athena/frontend/cmi5player/src/utils/constants.js
- athena/frontend/cmi5player/src/utils/api/interceptor.js
- athena/frontend/cmi5player/src/redux/middleware/appMiddleware.js
- learner-profile/frontend/student-profile/src/utils/urls.js
- learner-profile/frontend/student-profile/src/services/httpClient.js
- learner-profile/frontend/student-profile/src/Main.js
- metis/frontend/benchmark-academy/src/redux/slices/assignmentsData/index.js

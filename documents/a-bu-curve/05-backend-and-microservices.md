# 05 - Backend and Microservices Deep Dive

## 1) Core backend services in the interview narrative
- Atlantis: identity, user/collective domain APIs, token issuance/validation.
- Apollo: assessment instance lifecycle, interaction updates, grading, side effects.
- Hermes: LRS for xAPI statements and activity state, optional telemetry stream.
- Learner-profile backend: standards/progression/profile APIs with policy enforcement.

## 2) Atlantis backend deep dive

## Security and token architecture
- Separate security filter chains for:
  - OAuth/auth login paths (including /api/v1/auth/create-token)
  - General API paths requiring authentication/authorization.
- Supports signed JWT path and opaque token introspection path.
- OAuth2 authorization server maps token endpoint to /oauth/token.

## Important API domains
- User APIs including /api/v1/user/ids with optional includeCollectives.
- School/class/group/term APIs used by frontends and other services.
- User preference APIs under /api/users/userPreferences/*.

## 3) Apollo backend deep dive

## Route topology and middleware
- routes/web.php groups endpoints by middleware:
  - check_oauth_jwt for mixed OAuth/JWT acceptance
  - auth for user-token protected operations
  - oauth_client for OAuth-only client operations
- bootstrap/app.php registers middleware aliases and providers.

## Assessment lifecycle
- TestInstanceController handles:
  - submitTest
  - submitLinearTest
  - updateStatus
  - updateGrade
  - submitORRTestInstance
- TestItemInteractionInstanceController handles interaction updates and manual scoring paths.
- GradeTestInstance job performs async grading workflow.

## Side effects and events
- TestInstanceChanged triggers ProcessBUTestInstanceUpdates listener for BU/metis status propagation.
- TestInstanceGraded triggers PushGradePassback listener for LMS passback paths.

## Queue and async model
- Queue config defines multiple SQS/Redis connections for apollo and related workflows.
- Grading and integration jobs are dispatched to configured queue connections.

## 4) Hermes backend deep dive

## API surface
- StatementExtensionController:
  - GET/POST/PUT on /xAPI/v1/statements
- StateController:
  - POST/PUT/GET/DELETE /xAPI/v1/activities/state
  - POST /xAPI/v1/activities/state/clone

## Persistence model
- statements table with JSON document, agent/verb/activity/registration fields plus statement_id and timestamps.
- state table with JSON document and keys for agent/activity/state/registration.

## Processing pipeline
- StatementServiceImpl validates, persists, and conditionally forwards statements.
- LrsServiceImpl handles legacy receive-event conversion and optional stream delivery.
- KinesisDataServiceImpl enriches with roles/collectives and retries failed stream writes.

## 5) Learner-profile backend deep dive

## Security model
- Distinct filter chains for:
  - /api/v1/** (JWT authorization filter + optional authz manager)
  - /standards-hierarchy/** (Authn token validation filter)
- Additional token endpoint /jwt/token proxies Authn service token creation for client credentials use cases.

## Functional API surface
- /api/v1/learner-profile
- /api/v1/learner-profile/users
- /api/v1/learner-profile/standards-users
- /api/v1/learner-profile/skills-progression
- /api/v1/learner-profile/standards/{userId}

## 6) Service-to-service integration pattern
- Feign clients + OAuth client credentials used for AuthN/AuthZ/Atlantis/Bubba integrations.
- Memcached used for token/role-related caching to reduce repeated remote calls.

## 7) Backend quality and deployment pattern
- Java services use Gradle + JaCoCo coverage checks in CI.
- Apollo uses PHP lint + PHPUnit and deployment workflows.
- Deployments target Elastic Beanstalk through shared GitHub workflow templates.

## 8) Interview-ready backend strengths
- Clear separation of domain responsibilities across services.
- Event-driven side effects in assessment lifecycle.
- LRS model with durable JSON persistence plus optional stream fan-out.
- Centralized auth/token concerns concentrated in Atlantis and service-specific filters.

## 9) Risks and technical debt indicators
- Mixed legacy/new auth paths and middleware patterns increase cognitive load.
- Multiple integration dependencies can produce cascading failures without robust fallback.
- Some service docs contain older setup assumptions; runtime files are more reliable sources.

## 10) Evidence files reviewed
- atlantis/src/main/java/atlantis/config/WebSecurity.java
- atlantis/src/main/java/atlantis/config/BecResourceServerConfiguration.java
- atlantis/src/main/java/atlantis/config/AuthorizationServerConfig.java
- atlantis/src/main/java/atlantis/config/JwtAuthenticationFilter.java
- atlantis/src/main/java/atlantis/config/JwtAuthorizationFilter.java
- atlantis/src/main/java/atlantis/config/OpaqueTokenIntrospectionConfig.java
- atlantis/src/main/java/atlantis/controller/UserController.java
- atlantis/src/main/java/atlantis/controller/SchoolController.java
- atlantis/src/main/java/atlantis/controller/ClassController.java
- atlantis/src/main/java/atlantis/controller/DistrictTermController.java
- atlantis/src/main/java/atlantis/controller/UserPreferenceController.java
- apollo/routes/web.php
- apollo/bootstrap/app.php
- apollo/config/queue.php
- apollo/app/Http/Middleware/Authenticate.php
- apollo/app/Http/Middleware/CheckOauthOrJwt.php
- apollo/app/Http/Middleware/OAuthClient.php
- apollo/app/Http/Controllers/TestInstanceController.php
- apollo/app/Http/Controllers/TestItemInteractionInstanceController.php
- apollo/app/Listeners/ProcessBUTestInstanceUpdates.php
- apollo/app/Listeners/PushGradePassback.php
- apollo/app/Jobs/GradeTestInstance.php
- apollo/app/Events/TestInstanceChanged.php
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/security/LrsSecurityConfiguration.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/config/JwtAuthorizationFilter.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/controller/StatementExtensionController.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/controller/StateController.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/service/impl/StatementServiceImpl.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/service/impl/StateServiceImpl.java
- hermes/backend/lrs-app/src/main/resources/db/migration/V1__Initial.sql
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/security/LearnerProfileSecurityConfiguration.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/config/AuthnFilter.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/config/JwtAuthorizationFilter.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/controller/LearnerProfileController.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/controller/OauthTokenController.java

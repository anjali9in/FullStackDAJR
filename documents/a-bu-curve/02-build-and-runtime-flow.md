# 02 - Build and Runtime Flow

## 1) Purpose
This document explains how to run the core platform services locally and how code changes move from PR to deployment.

## 2) Practical service set for interview demos
For end-to-end login -> content -> assessment -> telemetry -> profile stories, the minimum useful set is:
- atlantis (identity + user/collectives APIs)
- apollo (assessment APIs + grading jobs)
- hermes (xAPI statements/state)
- learner-profile backend (skills/profile APIs)
- one frontend (athena cmi5player or learner-profile student-profile)

## 3) Runtime prerequisites matrix
- Java 21 + Gradle: atlantis, hermes, learner-profile backend
- PHP 8.2 + Composer: apollo
- Node/Yarn: athena and learner-profile frontend
- MySQL: atlantis/apollo/hermes
- Optional infra for parity: Memcached, Redis, SQS/Kinesis stubs or cloud access

## 4) Recommended local startup order
1. Databases and caches first (MySQL, Redis, Memcached)
2. Atlantis (token issuance and user/collective APIs)
3. Apollo and Hermes (assessment + xAPI)
4. Learner-profile backend
5. Frontend application

This order avoids early frontend failures due to unavailable auth/user APIs.

## 5) Local run entry points

## Atlantis
- Build/run from Gradle wrapper.
- Supports Docker compose with app + MySQL in repository.
- Exposes auth and OAuth token endpoints used by other modules.

## Apollo
- Docker-first path in repo: docker-compose up, composer install inside container.
- Non-docker path supports php local server for quick iterations.
- Queue behavior depends on environment and may require Redis/SQS-compatible setup.

## Hermes
- Gradle bootRun path documented in backend README.
- Requires application properties for DB/JWT and often reduced local cloud wiring.
- xAPI endpoints available under /xAPI/v1/*.

## Learner-profile backend
- Gradle bootRun path documented in backend README.
- Requires DB, JWT secret, and authn/authz integration configuration.

## Frontends
- Athena cmi5player: webpack start/build scripts from package.json.
- Learner-profile student-profile: webpack start/build/test scripts from package.json.

## 6) End-to-end runtime flow (representative)
1. Frontend obtains or rehydrates token context (sid/jwt/user).
2. Frontend calls Atlantis/Bubba endpoints for user and context data.
3. Assessment launch/resolution calls go through Bubba/Metis/Apollo URLs.
4. Learner actions produce xAPI statements sent to Hermes.
5. Apollo updates assignment/test instance statuses and may dispatch grading jobs.
6. Status/score side effects propagate to BU assignment systems and passback integrations.

## 7) Build and quality gates by repo

## Atlantis
- Gradle test + JaCoCo report + coverage verification in CI.
- Swagger validation workflow stage before deploy.

## Apollo
- Swagger validation, PHP lint, PHPUnit coverage in CI.
- Build/deploy path targets Elastic Beanstalk with environment routing.

## Hermes
- Gradle test + coverage verification, then build/deploy reusable workflow.

## Learner-profile backend
- Gradle test reusable workflow + build/deploy workflow.
- Separate flyway workflow for schema migrations.

## Athena and learner-profile frontend
- Yarn test/lint/build reusable workflows.
- Static artifact deployment (CloudFront/S3 style workflow abstraction).

## 8) Deployment flow summary
- Trigger: PR/open/sync/merge, release publish, or release branch push.
- Shared workflow resolves environment and branch metadata.
- Quality gates run first (tests/lint/swagger/coverage).
- If gates pass and deployment condition matches environment rules, deployment job runs.

## 9) Common local failure points and fixes
- Missing environment variables/secrets: most common startup blocker.
- Token mismatch across services: confirm JWT secret and expected issuer usage per service.
- Frontend host mapping errors: verify all REACT_APP_* base URLs.
- Queue/caching assumptions: disable or stub unavailable cloud services for local run.
- Legacy docs vs current runtime: prefer current build files/workflows over older README notes.

## 10) Evidence files reviewed
- atlantis/.github/workflows/config.yml
- atlantis/build.gradle
- atlantis/docker-compose.yml
- apollo/.github/workflows/backend.yml
- apollo/composer.json
- apollo/docker-compose.yml
- apollo/docker-setup.sh
- hermes/.github/workflows/backend.yml
- hermes/backend/lrs-app/build.gradle
- hermes/backend/lrs-app/src/main/resources/application.properties
- learner-profile/.github/workflows/learner-profile-app-gradle-backend.yml
- learner-profile/.github/workflows/flyway.yml
- learner-profile/backend/learner-profile-app/build.gradle
- learner-profile/backend/learner-profile-app/src/main/resources/application.properties
- athena/.github/workflows/cmi5player-standalone-react-frontend.yml
- athena/frontend/cmi5player/package.json
- learner-profile/.github/workflows/frontend.yml
- learner-profile/frontend/student-profile/package.json

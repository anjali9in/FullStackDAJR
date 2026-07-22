# 18 - Interview Questions and Model Answers

## 1) Architecture and system design

Q1. How would you explain this platform architecture in 60 seconds?
- Model answer:
  - It is a distributed learning platform where Atlantis handles identity/user context, Apollo handles assessment lifecycle and grading orchestration, Hermes stores xAPI telemetry, and learner-profile aggregates progression insights. Frontends orchestrate these APIs and rely on tokenized requests. Async events/queues handle side effects like status sync, reporting, and grade passback.

Q2. Why split Apollo and Hermes instead of storing telemetry in Apollo?
- Model answer:
  - Apollo is transactional assessment runtime state, while Hermes is xAPI-focused telemetry with JSON document persistence and telemetry-specific query patterns. Splitting reduces domain coupling and lets telemetry evolve without impacting core assessment transaction paths.

Q3. What consistency model does the platform use?
- Model answer:
  - Strong consistency for primary request writes inside each service DB transaction; eventual consistency for cross-service side effects via events/queues (status propagation, reporting, passback).

## 2) Authentication and authorization

Q4. What token models are in use?
- Model answer:
  - Signed user JWTs, OAuth2 client credentials tokens, and opaque-token introspection paths in specific chains. Apollo additionally uses route-group middleware to accept JWT, OAuth, or both based on endpoint class.

Q5. How is replay risk handled?
- Model answer:
  - Current implementation includes contextual sec_hash checks and token expiry handling, but explicit refresh-token one-time rotation/reuse detection should be strengthened. I would implement token-family tracking and atomic reuse detection.

Q6. What is a key auth complexity risk?
- Model answer:
  - Mixed token acceptance increases compatibility but can create endpoint-level ambiguity if contracts and tests are not explicit.

## 3) Assessment lifecycle and backend

Q7. Walk through submit to grade flow.
- Model answer:
  - Frontend submits responses to Apollo; Apollo persists interaction data and updates test instance status, dispatches grading jobs, emits change events, then graded events trigger downstream status synchronization and optional LMS passback.

Q8. How do you handle partial progress and resume?
- Model answer:
  - Interaction-level writes and test-taker state fields support incremental save; Hermes activity state adds additional runtime context for launch/resume experiences.

Q9. How are teacher-led and ORR flows different?
- Model answer:
  - They are modeled as specialized extensions with dedicated routes/models/listeners. ORR has reporting events and snapshot payload generation; teacher-led flows focus on assignment/form history and educator operations.

## 4) Reliability and performance

Q10. Where are major bottlenecks?
- Model answer:
  - Deep assessment graph assembly and high-frequency interaction updates in Apollo; cross-service synchronous dependency chains; queue lag in grading/reporting paths.

Q11. What reliability controls already exist?
- Model answer:
  - Queue-based decoupling, retries in select jobs/clients, cache layers for hot lookups, and route-level auth isolation to prevent broad blast radius.

Q12. What would you improve first?
- Model answer:
  - Unified observability (traces across sync/async boundaries), stricter idempotency on submit/status operations, and standardized timeout/circuit-breaker policies.

## 5) CI/CD and operations

Q13. How is release quality enforced?
- Model answer:
  - Pipelines run reusable workflow stages for env mapping, tests/lint/swagger/coverage checks, then deploy to environment-specific targets. Flyway migrations are explicit for learner-profile DB changes.

Q14. Why reusable workflows?
- Model answer:
  - They keep multi-repo behavior consistent and reduce YAML drift, making governance and rollout rules easier to maintain.

## 6) Behavioral and ownership questions

Q15. Describe a complex bug you could debug here.
- Model answer:
  - Delayed grade visibility: I would trace instance status transitions, grading job execution, listener emissions, Metis update calls, and frontend refresh paths to isolate whether the issue is primary write, async queue lag, or downstream sync failure.

Q16. How do you communicate tradeoffs to non-engineers?
- Model answer:
  - I separate user-critical success paths from secondary side effects, then explain latency/reliability budgets and what eventual consistency means for what users see and when.

## 7) Follow-up drill questions
- How do you guarantee one grading job per submission?
- What is your strategy for duplicate submit requests?
- How do you harden token refresh against replay under concurrency?
- What metrics would you alert on for grading and passback?

## 8) Evidence files reviewed
- docs/project-understanding/06-authentication-authorization-flow.md
- docs/project-understanding/07-refresh-token-replay-prevention.md
- docs/project-understanding/09-assessment-flow.md
- docs/project-understanding/13-system-design.md
- docs/project-understanding/14-deployment-cicd.md
- docs/project-understanding/15-security-review.md
- docs/project-understanding/16-reliability-performance.md

# PROJECT MASTER GUIDE - BU-Curve Interview Prep

## 1) What this guide gives you
A complete, interview-ready way to explain the BU-Curve platform end-to-end with architecture confidence.

You can use this guide to answer:
- What the system is
- How requests flow across services
- How auth/authz/token handling works
- How assessment data is captured and graded
- How reporting/passback/deployments operate
- What risks exist and how you would improve them

## 2) 30-second pitch
BU-Curve is a distributed learning platform. Atlantis is the identity and user-domain hub, Apollo is the assessment lifecycle engine, Hermes is the xAPI telemetry store, and learner-profile composes analytics-oriented student insights. Frontends orchestrate these APIs with tokenized calls. The architecture mixes synchronous user flows with asynchronous event/queue side effects for grading, reporting, status propagation, and LMS passback.

## 3) 2-minute architecture walkthrough
1. User authenticates through Atlantis and receives tokenized session context.
2. Frontend loads user/collective context and content launch resources.
3. Assessment runtime calls Apollo for instances/items/interactions.
4. Interaction updates persist incrementally; submit triggers grading jobs.
5. Apollo emits events for downstream status updates and grade passback.
6. Hermes stores xAPI statements/state for telemetry and resume context.
7. Learner-profile aggregates standards/progression/activity for educator-facing analytics views.

## 4) Service-by-service cheat sheet
- Atlantis:
  - user login JWT issuance
  - OAuth2 client token issuance
  - user/collective/class/school/preference APIs
- Apollo:
  - assessment assignments/instances/interactions
  - grading jobs and status transitions
  - ORR and teacher-led workflows
- Hermes:
  - xAPI statements CRUD
  - activity state read/write/clone
- Learner-profile:
  - standards/progression/profile APIs
  - authn/authz integrated gateway behavior

## 5) Key diagrams to memorize
- High-level architecture: docs/project-understanding/03-high-level-architecture.md
- Auth/authz flow: docs/project-understanding/06-authentication-authorization-flow.md
- Assessment lifecycle: docs/project-understanding/09-assessment-flow.md
- DFD traces (login/content/assessment/reporting/logout): docs/project-understanding/10-data-flow-diagrams.md
- ERD/data model: docs/project-understanding/11-database-and-erd.md

## 6) Authentication and authorization summary
- Multiple auth contracts coexist:
  - signed user JWT
  - OAuth2 client tokens
  - opaque token introspection
- Atlantis is the anchor for token issuance and hybrid validation chains.
- Apollo route groups intentionally allow different auth models by endpoint class.
- Replay-hardening opportunity: explicit one-time refresh-token rotation/reuse detection.

## 7) Assessment lifecycle summary
- Assignment and test instances are modeled in Apollo.
- Responses are captured at interaction granularity.
- Submit triggers grade orchestration and status transitions.
- Events drive side effects:
  - status sync to assignment systems
  - ORR reporting payload generation
  - LMS grade passback

## 8) Deployment and operations summary
- Multi-repo GitHub Actions pipelines use reusable workflow templates.
- Common gates: tests, lint, swagger validation, coverage checks.
- Backends deploy WAR artifacts to Elastic Beanstalk.
- Frontends build and deploy static bundles.
- Learner-profile runs dedicated Flyway migration workflow.

## 9) Security and reliability summary
Security strengths:
- layered filter chains
- JWT + OAuth support
- route-level auth separation

Security priorities:
- remove default UI credentials
- upgrade hash strategy for context binding
- implement replay-safe refresh-token rotation
- avoid sensitive payload logging in auth-failure paths

Reliability priorities:
- improve cross-service observability and queue lag monitoring
- standardize retries and idempotency patterns
- reduce high-fanout sync dependency pressure

## 10) Interview storytelling plan
- Open with architecture map.
- Deep dive into one end-to-end flow (submit to grade).
- Show one security hardening decision.
- Show one reliability/scale improvement plan.
- Close with CI/CD and operational ownership.

Use supporting docs:
- stories: docs/project-understanding/17-interview-project-stories.md
- Q&A: docs/project-understanding/18-interview-questions.md
- gap plan: docs/project-understanding/19-learning-gaps.md

## 11) Suggested answer structure for difficult questions
- Context: what business/user path is involved.
- Boundary: which service owns what.
- Flow: sync path + async side effects.
- Risk: likely failure/security hotspot.
- Improvement: concrete, incremental fix.

## 12) Final confidence checklist
- Can you draw the 4 core services and their responsibilities from memory?
- Can you explain signed JWT vs OAuth token paths clearly?
- Can you trace submit -> grade -> status update -> passback without notes?
- Can you list top 3 security improvements and why?
- Can you explain CI/CD quality gates and deploy targets?

## 13) Complete document index
1. 01-repository-overview.md
2. 02-build-and-runtime-flow.md
3. 03-high-level-architecture.md
4. 04-frontend-architecture.md
5. 05-backend-and-microservices.md
6. 06-authentication-authorization-flow.md
7. 07-refresh-token-replay-prevention.md
8. 08-business-flows.md
9. 09-assessment-flow.md
10. 10-data-flow-diagrams.md
11. 11-database-and-erd.md
12. 12-api-catalog.md
13. 13-system-design.md
14. 14-deployment-cicd.md
15. 15-security-review.md
16. 16-reliability-performance.md
17. 17-interview-project-stories.md
18. 18-interview-questions.md
19. 19-learning-gaps.md

## 14) Evidence basis
This master guide synthesizes findings from all documents above and the code evidence listed in each file's evidence section.

# 17 - Interview Project Stories

## 1) How to use this document
Use these as STAR-format stories. Pick based on role focus:
- backend engineer
- full-stack engineer
- platform/integration engineer
- architecture interview

## 2) Story: End-to-end assessment lifecycle ownership

## Situation
Assessment operations spanned multiple services and teams, making it hard to explain runtime behavior and side effects during incident triage.

## Task
Create clear, code-backed understanding of submit -> grade -> status sync -> passback flow and identify failure points.

## Actions
- Traced Apollo route groups, controller methods, status transitions, queue jobs, and event listeners.
- Mapped TestInstanceChanged and TestInstanceGraded side effects into Metis updates and LMS passback.
- Documented ORR-specific reporting/event behavior separately from standard assessment flows.

## Result
- Established a deterministic narrative for assessment lifecycle.
- Improved ability to debug delayed grading or downstream mismatch incidents quickly.
- Produced architecture artifacts reusable by engineering and onboarding.

## Interview angle
- Demonstrates ownership of complex distributed workflows and operational reasoning.

## 3) Story: Authentication model unification analysis

## Situation
The platform had mixed auth paths: user JWT, OAuth client tokens, and opaque token introspection.

## Task
Explain auth behavior end-to-end and identify modernization priorities without breaking compatibility.

## Actions
- Traced Atlantis split security chains and token-processing logic.
- Mapped Apollo middleware groups and learner-profile/hermes filter behavior.
- Produced current-state vs target-state token lifecycle analysis, including refresh replay prevention design.

## Result
- Created clear auth matrix by endpoint family.
- Identified actionable security upgrades (token rotation/reuse detection, stronger hash approach, policy simplification).
- Reduced ambiguity in integration expectations across services.

## Interview angle
- Demonstrates balancing security hardening with backward compatibility constraints.

## 4) Story: Reporting and external integration resilience

## Situation
Business outcomes depend on side effects (ORR reporting, LMS passback), which can fail independently from primary assessment writes.

## Task
Characterize side-effect pipelines and isolate reliability risks.

## Actions
- Traced event listener registration and downstream service call paths.
- Identified asynchronous boundaries and eventual-consistency windows.
- Documented retry, queue, and fallback behaviors where present.

## Result
- Enabled clearer SLO discussions for primary vs secondary outcomes.
- Highlighted where observability and idempotency should be strengthened.

## Interview angle
- Demonstrates system thinking and pragmatic reliability engineering.

## 5) Story: Cross-repo CI/CD standardization comprehension

## Situation
Multi-repo architecture can drift operationally if pipelines are inconsistent.

## Task
Explain deployment governance and quality gates across Java, PHP, and frontend stacks.

## Actions
- Reviewed reusable workflow usage and per-repo pipeline differences.
- Mapped test/lint/swagger/coverage gates before deploy.
- Summarized environment branching and Flyway migration flow.

## Result
- Produced unified CI/CD narrative for architecture and DevOps interviews.
- Clarified how release confidence is maintained across heterogeneous services.

## Interview angle
- Demonstrates platform-level engineering awareness beyond code implementation.

## 6) Story: Frontend-to-backend integration traceability

## Situation
Frontend applications consumed many backend services with environment-driven URLs and token strategies, creating integration fragility.

## Task
Build precise traceability from route/middleware/action to backend endpoints and auth expectations.

## Actions
- Traced Athena middleware and interceptor request flow.
- Traced learner-profile frontend login/bootstrap/service modules.
- Mapped API dependency chains to backend service responsibilities.

## Result
- Improved confidence in explaining end-user journey and request path.
- Identified integration contract risk areas and testing opportunities.

## Interview angle
- Demonstrates full-stack reasoning and operational debugging readiness.

## 7) Story: Security debt discovery and prioritization

## Situation
Security controls existed but with uneven maturity and distributed responsibility.

## Task
Prioritize high-impact improvements with minimal disruption.

## Actions
- Performed static review of token/session handling and filters.
- Identified concrete risks (default creds in UI state, weak hash primitive usage, payload logging risk, replay controls).
- Proposed phased remediation roadmap.

## Result
- Actionable security improvement plan with immediate and medium-term milestones.
- Better shared language between engineering and security stakeholders.

## Interview angle
- Demonstrates practical security leadership in legacy-modern hybrid systems.

## 8) Fast story selector
- If asked about architecture: Story 1 + Story 2.
- If asked about reliability: Story 3 + Story 4.
- If asked about full-stack execution: Story 1 + Story 6.
- If asked about security: Story 2 + Story 7.

## 9) Evidence files reviewed
- docs/project-understanding/04-frontend-architecture.md
- docs/project-understanding/05-backend-and-microservices.md
- docs/project-understanding/06-authentication-authorization-flow.md
- docs/project-understanding/07-refresh-token-replay-prevention.md
- docs/project-understanding/08-business-flows.md
- docs/project-understanding/09-assessment-flow.md
- docs/project-understanding/14-deployment-cicd.md
- docs/project-understanding/15-security-review.md
- docs/project-understanding/16-reliability-performance.md

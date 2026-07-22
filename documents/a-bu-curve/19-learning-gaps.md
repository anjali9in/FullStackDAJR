# 19 - Learning Gaps and Study Plan

## 1) Purpose
Identify remaining knowledge gaps that could appear in interviews and define a practical plan to close them.

## 2) Gap inventory

## Gap A: Runtime behavior validation vs static trace confidence
- Current state:
  - analysis is code-based; no runtime execution was performed.
- Interview risk:
  - inability to quote real latency/error characteristics.
- Closure plan:
  - run local integration flows for login -> assessment submit -> grade -> status update.
  - capture request/response examples and timing.

## Gap B: Exact policy decisions in external Authz/AuthN services
- Current state:
  - integration points are clear, but external service internals are not fully visible.
- Interview risk:
  - uncertainty when asked about policy rule authoring or token authority internals.
- Closure plan:
  - review authz/authn repos or service contracts.
  - prepare one-page token/policy ownership matrix.

## Gap C: Production-scale queue and throughput characteristics
- Current state:
  - queue topology is visible, but throughput/lag metrics are not observed.
- Interview risk:
  - weak answers to scale/incident-response questions.
- Closure plan:
  - gather CloudWatch or equivalent dashboards for queue lag, retries, DLQ rates.
  - map SLO thresholds for grading and passback completion.

## Gap D: Data model completeness outside sampled schemas
- Current state:
  - Apollo, Atlantis, and Hermes schema coverage is strong; learner-profile persistence internals are less explicit in this snapshot.
- Interview risk:
  - uncertainty on deep learner-profile DB schema questions.
- Closure plan:
  - inspect learner-profile DB scripts/repo docs/environment migration assets.

## Gap E: Frontend integration test coverage for contract drift
- Current state:
  - endpoint constants and middleware flow are clear; contract test evidence not fully reviewed.
- Interview risk:
  - weaker mitigation story for environment drift and endpoint mismatch.
- Closure plan:
  - identify/add API contract smoke tests around critical flows.

## Gap F: Security hardening implementation status
- Current state:
  - key findings identified; implementation progress unknown.
- Interview risk:
  - inability to show closed-loop remediation ownership.
- Closure plan:
  - convert findings into prioritized tickets and track closure state.
  - collect before/after control evidence.

## 3) 14-day targeted prep plan

Day 1-3:
- run local end-to-end happy path and one failure path
- capture sequence screenshots/log snippets

Day 4-6:
- prepare auth ownership matrix (Atlantis/AuthN/AuthZ)
- rehearse refresh-token replay-safe design explanation

Day 7-9:
- collect queue and deploy pipeline operational metrics
- draft SLO-based answers for reliability interviews

Day 10-12:
- rehearse 5 STAR stories from doc 17
- answer 20 questions from doc 18 aloud with timeboxing

Day 13-14:
- final mock interview using master guide
- tighten weak answers with concrete file-backed examples

## 4) Interview red flags to avoid
- Overstating runtime validation when only static analysis was done.
- Claiming a single auth model when multiple token paths exist.
- Ignoring async eventual-consistency when explaining status/reporting behaviors.

## 5) Evidence files reviewed
- docs/project-understanding/17-interview-project-stories.md
- docs/project-understanding/18-interview-questions.md
- docs/project-understanding/15-security-review.md
- docs/project-understanding/16-reliability-performance.md
- docs/project-understanding/14-deployment-cicd.md

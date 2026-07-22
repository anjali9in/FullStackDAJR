# 15 - Security Review

## 1) Scope
Static code review of authentication, authorization, token handling, API protection, and selected frontend/session behaviors across Atlantis, Apollo, Hermes, and learner-profile.

## 2) Security architecture strengths observed
- Multi-layer API protection with service-level security filters.
- JWT validation in multiple services with expiration and signature checks.
- Optional contextual sec_hash check (IP + User-Agent derived) for replay-resistance signal.
- OAuth2 authorization-server support with registered clients and token persistence.
- Route-group middleware in Apollo separates auth models by use case.

## 3) Security findings and risks

## 3.1 High: default credentials present in learner-profile login component
- Evidence: login component initializes username/password defaults in UI state.
- Risk:
  - accidental credential leakage in demos/screenshots
  - misuse if deployed in non-dev contexts
- Recommendation:
  - remove default credentials from source
  - gate dev-only autofill behind environment checks not shipped to production

## 3.2 Medium: weak hash primitive for sec_hash generation
- Evidence: sec_hash generation uses MD5 + static salt in JWT utility.
- Risk:
  - MD5 is cryptographically weak
  - static salt lowers entropy resilience
- Context:
  - this hash is a contextual check, not primary authentication.
- Recommendation:
  - migrate to HMAC-SHA256 with secret rotation support
  - include canonical client context normalization to reduce false mismatch issues

## 3.3 Medium: sec_hash enforcement is feature-flag dependent
- Evidence: multiple services guard hash mismatch enforcement behind feature flags.
- Risk:
  - inconsistent replay defenses across environments
- Recommendation:
  - standardize default-on policy and document any exception paths

## 3.4 Medium: refresh-token replay controls are not explicit in application layers
- Evidence:
  - token refresh helper logic exists in Apollo AuthN helper
  - no clear app-layer token family/reuse detection model in reviewed code
- Risk:
  - replayed refresh tokens may remain usable until expiry depending on upstream behavior
- Recommendation:
  - implement one-time-use refresh token rotation + reuse detection (see doc 07)

## 3.5 Medium: unauthorized payload logging may leak sensitive request data
- Evidence: Hermes JWT filter logs payload content in unauthorized scenarios.
- Risk:
  - sensitive user data may enter logs
- Recommendation:
  - redact/disable body logging for auth failures
  - log only request metadata and trace IDs

## 3.6 Low: mixed token acceptance paths add policy complexity
- Evidence: signed JWT and opaque introspection paths coexist.
- Risk:
  - endpoint-level auth expectations can be misunderstood
- Recommendation:
  - maintain endpoint auth matrix and integration contract tests

## 3.7 Low: logout is primarily client-state based
- Evidence: frontend reset/close flows observed without a universal revoke endpoint in reviewed scope.
- Risk:
  - token remains valid until expiry if exfiltrated
- Recommendation:
  - adopt token revocation/blacklist strategy for sensitive contexts

## 4) Control inventory snapshot
- Authentication:
  - Atlantis user login JWT issuance.
  - OAuth2 client credentials endpoint.
- Authorization:
  - role/claim-based checks and optional authz manager in services.
  - Apollo route-group middleware contracts.
- Token handling:
  - signed JWT checks and expiration validation.
  - opaque introspection support.
- Session/inactivity:
  - frontend inactivity modal and session closure behavior.

## 5) Security maturity recommendations (priority roadmap)
1. Remove default credentials from frontend source immediately.
2. Implement replay-safe refresh token rotation and family revocation.
3. Replace MD5 sec_hash with HMAC-based design.
4. Harden logging policy to prevent request-body leakage.
5. Add automated security tests for token-type and middleware boundary behavior.

## 6) Interview-ready framing
- Current posture is pragmatic and layered, with strong central auth capabilities.
- Main modernization priorities are token lifecycle hardening and policy simplification.
- Risk areas are known and actionable with a clear implementation roadmap.

## 7) Evidence files reviewed
- learner-profile/frontend/student-profile/src/containers/Login/index.jsx
- atlantis/src/main/java/atlantis/utility/JwtUtility.java
- atlantis/src/main/java/atlantis/config/JwtAuthorizationFilter.java
- atlantis/src/main/java/atlantis/config/WebSecurity.java
- atlantis/src/main/java/atlantis/config/OpaqueTokenIntrospectionConfig.java
- apollo/app/Utils/Helper/AuthNToken.php
- apollo/app/Http/Middleware/CheckOauthOrJwt.php
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/config/JwtAuthorizationFilter.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/config/JwtAuthorizationFilter.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/security/LearnerProfileSecurityConfiguration.java
- athena/frontend/cmi5player/src/components/BuInactivityModal/index.jsx
- learner-profile/frontend/student-profile/src/Main.js
- docs/project-understanding/07-refresh-token-replay-prevention.md

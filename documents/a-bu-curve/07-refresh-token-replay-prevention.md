# 07 - Refresh Token Replay Prevention

## 1) Purpose
This document separates current implementation behavior from a recommended replay-safe refresh-token architecture.

## 2) Threat model
Refresh-token replay attack pattern:
1. Attacker steals refresh token from client/device/log/memory.
2. Legitimate user and attacker both submit refresh requests.
3. Without rotation + reuse detection, attacker can continuously mint access tokens.

Impact:
- Long-lived session hijack.
- Difficult detection if tokens are bearer-only and not sender-constrained.

## 3) Current observed implementation (code-backed)

## 3.1 AuthN helper behavior in Apollo
- AuthNToken utility caches:
  - token
  - refresh_token
  - created_at
- Access token is refreshed when token age threshold is reached.
- If refresh fails or is stale, helper obtains new token from AuthN /jwt/token.
- Cache TTL aligns with refresh token lifetime assumption (4 hours in helper logic).

## 3.2 Learner-profile token proxy behavior
- /jwt/token endpoint proxies AuthN token generation using client credentials.
- AuthnService supports token validity checks through /jwt/token/status.
- No explicit replay detection logic is visible in learner-profile layer.

## 3.3 Atlantis and broader auth flow context
- Atlantis supports JWT issuance and OAuth2 access tokens.
- Current files reviewed do not show a platform-wide, explicit refresh-token one-time-use registry, token-family graph, or reuse detection response workflow.

## 3.4 Conclusion on current state
- Refresh behavior exists for specific service integrations.
- Strong replay-prevention guarantees are not clearly enforced in the application layers reviewed.
- Protection level appears dependent on upstream AuthN server behavior and transport security.

## 4) Risk assessment against replay attacks
- If refresh tokens are reusable until expiry and can be used from multiple clients, replay risk is high.
- If refresh token value is logged or leaked once, attacker persistence may last until token family expiry.
- Cached refresh tokens in service layers can become a theft target if memory dumps or logs are exposed.

## 5) Recommended replay-safe design

## 5.1 Design principles
- One-time-use refresh tokens (rotation every refresh).
- Token family tracking with parent-child lineage.
- Reuse detection that revokes entire family on suspicious replay.
- Short access-token lifetime + bounded refresh absolute lifetime.
- Sender-constrained refresh tokens where feasible (mTLS or DPoP-like proof).

## 5.2 Minimal data model
- refresh_token_id (jti, unique)
- family_id
- parent_jti
- subject/client_id
- issued_at, expires_at
- consumed_at
- revoked_at
- device_fingerprint (optional)
- ip_hash/user_agent_hash (optional risk signals)

## 5.3 Refresh algorithm (reference)
1. Client submits refresh token T_n.
2. Server validates signature, expiry, revocation state, and family state.
3. If T_n already consumed/revoked:
   - mark family compromised
   - revoke active descendants
   - require full re-authentication
   - emit security event
4. If valid first use:
   - atomically mark T_n consumed
   - mint access token + new refresh token T_n+1
   - persist parent-child edge (T_n -> T_n+1)

## 5.4 Concurrency rule
- Use transaction + unique constraints to guarantee one successful consumer per refresh token.
- Second concurrent use must deterministically fail and trigger replay response.

## 6) Recommended API behavior
- Refresh success response returns new access token and rotated refresh token.
- Refresh failure response categories:
  - invalid_token
  - expired_token
  - replay_detected (security event id)
- Replay detection should return an error that forces the client to clear session and re-login.

## 7) Logging, detection, and observability
- Emit structured events for refresh attempts:
  - token_jti
  - family_id
  - subject/client_id
  - source metadata (ip hash, ua hash, region)
  - outcome (success, expired, replay_detected)
- Alert on:
  - replay_detected > threshold
  - multiple geographies for same family within short interval

## 8) Migration plan for BU-Curve services
1. Introduce token-family store in primary auth service (AuthN/Atlantis boundary decision needed).
2. Update refresh endpoint contract to return rotated token each time.
3. Update service helpers (Apollo, learner-profile, others) to always replace stored refresh token after refresh.
4. Add replay error handling in callers: clear cache, force new token acquisition or interactive re-auth.
5. Add dashboards and alerts for replay events.

## 9) Client and service hardening guidance
- Never log refresh tokens.
- Encrypt at rest if persisted.
- Keep refresh tokens server-side only where possible.
- Bind refresh tokens to client identity and optional device posture.
- Use HTTPS everywhere and strict secret management.

## 10) Interview framing
- Current system: pragmatic token caching/refresh for service continuity.
- Gap identified: explicit replay-safe rotation and family revocation are not clearly represented in app-layer code.
- Proposed evolution: add one-time token rotation with atomic reuse detection and incident-grade telemetry.

## 11) Evidence files reviewed
- apollo/app/Utils/Helper/AuthNToken.php
- apollo/app/Utils/Helper/OAuthToken.php
- apollo/app/Utils/Helper/AuthzOauthToken.php
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/controller/OauthTokenController.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/externalcalls/authn/service/AuthnService.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/externalcalls/authn/service/AuthnClient.java
- atlantis/src/main/java/atlantis/config/AuthorizationServerConfig.java
- atlantis/src/main/java/atlantis/config/WebSecurity.java
- atlantis/src/main/java/atlantis/config/OpaqueTokenIntrospectionConfig.java
- atlantis/src/main/java/atlantis/dto/AuthNTokenResponse.java

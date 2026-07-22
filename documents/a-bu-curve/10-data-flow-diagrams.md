# 10 - Data Flow Diagrams

## 1) Purpose
This document provides diagram-driven traces for the requested flows:
- login
- authentication/authorization
- content launch and retrieval
- assessment submission and grading
- reporting/passback
- logout/session-end behavior

## 2) Login and authentication flow
```mermaid
sequenceDiagram
  autonumber
  participant U as User
  participant FE as Frontend
  participant ATL as Atlantis

  U->>FE: Enter credentials/launch context
  FE->>ATL: POST /api/v1/auth/create-token
  ATL->>ATL: Authenticate realm+username+password
  ATL->>ATL: Generate JWT + sec_hash claims
  ATL-->>FE: token
  FE->>FE: Store token in app state/storage
```

## 3) Authentication + authorization decision flow
```mermaid
flowchart TD
  A[Incoming API request with Bearer token] --> B{Signed JWT?}
  B -->|Yes| C[JwtAuthorizationFilter parse claims]
  C --> D{Valid + not expired + sec_hash ok?}
  D -->|No| E[401 Unauthorized]
  D -->|Yes| F[Authorize by roles/policies]

  B -->|No| G[Opaque token introspection path]
  G --> H{Token active?}
  H -->|No| E
  H -->|Yes| F

  F --> I[Controller/Business logic]
```

## 4) Content launch and content data flow
```mermaid
sequenceDiagram
  autonumber
  participant FE as Athena CMI5 Player
  participant BUB as Bubba
  participant ATL as Atlantis
  participant AUTHZ as Authz
  participant HER as Hermes

  FE->>BUB: GET /jwt-by-sid (or launch token context)
  BUB-->>FE: jwt
  FE->>ATL: GET /user/ids?includeCollectives=true
  ATL-->>FE: User+collective metadata

  FE->>AUTHZ: userAction / authorization checks
  AUTHZ-->>FE: feature permissions

  FE->>BUB: /component/componentDetail + /resources/retrieve
  BUB-->>FE: launch metadata/resources

  FE->>HER: GET/POST /xAPI/v1/activities/state
  HER-->>FE: learner state payload
```

## 5) Assessment execution and grading flow
```mermaid
sequenceDiagram
  autonumber
  participant FE as Frontend
  participant APO as Apollo
  participant DB as Apollo DB
  participant Q as Queue
  participant MET as Metis/BU

  FE->>APO: GET /instances/{id}/assessment-items
  APO->>DB: Load test instance + items + interactions
  DB-->>APO: Data
  APO-->>FE: Assessment payload

  FE->>APO: PUT /interactions/{id}
  APO->>DB: Persist response
  APO-->>FE: Saved response

  FE->>APO: POST /instances/submit or /submit-linear-test
  APO->>DB: Mark submitted + timestamps
  APO->>Q: Dispatch GradeTestInstance / AGCR jobs
  APO->>APO: Emit TestInstanceChanged

  Q->>APO: Grading complete
  APO->>DB: Update scores/status
  APO->>APO: Emit TestInstanceGraded
  APO->>MET: Update assignment/user status
```

## 6) Reporting and grade-passback flow
```mermaid
flowchart LR
  A[TestInstanceGraded Event] --> B[PushGradePassback Listener]
  B --> C{LMS integration route}
  C -->|Janus enabled| D[POST /api/gradePassbackForLMS]
  C -->|Direct LMS path| E[Schoology/Clever/Canvas/GC handlers]

  F[ORR update/submit] --> G[OrrReportingEvent/OrrRubricReportingEvent]
  G --> H[ORR Reporting Service snapshot build]
  H --> I[Message stream push]
```

## 7) Logout/session-end flow
```mermaid
sequenceDiagram
  autonumber
  participant U as User
  participant FE as Frontend
  participant SVC as Platform APIs

  alt Manual logout
    U->>FE: Click logout
    FE->>FE: resetState/clear local auth state
    FE-->>U: Return to login/landing
  else Inactivity timeout
    FE->>FE: Show inactivity modal countdown
    U->>FE: No activity
    FE->>FE: closeApplication + local session reset
    FE-->>U: Session expired view
  end

  note right of SVC: No explicit universal token revocation
  note right of FE: Session termination is primarily client-state driven
```

## 8) Data classification by flow
- Identity/session data: user credentials, JWTs, sid context, role/collective claims.
- Content data: launch URLs, component metadata, resources, user preferences.
- Assessment data: instance status, item responses, interaction scores, comments.
- Telemetry data: xAPI statements and activity state documents.
- Reporting data: ORR snapshots, grade passback payloads, status propagation updates.

## 9) Observations
- The platform uses a hybrid of synchronous user-request flows and asynchronous event/queue side effects.
- Authentication is centralized but enforcement is distributed per service/middleware chain.
- Logout is mostly app-state/session behavior rather than a single platform-wide revoke endpoint.

## 10) Evidence files reviewed
- atlantis/src/main/java/atlantis/config/BecResourceServerConfiguration.java
- atlantis/src/main/java/atlantis/config/WebSecurity.java
- atlantis/src/main/java/atlantis/config/JwtAuthenticationFilter.java
- atlantis/src/main/java/atlantis/config/JwtAuthorizationFilter.java
- apollo/routes/web.php
- apollo/app/Http/Controllers/TestInstanceController.php
- apollo/app/Http/Controllers/TestItemInteractionInstanceController.php
- apollo/app/Listeners/PushGradePassback.php
- apollo/app/Listeners/Reporting/ORR/OrrReportingListener.php
- apollo/app/Listeners/Reporting/ORR/OrrRubricReportingListener.php
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/controller/StatementExtensionController.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/controller/StateController.java
- athena/frontend/cmi5player/src/utils/constants.js
- athena/frontend/cmi5player/src/utils/api/interceptor.js
- athena/frontend/cmi5player/src/components/BuInactivityModal/index.jsx
- learner-profile/frontend/student-profile/src/Main.js

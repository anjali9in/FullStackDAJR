# 12 - API Catalog

## 1) Purpose
This catalog summarizes high-value APIs across Atlantis, Apollo, Hermes, and learner-profile backend for interview readiness and integration understanding.

## 2) Authentication conventions
- User JWT: common for frontend user actions.
- OAuth client token: used by service clients and selected protected API groups.
- Mixed-mode middleware: Apollo has route groups that accept JWT, OAuth client, or both depending on path.

## 3) Atlantis API domains

## 3.1 Authentication and token endpoints
- POST /api/v1/auth/create-token
  - Purpose: user login -> signed JWT.
- POST /api/v1/oauth/create-token
  - Purpose: OAuth-authenticated clients mint user JWT for delegated scenarios.
- POST /oauth/token
  - Purpose: OAuth2 access token issuance via client credentials and configured grants.

## 3.2 User and collective domain
- POST /api/v1/user/ids
- GET /api/v1/user/id/{id}
- GET /api/v1/user/{id}/details-license
- GET /api/v1/user/{id}/permissions
- POST /api/v1/user/{id}/permissions
- GET /api/v1/classes/{id}/students
- GET /api/v1/schools/{id}/classes
- GET /api/v1/terms/district/{id}
- POST /api/v1/collectives/ids

## 3.3 Preferences and support APIs
- GET /api/users/userPreferences/preferences
- POST /api/users/userPreferences/updatepreferences
- POST /api/users/userPreferences/modifyStudentUserPreferences
- POST /api/v1/error/log

## 4) Apollo API domains

## 4.1 Core assessment runtime
- GET /instances/{instanceId}
- GET /instances/{instanceId}/assessment-items
- GET /instances/{instanceId}/assessment-item/{identifier}
- PUT /instances/{instanceId}
- POST /instances/submit
- POST /instances/submit-linear-test
- POST /instances/status
- POST /instances/grade

## 4.2 Interaction and scoring
- PUT /interactions/{id}
- POST /interactions/manual-score/{id}
- POST /interactions/manual-score/batch
- GET /items/{id}/interactions

## 4.3 Assignment and reporting operations
- GET /assignments/{id}
- GET /assignments/{id}/instances
- POST /assignments/update-eassessment-assignments
- POST /api/v1/getTestStatusSummary
- POST /api/v1/getTestStatusDetails

## 4.4 ORR and reading records
- POST /readingrecords/create
- POST /readingrecords/student/submit/{instanceId}
- POST /readingrecords/student/{instanceId}
- POST /readingrecords/manually-send-reporting/{id}
- POST /readingrecords/manually-send-rubric/{id}

## 4.5 Teacher-led assessments
- POST /teacher-led-assessment/create-assignment
- POST /teacher-led-assessment/update-assignment
- POST /teacher-led-assessment/delete-assignment
- GET /teacher-led-assessment/get-most-recent-sub-test/{studentId}/{classId}/{sid}/{subTestSku?}
- POST /teacher-led-assessment/get-history-by-collective-noun

## 4.6 Middleware/auth grouping in Apollo
- check_oauth_jwt group: mixed JWT/OAuth acceptance.
- auth group: authenticated user operations.
- oauth_client group: OAuth client-only operations (for ingestion/import style operations).

## 5) Hermes API domains

## 5.1 xAPI statements
- GET /xAPI/v1/statements
- POST /xAPI/v1/statements
- PUT /xAPI/v1/statements/{statementId}

## 5.2 xAPI activity state
- POST /xAPI/v1/activities/state
- PUT /xAPI/v1/activities/state
- GET /xAPI/v1/activities/state
- DELETE /xAPI/v1/activities/state
- POST /xAPI/v1/activities/state/clone

## 5.3 Legacy/event compatibility
- POST /event/receive

## 6) Learner-profile backend API domains

## 6.1 Learner profile domain
- POST /api/v1/learner-profile/users
- POST /api/v1/learner-profile/standards-users
- POST /api/v1/learner-profile/skills-progression
- GET /api/v1/learner-profile/standards/{userId}
- GET /api/v1/learner-profile/sku/cutscore/colorcode/{subskillUuid}

## 6.2 Standards hierarchy domain
- GET /standards-hierarchy/standards
- POST /standards-hierarchy/rollup
- GET /standards-hierarchy/cache/{cacheKey}
- POST /standards-hierarchy/rollup/{userId}

## 6.3 Token proxy endpoint
- POST /jwt/token
  - Purpose: client credential token generation via AuthN upstream.

## 7) Frontend-consumed endpoint constants (Athena example)
- Bubba:
  - /resources/retrieve
  - /component/componentDetail
  - /component/launch-url
  - /jwt-by-sid
- Atlantis:
  - /user/ids?includeCollectives=true
- Hermes:
  - /xAPI/v1/statements
  - /xAPI/v1/activities/state
- Metis:
  - assignment and presentation operations
- Authz:
  - /userAction authorization checks

## 8) Interview usage tips
- Present APIs by business capability, not by repository:
  - auth
  - content launch
  - assessment runtime
  - telemetry
  - profile analytics
- Explicitly call out Apollo middleware groups to explain why auth behavior differs by route set.
- Mention that many internal APIs are environment-routed and validated through reusable CI workflows.

## 9) Evidence files reviewed
- atlantis/src/main/java/atlantis/config/BecResourceServerConfiguration.java
- atlantis/src/main/java/atlantis/config/AuthorizationServerConfig.java
- atlantis/src/main/java/atlantis/controller/UserController.java
- atlantis/src/main/java/atlantis/controller/ClassController.java
- atlantis/src/main/java/atlantis/controller/SchoolController.java
- atlantis/src/main/java/atlantis/controller/DistrictTermController.java
- atlantis/src/main/java/atlantis/controller/UserPreferenceController.java
- atlantis/src/main/java/atlantis/controller/ErrorLogController.java
- atlantis/src/main/java/atlantis/oauth/controller/OauthAuthenticationController.java
- apollo/routes/web.php
- apollo/app/Http/Middleware/CheckOauthOrJwt.php
- apollo/app/Http/Middleware/OAuthClient.php
- apollo/app/Http/Controllers/TestInstanceController.php
- apollo/app/Http/Controllers/TestItemInteractionInstanceController.php
- apollo/app/Http/Controllers/TeacherLedAssessmentController.php
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/controller/StatementExtensionController.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/controller/StateController.java
- hermes/backend/lrs-app/src/main/java/com/benchmarkuniverse/lrs/controller/LegacyController.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/controller/LearnerProfileController.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/controller/StandardsHierarchyController.java
- learner-profile/backend/learner-profile-app/src/main/java/com/benchmarkuniverse/learnerprofile/controller/OauthTokenController.java
- athena/frontend/cmi5player/src/utils/constants.js

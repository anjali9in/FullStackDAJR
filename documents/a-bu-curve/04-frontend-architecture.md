# 04 - Frontend Architecture

## 1) Frontend landscape
The workspace contains multiple React frontends with distinct roles:
- Athena cmi5player: cmi5 launch/runtime, xAPI statements/state, assignment-linked launch behavior.
- Learner-profile student-profile: school/class/student selection and profile analytics views.
- Metis benchmark-academy frontend: assignment management and learner/teacher assignment operations.

## 2) Athena cmi5player architecture

## Routing and launch context
- Uses HashRouter routes with URL segments carrying registration/customization/sku/mode/sid and optional parent assignment context.
- Route handler component initializes Redux state from URL params and recovers launch URL from session storage when needed.

## State management
- Redux store with middleware-driven side effects.
- app reducer stores jwt, sid, userData, mode, statementHistory, and launch/UI state.
- package reducer (separate file set) tracks course/resource/state data and assignment-linked context.

## API and auth behavior
- axios interceptor injects Authorization header from request.jwt for all non-login calls.
- Middleware action flow includes:
  - GENERATE_JWT -> GET_JWT_BY_SID endpoint
  - GENERATE_USER_DATA -> Atlantis /api/v1/user/ids call
  - SEND_STATEMENT -> Hermes /xAPI/v1/statements POST
  - GET_STATEMENTS -> Hermes /xAPI/v1/statements GET
  - STATE_DATA_URL -> Hermes /xAPI/v1/activities/state read/write
- Service URL constants include Atlantis, Bubba, Hermes, Metis, Apollo, Authz, and websocket endpoints.

## Typical runtime chain (Athena)
1. Launch route parses URL and dispatches token/user bootstrap actions.
2. Resource and metadata fetches hydrate package/course context.
3. Statements and activity state read/write calls persist learner progress.
4. Assignment-linked calls update ACUS status and load child component context.

## 3) Learner-profile student-profile frontend architecture

## Routing and app shell
- BrowserRouter with Main component handling login state, preference gates, and profile navigation.
- Supports standalone mode with token/sid persistence and recovery logic.

## Data layer
- Redux Toolkit configureStore with app middleware.
- Shared httpClient wraps fetch and sets Authorization Bearer header when token is passed.

## Service modules
- atlantisService:
  - district terms, schools, classes, students, groups
  - user preference endpoints
- hermesService:
  - statement query for weekly login/activity analysis
- metisService:
  - assignment/assessment summary request
- learnerprofileService:
  - standards backbone, standards-users, skills progression
- authzService:
  - explicit authorization check for student profile read access

## Login and bootstrap chain
1. Login screen posts username/password/realm to Atlantis auth token endpoint.
2. Token saved to Redux appData, optionally recovered from storage on refresh.
3. Main component validates preference gate and loads selection/profile flows.
4. Services execute token-bearing API requests to Atlantis/Hermes/Metis/LP backend.

## 4) Metis frontend integration notes
- Reads persisted auth state from localStorage key persist:benchmarkuniverse.
- Axios instance sets bearer token from persisted auth.
- Assignment slices call:
  - Atlantis school/class/group/student endpoints
  - Metis assignment endpoints (list/count/update/status)
  - Bubba sidlogin and component-related helpers
- Student assignments page performs paging and status-filtered fetches.

## 5) Cross-frontend token patterns
- Athena: custom axios communicator receives jwt in request config and injects bearer.
- Learner-profile frontend: service wrappers pass token to fetch helpers.
- Metis frontend: token is pulled from persisted auth and auto-injected by axios interceptor.

## 6) Frontend interview talking points
- Strong middleware-based side effect orchestration in Athena.
- Explicit service module separation by domain in learner-profile frontend.
- Heavy platform coupling through environment-based base URLs.
- Clear token propagation and request decoration patterns.

## 7) Risks and improvement opportunities
- Launch URLs and state orchestration are complex and fragile without contract tests.
- Multiple token source strategies (url/store/storage) can cause subtle mismatch issues.
- Environment variable drift across apps can break cross-service paths.

## 8) Evidence files reviewed
- athena/frontend/cmi5player/src/containers/appRouter/index.jsx
- athena/frontend/cmi5player/src/utils/constants.js
- athena/frontend/cmi5player/src/utils/api/interceptor.js
- athena/frontend/cmi5player/src/redux/store/store.js
- athena/frontend/cmi5player/src/redux/reducers/appReducer.js
- athena/frontend/cmi5player/src/redux/actions/appActions.js
- athena/frontend/cmi5player/src/redux/middleware/appMiddleware.js
- learner-profile/frontend/student-profile/src/App.js
- learner-profile/frontend/student-profile/src/Main.js
- learner-profile/frontend/student-profile/src/containers/Login/index.jsx
- learner-profile/frontend/student-profile/src/utils/urls.js
- learner-profile/frontend/student-profile/src/services/httpClient.js
- learner-profile/frontend/student-profile/src/services/atlantisService.js
- learner-profile/frontend/student-profile/src/services/hermesService.js
- learner-profile/frontend/student-profile/src/services/metisService.js
- learner-profile/frontend/student-profile/src/services/learnerprofileService.js
- learner-profile/frontend/student-profile/src/services/authzService.js
- metis/frontend/benchmark-academy/src/Axios/index.js
- metis/frontend/benchmark-academy/src/redux/slices/assignmentsData/index.js
- metis/frontend/benchmark-academy/src/Pages/StudentAssignments/index.jsx

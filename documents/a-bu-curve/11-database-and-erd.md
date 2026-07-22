# 11 - Database and ERD

## 1) Scope and caveat
This ERD is a practical interview-focused data model synthesis from schema and migration files reviewed in this workspace. It is not an exhaustive dump of every table in each service.

## 2) Primary data stores by service
- Atlantis (gypsum): identity, realms, users, roles, OAuth/OIDC and authn token persistence.
- Apollo (marble): assessments, assignments, instances, interactions, ORR and teacher-led artifacts.
- Hermes (granite): xAPI statements and activity state JSON documents.
- Learner-profile backend: API/service layer reviewed; direct DB schema artifacts were not discovered in this workspace snapshot.

## 3) Apollo core ERD
```mermaid
erDiagram
  ASSESSMENT_TEST ||--o{ TEST_ASSIGNMENT : has_versions
  TEST_ASSIGNMENT ||--o{ ASSIGNMENTS : links
  TEST_INSTANCE ||--o{ ASSIGNMENTS : links
  TEST_STATUS ||--o{ TEST_INSTANCE : status_of
  TEST_INSTANCE ||--o{ TEST_ITEM_INSTANCE : contains
  TEST_ITEM_INSTANCE ||--o{ TEST_ITEM_INTERACTION_INSTANCE : records
  ASSESSMENT_TEST ||--o{ ORR_ASSIGNMENTS : source_for
  TEST_INSTANCE ||--|| ORR_ASSIGNMENTS : orr_instance
  TEACHER_LED_ASSESSMENTS ||--o{ TEACHER_LED_ASSESSMENT_FORMS : owns
  TEACHER_LED_ASSESSMENT_FORMS ||--o{ TEACHER_LED_ASSESSMENT_ASSIGNED_FORMS : assigned_as

  ASSESSMENT_TEST {
    int at_id PK
    string at_component_code
    text at_manifest_xml
    text at_test_xml
    float at_version
  }

  TEST_ASSIGNMENT {
    int ta_id PK
    int ta_assessment_original_id FK
    int ta_assessment_current_id
    int ta_bu_assignment_id
    int ta_bu_district_id
    int ta_bu_school_id
    text ta_preferences
  }

  TEST_STATUS {
    int ts_id PK
    string ts_name
    string ts_bu_status
    int ts_bu_status_id
  }

  TEST_INSTANCE {
    bigint ti_id PK
    int ti_user_id
    bigint ti_acus_id
    int ti_status_id FK
    int ti_collective_noun_id
    float ti_score
    string ti_grade
    datetime ti_submitted_at
    date ti_released_at
  }

  TEST_ITEM_INSTANCE {
    bigint tii_id PK
    bigint tii_test_instance_id FK
    string tii_item_identifier
    int tii_order_index
    int tii_final_elapse_time
  }

  TEST_ITEM_INTERACTION_INSTANCE {
    bigint tiii_id PK
    bigint tiii_item_instance_id FK
    text tiii_response
    string tiii_response_identifier
    float tiii_score
    float tiii_autograde_score
    int tiii_autograde_status
  }

  ASSIGNMENTS {
    bigint id PK
    int a_ta_id FK
    bigint a_ti_id FK
  }

  ORR_ASSIGNMENTS {
    int oa_id PK
    int oa_assessment_id FK
    bigint oa_test_instance_id FK
    int oa_bu_assignment_id
    datetime oa_complete
    float oa_accuracy
    int oa_fluency
  }

  TEACHER_LED_ASSESSMENTS {
    int tla_id PK
    string tla_name
  }

  TEACHER_LED_ASSESSMENT_FORMS {
    int tlaf_id PK
    int tlaf_tla_id FK
    string tlaf_form_name
  }

  TEACHER_LED_ASSESSMENT_ASSIGNED_FORMS {
    int taf_id PK
    int taf_tla_id FK
    int taf_tlaf_id FK
    int taf_learner_id
    int taf_bu_assignment_id
    int taf_test_status_id
  }
```

## 4) Atlantis identity/token ERD (selected)
```mermaid
erDiagram
  REALM ||--o{ USER : contains
  ROLE ||--o{ USER_ROLE : maps
  USER ||--o{ USER_ROLE : maps
  USER ||--o{ USER_EXTERNAL_ID : has
  OAUTH2_REGISTERED_CLIENT ||--o{ OAUTH2_AUTHORIZATION : issues
  AUTHN_ACCESS_TOKEN }o--|| USER : optional_user_binding

  REALM {
    int r_id PK
    string r_name
    int r_realm_type_id
  }

  USER {
    int u_id PK
    int u_realm_id FK
    string u_username
    string u_email
    string u_external_id
  }

  ROLE {
    int role_id PK
    string role_name
  }

  USER_ROLE {
    int user_id FK
    int role_id FK
  }

  USER_EXTERNAL_ID {
    int id PK
    int user_id FK
    string external_id
    string external_source
  }

  OAUTH2_REGISTERED_CLIENT {
    string id PK
    string client_id
    string client_name
    string authorization_grant_types
    string scopes
  }

  OAUTH2_AUTHORIZATION {
    string id PK
    string registered_client_id FK
    string principal_name
    blob access_token_value
    blob refresh_token_value
  }

  AUTHN_ACCESS_TOKEN {
    int id PK
    string token
    datetime expired_ttl_dt
    string user_id
    string client_id
    string refresh_token
  }
```

## 5) Hermes telemetry ERD
```mermaid
erDiagram
  STATEMENTS {
    bigint id PK
    int agent_id
    string verb_id
    string activity_id
    string registration_id
    timestamp created
    json document
  }

  STATE {
    bigint id PK
    int agent_id
    string activity_id
    string registration_id
    timestamp created
    timestamp updated
    json document
  }
```

## 6) Data lifecycle summary
- Atlantis:
  - creates and validates user/auth client token context.
  - persists OAuth2 authorization records and authn token records.
- Apollo:
  - stores assessment definitions and test execution state.
  - updates interaction-level responses and computed scores.
  - feeds downstream reporting and status synchronization.
- Hermes:
  - stores xAPI statement and state documents for learning telemetry and resume context.

## 7) Indexing and scale indicators observed
- Atlantis has targeted indexes for user identity lookups and token identifiers.
- Apollo has indexes around status/time and relationship joins on instance/interactions.
- Hermes has registration and agent-registration indexes for statement/state retrieval.

## 8) Interview talking points
- The strongest data backbone is in Apollo where operational assessment state is normalized from assignment to interaction granularity.
- Atlantis combines identity domain tables with evolving OAuth2 authorization-server persistence.
- Hermes intentionally uses JSON document columns to preserve xAPI payload shape while still indexing common lookup keys.

## 9) Evidence files reviewed
- apollo/database/schema/Marble.sql
- apollo/database/migrations/2023_09_12_123126_create_test_assignment_table.php
- apollo/database/migrations/2023_09_12_123127_create_assignments_table.php
- apollo/database/migrations/2023_09_12_123108_create_test_instance_table.php
- apollo/database/migrations/2023_09_12_123109_create_test_item_instance_table.php
- apollo/database/migrations/2023_09_12_123110_create_test_item_interaction_instance_table.php
- apollo/database/migrations/2023_09_12_123154_create_orr_assignments_table.php
- apollo/database/migrations/2023_09_12_123235_create_teacher_led_assessment_assigned_forms_table.php
- atlantis/src/main/resources/db/migration/V1__Initial.sql
- atlantis/src/main/resources/db/migration/V28__OAuth2Migration.sql
- atlantis/src/main/resources/db/Create_AuthN_Access_Token.sql
- hermes/backend/lrs-app/src/main/resources/db/migration/V1__Initial.sql

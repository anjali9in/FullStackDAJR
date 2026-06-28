# Explored Hibernate, JPA, Spring Data JPA

Revision notes for a 1-5 years backend developer working with Hibernate, JPA, DTOs, transactions, repositories, database operations, and security.

Current version context: Hibernate ORM 7.4 is the latest stable series, Hibernate ORM 8.0 is in development, and Hibernate 6.6 is in limited support. Spring Data JPA current docs target Jakarta Persistence APIs. In Spring Boot 3+/4+, imports use `jakarta.persistence.*`, not old `javax.persistence.*`.

---

# 1. Hibernate, JPA, Spring Data JPA In One Paragraph

JPA is a specification for object-relational mapping in Java. Hibernate is a popular implementation of JPA. Spring Data JPA is a Spring abstraction that reduces repository boilerplate on top of JPA/Hibernate. In real projects, entities map Java objects to database tables, repositories perform data operations, DTOs expose API-safe data, services manage business logic and transactions, and Hibernate handles SQL generation, persistence context, dirty checking, lazy loading, caching, and relationship mapping.

Interview answer:

```text
JPA is the standard specification. Hibernate is an implementation of that specification. Spring Data JPA builds on top of JPA and provides repository abstractions like JpaRepository, derived queries, pagination, specifications, and custom queries. In a Spring Boot project, I usually define entities, repositories, DTOs, service-layer transactions, and mapping between entities and DTOs.
```

---

# 2. Core Terms

| Term | Meaning |
|---|---|
| Entity | Java class mapped to DB table |
| Table | Database table |
| Field/Property | Entity attribute mapped to column |
| Primary Key | Unique row identifier |
| Persistence Context | First-level cache managed by EntityManager |
| EntityManager | JPA API for persist/find/merge/remove/query |
| Hibernate Session | Hibernate-specific API similar to EntityManager |
| Repository | Data access abstraction |
| DTO | API/request/response data object |
| Transaction | Atomic unit of work |
| Dirty Checking | Hibernate detects changed managed entities |
| Lazy Loading | Related data loaded only when accessed |
| Eager Loading | Related data loaded immediately |
| JPQL | JPA query language using entity names/fields |
| HQL | Hibernate query language, JPQL superset |
| Native Query | Raw SQL |
| N+1 Problem | One query for parent rows, then N more for children |
| Optimistic Locking | Version-based concurrent update protection |
| Pessimistic Locking | DB lock-based concurrent update protection |

---

# 3. Layered Backend Design

Typical Spring Boot structure:

```text
Controller -> Service -> Repository -> Database
      DTO        Entity       JPA/Hibernate
```

Example package structure:

```text
com.example.orders
  controller/
  service/
  repository/
  entity/
  dto/
  mapper/
  exception/
```

Rule of thumb:

- Controller handles HTTP request/response.
- DTO defines API contract.
- Service handles business logic and transactions.
- Repository handles data access.
- Entity models persistence state.
- Mapper converts DTO <-> entity.

Interview answer:

```text
I avoid exposing JPA entities directly from REST APIs. Entities are persistence models and may contain lazy relationships, internal fields, and bidirectional references. DTOs keep API contracts stable and prevent accidental data exposure.
```

---

# 4. Dependencies

Spring Boot Maven example:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Common additional tools:

- Flyway or Liquibase for schema migration.
- MapStruct for DTO mapping.
- Testcontainers for DB integration tests.
- Spring Security for authentication/authorization.
- Hypersistence Utils for advanced Hibernate types/utilities.

---

# 5. Entity Basics

```java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    protected User() {
        // Required by JPA.
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
```

Important:

- JPA needs a no-args constructor. It can be `protected`.
- Avoid public setters for everything if domain invariants matter.
- Avoid business logic in controllers.
- Prefer `Instant`, `LocalDate`, `LocalDateTime` over old `Date`.
- In Spring Boot 3+/Hibernate 6+, use `jakarta.persistence`.

---

# 6. Primary Key Generation

## `IDENTITY`

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

Common with MySQL auto-increment.

Tradeoff:

- Simple.
- Insert is needed immediately to get id.
- Can reduce insert batching efficiency.

## `SEQUENCE`

```java
@SequenceGenerator(
        name = "order_seq",
        sequenceName = "order_seq",
        allocationSize = 50
)
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
private Long id;
```

Common with PostgreSQL/Oracle.

Good for batching with allocation.

## UUID

```java
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

Use when:

- IDs generated outside DB.
- Distributed systems.
- Public identifiers.

Tradeoff:

- Larger index size.
- Random UUID can hurt clustered index locality.

Interview answer:

```text
For MySQL I often see IDENTITY because of auto-increment. For databases with sequences, SEQUENCE with allocation size is better for batching. UUID is useful for distributed/public IDs but has indexing tradeoffs.
```

---

# 7. Entity Lifecycle States

## Transient

New object, not associated with persistence context.

```java
User user = new User("Anjali", "a@example.com");
```

## Managed/Persistent

Attached to persistence context.

```java
entityManager.persist(user);
```

or loaded:

```java
User user = entityManager.find(User.class, 1L);
```

## Detached

Was managed, but persistence context closed/cleared.

```java
entityManager.detach(user);
```

## Removed

Scheduled for delete.

```java
entityManager.remove(user);
```

Interview answer:

```text
Hibernate tracks changes only for managed entities inside a persistence context. If an entity is detached, changes are not automatically synchronized unless it is merged or reloaded in a transaction.
```

---

# 8. Persistence Context And Dirty Checking

Persistence context is the first-level cache for a transaction/session.

```java
@Transactional
public void changeEmail(Long userId, String email) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

    user.changeEmail(email);
    // No explicit save required for managed entity.
}
```

Why no `save()`?

```text
The entity is managed. Hibernate detects changes during flush and generates UPDATE.
```

Dirty checking flow:

```text
Load entity -> Hibernate stores snapshot -> code modifies entity -> flush compares current state with snapshot -> SQL UPDATE
```

Interview hack:

```text
Inside @Transactional, modifying a managed entity is enough. Calling save again is often unnecessary, though it is commonly seen.
```

---

# 9. EntityManager Operations

```java
entityManager.persist(entity);   // insert new managed entity
entityManager.find(User.class, id); // find by primary key
entityManager.merge(entity);     // copy detached state into managed entity
entityManager.remove(entity);    // delete managed entity
entityManager.flush();           // synchronize persistence context to DB
entityManager.clear();           // detach all entities
entityManager.detach(entity);    // detach one entity
```

`persist` vs `merge`:

```text
persist attaches a new entity.
merge copies detached entity data into a managed instance and returns that managed instance.
```

Example:

```java
User managed = entityManager.merge(detachedUser);
```

Use returned `managed`, not necessarily the original detached object.

---

# 10. Spring Data JPA Repository

```java
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByActiveTrue();

    boolean existsByEmail(String email);
}
```

`JpaRepository` gives:

- `save`
- `findById`
- `findAll`
- `delete`
- `existsById`
- pagination/sorting
- batch-related methods

Repository hierarchy:

```text
Repository
CrudRepository
PagingAndSortingRepository
JpaRepository
```

Interview answer:

```text
Spring Data JPA generates repository implementations at runtime. Method names can derive queries, and @Query can define JPQL/native SQL when names become complex.
```

---

# 11. Derived Query Methods

```java
List<User> findByName(String name);

List<User> findByNameContainingIgnoreCase(String name);

List<User> findByActiveTrue();

List<User> findByCreatedAtBetween(Instant start, Instant end);

Optional<User> findFirstByEmailOrderByCreatedAtDesc(String email);

List<Order> findTop10ByStatusOrderByCreatedAtDesc(OrderStatus status);
```

Common keywords:

- `And`, `Or`
- `Between`
- `LessThan`, `GreaterThan`
- `Before`, `After`
- `IsNull`, `IsNotNull`
- `Like`, `Containing`, `StartingWith`, `EndingWith`
- `IgnoreCase`
- `OrderBy`
- `Top`, `First`
- `ExistsBy`

Do not write giant method names:

```java
findByStatusAndCreatedAtBetweenAndCustomerEmailContainingIgnoreCaseAndTotalAmountGreaterThan...
```

For complex filters, use:

- `@Query`
- Specification
- Querydsl
- Criteria API
- custom repository implementation

---

# 12. JPQL, HQL, Native Query

## JPQL

JPQL uses entity names and entity fields.

```java
@Query("""
    select u
    from User u
    where u.email = :email
    """)
Optional<User> findUserByEmail(@Param("email") String email);
```

## Projection DTO Query

```java
@Query("""
    select new com.example.user.dto.UserSummaryDto(u.id, u.name, u.email)
    from User u
    where u.active = true
    """)
List<UserSummaryDto> findActiveUserSummaries();
```

## Native SQL

```java
@Query(
    value = """
        select *
        from users
        where email = :email
        """,
    nativeQuery = true
)
Optional<User> findByEmailNative(@Param("email") String email);
```

Use native query when:

- Database-specific feature is needed.
- Query is too complex for JPQL.
- Performance requires tuned SQL.
- Using CTE/window/vendor functions not mapped well.

Security rule:

```text
Use parameters. Never concatenate user input into JPQL or SQL.
```

Good:

```java
@Query("select u from User u where u.email = :email")
Optional<User> findByEmailSafe(@Param("email") String email);
```

Bad:

```java
"select * from users where email = '" + email + "'"
```

---

# 13. DTOs

DTO means Data Transfer Object. It is used for API request/response or cross-layer data transfer.

## Request DTO

```java
public record CreateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email
) {
}
```

## Response DTO

```java
public record UserResponse(
        Long id,
        String name,
        String email,
        boolean active
) {
}
```

## Why DTOs

- Avoid exposing entity internals.
- Avoid lazy loading serialization issues.
- Prevent over-posting attacks.
- Keep API stable even if entity changes.
- Hide sensitive fields like password hash.
- Control response shape.

Bad:

```java
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userService.getUser(id);
}
```

Good:

```java
@GetMapping("/{id}")
public UserResponse getUser(@PathVariable Long id) {
    return userService.getUser(id);
}
```

---

# 14. Entity DTO Mapping

## Manual Mapper

```java
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isActive()
        );
    }

    public User toEntity(CreateUserRequest request) {
        return new User(request.name(), request.email());
    }
}
```

Manual mapping is fine for small projects.

## MapStruct

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(CreateUserRequest request);
}
```

Good for:

- Large projects.
- Many DTOs.
- Compile-time generated mapping.

## Projection Instead Of Mapping

Interface projection:

```java
public interface UserSummary {
    Long getId();
    String getName();
    String getEmail();
}
```

Repository:

```java
List<UserSummary> findByActiveTrue();
```

Use projections when:

- Read-only query.
- Need only selected columns.
- Want to avoid loading full entity.

---

# 15. Controller Service Repository Example

## Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }
}
```

## Service

```java
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User(request.name(), request.email());
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }
}
```

## Repository

```java
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
```

---

# 16. Transactions

Use `@Transactional` at service layer.

```java
@Transactional
public void placeOrder(CreateOrderRequest request) {
    Order order = orderRepository.save(new Order(request.userId()));
    paymentService.charge(request.paymentId(), request.amount());
    inventoryService.reserve(request.items());
    order.markPaid();
}
```

If runtime exception happens, transaction rolls back by default.

## Read-Only Transaction

```java
@Transactional(readOnly = true)
public List<UserResponse> listUsers() {
    return userRepository.findByActiveTrue()
            .stream()
            .map(userMapper::toResponse)
            .toList();
}
```

Benefits:

- Documents intent.
- Can optimize flush behavior.
- Helps avoid accidental writes.

## Rollback Rules

By default:

- Rolls back on `RuntimeException` and `Error`.
- Does not roll back on checked exceptions unless configured.

```java
@Transactional(rollbackFor = IOException.class)
public void importUsers() throws IOException {
    // ...
}
```

## Propagation

Common propagation:

| Propagation | Meaning |
|---|---|
| `REQUIRED` | Join existing transaction or create new one |
| `REQUIRES_NEW` | Suspend current and create new transaction |
| `SUPPORTS` | Join if exists, otherwise non-transactional |
| `MANDATORY` | Must have existing transaction |
| `NOT_SUPPORTED` | Run without transaction |
| `NESTED` | Nested transaction using savepoint if supported |

Default is `REQUIRED`.

## Isolation

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public void updateStock() {
}
```

Common isolation:

- `READ_UNCOMMITTED`
- `READ_COMMITTED`
- `REPEATABLE_READ`
- `SERIALIZABLE`

Interview answer:

```text
I keep transactions at service layer because one business use case may involve multiple repositories. Controller should not manage transactions. Repository methods are too low-level for business transaction boundaries.
```

---

# 17. Transaction Pitfalls

## Self Invocation

```java
public void outer() {
    inner(); // @Transactional on inner may not apply
}

@Transactional
public void inner() {
}
```

Why:

```text
Spring @Transactional works through proxies. Calling a method inside the same class bypasses the proxy.
```

Fix:

- Put transactional method in another Spring bean.
- Call through proxy carefully.
- Put transaction at public service method entry.

## Private Method

`@Transactional` on private method does not work with proxy-based Spring AOP.

## Checked Exception

Checked exception does not rollback by default.

## Lazy Loading Outside Transaction

```text
LazyInitializationException happens when lazy relation is accessed after session/persistence context is closed.
```

Fix:

- Fetch required data in service transaction.
- Use DTO projection.
- Use `JOIN FETCH` or `@EntityGraph`.
- Avoid Open Session In View as a lazy fix for APIs.

---

# 18. Relationships

## One-To-Many / Many-To-One

Order belongs to one user. User has many orders.

```java
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }
}
```

Recommended:

```text
Make @ManyToOne LAZY explicitly. Many providers default ManyToOne to EAGER by JPA spec, which can cause unexpected joins.
```

## One-To-One

```java
@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
@JoinColumn(name = "profile_id")
private UserProfile profile;
```

## Many-To-Many

Direct many-to-many:

```java
@ManyToMany
@JoinTable(
        name = "student_courses",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
)
private Set<Course> courses = new HashSet<>();
```

Better for real projects:

```text
Use an explicit join entity when the relationship has fields like enrolledAt, status, createdBy.
```

```java
@Entity
public class StudentCourse {

    @EmbeddedId
    private StudentCourseId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    private Course course;

    private Instant enrolledAt;
}
```

---

# 19. Cascade And Orphan Removal

Cascade means operations on parent propagate to child.

Common cascade types:

- `PERSIST`
- `MERGE`
- `REMOVE`
- `REFRESH`
- `DETACH`
- `ALL`

Example:

```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItem> items = new ArrayList<>();
```

`orphanRemoval = true` means:

```text
If child is removed from parent collection, Hibernate deletes it from DB.
```

Use carefully:

```text
Do not blindly use CascadeType.REMOVE or CascadeType.ALL on @ManyToMany. It can delete shared records unexpectedly.
```

---

# 20. Fetch Types

JPA defaults:

| Relation | Default |
|---|---|
| `@OneToOne` | EAGER |
| `@ManyToOne` | EAGER |
| `@OneToMany` | LAZY |
| `@ManyToMany` | LAZY |

Best practice:

```text
Prefer LAZY by default. Fetch exactly what each use case needs with fetch join, entity graph, or DTO projection.
```

Example:

```java
@ManyToOne(fetch = FetchType.LAZY)
private User user;
```

---

# 21. N+1 Problem

Problem:

```java
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    System.out.println(order.getUser().getName());
}
```

SQL:

```text
1 query for orders
N queries for users
```

## Fix 1: JOIN FETCH

```java
@Query("""
    select o
    from Order o
    join fetch o.user
    where o.status = :status
    """)
List<Order> findByStatusWithUser(@Param("status") OrderStatus status);
```

## Fix 2: EntityGraph

```java
@EntityGraph(attributePaths = {"user", "items"})
List<Order> findByStatus(OrderStatus status);
```

## Fix 3: DTO Projection

```java
@Query("""
    select new com.example.order.dto.OrderListItemDto(
        o.id,
        u.name,
        o.totalAmount,
        o.status
    )
    from Order o
    join o.user u
    where o.status = :status
    """)
List<OrderListItemDto> findOrderListItems(@Param("status") OrderStatus status);
```

Interview answer:

```text
N+1 happens when Hibernate loads parent rows and then lazily loads a child/parent relation separately for each row. I fix it with fetch join, EntityGraph, batch fetching, or DTO projection depending on the use case.
```

---

# 22. Pagination And Sorting

```java
Page<User> findByActiveTrue(Pageable pageable);
```

Service:

```java
@Transactional(readOnly = true)
public Page<UserResponse> listUsers(int page, int size) {
    Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
    );

    return userRepository.findByActiveTrue(pageable)
            .map(userMapper::toResponse);
}
```

`Page` vs `Slice`:

| Type | Meaning |
|---|---|
| `Page` | Content + total count |
| `Slice` | Content + hasNext, no total count |
| `List` | Content only |

Use `Slice` when count query is expensive.

Warning:

```text
Fetch join with collection and pagination can produce wrong/inefficient results. Prefer DTO query, two-step query, or batch fetching.
```

---

# 23. Specifications

Useful for dynamic filters.

```java
public class UserSpecifications {

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<User> emailContains(String email) {
        return (root, query, cb) ->
                email == null ? null : cb.like(
                        cb.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"
                );
    }
}
```

Repository:

```java
public interface UserRepository
        extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
}
```

Usage:

```java
Specification<User> spec = Specification
        .where(UserSpecifications.hasStatus(status))
        .and(UserSpecifications.emailContains(email));

Page<User> result = userRepository.findAll(spec, pageable);
```

Use when:

- Many optional filters.
- Query should be composable.
- Avoid many derived query methods.

---

# 24. Query By Example

```java
User probe = new User();
probe.setActive(true);
probe.setEmail("gmail");

ExampleMatcher matcher = ExampleMatcher.matching()
        .withIgnoreNullValues()
        .withMatcher("email", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

Example<User> example = Example.of(probe, matcher);

List<User> users = userRepository.findAll(example);
```

Good for simple matching. Not ideal for complex joins/ranges.

---

# 25. Bulk Operations

Bulk update:

```java
@Modifying
@Query("""
    update User u
    set u.active = false
    where u.lastLoginAt < :cutoff
    """)
int deactivateInactiveUsers(@Param("cutoff") Instant cutoff);
```

Service:

```java
@Transactional
public int deactivateInactiveUsers(Instant cutoff) {
    return userRepository.deactivateInactiveUsers(cutoff);
}
```

Important:

```text
Bulk JPQL updates bypass persistence context and dirty checking. Already loaded entities may become stale.
```

Fix:

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
```

---

# 26. Batch Inserts And Updates

Useful properties:

```properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
```

For large imports:

```java
@Transactional
public void importUsers(List<CreateUserRequest> requests) {
    int batchSize = 50;

    for (int i = 0; i < requests.size(); i++) {
        User user = new User(requests.get(i).name(), requests.get(i).email());
        entityManager.persist(user);

        if (i > 0 && i % batchSize == 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }
}
```

Notes:

- `IDENTITY` strategy can reduce batching benefits.
- Clear persistence context to avoid memory growth.
- Use JDBC/bulk loading for very large imports.

---

# 27. Optimistic Locking

Use when concurrent updates may overwrite each other.

```java
@Version
private Long version;
```

Flow:

```text
T1 loads product version 1
T2 loads product version 1
T1 updates -> version becomes 2
T2 updates -> OptimisticLockException
```

Handle:

```java
try {
    productService.updatePrice(id, price);
} catch (ObjectOptimisticLockingFailureException ex) {
    throw new ConflictException("Product was updated by another user");
}
```

Use for:

- Product updates.
- Account/profile edits.
- Inventory with careful retry logic.

---

# 28. Pessimistic Locking

Locks rows in database.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select p from Product p where p.id = :id")
Optional<Product> findByIdForUpdate(@Param("id") Long id);
```

Use:

```java
@Transactional
public void reserveStock(Long productId, int quantity) {
    Product product = productRepository.findByIdForUpdate(productId)
            .orElseThrow();

    product.reserve(quantity);
}
```

Tradeoff:

- Stronger consistency.
- Lower concurrency.
- Risk of deadlocks/timeouts.

Interview answer:

```text
Optimistic locking is version-based and good when conflicts are rare. Pessimistic locking uses database locks and is useful when conflicts are likely or overselling must be strictly prevented.
```

---

# 29. Auditing

Enable auditing:

```java
@EnableJpaAuditing
@SpringBootApplication
public class App {
}
```

Base entity:

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @CreatedBy
    private Long createdBy;

    @LastModifiedBy
    private Long updatedBy;
}
```

Auditor provider:

```java
@Bean
AuditorAware<Long> auditorProvider() {
    return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getPrincipal)
            .map(CustomUserDetails.class::cast)
            .map(CustomUserDetails::id);
}
```

---

# 30. Soft Delete

Simple approach:

```java
private Instant deletedAt;
```

Repository query:

```java
List<User> findByDeletedAtIsNull();
```

Hibernate-specific approach:

```java
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Entity
public class User {
}
```

Tradeoff:

- Soft delete preserves data.
- Every query must handle deleted rows.
- Unique constraints become tricky.
- Index `deleted_at` if heavily filtered.

Interview note:

```text
Soft delete is not free. It impacts unique keys, queries, storage, and reporting. Sometimes archive tables are cleaner.
```

---

# 31. Validation

Request DTO:

```java
public record CreateProductRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal price,
        @Min(0) int stock
) {
}
```

Controller:

```java
public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
    return productService.create(request);
}
```

Entity-level validation can exist, but API validation belongs on DTOs.

Common annotations:

- `@NotNull`
- `@NotBlank`
- `@Email`
- `@Size`
- `@Min`
- `@Max`
- `@Positive`
- `@Past`
- `@Future`

---

# 32. Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return ResponseEntity.badRequest()
                .body(new ApiError("VALIDATION_ERROR", message));
    }
}
```

DTO:

```java
public record ApiError(String code, String message) {
}
```

---

# 33. Security For Data Operations

## SQL Injection Prevention

Safe:

```java
@Query("select u from User u where u.email = :email")
Optional<User> findByEmail(@Param("email") String email);
```

Unsafe:

```java
String query = "select * from users where email = '" + email + "'";
```

Rules:

- Always bind parameters.
- Avoid string concatenation for SQL/JPQL.
- Validate sort fields before dynamic sorting.
- Limit native query use.

## Dynamic Sort Security

Unsafe:

```java
Sort.by(request.getSortField());
```

If field comes from user directly, validate it:

```java
private static final Set<String> ALLOWED_SORTS = Set.of("name", "createdAt", "email");

public Sort sortBy(String field, String direction) {
    if (!ALLOWED_SORTS.contains(field)) {
        throw new BadRequestException("Invalid sort field");
    }

    Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

    return Sort.by(sortDirection, field);
}
```

## DTO Over-Posting Protection

Bad:

```java
public User update(@RequestBody User user) {
    return userRepository.save(user);
}
```

Attacker may send:

```json
{
  "id": 1,
  "role": "ADMIN",
  "active": true
}
```

Good:

```java
public record UpdateProfileRequest(
        @NotBlank String name
) {
}
```

Only expose fields user is allowed to update.

## Authorization At Service Layer

```java
@PreAuthorize("@orderSecurity.canViewOrder(authentication, #orderId)")
@Transactional(readOnly = true)
public OrderResponse getOrder(Long orderId) {
    return orderRepository.findById(orderId)
            .map(orderMapper::toResponse)
            .orElseThrow();
}
```

Security component:

```java
@Component
public class OrderSecurity {

    private final OrderRepository orderRepository;

    public boolean canViewOrder(Authentication authentication, Long orderId) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).id();
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }
}
```

Repository:

```java
boolean existsByIdAndUserId(Long orderId, Long userId);
```

## Multi-Tenant Data Filter

Always include tenant filter:

```java
Optional<Order> findByIdAndTenantId(Long id, Long tenantId);
```

Do not trust tenant id from request body. Read it from authenticated principal/token.

## Sensitive Data

Never return:

- Password hash.
- Tokens.
- Secret keys.
- Internal notes.
- Security flags.

Use:

```java
@JsonIgnore
private String passwordHash;
```

But better:

```text
Do not include sensitive fields in response DTO at all.
```

---

# 34. Password Storage

Do not store plain passwords.

```java
@Service
public class RegistrationService {

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        String hash = passwordEncoder.encode(request.password());
        User user = new User(request.email(), hash);
        userRepository.save(user);
    }
}
```

Config:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Rules:

- Use BCrypt/Argon2/PBKDF2.
- Never log passwords.
- Never return password hash.
- Use unique constraints for username/email.

---

# 35. Schema Migration

Do not rely on Hibernate `ddl-auto=update` in production.

Development:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Production:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Use Flyway:

```sql
-- V1__create_users.sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_email (email)
);
```

Why migration tools:

- Versioned DB changes.
- Repeatable deployments.
- Rollback planning.
- Team visibility.
- Production safety.

---

# 36. Hibernate Configuration

Common properties:

```properties
spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.default_batch_fetch_size=50
```

SQL logging for development:

```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

Warning:

```text
Do not enable bind parameter TRACE logging in production because sensitive data can leak to logs.
```

Open Session In View:

```properties
spring.jpa.open-in-view=false
```

Why disable:

- Prevent lazy loading from controller/view layer.
- Forces service layer to fetch required data.
- Avoids hidden queries during JSON serialization.

---

# 37. Performance Cheat Sheet

## Common Problems And Fixes

| Problem | Fix |
|---|---|
| N+1 queries | Fetch join, EntityGraph, DTO projection, batch fetch |
| Slow list API | Pagination, indexes, DTO projection |
| Loading too many columns | Projection/DTO query |
| LazyInitializationException | Fetch required data in transaction |
| Too many inserts slow | JDBC batching, sequence allocation, flush/clear |
| Large offset slow | Keyset pagination |
| Count query slow | Slice instead of Page, custom count query |
| Memory high in import | Flush/clear in batches |
| Entity graph too big | Split use cases, projection |

## DTO Projection For List APIs

```java
@Query("""
    select new com.example.order.dto.OrderRowDto(
        o.id,
        o.orderNumber,
        u.name,
        o.totalAmount,
        o.status,
        o.createdAt
    )
    from Order o
    join o.user u
    where o.status = :status
    order by o.createdAt desc
    """)
Page<OrderRowDto> findRowsByStatus(OrderStatus status, Pageable pageable);
```

## Batch Fetching

```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=50
```

This can reduce N+1 by loading lazy associations in batches.

---

# 38. Caching

## First-Level Cache

Persistence context cache. Always enabled.

```java
@Transactional
public void example(Long id) {
    User u1 = entityManager.find(User.class, id);
    User u2 = entityManager.find(User.class, id);
    // Same persistence context, second find usually does not hit DB.
}
```

## Second-Level Cache

Shared across sessions, optional.

Use carefully for:

- Read-mostly reference data.
- Countries, currencies, settings.

Avoid for:

- Frequently changing data.
- Security-sensitive data unless carefully configured.

## Query Cache

Caches query result identifiers, not always beneficial.

Interview answer:

```text
First-level cache is per persistence context and always there. Second-level cache is optional and shared. I use second-level cache only for read-mostly data after measuring.
```

---

# 39. Entity Equality

Avoid equality based on mutable fields.

Common approach:

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User other)) return false;
    return id != null && id.equals(other.id);
}

@Override
public int hashCode() {
    return getClass().hashCode();
}
```

Be careful with Lombok:

```text
Do not use @Data blindly on entities. It generates equals/hashCode/toString using fields and relationships, which can trigger lazy loading or recursion.
```

Prefer:

```java
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

Avoid including relationships in `toString`, `equals`, `hashCode`.

---

# 40. JSON Serialization Issues

Bidirectional relationship:

```java
User -> orders
Order -> user
```

Returning entity directly can cause:

- Infinite recursion.
- Lazy loading exception.
- Extra queries.
- Sensitive data exposure.

Better:

```text
Return DTOs from controllers.
```

If needed:

```java
@JsonIgnore
private User user;
```

But DTOs are cleaner.

---

# 41. Inheritance Mapping

## Single Table

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
public abstract class Payment {
}
```

Pros:

- Fast queries.
- One table.

Cons:

- Many nullable columns.

## Joined

```java
@Inheritance(strategy = InheritanceType.JOINED)
```

Pros:

- Normalized.

Cons:

- Joins needed.

## Table Per Class

```java
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
```

Usually less common.

Interview answer:

```text
SINGLE_TABLE is usually fastest but can create sparse columns. JOINED is normalized but costs joins. I avoid inheritance mapping unless it truly models the domain well.
```

---

# 42. Embeddables

Use for value objects.

```java
@Embeddable
public class Address {
    private String line1;
    private String city;
    private String postalCode;
}
```

Entity:

```java
@Embedded
private Address shippingAddress;
```

Attribute override:

```java
@AttributeOverrides({
    @AttributeOverride(name = "line1", column = @Column(name = "billing_line1")),
    @AttributeOverride(name = "city", column = @Column(name = "billing_city"))
})
@Embedded
private Address billingAddress;
```

---

# 43. Enums

Bad:

```java
@Enumerated(EnumType.ORDINAL)
private OrderStatus status;
```

Good:

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 30)
private OrderStatus status;
```

Why:

```text
ORDINAL stores enum position. Reordering enum values corrupts meaning. STRING is safer and readable.
```

---

# 44. Date And Time

Use:

- `Instant` for timestamps.
- `LocalDate` for dates without time.
- `LocalDateTime` when local timestamp is truly needed.

Example:

```java
private Instant createdAt;
private LocalDate birthDate;
```

Timezone advice:

```text
Store timestamps in UTC. Convert to user's timezone at API/UI boundary.
```

---

# 45. Testing JPA

## Repository Test

```java
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findsUserByEmail() {
        userRepository.save(new User("Anjali", "a@example.com"));

        Optional<User> user = userRepository.findByEmail("a@example.com");

        assertThat(user).isPresent();
    }
}
```

## Testcontainers

Use real DB engine for integration tests.

```java
@Testcontainers
@SpringBootTest
class UserIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
```

Why:

```text
H2 can behave differently from MySQL/PostgreSQL. Testcontainers catches real SQL dialect, migration, index, and constraint issues.
```

---

# 46. Common Data Operations

## Create

```java
@Transactional
public ProductResponse create(CreateProductRequest request) {
    Product product = new Product(request.name(), request.price());
    return mapper.toResponse(productRepository.save(product));
}
```

## Read

```java
@Transactional(readOnly = true)
public ProductResponse get(Long id) {
    return productRepository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
}
```

## Update

```java
@Transactional
public ProductResponse update(Long id, UpdateProductRequest request) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    product.updateName(request.name());
    product.changePrice(request.price());

    return mapper.toResponse(product);
}
```

## Delete

```java
@Transactional
public void delete(Long id) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    productRepository.delete(product);
}
```

## Exists

```java
if (userRepository.existsByEmail(email)) {
    throw new DuplicateResourceException("Email already exists");
}
```

## Count

```java
long total = orderRepository.countByStatus(OrderStatus.PAID);
```

---

# 47. Interview Hacks

## Strong Answer Pattern

Use this format:

```text
Concept -> Why it exists -> Example -> Pitfall -> Best practice
```

Example:

```text
Lazy loading delays loading relationships until accessed. It avoids loading unnecessary data. But it can cause LazyInitializationException or N+1 queries. I usually keep relations lazy and fetch use-case-specific data using fetch joins, EntityGraph, or DTO projections.
```

## When Asked "Why Not Return Entity?"

Answer:

```text
Entity is persistence model, not API model. Returning it can expose sensitive fields, trigger lazy loading, create infinite recursion, couple API to database schema, and allow over-posting. DTOs solve this.
```

## When Asked "Why @Transactional In Service?"

Answer:

```text
Because transaction boundary should match business use case. A service method can call multiple repositories and external operations. Repository methods are too granular, and controllers should stay HTTP-focused.
```

## When Asked "How To Fix Slow JPA Query?"

Answer:

```text
First check generated SQL and EXPLAIN. Then verify indexes, N+1 queries, selected columns, pagination, joins, fetch strategy, transaction size, and whether DTO projection or native query is better.
```

## When Asked "persist vs merge?"

Answer:

```text
persist makes a new entity managed. merge copies state from a detached entity into a managed entity and returns the managed instance. The original object remains detached.
```

---

# 48. Common Mistakes

- Returning entities from controllers.
- Using `@Data` on entities.
- EAGER relationships everywhere.
- Ignoring N+1 queries.
- Calling `save()` unnecessarily for managed updates.
- Putting `@Transactional` on private methods.
- Expecting self-invoked transactional methods to work.
- Using `EnumType.ORDINAL`.
- Using `CascadeType.ALL` blindly.
- Using `orphanRemoval` without understanding delete behavior.
- Not validating DTOs.
- Concatenating SQL strings.
- Not checking generated SQL.
- Relying on `ddl-auto=update` in production.
- Using H2 only while production is MySQL/PostgreSQL.
- Forgetting indexes for foreign keys/filter columns.
- Large imports without `flush()` and `clear()`.
- Fetch joining collections with pagination.
- Keeping transactions open during slow external API calls.

---

# 49. Cheat Sheet: Annotations

## JPA/Hibernate

```text
@Entity = persistent class
@Table = table mapping
@Id = primary key
@GeneratedValue = id generation
@Column = column mapping
@Enumerated = enum mapping
@Temporal = old Date/Calendar mapping
@Version = optimistic locking
@ManyToOne = many rows to one parent
@OneToMany = one parent to many rows
@OneToOne = one-to-one relation
@ManyToMany = many-to-many relation
@JoinColumn = foreign key column
@JoinTable = join table
@Embedded = embedded value object
@Embeddable = value object class
@MappedSuperclass = base mapped fields
@Transient = not persisted
@EntityGraph = fetch graph
```

## Spring Data

```text
@Query = custom JPQL/native query
@Modifying = update/delete query
@Param = bind named parameter
@EnableJpaAuditing = enable auditing
@CreatedDate = creation timestamp
@LastModifiedDate = update timestamp
@CreatedBy = creator
@LastModifiedBy = updater
```

## Spring Transaction/Security

```text
@Transactional = transaction boundary
@PreAuthorize = method authorization
@PostAuthorize = after-return authorization
@Valid = validate request DTO
@RestControllerAdvice = global exception handling
```

---

# 50. Cheat Sheet: Fetch Strategy

```text
Default @ManyToOne = EAGER by JPA spec, set LAZY manually
Default @OneToMany = LAZY
Default @ManyToMany = LAZY
Default @OneToOne = EAGER
Prefer LAZY
Use fetch join for specific query
Use EntityGraph for reusable fetch plan
Use DTO projection for list/read APIs
Use batch fetch for reducing N+1
Avoid OSIV for REST APIs
```

---

# 51. Cheat Sheet: Repository Methods

```java
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
long countByStatus(UserStatus status);
List<User> findTop10ByActiveTrueOrderByCreatedAtDesc();
Page<User> findByStatus(UserStatus status, Pageable pageable);
Slice<User> findByActiveTrue(Pageable pageable);
void deleteByEmail(String email);
```

---

# 52. Cheat Sheet: Performance

```text
List page API -> DTO projection + pagination + index
Detail API -> fetch exactly required relations
Write API -> load managed entity, change fields, dirty checking
Bulk update -> @Modifying query, clear persistence context
Import -> batching + flush/clear
N+1 -> fetch join / EntityGraph / batch size / DTO
Lazy exception -> fetch in service transaction
Deep pagination -> keyset pagination
Slow query -> generated SQL + EXPLAIN + indexes
```

---

# 53. Quick Interview Questions And Answers

## What Is JPA?

JPA is a Java specification for ORM. It defines annotations and APIs like `EntityManager`, but it is not the implementation.

## What Is Hibernate?

Hibernate is an ORM framework and JPA implementation. It maps Java objects to database tables and manages persistence.

## What Is Spring Data JPA?

Spring Data JPA is a repository abstraction over JPA that reduces boilerplate and provides query methods, pagination, specifications, and custom queries.

## What Is Persistence Context?

Persistence context is a set of managed entities tracked by EntityManager. It acts as first-level cache and enables dirty checking.

## What Is Dirty Checking?

Hibernate automatically detects changes in managed entities and flushes SQL updates during transaction commit.

## What Is Lazy Loading?

Lazy loading delays loading related data until accessed.

## What Is LazyInitializationException?

It happens when lazy data is accessed outside an active persistence context/session.

## How To Avoid N+1?

Use fetch join, EntityGraph, DTO projection, or batch fetching.

## What Is Optimistic Locking?

Version-based concurrency control using `@Version`.

## What Is Pessimistic Locking?

Database lock-based concurrency control using lock modes such as `PESSIMISTIC_WRITE`.

## What Is DTO?

DTO is an API/request/response model used to transfer data without exposing persistence entity directly.

## Why Not Use Entity As Request DTO?

It causes over-posting risk, tight API-database coupling, validation confusion, and accidental field updates.

## What Is Cascade?

Cascade propagates operations from parent entity to child entities.

## What Is Orphan Removal?

It deletes child row when removed from parent's collection.

## What Is First-Level Cache?

Persistence context cache. It is enabled by default and scoped to EntityManager/session.

## What Is Second-Level Cache?

Optional shared cache across sessions. Useful for read-mostly data.

## What Is `save()` In Spring Data JPA?

`save()` persists new entities or merges existing ones depending on entity state detection.

## What Is Difference Between `getReferenceById` And `findById`?

`findById` queries immediately and returns `Optional`. `getReferenceById` returns a lazy proxy and may hit DB later when accessed.

## What Is Difference Between JPQL And Native SQL?

JPQL uses entity names and fields. Native SQL uses database tables and columns.

## What Is `@Modifying`?

It marks a repository `@Query` as update/delete/insert operation rather than select.

---

# 54. Final Interview Story

```text
In a Spring Boot backend, I use JPA entities for persistence, DTOs for API contracts, repositories for data access, and service methods as transaction boundaries. I keep relationships lazy by default and fetch what each use case needs using EntityGraph, fetch join, or DTO projection. I avoid returning entities directly from controllers because of lazy loading, recursion, security exposure, and API coupling.

For writes, I load managed entities inside @Transactional methods and rely on dirty checking. For concurrency, I use optimistic locking with @Version when conflicts are rare and pessimistic locking when strict DB locks are needed. For performance, I check generated SQL, EXPLAIN plans, indexes, N+1 queries, pagination, batch size, and projection usage. For security, I bind query parameters, validate DTOs, whitelist dynamic sort fields, enforce authorization in service layer, and never expose sensitive fields.
```

---

# 55. References

- Hibernate ORM documentation: https://hibernate.org/orm/documentation/
- Hibernate ORM releases: https://hibernate.org/orm/releases/
- Spring Data JPA reference: https://docs.spring.io/spring-data/jpa/reference/index.html
- Spring Boot SQL Databases reference: https://docs.spring.io/spring-boot/reference/data/sql.html
- Spring Framework Hibernate ORM integration: https://docs.spring.io/spring-framework/reference/data-access/orm/hibernate.html

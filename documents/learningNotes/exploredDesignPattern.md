# Explored Design Patterns

Revision notes for a 5 years developer / architect interview. The examples use Java-style code because most backend interviews expect object-oriented examples, but the ideas apply to any language.

## How To Think About Design Patterns

A design pattern is a reusable solution to a common design problem. It is not a library, framework, or rule. Use patterns when they reduce coupling, make behavior easier to change, or clarify ownership. Do not force a pattern just to make code look advanced.

Good interview framing:

- **Intent**: What problem does the pattern solve?
- **Context**: Where does it fit in a real application?
- **Tradeoff**: What complexity does it introduce?
- **Example**: Can you show a small, practical implementation?
- **Alternatives**: When would another pattern be better?

Main categories:

- **Creational patterns**: Object creation logic.
- **Structural patterns**: Object/class composition.
- **Behavioral patterns**: Communication and responsibility between objects.
- **Enterprise/application patterns**: Common backend application structure.
- **Architectural/distributed patterns**: System-level design for scalability, reliability, and maintainability.

---

# 1. Creational Design Patterns

Creational patterns hide or organize object creation so callers are not tightly coupled to concrete classes.

## 1.1 Singleton

### Intent

Ensure a class has only one instance and provide a global access point to it.

### When To Use

- Shared configuration.
- Logger.
- Connection factory.
- Expensive stateless service.

### Avoid When

- The object has mutable shared state.
- It makes testing difficult.
- Dependency injection can manage the lifecycle.

### Java Example

```java
public final class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();

    private AppConfig() {
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public String getEnv() {
        return "prod";
    }
}
```

### Thread-Safe Lazy Singleton

```java
public final class DatabaseClient {
    private DatabaseClient() {
    }

    private static class Holder {
        private static final DatabaseClient INSTANCE = new DatabaseClient();
    }

    public static DatabaseClient getInstance() {
        return Holder.INSTANCE;
    }
}
```

### Interview Notes

- Eager singleton is simple and thread-safe.
- Lazy holder is also thread-safe and lazy.
- Enum singleton is safe from serialization/reflection issues.
- In Spring, singleton scope is default, so manual singleton is usually unnecessary.

---

## 1.2 Factory Method

### Intent

Define an interface for creating an object, but let subclasses or factory methods decide which concrete class to instantiate.

### When To Use

- Creation depends on input.
- Client should depend on an interface, not concrete classes.
- New implementations may be added later.

### Example

```java
interface NotificationSender {
    void send(String message);
}

class EmailSender implements NotificationSender {
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

class SmsSender implements NotificationSender {
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

class NotificationFactory {
    public static NotificationSender create(String channel) {
        return switch (channel) {
            case "email" -> new EmailSender();
            case "sms" -> new SmsSender();
            default -> throw new IllegalArgumentException("Unsupported channel: " + channel);
        };
    }
}
```

### Real Use Cases

- Payment gateway creation.
- Parser creation based on file type.
- Notification sender creation.
- Spring `FactoryBean`.

### Interview Notes

- Factory Method centralizes creation.
- It improves Open/Closed Principle when combined with registration or DI.
- If the factory has a huge `switch`, consider using a map of strategies/providers.

---

## 1.3 Abstract Factory

### Intent

Create families of related objects without specifying their concrete classes.

### When To Use

- Objects must be compatible with each other.
- You need to switch a complete family at runtime.

### Example

```java
interface Button {
    void render();
}

interface Checkbox {
    void render();
}

class WindowsButton implements Button {
    public void render() {
        System.out.println("Windows button");
    }
}

class WindowsCheckbox implements Checkbox {
    public void render() {
        System.out.println("Windows checkbox");
    }
}

class MacButton implements Button {
    public void render() {
        System.out.println("Mac button");
    }
}

class MacCheckbox implements Checkbox {
    public void render() {
        System.out.println("Mac checkbox");
    }
}

interface UIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

class WindowsFactory implements UIFactory {
    public Button createButton() {
        return new WindowsButton();
    }

    public Checkbox createCheckbox() {
        return new WindowsCheckbox();
    }
}

class MacFactory implements UIFactory {
    public Button createButton() {
        return new MacButton();
    }

    public Checkbox createCheckbox() {
        return new MacCheckbox();
    }
}
```

### Interview Notes

- Factory Method creates one product type.
- Abstract Factory creates a family of related product types.
- Useful for cloud providers, UI themes, database-specific implementations, and payment provider SDK wrappers.

---

## 1.4 Builder

### Intent

Construct complex objects step by step without large constructors.

### When To Use

- Many optional fields.
- Object should be immutable.
- Constructor has too many parameters.

### Example

```java
public class User {
    private final String name;
    private final String email;
    private final String phone;
    private final boolean active;

    private User(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.phone = builder.phone;
        this.active = builder.active;
    }

    public static class Builder {
        private final String name;
        private final String email;
        private String phone;
        private boolean active = true;

        public Builder(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}

User user = new User.Builder("Anjali", "anjali@example.com")
        .phone("9999999999")
        .active(true)
        .build();
```

### Interview Notes

- Builder improves readability.
- It avoids telescoping constructors.
- With Lombok, `@Builder` generates this pattern.
- Validate required fields in `build()`.

---

## 1.5 Prototype

### Intent

Create new objects by copying an existing object.

### When To Use

- Object creation is expensive.
- Many objects share most of their configuration.
- You need runtime-defined object templates.

### Example

```java
class ReportTemplate implements Cloneable {
    private String title;
    private String layout;

    public ReportTemplate(String title, String layout) {
        this.title = title;
        this.layout = layout;
    }

    public ReportTemplate copy() {
        return new ReportTemplate(this.title, this.layout);
    }
}

ReportTemplate monthly = new ReportTemplate("Monthly Sales", "TABLE");
ReportTemplate regional = monthly.copy();
```

### Interview Notes

- Prefer explicit copy constructors or `copy()` methods over Java `Cloneable`.
- Be clear about shallow copy vs deep copy.
- Useful in game objects, document templates, and workflow templates.

---

# 2. Structural Design Patterns

Structural patterns define how classes and objects are composed to form larger structures.

## 2.1 Adapter

### Intent

Convert one interface into another interface expected by the client.

### When To Use

- Integrating legacy code.
- Wrapping third-party APIs.
- Migrating from one interface to another.

### Example

```java
interface PaymentProcessor {
    void pay(int amount);
}

class StripeClient {
    void makePayment(int amountInCents) {
        System.out.println("Paid cents: " + amountInCents);
    }
}

class StripeAdapter implements PaymentProcessor {
    private final StripeClient stripeClient;

    StripeAdapter(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    public void pay(int amount) {
        stripeClient.makePayment(amount * 100);
    }
}
```

### Interview Notes

- Adapter changes interface, not behavior.
- Facade simplifies a subsystem.
- Decorator adds behavior.

---

## 2.2 Bridge

### Intent

Separate abstraction from implementation so both can vary independently.

### When To Use

- Multiple dimensions of variation cause class explosion.
- Example: shape type and rendering engine.

### Example

```java
interface Renderer {
    void renderCircle(double radius);
}

class SvgRenderer implements Renderer {
    public void renderCircle(double radius) {
        System.out.println("SVG circle: " + radius);
    }
}

class CanvasRenderer implements Renderer {
    public void renderCircle(double radius) {
        System.out.println("Canvas circle: " + radius);
    }
}

abstract class Shape {
    protected final Renderer renderer;

    protected Shape(Renderer renderer) {
        this.renderer = renderer;
    }

    abstract void draw();
}

class Circle extends Shape {
    private final double radius;

    Circle(Renderer renderer, double radius) {
        super(renderer);
        this.radius = radius;
    }

    void draw() {
        renderer.renderCircle(radius);
    }
}
```

### Interview Notes

- Bridge is often confused with Adapter.
- Adapter is usually introduced after incompatible interfaces exist.
- Bridge is designed upfront to avoid tight coupling between dimensions.

---

## 2.3 Composite

### Intent

Treat individual objects and groups of objects uniformly.

### When To Use

- Tree structures.
- File systems.
- Organization hierarchy.
- Menu/submenu structures.

### Example

```java
interface FileSystemItem {
    int size();
}

class FileItem implements FileSystemItem {
    private final int size;

    FileItem(int size) {
        this.size = size;
    }

    public int size() {
        return size;
    }
}

class Folder implements FileSystemItem {
    private final List<FileSystemItem> children = new ArrayList<>();

    public void add(FileSystemItem item) {
        children.add(item);
    }

    public int size() {
        return children.stream().mapToInt(FileSystemItem::size).sum();
    }
}
```

### Interview Notes

- Component interface defines common operations.
- Leaf is an individual object.
- Composite contains children.
- Useful when recursive behavior is natural.

---

## 2.4 Decorator

### Intent

Add behavior to an object dynamically without changing its class.

### When To Use

- Optional features can be combined.
- Inheritance would create too many subclasses.
- You need runtime behavior extension.

### Example

```java
interface Coffee {
    int cost();
    String description();
}

class BasicCoffee implements Coffee {
    public int cost() {
        return 50;
    }

    public String description() {
        return "Coffee";
    }
}

class MilkDecorator implements Coffee {
    private final Coffee coffee;

    MilkDecorator(Coffee coffee) {
        this.coffee = coffee;
    }

    public int cost() {
        return coffee.cost() + 10;
    }

    public String description() {
        return coffee.description() + ", milk";
    }
}

Coffee coffee = new MilkDecorator(new BasicCoffee());
```

### Real Use Cases

- Java IO streams.
- Servlet filters.
- Spring security filters.
- HTTP client interceptors.

### Interview Notes

- Decorator has the same interface as the object it wraps.
- It is flexible but can create many small wrapper objects.

---

## 2.5 Facade

### Intent

Provide a simple interface to a complex subsystem.

### When To Use

- Client code talks to many classes for one use case.
- You want to hide subsystem complexity.
- You need a stable API over changing internals.

### Example

```java
class InventoryService {
    boolean isAvailable(String sku) {
        return true;
    }
}

class PaymentService {
    void charge(String userId, int amount) {
    }
}

class ShippingService {
    void ship(String sku, String address) {
    }
}

class OrderFacade {
    private final InventoryService inventory = new InventoryService();
    private final PaymentService payment = new PaymentService();
    private final ShippingService shipping = new ShippingService();

    public void placeOrder(String userId, String sku, int amount, String address) {
        if (!inventory.isAvailable(sku)) {
            throw new IllegalStateException("Out of stock");
        }
        payment.charge(userId, amount);
        shipping.ship(sku, address);
    }
}
```

### Interview Notes

- Facade reduces dependency surface.
- It should not become a god object.
- In layered architecture, application services often act like facades over domain operations.

---

## 2.6 Flyweight

### Intent

Share common object state to reduce memory usage.

### When To Use

- Very large number of similar objects.
- Intrinsic state can be shared.
- Extrinsic state can be passed from outside.

### Example

```java
class FontStyle {
    private final String family;
    private final int size;

    FontStyle(String family, int size) {
        this.family = family;
        this.size = size;
    }
}

class FontStyleFactory {
    private final Map<String, FontStyle> cache = new HashMap<>();

    FontStyle get(String family, int size) {
        String key = family + ":" + size;
        return cache.computeIfAbsent(key, ignored -> new FontStyle(family, size));
    }
}
```

### Interview Notes

- Intrinsic state is shared and immutable.
- Extrinsic state is context-specific.
- Examples: string interning, character rendering, map tiles, game particles.

---

## 2.7 Proxy

### Intent

Provide a placeholder or wrapper that controls access to another object.

### Types

- **Virtual proxy**: Lazy loading.
- **Protection proxy**: Access control.
- **Remote proxy**: Represents remote service.
- **Caching proxy**: Adds cache.
- **Logging proxy**: Adds audit/logging.

### Example

```java
interface Document {
    String content();
}

class RealDocument implements Document {
    private final String path;

    RealDocument(String path) {
        this.path = path;
        loadFromDisk();
    }

    private void loadFromDisk() {
        System.out.println("Loading " + path);
    }

    public String content() {
        return "file content";
    }
}

class LazyDocumentProxy implements Document {
    private final String path;
    private RealDocument realDocument;

    LazyDocumentProxy(String path) {
        this.path = path;
    }

    public String content() {
        if (realDocument == null) {
            realDocument = new RealDocument(path);
        }
        return realDocument.content();
    }
}
```

### Interview Notes

- Spring AOP uses proxies.
- Hibernate lazy-loaded entities use proxy-like behavior.
- Proxy controls access; Decorator adds behavior while preserving responsibility.

---

# 3. Behavioral Design Patterns

Behavioral patterns define how objects communicate and divide responsibilities.

## 3.1 Chain Of Responsibility

### Intent

Pass a request along a chain of handlers until one handles it.

### When To Use

- Multiple handlers can process a request.
- Handler order matters.
- Sender should not know the final handler.

### Example

```java
abstract class SupportHandler {
    private SupportHandler next;

    public SupportHandler setNext(SupportHandler next) {
        this.next = next;
        return next;
    }

    public void handle(String issue) {
        if (!process(issue) && next != null) {
            next.handle(issue);
        }
    }

    protected abstract boolean process(String issue);
}

class BillingHandler extends SupportHandler {
    protected boolean process(String issue) {
        if (issue.contains("billing")) {
            System.out.println("Handled by billing");
            return true;
        }
        return false;
    }
}
```

### Real Use Cases

- Servlet filters.
- Spring Security filter chain.
- Logging frameworks.
- Validation pipeline.

### Interview Notes

- Chain can stop at first handler or allow all handlers to run.
- Be careful with hidden control flow and debugging complexity.

---

## 3.2 Command

### Intent

Encapsulate a request as an object.

### When To Use

- Queue operations.
- Support undo/redo.
- Log requests.
- Execute later.

### Example

```java
interface Command {
    void execute();
}

class Light {
    void on() {
        System.out.println("Light on");
    }
}

class LightOnCommand implements Command {
    private final Light light;

    LightOnCommand(Light light) {
        this.light = light;
    }

    public void execute() {
        light.on();
    }
}

class RemoteControl {
    private Command command;

    void setCommand(Command command) {
        this.command = command;
    }

    void press() {
        command.execute();
    }
}
```

### Interview Notes

- Command separates invoker from receiver.
- In CQRS, commands represent write intentions such as `CreateOrderCommand`.
- Command pattern is useful for job queues and workflow engines.

---

## 3.3 Interpreter

### Intent

Define a grammar and interpret sentences in that grammar.

### When To Use

- Simple DSL.
- Rule evaluation.
- Query expression parsing.

### Example

```java
interface Expression {
    boolean interpret(String context);
}

class ContainsExpression implements Expression {
    private final String word;

    ContainsExpression(String word) {
        this.word = word;
    }

    public boolean interpret(String context) {
        return context.contains(word);
    }
}

class AndExpression implements Expression {
    private final Expression left;
    private final Expression right;

    AndExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public boolean interpret(String context) {
        return left.interpret(context) && right.interpret(context);
    }
}
```

### Interview Notes

- Useful for small grammars.
- For complex languages, use a parser generator or established parsing library.
- Specification pattern is often a more practical business-rule alternative.

---

## 3.4 Iterator

### Intent

Access elements of a collection sequentially without exposing internal representation.

### Example

```java
class NameRepository implements Iterable<String> {
    private final List<String> names = List.of("A", "B", "C");

    public Iterator<String> iterator() {
        return names.iterator();
    }
}

for (String name : new NameRepository()) {
    System.out.println(name);
}
```

### Interview Notes

- Java collections heavily use Iterator.
- It hides whether data comes from list, set, database cursor, or stream.
- Be aware of fail-fast iterators and concurrent modification.

---

## 3.5 Mediator

### Intent

Centralize communication between objects so they do not directly depend on each other.

### When To Use

- Many objects communicate in complex ways.
- Direct dependencies create tight coupling.

### Example

```java
interface ChatMediator {
    void send(String message, User user);
}

class ChatRoom implements ChatMediator {
    private final List<User> users = new ArrayList<>();

    void add(User user) {
        users.add(user);
    }

    public void send(String message, User sender) {
        for (User user : users) {
            if (user != sender) {
                user.receive(message);
            }
        }
    }
}

class User {
    private final String name;
    private final ChatMediator mediator;

    User(String name, ChatMediator mediator) {
        this.name = name;
        this.mediator = mediator;
    }

    void send(String message) {
        mediator.send(message, this);
    }

    void receive(String message) {
        System.out.println(name + " received " + message);
    }
}
```

### Interview Notes

- Mediator reduces many-to-many dependencies.
- Message brokers can be viewed as distributed mediators.
- Avoid creating a mediator that knows too much business logic.

---

## 3.6 Memento

### Intent

Capture and restore an object's previous state without exposing internals.

### When To Use

- Undo/redo.
- Checkpointing.
- Draft restore.

### Example

```java
class Editor {
    private String text = "";

    void setText(String text) {
        this.text = text;
    }

    Snapshot save() {
        return new Snapshot(text);
    }

    void restore(Snapshot snapshot) {
        this.text = snapshot.text();
    }

    record Snapshot(String text) {
    }
}
```

### Interview Notes

- Memento protects encapsulation.
- For large states, store diffs instead of full snapshots.
- Event sourcing is related but stores business events, not object snapshots only.

---

## 3.7 Observer

### Intent

Notify multiple dependent objects when one object changes state.

### When To Use

- Event notifications.
- UI events.
- Domain events.
- Pub-sub style communication inside a process.

### Example

```java
interface OrderListener {
    void onOrderCreated(String orderId);
}

class EmailListener implements OrderListener {
    public void onOrderCreated(String orderId) {
        System.out.println("Send email for " + orderId);
    }
}

class OrderService {
    private final List<OrderListener> listeners = new ArrayList<>();

    void addListener(OrderListener listener) {
        listeners.add(listener);
    }

    void createOrder(String orderId) {
        System.out.println("Order created");
        listeners.forEach(listener -> listener.onOrderCreated(orderId));
    }
}
```

### Interview Notes

- Observer is in-memory.
- Pub-sub/message brokers are distributed versions of the idea.
- Be careful with listener failures, ordering, retries, and memory leaks.

---

## 3.8 State

### Intent

Allow an object to change behavior when its internal state changes.

### When To Use

- Many conditionals depend on state.
- State transitions have behavior.
- Object behaves like a state machine.

### Example

```java
interface OrderState {
    void pay(Order order);
    void ship(Order order);
}

class CreatedState implements OrderState {
    public void pay(Order order) {
        order.setState(new PaidState());
    }

    public void ship(Order order) {
        throw new IllegalStateException("Pay before shipping");
    }
}

class PaidState implements OrderState {
    public void pay(Order order) {
        throw new IllegalStateException("Already paid");
    }

    public void ship(Order order) {
        order.setState(new ShippedState());
    }
}

class ShippedState implements OrderState {
    public void pay(Order order) {
        throw new IllegalStateException("Already shipped");
    }

    public void ship(Order order) {
        throw new IllegalStateException("Already shipped");
    }
}

class Order {
    private OrderState state = new CreatedState();

    void setState(OrderState state) {
        this.state = state;
    }

    void pay() {
        state.pay(this);
    }

    void ship() {
        state.ship(this);
    }
}
```

### Interview Notes

- State pattern moves state-specific behavior out of the context object.
- Strategy changes algorithm by caller choice.
- State changes behavior based on internal lifecycle.

---

## 3.9 Strategy

### Intent

Define a family of algorithms and make them interchangeable.

### When To Use

- You have multiple ways to perform an operation.
- You want to remove `if-else` or `switch` from business logic.
- Algorithm should be selected at runtime.

### Example

```java
interface DiscountStrategy {
    int apply(int amount);
}

class RegularDiscount implements DiscountStrategy {
    public int apply(int amount) {
        return amount;
    }
}

class FestivalDiscount implements DiscountStrategy {
    public int apply(int amount) {
        return amount - (amount * 20 / 100);
    }
}

class CheckoutService {
    int calculatePayable(int amount, DiscountStrategy strategy) {
        return strategy.apply(amount);
    }
}
```

### Spring Example

```java
@Service
class PaymentService {
    private final Map<String, PaymentGateway> gateways;

    PaymentService(List<PaymentGateway> gatewayList) {
        this.gateways = gatewayList.stream()
                .collect(Collectors.toMap(PaymentGateway::name, Function.identity()));
    }

    void pay(String gatewayName, int amount) {
        gateways.get(gatewayName).pay(amount);
    }
}
```

### Interview Notes

- Strategy is one of the most common production patterns.
- Use it for payment modes, pricing rules, sorting, validation, and routing logic.

---

## 3.10 Template Method

### Intent

Define the skeleton of an algorithm in a base class and let subclasses override specific steps.

### When To Use

- Algorithm steps are mostly fixed.
- Some steps vary.
- You want reuse with controlled extension points.

### Example

```java
abstract class DataImporter {
    public final void importData() {
        read();
        validate();
        save();
    }

    protected abstract void read();

    protected void validate() {
        System.out.println("Default validation");
    }

    protected abstract void save();
}

class CsvImporter extends DataImporter {
    protected void read() {
        System.out.println("Read CSV");
    }

    protected void save() {
        System.out.println("Save CSV data");
    }
}
```

### Interview Notes

- Template Method uses inheritance.
- Strategy uses composition.
- Prefer Strategy when behavior needs runtime replacement.

---

## 3.11 Visitor

### Intent

Add new operations to object structures without changing the classes of those objects.

### When To Use

- Object structure is stable.
- New operations are added frequently.
- You need type-specific behavior outside the model classes.

### Example

```java
interface ShapeElement {
    void accept(ShapeVisitor visitor);
}

class CircleElement implements ShapeElement {
    public void accept(ShapeVisitor visitor) {
        visitor.visit(this);
    }
}

class RectangleElement implements ShapeElement {
    public void accept(ShapeVisitor visitor) {
        visitor.visit(this);
    }
}

interface ShapeVisitor {
    void visit(CircleElement circle);
    void visit(RectangleElement rectangle);
}

class AreaCalculator implements ShapeVisitor {
    public void visit(CircleElement circle) {
        System.out.println("Circle area");
    }

    public void visit(RectangleElement rectangle) {
        System.out.println("Rectangle area");
    }
}
```

### Interview Notes

- Visitor makes adding operations easy but adding new element types harder.
- Useful in compilers, AST processing, validation, export operations, and reporting.

---

# 4. Practical Enterprise/Application Patterns

These patterns are common in real backend projects and interviews, even though not all are GoF patterns.

## 4.1 Dependency Injection

### Intent

Provide dependencies from outside instead of creating them inside a class.

### Example

```java
class UserService {
    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### Why It Matters

- Improves testability.
- Reduces tight coupling.
- Supports inversion of control containers like Spring.

### Interview Notes

- Prefer constructor injection.
- Avoid field injection because it hides required dependencies and complicates testing.

---

## 4.2 Repository

### Intent

Encapsulate data access and provide collection-like methods for domain objects.

### Example

```java
interface UserRepository {
    Optional<User> findById(Long id);
    User save(User user);
}
```

### Interview Notes

- Repository hides persistence details.
- DAO is often lower-level and table/query focused.
- In Spring Data JPA, repository interfaces are generated automatically.

---

## 4.3 DAO

### Intent

Separate low-level persistence logic from business logic.

### Example

```java
class UserDao {
    private final JdbcTemplate jdbcTemplate;

    UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    User findByEmail(String email) {
        return jdbcTemplate.queryForObject(
                "select id, email from users where email = ?",
                (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("email")),
                email
        );
    }
}
```

### Interview Notes

- DAO focuses on database operations.
- Repository often works at domain aggregate level.

---

## 4.4 Service Layer

### Intent

Hold application use-case logic and coordinate repositories, external clients, and domain operations.

### Example

```java
class OrderApplicationService {
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;

    void placeOrder(CreateOrderRequest request) {
        Order order = Order.create(request.items());
        paymentClient.charge(request.paymentDetails(), order.total());
        orderRepository.save(order);
    }
}
```

### Interview Notes

- Controller should be thin.
- Service coordinates use cases.
- Domain model should own core business rules when domain complexity is high.

---

## 4.5 DTO

### Intent

Transfer data across layers or network boundaries without exposing domain/internal models.

### Example

```java
public record UserResponse(Long id, String name, String email) {
}
```

### Interview Notes

- DTO protects API contracts from internal model changes.
- Avoid using JPA entities directly in API responses.
- Use mapping manually or with tools like MapStruct.

---

## 4.6 Unit Of Work

### Intent

Track changes to objects and commit them as one transaction.

### Example

```java
@Transactional
public void transfer(Long fromAccount, Long toAccount, int amount) {
    Account from = accountRepository.findById(fromAccount).orElseThrow();
    Account to = accountRepository.findById(toAccount).orElseThrow();

    from.debit(amount);
    to.credit(amount);
}
```

### Interview Notes

- JPA persistence context acts like Unit of Work.
- Commit or rollback should be atomic.
- Useful when multiple repository changes belong to one business transaction.

---

## 4.7 Specification

### Intent

Encapsulate business rules or query criteria as reusable objects.

### Example

```java
interface Specification<T> {
    boolean isSatisfiedBy(T item);
}

class ActiveUserSpecification implements Specification<User> {
    public boolean isSatisfiedBy(User user) {
        return user.isActive();
    }
}
```

### Interview Notes

- Useful for complex filtering and validation rules.
- Spring Data JPA has `Specification<T>` for dynamic queries.

---

## 4.8 Null Object

### Intent

Use a harmless default object instead of returning `null`.

### Example

```java
interface NotificationPreference {
    void notify(String message);
}

class NoNotification implements NotificationPreference {
    public void notify(String message) {
        // intentionally do nothing
    }
}
```

### Interview Notes

- Reduces null checks.
- Use carefully because it can hide missing data bugs.

---

## 4.9 Front Controller

### Intent

Route all requests through one central controller/entry point.

### Real Use

- Spring MVC `DispatcherServlet`.
- API gateways.

### Interview Notes

- Centralizes routing, authentication, logging, and exception handling.
- Should delegate business work to services.

---

## 4.10 MVC

### Intent

Separate model, view, and controller responsibilities.

### Parts

- **Model**: Data and business state.
- **View**: Presentation.
- **Controller**: Handles input and coordinates response.

### Spring MVC Flow

```text
Client -> DispatcherServlet -> Controller -> Service -> Repository -> Database
                                      |
                                      v
                                  Response DTO
```

### Interview Notes

- Keep controllers thin.
- Avoid placing business logic in views/controllers.

---

## 4.11 MVVM

### Intent

Separate UI from state and behavior using a ViewModel.

### Common Use

- Frontend frameworks.
- Android.
- Desktop apps.

### Interview Notes

- View binds to ViewModel.
- ViewModel exposes UI state and actions.
- Better for rich interactive UIs than simple MVC.

---

## 4.12 Domain Model

### Intent

Place business rules inside domain objects instead of procedural services.

### Example

```java
class Account {
    private int balance;

    void debit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (balance < amount) {
            throw new IllegalStateException("Insufficient balance");
        }
        balance -= amount;
    }
}
```

### Interview Notes

- Rich domain model is useful for complex business rules.
- Anemic domain model keeps entities as data containers and moves logic to services.
- Both can be valid depending on complexity.

---

# 5. Architectural Patterns

Architectural patterns shape the whole application or system.

## 5.1 Layered Architecture

### Intent

Organize code into layers with clear responsibilities.

### Common Layers

```text
Controller/API
Application Service
Domain
Repository/Infrastructure
Database/External Systems
```

### Pros

- Simple and familiar.
- Easy for teams to understand.
- Works well for CRUD-heavy applications.

### Cons

- Business logic may leak across layers.
- Can become tightly coupled to database models.

### Interview Notes

- Dependency direction should be controlled.
- Do not let controllers call repositories directly for complex use cases.

---

## 5.2 Clean Architecture / Hexagonal Architecture / Ports And Adapters

### Intent

Keep business logic independent from frameworks, databases, and external systems.

### Core Idea

```text
External World -> Adapter -> Port -> Application/Domain Core
Database       <- Adapter <- Port <-
```

### Example

```java
interface PaymentPort {
    void charge(int amount);
}

class PlaceOrderUseCase {
    private final PaymentPort paymentPort;

    PlaceOrderUseCase(PaymentPort paymentPort) {
        this.paymentPort = paymentPort;
    }

    void execute(int amount) {
        paymentPort.charge(amount);
    }
}

class StripePaymentAdapter implements PaymentPort {
    public void charge(int amount) {
        System.out.println("Call Stripe");
    }
}
```

### Pros

- Testable core business logic.
- Framework-independent domain.
- Easy to replace infrastructure.

### Cons

- More interfaces and mapping.
- Can be overkill for simple CRUD apps.

### Interview Notes

- Ports are interfaces owned by the application.
- Adapters implement ports for HTTP, database, message broker, external APIs.
- Dependency points inward toward domain/application core.

---

## 5.3 Monolithic Architecture

### Intent

Build and deploy the application as a single unit.

### Pros

- Simple deployment.
- Easier local development.
- Strong consistency with local transactions.
- Lower operational complexity.

### Cons

- Can become hard to maintain at scale.
- Full application redeploy for changes.
- Team ownership boundaries can blur.

### Interview Notes

- A well-structured modular monolith is often better than premature microservices.
- Split into microservices only when team scale, deployment independence, or domain boundaries justify it.

---

## 5.4 Modular Monolith

### Intent

Keep one deployable application but divide code into strong internal modules.

### Example Boundaries

```text
orders/
payments/
inventory/
users/
```

### Rules

- Modules expose public APIs.
- Internal models are not shared directly.
- Database access should respect ownership boundaries.

### Interview Notes

- Good stepping stone before microservices.
- Reduces distributed system complexity.

---

## 5.5 Microservices

### Intent

Build independent services around business capabilities.

### Pros

- Independent deployment.
- Independent scaling.
- Clear team ownership.
- Technology flexibility.

### Cons

- Distributed transactions are hard.
- Network failures must be handled.
- Observability, versioning, and deployment become more complex.
- Data consistency is often eventual.

### Interview Notes

- Service boundaries should follow business domains, not technical layers.
- Each service should own its data.
- Avoid distributed monoliths where services are separate but tightly coupled.

---

## 5.6 Event-Driven Architecture

### Intent

Services communicate by producing and consuming events.

### Example

```text
Order Service -> OrderCreated event -> Kafka/RabbitMQ -> Inventory, Email, Analytics
```

### Pros

- Loose coupling.
- Good for async workflows.
- Scales consumers independently.

### Cons

- Event ordering can be difficult.
- Debugging is harder.
- Event schema/versioning needs discipline.
- Consumers must be idempotent.

### Interview Notes

- Event means something already happened: `OrderCreated`.
- Command means request to do something: `CreateOrder`.
- Use correlation IDs for tracing.

---

## 5.7 CQRS

### Intent

Separate command/write model from query/read model.

### Example

```text
Write API -> Command Model -> Database
Read API  -> Query Model   -> Read-optimized Store
```

### When To Use

- Reads and writes have very different performance needs.
- Read models need denormalized views.
- Complex domain writes but simple high-volume reads.

### Pros

- Independent optimization for reads and writes.
- Clear command intent.

### Cons

- More moving parts.
- Eventual consistency.
- Duplicate models.

### Interview Notes

- CQRS does not require event sourcing.
- CQRS can be implemented simply with separate service methods/classes.

---

## 5.8 Event Sourcing

### Intent

Store state changes as an ordered sequence of events instead of only current state.

### Example

```text
AccountOpened
MoneyDeposited
MoneyWithdrawn
```

Current state is rebuilt by replaying events.

### Pros

- Full audit trail.
- Rebuild read models.
- Time-travel debugging.

### Cons

- Event versioning is complex.
- Replaying events can be expensive.
- Requires strong discipline in event design.

### Interview Notes

- Events must be immutable facts.
- Snapshots can speed up rebuilds.
- Often paired with CQRS but not required.

---

## 5.9 Saga

### Intent

Manage distributed transactions through a sequence of local transactions and compensating actions.

### Example

```text
Create Order -> Reserve Inventory -> Charge Payment -> Ship Order
                       |
                       v
              if payment fails, release inventory
```

### Types

- **Choreography**: Services react to each other's events.
- **Orchestration**: Central coordinator tells services what to do.

### Interview Notes

- Use Saga when ACID transaction across services is not possible.
- Compensation is not always perfect rollback; it is a business correction.
- Idempotency is critical.

---

## 5.10 API Gateway

### Intent

Provide a single entry point for clients to access backend services.

### Responsibilities

- Routing.
- Authentication.
- Rate limiting.
- Request/response transformation.
- TLS termination.
- Observability.

### Interview Notes

- Gateway should not contain heavy business logic.
- It helps hide internal service topology.
- It can become a bottleneck if not designed carefully.

---

## 5.11 Backend For Frontend

### Intent

Create separate backend APIs optimized for each frontend/client experience.

### Example

```text
Mobile BFF -> Mobile-specific response shape
Web BFF    -> Web-specific response shape
Admin BFF  -> Admin-specific response shape
```

### Interview Notes

- Useful when mobile, web, and admin clients need different data shapes.
- Avoid one generic API that over-fetches or under-fetches for every client.

---

## 5.12 Strangler Fig Pattern

### Intent

Gradually replace a legacy system by routing parts of traffic to a new system.

### Flow

```text
Client -> Router/Gateway -> Legacy System
                       \-> New Service
```

### Interview Notes

- Useful for migration without big-bang rewrite.
- Move one capability at a time.
- Need routing, data synchronization, and rollback strategy.

---

## 5.13 Circuit Breaker

### Intent

Stop calling a failing dependency temporarily to prevent cascading failures.

### States

- **Closed**: Calls pass normally.
- **Open**: Calls fail fast.
- **Half-open**: Limited trial calls check recovery.

### Example Concept

```java
if (circuitBreaker.allowRequest()) {
    try {
        callRemoteService();
        circuitBreaker.recordSuccess();
    } catch (Exception ex) {
        circuitBreaker.recordFailure();
    }
}
```

### Interview Notes

- Common libraries: Resilience4j, Hystrix legacy.
- Combine with timeouts, retries, fallbacks, and bulkheads.

---

## 5.14 Retry Pattern

### Intent

Retry transient failures such as network timeout or temporary unavailability.

### Best Practices

- Use exponential backoff.
- Add jitter to avoid retry storms.
- Retry only idempotent operations or use idempotency keys.
- Set max retry limit.

### Interview Notes

- Do not retry validation errors or permanent failures.
- Retry can worsen outages if not controlled.

---

## 5.15 Bulkhead

### Intent

Isolate resources so one failing area does not exhaust the whole system.

### Example

- Separate thread pools for payment and search calls.
- Separate database connection pools.
- Separate queues per priority.

### Interview Notes

- Prevents cascading failure.
- Often paired with circuit breaker and timeout.

---

## 5.16 Outbox Pattern

### Intent

Reliably publish events when changing database state.

### Problem

If you save an order and publish an event separately, one may succeed and the other may fail.

### Solution

```text
Transaction:
  1. Save order
  2. Save event to outbox table

Async relay:
  3. Read outbox table
  4. Publish event to broker
  5. Mark event as sent
```

### Interview Notes

- Solves dual-write problem.
- Consumers should still be idempotent because duplicate delivery can happen.

---

## 5.17 Sidecar Pattern

### Intent

Deploy helper functionality alongside the main application.

### Examples

- Service mesh proxy.
- Log collector.
- Configuration agent.

### Interview Notes

- Main app stays focused on business logic.
- Sidecar shares lifecycle and network context with the app instance.

---

# 6. Pattern Comparison Cheat Sheet

| Problem | Prefer This Pattern | Why |
|---|---|---|
| Too many object creation branches | Factory Method | Centralizes creation |
| Need related object families | Abstract Factory | Keeps products compatible |
| Too many constructor parameters | Builder | Readable object creation |
| Need one shared instance | Singleton | Controlled single lifecycle |
| Need to integrate incompatible API | Adapter | Converts interface |
| Need simple API over complex subsystem | Facade | Hides complexity |
| Need optional behavior stacking | Decorator | Runtime composition |
| Need lazy access/control | Proxy | Controls access |
| Need tree structure | Composite | Uniform leaf/group handling |
| Need interchangeable algorithms | Strategy | Runtime algorithm selection |
| Need object behavior by lifecycle state | State | State-specific behavior |
| Need event/listener notification | Observer | One-to-many updates |
| Need request pipeline | Chain of Responsibility | Ordered handlers |
| Need undo/queue operation | Command | Request as object |
| Need fixed algorithm with variable steps | Template Method | Reuse algorithm skeleton |
| Need stable domain, many operations | Visitor | Add operations externally |
| Need independent services | Microservices | Deployment/team autonomy |
| Need reliable async event publish | Outbox | Avoids dual-write failure |
| Need distributed transaction | Saga | Local transactions plus compensation |
| Need separate read/write models | CQRS | Optimize reads and writes separately |

---

# 7. Interview-Ready Answers

## Strategy vs State

- Strategy changes algorithm based on caller/configuration.
- State changes behavior based on object's internal lifecycle.
- Strategy objects are usually independent.
- State objects often know valid transitions.

Example:

- Strategy: choose `CreditCardPayment` vs `UPIPayment`.
- State: order behaves differently in `Created`, `Paid`, `Shipped`.

## Factory vs Abstract Factory

- Factory Method creates one product type.
- Abstract Factory creates families of related products.

Example:

- Factory Method: create `NotificationSender`.
- Abstract Factory: create complete `WindowsButton`, `WindowsCheckbox`, `WindowsMenu`.

## Adapter vs Facade vs Proxy vs Decorator

- Adapter changes interface.
- Facade simplifies access to a subsystem.
- Proxy controls access to an object.
- Decorator adds behavior while keeping the same interface.

## Inheritance vs Composition

Prefer composition when:

- Behavior must change at runtime.
- You want to combine behaviors.
- You want to reduce tight coupling.

Inheritance is acceptable when:

- There is a true "is-a" relationship.
- Base class defines a stable template.
- Extension points are controlled.

## Monolith vs Microservices

Choose monolith/modular monolith when:

- Team is small.
- Domain boundaries are unclear.
- Strong consistency is important.
- Operational maturity is low.

Choose microservices when:

- Teams need independent ownership.
- Services need independent scaling/deployment.
- Domain boundaries are clear.
- Observability and DevOps maturity exist.

## CQRS vs Event Sourcing

- CQRS separates read and write models.
- Event sourcing stores events as source of truth.
- They can be used together but are independent.

## Saga vs Two-Phase Commit

- Two-phase commit coordinates a distributed ACID transaction but can block and reduce availability.
- Saga uses local transactions and compensation, which fits microservices better.
- Saga gives eventual consistency, not immediate global consistency.

---

# 8. SOLID Principles And Patterns

Patterns often support SOLID principles.

## Single Responsibility Principle

A class should have one reason to change.

Patterns:

- Facade separates subsystem coordination.
- Repository separates persistence.
- Strategy separates algorithm.

## Open/Closed Principle

Open for extension, closed for modification.

Patterns:

- Strategy.
- Decorator.
- Observer.
- Factory with registration.

## Liskov Substitution Principle

Subtypes should be replaceable for base types.

Watch out:

- Subclass should not weaken behavior.
- Throwing unsupported operation from subtype is often a smell.

## Interface Segregation Principle

Clients should not depend on methods they do not use.

Patterns:

- Adapter.
- Ports and adapters.
- Role-specific interfaces.

## Dependency Inversion Principle

High-level modules should depend on abstractions, not low-level details.

Patterns:

- Dependency Injection.
- Repository.
- Hexagonal Architecture.
- Strategy.

---

# 9. Common Anti-Patterns

## God Object

One class knows too much or does too much.

Fix:

- Split by responsibility.
- Extract services, strategies, repositories, or domain objects.

## Big Ball Of Mud

No clear architecture, boundaries, or dependency direction.

Fix:

- Introduce modules.
- Define ownership boundaries.
- Add tests before refactoring.

## Anemic Domain Model

Domain objects only hold data and all business logic lives in services.

Fix:

- Move business invariants into domain objects when domain complexity is high.

## Golden Hammer

Using one familiar pattern everywhere.

Fix:

- Start from the problem.
- Choose the simplest design that keeps change easy.

## Distributed Monolith

Services are deployed separately but tightly coupled through synchronous calls or shared databases.

Fix:

- Clarify ownership.
- Use async events where useful.
- Avoid shared database writes.

---

# 10. How To Choose A Pattern In Real Projects

Ask these questions:

1. What varies?
2. What should stay stable?
3. Is the complexity real or imagined?
4. Will this improve testing?
5. Will it reduce coupling?
6. Can the next developer understand it quickly?
7. Does the framework already provide this pattern?

Examples:

- Payment method varies: use Strategy.
- Object creation varies by input: use Factory.
- External API interface is incompatible: use Adapter.
- Need to publish event after database save: use Outbox.
- Many order states and transitions: use State.
- Legacy migration: use Strangler Fig.
- Remote service failure isolation: use Circuit Breaker + Timeout + Bulkhead.

---

# 11. Quick Revision List

## GoF Creational

- Singleton.
- Factory Method.
- Abstract Factory.
- Builder.
- Prototype.

## GoF Structural

- Adapter.
- Bridge.
- Composite.
- Decorator.
- Facade.
- Flyweight.
- Proxy.

## GoF Behavioral

- Chain of Responsibility.
- Command.
- Interpreter.
- Iterator.
- Mediator.
- Memento.
- Observer.
- State.
- Strategy.
- Template Method.
- Visitor.

## Enterprise/Application

- Dependency Injection.
- Repository.
- DAO.
- Service Layer.
- DTO.
- Unit of Work.
- Specification.
- Null Object.
- Front Controller.
- MVC.
- MVVM.
- Domain Model.

## Architecture/Distributed

- Layered Architecture.
- Clean Architecture / Hexagonal Architecture.
- Monolith.
- Modular Monolith.
- Microservices.
- Event-Driven Architecture.
- CQRS.
- Event Sourcing.
- Saga.
- API Gateway.
- Backend For Frontend.
- Strangler Fig.
- Circuit Breaker.
- Retry.
- Bulkhead.
- Outbox.
- Sidecar.

---

# 12. Final Interview Tip

Do not answer only with a definition. A strong answer includes:

```text
Pattern name -> problem -> solution -> example -> tradeoff -> real project usage
```

Example:

```text
I used Strategy for payment processing. The checkout flow should not know every payment provider.
Each provider implements PaymentStrategy. The service selects one based on request/configuration.
It made adding UPI and wallet payments easier without changing checkout logic.
The tradeoff is more classes, but the extension point is clear.
```

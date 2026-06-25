# Explored Kafka

Revision notes for a 5 years Java/Spring Boot developer interview when you have not worked deeply on Kafka but need to explain concepts, internals, and project usage confidently.

Kafka is not only a message queue. It is a distributed event streaming platform. It stores events durably, lets multiple consumers read independently, supports high throughput, and is commonly used to connect microservices, build event-driven systems, move data between systems, and process streams.

> Current context: Apache Kafka 4.x uses KRaft for cluster metadata management. ZooKeeper was used by older Kafka clusters and is still important for legacy interview questions.

---

# 1. Kafka In One Paragraph

Apache Kafka is a distributed commit log. Producers write records to topics. Topics are split into partitions. Each partition is an ordered append-only log. Brokers store partitions. Consumers read records from partitions and track their current position using offsets. Consumer groups allow multiple instances of the same application to share work. Kafka keeps data for a configured retention period, so records are not removed just because one consumer has read them.

Simple flow:

```text
Producer -> Topic -> Partition(s) on Broker(s) -> Consumer Group -> Consumer(s)
```

Example:

```text
Order Service publishes OrderCreated event
Kafka stores it in orders.events topic
Inventory Service consumes it
Email Service consumes it
Analytics Service consumes it
```

Each consuming service can have its own consumer group, so all services receive the same event independently.

---

# 2. Why Kafka Is Used

## Common Use Cases

- Event-driven microservices.
- Async communication between services.
- High-throughput logging and metrics pipelines.
- Audit trail/event history.
- Real-time analytics.
- Data integration between databases, search systems, and data lakes.
- Stream processing using Kafka Streams, Flink, or Spark.
- Outbox pattern event publishing.
- CDC pipelines using Debezium + Kafka Connect.

## When Kafka Is A Good Fit

- You need high throughput.
- Many consumers need to process the same data independently.
- Events should be stored for replay.
- Services should be loosely coupled.
- Processing can be eventually consistent.
- Ordering is needed per business key, such as `orderId` or `userId`.

## When Kafka Is Not A Good Fit

- You need simple request/response communication.
- You need immediate synchronous result.
- You need strict global ordering across all records.
- Message volume is low and simple queue is enough.
- Team cannot operate distributed systems yet.

Interview answer:

```text
I would use Kafka when the business event should be stored durably and consumed by multiple systems independently. I would not use it just to replace every REST call. For request/response, REST or gRPC is simpler. For async event fan-out, replay, and high throughput, Kafka is better.
```

---

# 3. Core Terminology

| Term | Meaning |
|---|---|
| Event/Record/Message | One unit of data written to Kafka |
| Topic | Logical stream/category of records |
| Partition | Ordered log inside a topic |
| Offset | Position number of a record inside a partition |
| Producer | Application that writes records |
| Consumer | Application that reads records |
| Consumer Group | Group of consumers sharing partitions |
| Broker | Kafka server that stores partitions and handles reads/writes |
| Cluster | Multiple brokers working together |
| Leader Replica | Broker replica that handles reads/writes for a partition |
| Follower Replica | Copies data from leader for fault tolerance |
| Replication Factor | Number of copies of each partition |
| ISR | In-sync replicas that are caught up with leader |
| Controller | Kafka node role that manages metadata and leadership |
| KRaft | Kafka's built-in metadata consensus mode |
| ZooKeeper | Legacy metadata system used before KRaft |
| Retention | How long Kafka keeps records |
| Consumer Lag | Difference between latest offset and consumed offset |
| DLQ/DLT | Dead-letter queue/topic for failed records |
| SerDe | Serializer/deserializer |
| Schema Registry | Service that stores schemas for Avro/Protobuf/JSON Schema |

---

# 4. Kafka Architecture

## Main Components

```text
              +-------------------+
              |   Kafka Cluster   |
              |                   |
Producer ---> | Broker 1          | ---> Consumer Group A
Producer ---> | Broker 2          | ---> Consumer Group B
Producer ---> | Broker 3          | ---> Consumer Group C
              +-------------------+
```

Kafka cluster responsibilities:

- Store topic partitions.
- Replicate partitions across brokers.
- Accept writes from producers.
- Serve reads to consumers.
- Track group membership and offsets.
- Manage metadata such as topics, partitions, configs, and leaders.

## Broker

A broker is a Kafka server process.

Broker responsibilities:

- Stores partition log files on disk.
- Handles produce requests.
- Handles fetch requests.
- Maintains partition replicas.
- Coordinates consumer groups.
- Participates in cluster metadata management.

Example:

```text
Broker 1 stores:
  orders.events partition 0 leader
  payments.events partition 1 follower

Broker 2 stores:
  orders.events partition 1 leader
  orders.events partition 0 follower
```

## Controller

The controller manages cluster metadata and leadership changes.

In modern Kafka:

- KRaft controllers manage metadata using Kafka's own Raft-based quorum.
- Kafka no longer needs ZooKeeper in Kafka 4.x.

In old Kafka:

- ZooKeeper stored metadata.
- Brokers used ZooKeeper for controller election and coordination.

Interview answer:

```text
Older Kafka used ZooKeeper to store cluster metadata and elect controllers. Modern Kafka uses KRaft, where Kafka manages metadata internally through controller quorum. If the interviewer asks about ZooKeeper, I would say it is legacy but many older production clusters still have it.
```

---

# 5. Topic, Partition, Offset, And "Bucket"

Kafka does not usually use the word "bucket" for core architecture. In interviews, when someone says bucket, they may mean:

- **Partition**: A logical bucket of records inside a topic.
- **Log segment**: Physical file chunks on disk inside a partition.
- **Object storage bucket**: Used with tiered storage in newer Kafka deployments.

For basic Kafka interviews, treat "bucket" as partition unless they clearly mean storage bucket.

## Topic

A topic is a named stream of records.

Examples:

```text
orders.created
orders.cancelled
payments.completed
inventory.reserved
user.activity
```

Topic properties:

- Topic is split into partitions.
- Topic has retention settings.
- Topic has replication factor.
- Producers write to topic.
- Consumers subscribe to topic.

## Partition

A partition is an ordered append-only log.

```text
orders.created topic

Partition 0: [offset 0] [offset 1] [offset 2]
Partition 1: [offset 0] [offset 1] [offset 2]
Partition 2: [offset 0] [offset 1] [offset 2]
```

Important:

- Ordering is guaranteed only inside one partition.
- Records across different partitions do not have global order.
- Partition count controls parallelism.
- Each partition has its own offset sequence.

## Offset

Offset is the position of a record inside a partition.

Example:

```text
topic: orders.created
partition: 1
offset: 42
```

This uniquely identifies a record position.

Offsets are:

- Monotonically increasing per partition.
- Stored by consumer group.
- Used to resume consumption after restart.

## Log Segment

Kafka stores each partition as log segment files on disk.

Conceptual view:

```text
/kafka-logs/orders.created-0/
  00000000000000000000.log
  00000000000000000000.index
  00000000000000000000.timeindex
  00000000000000100000.log
  00000000000000100000.index
```

Why segments exist:

- Easier retention deletion.
- Easier indexing.
- Avoids one huge file.

Interview answer:

```text
A topic is split into partitions. A partition is an ordered log. Kafka physically stores that log in segment files. If someone says bucket, I would clarify whether they mean partition, segment, or object storage bucket.
```

---

# 6. How Producer Works

## Producer Responsibility

Producer sends records to Kafka topics.

Producer decides:

- Which topic to write to.
- Key and value of record.
- Headers if needed.
- Which partition to use, directly or indirectly.
- Serialization format.
- Reliability settings.

## Record Structure

```text
Record:
  topic: orders.created
  key: order-123
  value: {"orderId":"order-123","amount":2500}
  headers:
    eventType: OrderCreated
    correlationId: abc-123
  timestamp: 2026-06-25T10:00:00Z
```

## Producer Send Flow

```text
Application creates event
Producer serializes key/value
Partitioner chooses partition
Producer batches records
Producer sends batch to broker leader
Broker writes to partition log
Followers replicate
Broker sends acknowledgement
Producer callback receives success/failure
```

## Partition Selection

Producer chooses partition like this:

1. If partition is explicitly provided, use that partition.
2. Else if key is provided, hash key and choose partition.
3. Else use sticky/round-robin style partitioning depending on Kafka client behavior.

Key-based ordering example:

```text
key = order-123 -> always goes to same partition
```

This guarantees all events for `order-123` are ordered.

Example:

```text
OrderCreated(order-123)
PaymentCompleted(order-123)
OrderShipped(order-123)
```

If all use key `order-123`, they go to the same partition and are consumed in order.

## Producer Acknowledgement: `acks`

`acks` controls durability guarantee.

| Setting | Meaning | Risk |
|---|---|---|
| `acks=0` | Producer does not wait for broker ack | Fast but can lose data |
| `acks=1` | Leader broker acknowledges after write | Can lose data if leader fails before replication |
| `acks=all` / `acks=-1` | Leader waits for in-sync replicas | Strongest durability |

Production recommendation:

```text
acks=all
enable.idempotence=true
retries > 0
```

## Idempotent Producer

Idempotent producer prevents duplicate writes caused by producer retries.

Without idempotence:

```text
Producer sends record
Broker writes record
Ack is lost due to network issue
Producer retries
Broker writes duplicate record
```

With idempotence:

```text
Kafka detects duplicate retry using producer id + sequence number
Duplicate is ignored
```

Important:

- Idempotence helps within producer sessions and partition writes.
- End-to-end exactly-once also depends on consumer processing and transactions.

## Producer Important Configs

```properties
bootstrap.servers=localhost:9092
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=org.springframework.kafka.support.serializer.JsonSerializer
acks=all
enable.idempotence=true
retries=10
linger.ms=5
batch.size=32768
compression.type=snappy
delivery.timeout.ms=120000
request.timeout.ms=30000
max.in.flight.requests.per.connection=5
```

Explanation:

- `bootstrap.servers`: Initial brokers to connect.
- `key.serializer`: Converts key object to bytes.
- `value.serializer`: Converts value object to bytes.
- `acks`: Durability level.
- `enable.idempotence`: Avoid duplicate writes on retry.
- `retries`: Retry transient failures.
- `linger.ms`: Wait briefly to batch more records.
- `batch.size`: Max batch memory per partition.
- `compression.type`: Compress batches.
- `delivery.timeout.ms`: Total time allowed for send.
- `max.in.flight.requests.per.connection`: Number of unacknowledged requests.

## Producer Interview Answer

```text
Producer writes records to Kafka topics. A record has key, value, timestamp, and optional headers. Kafka serializes the record, chooses a partition based on key or partitioner, batches records for throughput, and sends them to the leader broker for that partition. For reliable production I would use acks=all, idempotence enabled, retries, compression, and meaningful keys for ordering.
```

---

# 7. How Consumer Works

## Consumer Responsibility

Consumer reads records from Kafka topics.

Consumer decides:

- Which topic to subscribe to.
- Which group id to use.
- How to deserialize records.
- How and when to commit offsets.
- How to handle errors.
- How to retry or send to dead-letter topic.

## Consumer Group

A consumer group is a set of consumers working together.

Rule:

```text
Within one consumer group, one partition is consumed by only one consumer at a time.
```

Example:

```text
Topic: orders.created
Partitions: 3
Consumer Group: inventory-service

Consumer 1 -> partition 0
Consumer 2 -> partition 1
Consumer 3 -> partition 2
```

If there are more consumers than partitions:

```text
Topic partitions: 3
Consumers: 5

Only 3 consumers actively consume.
2 consumers stay idle.
```

If there are fewer consumers than partitions:

```text
Topic partitions: 6
Consumers: 2

Consumer 1 -> partitions 0, 1, 2
Consumer 2 -> partitions 3, 4, 5
```

## Multiple Consumer Groups

Each group receives its own copy of the stream.

```text
orders.created topic

inventory-service group -> consumes all order events
email-service group     -> consumes all order events
analytics-service group -> consumes all order events
```

This is why Kafka is good for event fan-out.

## Offset Commit

Consumer tracks progress by committing offsets.

```text
Consumed partition 0 offset 100
Commit offset 101 as next offset to read
```

Kafka commits the next offset, not the current offset.

Offset storage:

- Kafka stores committed offsets in internal topic `__consumer_offsets`.
- Offset is tracked per `groupId + topic + partition`.

## Auto Commit vs Manual Commit

### Auto Commit

```properties
enable.auto.commit=true
```

Kafka client commits automatically at intervals.

Pros:

- Simple.

Cons:

- Can lose messages if offset is committed before processing completes.

### Manual Commit

Commit after successful processing.

Pros:

- More reliable.
- Better control.

Cons:

- More code and careful error handling.

Recommended for important business processing:

```text
Process record successfully -> commit offset
Processing fails -> do not commit, retry or send to DLT
```

## Delivery Semantics

| Semantic | Meaning | How |
|---|---|---|
| At-most-once | May lose messages, no duplicates | Commit before processing |
| At-least-once | No loss, duplicates possible | Process then commit |
| Exactly-once | Process once in Kafka transactional pipeline | Idempotent producer + transactions + read committed |

Most business systems use:

```text
At-least-once + idempotent consumer
```

Because exactly-once end-to-end with external DBs is hard.

## Idempotent Consumer

Consumer should handle duplicate records safely.

Why duplicates happen:

- Consumer processed DB update but crashed before offset commit.
- Rebalance happened during processing.
- Producer retried without proper idempotence.
- Manual replay.

How to make consumer idempotent:

- Use event id and processed-event table.
- Use unique constraint in database.
- Use natural idempotency, such as `orderStatus = SHIPPED`.
- Use versioning.
- Use upsert carefully.

Example:

```sql
create table processed_events (
    event_id varchar(100) primary key,
    processed_at timestamp not null
);
```

Flow:

```text
Start DB transaction
Check event_id in processed_events
If already processed, skip
Apply business update
Insert event_id
Commit DB transaction
Commit Kafka offset
```

## Rebalance

Rebalance means Kafka reassigns partitions among consumers in a group.

Rebalance happens when:

- New consumer joins group.
- Consumer leaves group.
- Consumer crashes.
- Partitions are added.
- Consumer is too slow and misses heartbeat.

Impact:

- Consumption pauses briefly.
- Partitions move to different consumers.
- In-flight processing must be handled carefully.

Important configs:

```properties
max.poll.interval.ms=300000
session.timeout.ms=45000
heartbeat.interval.ms=15000
max.poll.records=500
```

## Consumer Important Configs

```properties
bootstrap.servers=localhost:9092
group.id=order-service
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
value.deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
enable.auto.commit=false
auto.offset.reset=earliest
max.poll.records=100
max.poll.interval.ms=300000
isolation.level=read_committed
```

Explanation:

- `group.id`: Consumer group identity.
- `enable.auto.commit`: Whether Kafka commits automatically.
- `auto.offset.reset`: What to do when no committed offset exists.
- `max.poll.records`: Batch size returned in one poll.
- `max.poll.interval.ms`: Max processing time between polls.
- `isolation.level=read_committed`: Read only committed transactional records.

## `auto.offset.reset`

Used only when consumer group has no committed offset for a partition.

| Value | Meaning |
|---|---|
| `earliest` | Start from oldest available record |
| `latest` | Start from new records only |
| `none` | Throw error if offset missing |

Interview answer:

```text
auto.offset.reset does not reset offsets every time. It is used only when no committed offset exists or the committed offset is no longer available due to retention.
```

## Consumer Interview Answer

```text
A consumer reads records from topic partitions. Consumers with the same group id divide partitions among themselves, so each partition is processed by only one consumer in that group. Kafka tracks progress using committed offsets. For reliable processing I would disable auto commit, process the record, commit offset after success, and design the consumer to be idempotent because at-least-once delivery can produce duplicates.
```

---

# 8. Broker, Replication, Leader, ISR

## Partition Replication

Replication gives fault tolerance.

Example:

```text
Topic: orders.created
Partitions: 3
Replication factor: 3

Partition 0 replicas: Broker 1 leader, Broker 2 follower, Broker 3 follower
Partition 1 replicas: Broker 2 leader, Broker 3 follower, Broker 1 follower
Partition 2 replicas: Broker 3 leader, Broker 1 follower, Broker 2 follower
```

## Leader And Follower

- Producer writes to leader replica.
- Consumer reads from leader replica by default.
- Followers copy data from leader.
- If leader fails, Kafka elects a follower from ISR as new leader.

## ISR: In-Sync Replicas

ISR means replicas that are caught up enough with the leader.

If replication factor is 3:

```text
Leader: Broker 1
ISR: Broker 1, Broker 2, Broker 3
```

If Broker 3 becomes slow:

```text
ISR: Broker 1, Broker 2
Broker 3 removed from ISR temporarily
```

## `min.insync.replicas`

This controls how many replicas must be in sync for writes to succeed when producer uses `acks=all`.

Common production setting:

```properties
replication.factor=3
min.insync.replicas=2
producer.acks=all
```

Meaning:

- Data is written only if at least 2 replicas are in sync.
- Can tolerate one broker failure.
- If only one replica is alive/in-sync, writes fail instead of risking data loss.

Interview answer:

```text
For durability, I would use replication factor 3, min.insync.replicas 2, and producer acks=all. This means the leader waits for enough in-sync replicas before acknowledging the write. If ISR drops below 2, writes fail, which protects durability.
```

---

# 9. Retention, Cleanup, And Log Compaction

Kafka stores records based on retention, not based on whether consumers have read them.

## Time-Based Retention

Example:

```properties
retention.ms=604800000
```

Keep records for 7 days.

## Size-Based Retention

Example:

```properties
retention.bytes=10737418240
```

Keep up to 10 GB per partition.

## Delete Cleanup Policy

Default cleanup policy:

```properties
cleanup.policy=delete
```

Kafka deletes old segments when retention is exceeded.

## Log Compaction

Compaction keeps the latest value per key.

```properties
cleanup.policy=compact
```

Example:

```text
key=user-1, value=email-a
key=user-2, value=email-b
key=user-1, value=email-c
```

After compaction, Kafka can keep:

```text
key=user-2, value=email-b
key=user-1, value=email-c
```

Use cases:

- Latest user profile by user id.
- Configuration changes.
- CDC changelog topics.
- Kafka Streams state store changelog.

## Tombstone Record

A tombstone is a record with key but null value.

```text
key=user-1, value=null
```

In compacted topics, tombstone means delete this key.

---

# 10. Serialization And Schema

Kafka stores bytes. Producers serialize objects to bytes, consumers deserialize bytes to objects.

## Common Formats

| Format | Pros | Cons |
|---|---|---|
| String | Simple | Not structured |
| JSON | Human-readable, easy | Larger, schema not enforced by default |
| Avro | Compact, schema evolution | Needs schema registry |
| Protobuf | Compact, typed | Needs schema discipline |
| JSON Schema | Human-readable with schema | Larger than Avro/Protobuf |

## Why Schema Matters

In event-driven systems, producer and consumer are independent. If producer changes event shape, consumers may break.

Bad change:

```json
{
  "orderId": "123",
  "amount": 1000
}
```

Changed to:

```json
{
  "id": "123",
  "totalAmount": 1000
}
```

Consumer expecting `orderId` breaks.

## Schema Evolution Rules

Safe changes:

- Add optional field.
- Add field with default value.
- Stop using a field but keep it for compatibility.

Risky changes:

- Rename field.
- Remove required field.
- Change field type.
- Change meaning of a field.

Interview answer:

```text
For production, I would avoid random JSON without schema for critical events. I would use Avro or Protobuf with Schema Registry, or at least versioned JSON DTOs with compatibility rules. Event contracts need backward and forward compatibility.
```

---

# 11. Topic Design

## Naming

Use clear domain event names.

Good:

```text
orders.created
orders.cancelled
payments.completed
inventory.reserved
users.email-changed
```

Avoid:

```text
topic1
test
data
events
```

## Topic Granularity

Two approaches:

### One Topic Per Event Type

```text
orders.created
orders.cancelled
orders.shipped
```

Pros:

- Easy subscriptions.
- Clear meaning.

Cons:

- More topics.

### One Topic Per Domain/Aggregate

```text
orders.events
```

Event type in payload/header:

```json
{
  "eventType": "OrderCreated",
  "eventId": "evt-123",
  "orderId": "order-123"
}
```

Pros:

- All order events in one stream.
- Easier ordering per order key.

Cons:

- Consumers must filter event types.

Recommendation:

```text
Use domain topic like orders.events when events are related and ordering matters per aggregate id. Use separate topics when event types have very different consumers, retention, security, or volume.
```

## Choosing Partition Count

Partition count affects:

- Consumer parallelism.
- Throughput.
- Ordering.
- Future scalability.

Rules:

- Max active consumers in one group = number of partitions.
- More partitions increase parallelism but also overhead.
- You can increase partitions later, but key-to-partition mapping changes.
- You cannot easily reduce partitions.

Example:

```text
orders.events has 12 partitions
inventory-service can run up to 12 active consumers in one group
```

## Choosing Key

Choose key based on ordering requirement.

Examples:

| Use Case | Key |
|---|---|
| Order lifecycle events | `orderId` |
| User profile events | `userId` |
| Payment events | `paymentId` or `orderId` |
| Account transactions | `accountId` |

Bad key choices:

- Random UUID when ordering matters.
- Constant key because it sends everything to one partition.
- Null key when related events must be ordered.

## Retention By Topic

Examples:

| Topic | Retention |
|---|---|
| `orders.events` | 7 to 30 days |
| `audit.events` | 1 year or more |
| `user.profile.compacted` | compacted |
| retry topics | few days |
| DLT topics | 7 to 30 days depending on operation policy |

---

# 12. Project Integration With Spring Boot

This section is practical for Java/Spring Boot projects.

## Step 1: Add Dependency

Maven:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

For tests:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Step 2: Local Kafka With Docker Compose

Simplest official-style local run:

```bash
docker run -p 9092:9092 apache/kafka:4.3.0
```

Docker Compose example for local development:

```yaml
services:
  kafka:
    image: apache/kafka:4.3.0
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
```

For production:

- Do not use single broker.
- Use replication factor 3.
- Use TLS/SASL.
- Use monitoring.
- Use proper retention and topic configs.

## Step 3: Application Config

`application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      properties:
        enable.idempotence: true
        compression.type: snappy
        linger.ms: 5
    consumer:
      group-id: order-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false
      properties:
        spring.json.trusted.packages: "com.core.fullstack.*"
    listener:
      ack-mode: manual
```

## Step 4: Event DTO

```java
public record OrderCreatedEvent(
        String eventId,
        String orderId,
        String userId,
        int amount,
        Instant occurredAt
) {
}
```

Good event fields:

- `eventId`: Unique id for idempotency.
- `eventType`: If topic has multiple event types.
- `aggregateId`: Example `orderId`.
- `occurredAt`: When business event happened.
- `version`: Event schema version if needed.
- `correlationId`: Trace request across services.

## Step 5: Topic Config

```java
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic ordersEventsTopic() {
        return TopicBuilder.name("orders.events")
                .partitions(6)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(7).toMillis()))
                .build();
    }

    @Bean
    public NewTopic ordersEventsDltTopic() {
        return TopicBuilder.name("orders.events.DLT")
                .partitions(6)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(14).toMillis()))
                .build();
    }
}
```

Production:

```java
.replicas(3)
.config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
```

## Step 6: Producer Service

```java
@Service
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, OrderCreatedEvent>> publish(OrderCreatedEvent event) {
        ProducerRecord<String, OrderCreatedEvent> record =
                new ProducerRecord<>("orders.events", event.orderId(), event);

        record.headers().add("eventType", "OrderCreated".getBytes(StandardCharsets.UTF_8));
        record.headers().add("eventId", event.eventId().getBytes(StandardCharsets.UTF_8));

        return kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // log and let caller decide retry/transaction behavior
                        return;
                    }

                    RecordMetadata metadata = result.getRecordMetadata();
                    // log topic, partition, offset for traceability
                });
    }
}
```

Important:

- Use `orderId` as key to preserve order per order.
- Log topic, partition, offset on success.
- Do not swallow publish failures silently.

## Step 7: Consumer Service

```java
@Service
public class OrderCreatedConsumer {

    private final ProcessedEventRepository processedEventRepository;
    private final InventoryService inventoryService;

    public OrderCreatedConsumer(
            ProcessedEventRepository processedEventRepository,
            InventoryService inventoryService
    ) {
        this.processedEventRepository = processedEventRepository;
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "orders.events",
            groupId = "inventory-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            OrderCreatedEvent event,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        if (processedEventRepository.existsById(event.eventId())) {
            acknowledgment.acknowledge();
            return;
        }

        inventoryService.reserveInventory(event.orderId());
        processedEventRepository.save(new ProcessedEvent(event.eventId(), Instant.now()));

        acknowledgment.acknowledge();
    }
}
```

Important:

- Commit only after processing succeeds.
- Use `eventId` to prevent duplicate effects.
- Log topic, partition, offset for debugging.

## Step 8: Error Handling And Dead Letter Topic

Spring Kafka supports error handlers and dead-letter publishing.

Conceptual config:

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaListenerContainerFactory(
        ConsumerFactory<String, OrderCreatedEvent> consumerFactory,
        KafkaTemplate<String, Object> kafkaTemplate
) {
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition())
    );

    DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            recoverer,
            new FixedBackOff(1000L, 3L)
    );

    ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setCommonErrorHandler(errorHandler);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    return factory;
}
```

Flow:

```text
Consumer fails processing
Retry 3 times
Still fails
Publish record to orders.events.DLT
Commit original offset so consumer can continue
Alert team to inspect DLT
```

## Step 9: Retry Topic Pattern

For temporary downstream failures:

```text
orders.events
orders.events.retry.1m
orders.events.retry.5m
orders.events.retry.30m
orders.events.DLT
```

Why:

- Avoid blocking main partition for long time.
- Retry later.
- Keep main consumer moving.

Use for:

- External API temporarily down.
- Database temporarily unavailable.
- Rate limit failures.

Do not retry:

- Bad schema.
- Validation failure.
- Missing mandatory data.

## Step 10: Testing

Options:

- Unit test producer service with mocked `KafkaTemplate`.
- Unit test consumer business logic without Kafka.
- Integration test with Embedded Kafka or Testcontainers.
- Contract test event schema.

Testcontainers example idea:

```text
Start Kafka container
Produce test event
Consumer processes event
Assert database changed
Assert offset committed or DLT received
```

---

# 13. Outbox Pattern With Kafka

Direct event publishing problem:

```text
Save order in DB -> success
Publish Kafka event -> fail
```

Now database says order exists, but no event was published.

Reverse problem:

```text
Publish Kafka event -> success
Save order in DB -> fail
```

Now consumers see an order that does not exist.

## Outbox Solution

Store business data and event in same database transaction.

```text
Transaction:
  insert into orders
  insert into outbox_events

Background publisher:
  read unpublished outbox_events
  publish to Kafka
  mark as published
```

Tables:

```sql
create table outbox_events (
    id varchar(100) primary key,
    aggregate_type varchar(100) not null,
    aggregate_id varchar(100) not null,
    event_type varchar(100) not null,
    payload json not null,
    status varchar(30) not null,
    created_at timestamp not null,
    published_at timestamp
);
```

Interview answer:

```text
For reliable event publishing from a service database to Kafka, I would use the outbox pattern. The service writes the business row and outbox row in the same local DB transaction. A separate publisher or CDC tool publishes outbox rows to Kafka. This avoids the dual-write problem.
```

## Debezium CDC Option

Instead of writing a custom publisher:

```text
Application writes outbox row
Debezium reads DB transaction log
Debezium publishes event to Kafka
```

Pros:

- Less custom polling code.
- Reliable ordering from DB log.

Cons:

- More infrastructure.
- Requires CDC knowledge and connector operation.

---

# 14. Kafka Connect

Kafka Connect is a framework for moving data between Kafka and external systems.

## Source Connector

External system -> Kafka

Examples:

- MySQL CDC to Kafka.
- PostgreSQL CDC to Kafka.
- S3 files to Kafka.

## Sink Connector

Kafka -> External system

Examples:

- Kafka to Elasticsearch.
- Kafka to S3.
- Kafka to PostgreSQL.
- Kafka to data warehouse.

Interview answer:

```text
Kafka Connect is used when we need data integration without writing custom producer or consumer code. Source connectors bring data into Kafka, sink connectors push Kafka data into external systems.
```

---

# 15. Kafka Streams

Kafka Streams is a Java library for stream processing.

Use it when:

- Transforming events.
- Aggregating events.
- Joining streams.
- Building materialized views.
- Windowed calculations.

Example:

```text
orders.events -> Kafka Streams app -> daily-order-sales topic
```

Kafka Streams concepts:

- `KStream`: Infinite stream of records.
- `KTable`: Changelog stream representing latest value per key.
- State store: Local storage for aggregations/joins.
- Changelog topic: Kafka topic used to restore state.

Interview difference:

```text
Kafka consumer is for consuming and processing records manually. Kafka Streams is a higher-level library for transformations, joins, windows, and stateful stream processing.
```

---

# 16. Kafka vs RabbitMQ vs REST

## Kafka vs RabbitMQ

| Topic | Kafka | RabbitMQ |
|---|---|---|
| Model | Distributed log/event streaming | Message broker/queue |
| Retention | Stores messages by retention | Usually removes after ack |
| Replay | Natural | Not default |
| Throughput | Very high | Good but different design |
| Ordering | Per partition | Queue ordering |
| Consumers | Multiple groups can replay independently | Queue consumers compete |
| Best for | Event streaming, logs, fan-out, replay | Task queues, routing, commands |

## Kafka vs REST

| Topic | Kafka | REST |
|---|---|---|
| Communication | Async | Sync |
| Coupling | Lower | Higher |
| Response | Not immediate | Immediate |
| Failure handling | Retry/replay/eventual consistency | Request failure visible immediately |
| Use case | Business events | Query/request response |

Interview answer:

```text
Kafka is not a replacement for REST. REST is better when caller needs immediate response. Kafka is better when an event happened and multiple services can react asynchronously.
```

---

# 17. Ordering In Kafka

Kafka ordering guarantee:

```text
Kafka guarantees ordering only within a partition.
```

To preserve order for one entity:

```text
Use same key for related events.
```

Example:

```text
key=order-123 -> partition 2

OrderCreated
PaymentCompleted
OrderShipped
```

All are ordered in partition 2.

What Kafka does not guarantee:

- Global order across partitions.
- Order across different keys.
- Order after repartitioning if key strategy changes.

Interview answer:

```text
If I need order per order, I use orderId as the key. Kafka will send same key to the same partition, so events for that order are ordered. I would not claim Kafka gives total ordering across the whole topic unless the topic has one partition, which limits scalability.
```

---

# 18. Transactions And Exactly-Once Semantics

## At-Least-Once Is Common

Most systems use:

```text
Process message -> commit offset
```

If crash happens after processing but before commit, message is processed again.

So consumer must be idempotent.

## Exactly-Once In Kafka

Kafka supports exactly-once semantics for Kafka-to-Kafka workflows using:

- Idempotent producer.
- Transactions.
- Transactional id.
- Consumer `isolation.level=read_committed`.

Example flow:

```text
Read from input topic
Process
Write to output topic
Commit consumed offsets in same Kafka transaction
```

If transaction commits:

- Output records are visible.
- Offsets are committed.

If transaction aborts:

- Output records are not visible to `read_committed` consumers.
- Offsets are not committed.

Important limitation:

```text
Kafka exactly-once does not automatically make external database writes exactly-once.
```

For DB writes:

- Use idempotent consumer.
- Use outbox/inbox patterns.
- Use transactional DB constraints.

Interview answer:

```text
Kafka exactly-once is strongest when the whole pipeline is Kafka read-process-write. If the consumer writes to an external database, I still need idempotency or an inbox table because Kafka cannot atomically commit my database transaction and Kafka offset without extra design.
```

---

# 19. Error Handling Strategy

## Error Types

### Retryable

- Temporary network issue.
- Database timeout.
- External service unavailable.
- Rate limit.

Action:

- Retry with backoff.
- Use retry topic for long delays.
- Eventually send to DLT.

### Non-Retryable

- Invalid JSON.
- Missing required field.
- Unknown event version.
- Business validation impossible.

Action:

- Send to DLT quickly.
- Alert and inspect.

## DLT Message Should Include

- Original topic.
- Original partition.
- Original offset.
- Original key.
- Original payload.
- Error class/message.
- Stack trace if allowed.
- Failure timestamp.
- Consumer group.

## Operational Flow

```text
Main topic -> retry -> retry -> DLT -> alert -> manual fix/replay
```

## Reprocessing DLT

Options:

- Fix data and republish to original topic.
- Build DLT replay tool.
- Consume DLT with separate repair service.

Be careful:

- Do not blindly replay poison messages.
- Preserve original key when republishing.
- Keep audit trail.

---

# 20. Security

Production Kafka should not be open plaintext.

## Authentication

Common options:

- SASL/PLAIN.
- SASL/SCRAM.
- SASL/OAUTHBEARER.
- mTLS.
- Kerberos in enterprise environments.

## Encryption

- TLS between clients and brokers.
- TLS between brokers.

## Authorization

Use ACLs:

```text
order-service can WRITE orders.events
inventory-service can READ orders.events with group inventory-service
analytics-service can READ orders.events
```

## Security Interview Answer

```text
In production I would configure TLS for encryption, SASL or mTLS for authentication, ACLs for topic and consumer group permissions, and avoid sharing admin credentials in applications.
```

---

# 21. Monitoring And Operations

## Important Metrics

### Consumer Metrics

- Consumer lag.
- Records consumed per second.
- Processing latency.
- Rebalance count.
- Commit failures.
- DLT count.

### Producer Metrics

- Send rate.
- Error rate.
- Retry rate.
- Request latency.
- Batch size.
- Compression ratio.

### Broker Metrics

- Under-replicated partitions.
- Offline partitions.
- ISR shrink/expand rate.
- Disk usage.
- Network throughput.
- Request handler idle percent.
- Controller health.

## Consumer Lag

Lag means:

```text
latest offset - committed consumer offset
```

Example:

```text
Latest offset: 10000
Consumer committed offset: 9500
Lag: 500
```

Lag can mean:

- Consumer is too slow.
- Consumer is down.
- Partition count too low.
- Downstream dependency is slow.
- Rebalance is happening often.

## Alerting

Alert on:

- High consumer lag for critical topics.
- DLT messages.
- Under-replicated partitions.
- Offline partitions.
- Broker disk usage.
- Producer error rate.
- Authentication/authorization failures.

---

# 22. Performance Tuning

## Producer Tuning

Increase throughput:

- Increase `batch.size`.
- Increase `linger.ms` slightly.
- Use compression: `snappy`, `lz4`, or `zstd`.
- Use proper key distribution.
- Use enough partitions.

Tradeoff:

- Larger batches improve throughput but can increase latency.

## Consumer Tuning

Improve consumption:

- Increase consumer instances up to partition count.
- Increase `max.poll.records` if processing is fast.
- Use batch listener for bulk DB writes.
- Optimize downstream DB/API.
- Avoid slow synchronous work inside consumer.

## Broker Tuning

Production considerations:

- Use fast disks.
- Monitor disk usage.
- Spread partitions across brokers.
- Keep replication factor usually 3.
- Avoid too many tiny topics/partitions.
- Plan network bandwidth.

---

# 23. Common Project Design: Order Service Example

## Requirement

When order is created:

- Save order.
- Publish event.
- Inventory reserves stock.
- Email service sends confirmation.
- Analytics service records event.

## Architecture

```text
Client -> Order Service -> orders DB
                    |
                    v
              orders.events topic
                    |
        +-----------+------------+
        |           |            |
 Inventory      Email       Analytics
 Service        Service     Service
```

## Event

```json
{
  "eventId": "evt-001",
  "eventType": "OrderCreated",
  "orderId": "ord-123",
  "userId": "user-456",
  "amount": 2500,
  "occurredAt": "2026-06-25T10:00:00Z",
  "version": 1
}
```

## Topic Design

```text
topic: orders.events
partitions: 12
replication.factor: 3
min.insync.replicas: 2
retention: 7 days
key: orderId
```

## Producer

Order Service publishes with:

```text
key = orderId
acks = all
idempotence = true
outbox pattern for DB + Kafka reliability
```

## Consumers

```text
inventory-service group
email-service group
analytics-service group
```

Each service gets all order events independently.

## Failure Handling

```text
orders.events.retry.1m
orders.events.retry.5m
orders.events.DLT
```

Consumer should be idempotent using `eventId`.

---

# 24. Kafka CLI Commands

## Create Topic

```bash
bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic orders.events \
  --partitions 6 \
  --replication-factor 1
```

## List Topics

```bash
bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list
```

## Describe Topic

```bash
bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic orders.events
```

## Produce Messages

```bash
bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic orders.events \
  --property parse.key=true \
  --property key.separator=:
```

Input:

```text
order-1:{"eventId":"evt-1","orderId":"order-1"}
order-2:{"eventId":"evt-2","orderId":"order-2"}
```

## Consume Messages

```bash
bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic orders.events \
  --from-beginning \
  --property print.key=true \
  --property key.separator=:
```

## Consumer Group Lag

```bash
bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group inventory-service
```

## Reset Offsets

Preview:

```bash
bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group inventory-service \
  --topic orders.events \
  --reset-offsets \
  --to-earliest \
  --dry-run
```

Execute:

```bash
bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group inventory-service \
  --topic orders.events \
  --reset-offsets \
  --to-earliest \
  --execute
```

Important:

- Stop consumers before resetting offsets.
- Use dry run first.
- Resetting can reprocess old events.

---

# 25. Interview Questions And Answers

## What Is Kafka?

Kafka is a distributed event streaming platform. It stores records in topics, splits topics into partitions, replicates partitions across brokers, and lets consumers read records independently using offsets.

## Topic vs Partition

A topic is a logical stream. A partition is an ordered log inside a topic. Topic gives logical separation; partition gives scalability and ordering boundary.

## Why Partitions?

Partitions provide:

- Parallelism.
- Scalability.
- Ordering per partition.
- Distribution across brokers.

## How Does Kafka Maintain Ordering?

Kafka maintains ordering within a partition. To maintain order for related events, use the same key so records go to the same partition.

## What Is Offset?

Offset is the position of a record in a partition. Consumers commit offsets to remember progress.

## What Is Consumer Group?

A consumer group is multiple consumers sharing work. Each partition is assigned to one consumer within a group. Different groups consume independently.

## What Happens If Consumer Count Is Greater Than Partition Count?

Extra consumers remain idle because one partition cannot be consumed by multiple consumers in the same group at the same time.

## What Is Rebalance?

Rebalance is reassignment of partitions among consumers when group membership or topic partitions change.

## What Is Consumer Lag?

Consumer lag is the difference between latest partition offset and consumer's committed offset. It shows how far behind a consumer group is.

## What Is Replication Factor?

Replication factor is the number of copies of each partition. Replication factor 3 means each partition has three replicas across brokers.

## What Is ISR?

ISR means in-sync replicas. These replicas are caught up with the leader and eligible for safe leader election.

## What Is `acks=all`?

Producer waits until leader and required in-sync replicas acknowledge the write. It gives stronger durability than `acks=1`.

## What Is `min.insync.replicas`?

Minimum number of replicas that must be in sync for writes to succeed when producer uses `acks=all`.

## Can Kafka Lose Messages?

Yes, with bad configuration or failures. Risk increases with `acks=0`, `acks=1`, low replication, unclean leader election, no retries, or bad consumer offset handling. Stronger setup uses `acks=all`, replication factor 3, `min.insync.replicas=2`, idempotent producer, and careful consumer commits.

## Can Kafka Create Duplicate Messages?

Yes. Duplicates can happen due to retries, crashes before offset commit, or reprocessing. Consumers should be idempotent.

## Kafka Push Or Pull?

Kafka consumers pull records from brokers. Pull model lets consumers control pace and batching.

## What Is Log Compaction?

Log compaction keeps latest value per key instead of deleting only by time/size. Useful for changelog/latest-state topics.

## What Is Dead Letter Topic?

DLT stores records that cannot be processed after retries. It allows the main consumer to continue and gives operations a place to inspect failures.

## What Is KRaft?

KRaft is Kafka's built-in metadata management mode based on a controller quorum. It replaces ZooKeeper in modern Kafka.

## What Was ZooKeeper Used For?

ZooKeeper was used in older Kafka clusters for metadata, broker coordination, and controller election. Kafka 4.x uses KRaft instead.

## Kafka vs RabbitMQ?

Kafka is a distributed log optimized for event streaming, replay, and multiple independent consumer groups. RabbitMQ is a message broker optimized for queues, routing, and task distribution.

## How Would You Use Kafka In A Project?

I would identify business events such as `OrderCreated`, design topics and keys, add Spring Kafka dependency, configure producer and consumer serializers, use meaningful consumer groups, add retry and DLT handling, make consumers idempotent, add monitoring for lag and DLT, secure Kafka with TLS/SASL/ACLs, and use outbox pattern if publishing events together with database changes.

---

# 26. Common Mistakes

- Thinking Kafka deletes a message after one consumer reads it.
- Expecting global ordering across partitions.
- Using null/random keys when ordering matters.
- Creating too few partitions and limiting consumer scalability.
- Creating too many partitions without operational reason.
- Using auto commit for critical processing.
- Not designing consumers for duplicate messages.
- Ignoring DLT and retry strategy.
- Publishing DB changes and Kafka events without outbox pattern.
- Using one consumer group for different services that all need all events.
- Not monitoring consumer lag.
- Not setting retention properly.
- Treating Kafka as synchronous request/response.
- Sharing one topic for unrelated data with different retention/security needs.

---

# 27. What To Add In A Real Project Checklist

## Build-Time

- `spring-kafka` dependency.
- Event DTOs.
- Topic constants.
- Producer service.
- Consumer listener.
- Tests with Testcontainers or Embedded Kafka.

## Runtime Config

- `bootstrap.servers`.
- Producer serializers.
- Consumer deserializers.
- Consumer group id.
- Manual ack mode.
- Retry/DLT config.
- Security configs.
- Observability configs.

## Kafka Topics

- Main topic.
- Retry topics.
- DLT topic.
- Compacted topics if latest-state use case exists.
- Retention configs.
- Partition count.
- Replication factor.
- `min.insync.replicas`.

## Reliability

- Producer `acks=all`.
- Idempotent producer.
- Retries with backoff.
- Outbox pattern for DB + Kafka.
- Idempotent consumer.
- Manual offset commit after success.
- DLT and replay plan.

## Observability

- Log event id, key, topic, partition, offset.
- Metrics for producer errors.
- Metrics for consumer lag.
- DLT alerts.
- Distributed tracing/correlation id.
- Dashboard for broker health.

## Security

- TLS.
- SASL/mTLS.
- ACLs.
- Separate credentials per service.
- Secret management.

## Governance

- Topic naming convention.
- Schema versioning.
- Compatibility rules.
- Ownership per topic.
- Retention policy.
- Replay policy.

---

# 28. Quick Revision Cheat Sheet

```text
Kafka = distributed commit log/event streaming platform
Topic = logical stream
Partition = ordered log inside topic
Offset = record position in partition
Broker = Kafka server
Producer = writes records
Consumer = reads records
Consumer group = consumers sharing partitions
Ordering = guaranteed per partition only
Key = decides partition and preserves order per key
Replication = copies partitions for fault tolerance
Leader = handles reads/writes for partition
Follower = replicates from leader
ISR = replicas caught up with leader
acks=all = strongest producer ack
min.insync.replicas = required synced replicas
Retention = Kafka keeps records by time/size
Compaction = keep latest value per key
Lag = latest offset - committed offset
DLT = failed records after retry
KRaft = modern Kafka metadata mode
ZooKeeper = legacy metadata dependency
Outbox = reliable DB + Kafka publishing
```

---

# 29. Interview Story You Can Say

```text
I have not operated Kafka deeply in production yet, but I understand how I would design and integrate it. Kafka stores events in topics split into partitions. Producers publish records with keys so Kafka can route related events to the same partition for ordering. Brokers store and replicate partitions. Consumers read in consumer groups, and each group tracks offsets independently.

In a Spring Boot order service, I would publish OrderCreated events to an orders.events topic using orderId as key. Inventory, email, and analytics would use separate consumer groups so each receives all events. For reliability, I would use acks=all, idempotent producer, replication factor 3, min.insync.replicas 2, manual offset commits, idempotent consumers, retries, DLT, and monitoring for lag. If the event is published after saving to DB, I would use the outbox pattern to avoid dual-write inconsistency.
```

---

# 30. References For Further Study

- Apache Kafka latest documentation: https://kafka.apache.org/documentation/
- Apache Kafka 4.3 documentation index: https://kafka.apache.org/43/
- Apache Kafka KRaft vs ZooKeeper documentation: https://kafka.apache.org/43/getting-started/zk2kraft/
- Apache Kafka Docker documentation: https://kafka.apache.org/43/getting-started/docker/
- Spring for Apache Kafka reference: https://docs.spring.io/spring-kafka/reference/index.html

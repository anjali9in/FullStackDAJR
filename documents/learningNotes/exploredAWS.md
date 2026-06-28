# AWS Certified Developer Associate (DVA-C02) Notes

Reference notes tailored for developers with 0-5 years of experience.
Goal: cover exam-ready fundamentals, interview talking points, and practical "zero to hero" understanding.

## 1. Exam Lens

- Think like a developer building, securing, deploying, and troubleshooting cloud-native apps on AWS.
- Focus less on "what is cloud?" and more on "which AWS service fits this application problem?"
- DVA-C02 is heavily practical: Lambda, API Gateway, DynamoDB, S3, IAM, event-driven design, CI/CD, observability, and troubleshooting matter a lot.

### DVA-C02 domain map

- Domain 1: Development with AWS Services
- Domain 2: Security
- Domain 3: Deployment
- Domain 4: Troubleshooting and Optimization

### What to optimize for in answers

- Use managed services first.
- Prefer least privilege IAM.
- Prefer decoupled and event-driven architectures.
- Prefer retry + idempotency for distributed systems.
- Prefer observability built in from the start.
- Prefer cost-aware scaling choices such as on-demand, auto scaling, and serverless where suitable.

## 2. Zero-to-Hero Mental Model

### Core AWS app flow

1. Client sends request.
2. Request hits API Gateway, ALB, or CloudFront.
3. Compute layer processes it using Lambda, ECS, EC2, or Elastic Beanstalk.
4. App reads or writes data to DynamoDB, RDS, ElastiCache, or S3.
5. Async work gets pushed to SQS, SNS, or EventBridge.
6. Monitoring goes to CloudWatch and tracing to X-Ray.
7. Access control is enforced using IAM, Cognito, KMS, Secrets Manager, and VPC rules.

### How to choose compute

- Lambda: best for event-driven, short-lived, bursty workloads, backend APIs, automation jobs.
- ECS/Fargate: best when you want containers without managing servers.
- EC2: best when you need OS-level control, custom runtimes, or long-running processes.
- Elastic Beanstalk: best for simple managed app deployment when you do not want to design all infrastructure yourself.

### How to choose storage

- S3: object storage for files, backups, logs, static assets.
- DynamoDB: NoSQL for high-scale key-value/document access with low latency.
- RDS: relational database when joins, ACID SQL modeling, and familiar engines matter.
- ElastiCache: cache for reducing read latency and offloading DB pressure.

## 3. Core Compute Notes

### AWS Lambda

- Runs code without managing servers.
- Billing is based on requests and execution duration.
- Good for APIs, stream processing, file processing, cron-style jobs, and event handlers.

### Lambda concepts that matter

- Cold start: extra startup latency when a new execution environment is created.
- Warm start: reused execution environment, usually faster.
- Execution context reuse: initialize SDK clients, DB connections, and static config outside the handler.
- Timeout: maximum 15 minutes.
- `/tmp` ephemeral storage: configurable for temporary files.
- Concurrency: number of function instances serving requests at the same time.
- Reserved concurrency: guarantees capacity for one function and limits its max concurrency.
- Provisioned concurrency: pre-initialized environments for lower latency.

### Invocation models

- Synchronous: API Gateway invokes Lambda and waits for response.
- Asynchronous: SNS, EventBridge, S3 can invoke Lambda and AWS retries automatically.
- Poll-based: Lambda polls SQS, Kinesis, DynamoDB Streams on your behalf.

### Failure handling

- Async Lambda retries can cause duplicate processing. Build idempotent handlers.
- DLQ can capture failed async events.
- Lambda Destinations can route success or failure records after invocation.
- For SQS event sources, failed messages can return to the queue after visibility timeout and eventually move to a DLQ.

### Interview note

- If asked how to optimize Lambda:
  - reduce package size
  - reuse connections outside handler
  - increase memory if CPU-bound
  - use provisioned concurrency only for latency-sensitive workloads
  - avoid putting Lambda in a VPC unless private access is required

### Elastic Beanstalk

- PaaS-style deployment for web apps.
- You upload code, AWS provisions instances, load balancer, scaling, health checks.
- Useful when the team wants less infra work but still needs traditional app hosting.

### ECS and Fargate

- ECS is the container orchestration service.
- Fargate is the serverless compute engine for ECS tasks.
- Prefer Fargate when you want containers without EC2 node management.
- Common exam contrast:
  - Lambda for event-driven short tasks
  - Fargate for containerized apps or jobs that need more runtime control

## 4. Storage and Database Notes

### Amazon S3

- Object storage with high durability and broad integration across AWS.
- Strong read-after-write consistency for object PUTs and DELETEs.
- Common uses: static site assets, uploads, logs, data lake, backup.

### S3 features developers should know

- Storage classes: Standard, Intelligent-Tiering, Standard-IA, One Zone-IA, Glacier tiers.
- Versioning: protects against accidental overwrites and deletes.
- Lifecycle rules: automatically transition or expire objects.
- Pre-signed URLs: secure temporary access to upload or download objects.
- Multipart upload: use for large objects.
- Event notifications: trigger Lambda, SNS, or SQS when objects are created or removed.

### S3 security

- Bucket policy: resource-based access policy on the bucket.
- IAM policy: identity-based access policy on users or roles.
- Block Public Access: prevent accidental public exposure.
- Encryption options:
  - SSE-S3: AWS managed keys
  - SSE-KMS: KMS-managed keys with audit/control
  - SSE-C: customer-provided keys

### DynamoDB

- Fully managed NoSQL service.
- Excellent for predictable low-latency access at scale.
- Model access patterns first, then design keys.

### DynamoDB key design

- Partition key only: simple key-value access.
- Partition key + sort key: enables grouped items and range queries.
- Hot partition problem: too much traffic on one partition key.
- Solve hot keys with better partition strategy, write sharding, caching, or different access design.

### Capacity and consistency

- On-demand: AWS scales automatically, simpler for variable traffic.
- Provisioned: define RCUs/WCUs, use auto scaling if traffic is more predictable.
- Strongly consistent reads: supported on base table and LSI, not GSI.
- Eventually consistent reads: cheaper and default behavior.

### Indexes and related features

- LSI: same partition key, different sort key, created at table creation time.
- GSI: different partition/sort key, can be added later, separate throughput behavior.
- Streams: capture item-level changes for event processing.
- TTL: expire items automatically for cleanup use cases like sessions.
- Transactions: use when multiple item writes must succeed atomically.
- DAX: in-memory cache for DynamoDB when ultra-fast read performance is needed.

### DynamoDB interview note

- Strong answer pattern:
  - explain access pattern
  - justify partition key choice
  - mention GSI if alternate query path is needed
  - mention idempotency and retry handling
  - mention eventual consistency tradeoff

### RDS and Aurora

- RDS is managed relational database service.
- Aurora is AWS cloud-optimized relational engine compatible with MySQL/PostgreSQL.
- Use RDS/Aurora when transactions, joins, and relational modeling matter.
- Read replicas improve read scalability.
- Multi-AZ improves availability, not read scaling.

## 5. Security, IAM, and Identity

### IAM basics

- Authentication: who are you?
- Authorization: what are you allowed to do?
- IAM user: long-term identity, less preferred for app workloads.
- IAM role: temporary credentials, preferred for AWS services and apps.
- IAM policy: JSON permissions document.

### Best practices

- Never hardcode access keys in code.
- Use IAM roles for EC2, Lambda, ECS tasks.
- Grant least privilege only.
- Prefer managed secrets services over env files for sensitive data.

### Policy evaluation ideas

- Explicit deny always wins.
- By default, requests are denied unless allowed.
- Resource-based and identity-based policies can both affect access.

### Secrets and configuration

- Systems Manager Parameter Store:
  - good for config values
  - supports SecureString
  - simpler and lower cost
- AWS Secrets Manager:
  - designed for secrets
  - supports rotation
  - common for database credentials and API keys

### KMS

- Used to create and manage encryption keys.
- Frequently appears with S3, EBS, RDS, Lambda env vars, and Secrets Manager.
- Know the difference between AWS managed keys and customer managed keys.

### Cognito

- User Pools: authentication, signup, signin, tokens.
- Identity Pools: grant temporary AWS credentials to users.
- Good fit when app needs user auth without building auth from scratch.

### Interview note

- If asked how a mobile or web app uploads directly to S3 securely:
  - authenticate user with Cognito
  - authorize temporary credentials or use pre-signed URL
  - keep bucket private

## 6. API, Messaging, and Event-Driven Design

### API Gateway

- Managed API front door for REST, HTTP, and WebSocket APIs.
- Commonly used with Lambda.
- Supports throttling, auth, request validation, transformations, caching, and stages.

### API auth options

- IAM auth
- Cognito authorizer
- Lambda authorizer

### SQS

- Pull-based message queue for decoupling components.
- Standard queue:
  - very high throughput
  - at-least-once delivery
  - best-effort ordering
- FIFO queue:
  - ordered processing
  - exactly-once processing semantics on the queue side
  - use deduplication ID and message group ID

### Important SQS concepts

- Visibility timeout: hide message while being processed.
- Long polling: reduces empty responses and cost.
- DLQ: capture poison messages after max receive count.
- Idempotency still matters because consumers may see duplicates.

### SNS

- Push-based pub/sub service.
- Good for fan-out to multiple subscribers.
- Can send to SQS, Lambda, HTTP endpoints, email.

### EventBridge

- Event bus for routing events between AWS services, SaaS apps, and custom apps.
- Better than SNS when you need filtering, routing rules, scheduled events, and loose event contracts.

### When to choose what

- SQS: queue work for one consumer group to process reliably.
- SNS: broadcast same message to multiple subscribers.
- EventBridge: route events based on rules and integrate many producers/consumers.

## 7. Deployment and DevOps Notes

### CloudFormation

- Infrastructure as Code using JSON or YAML.
- Supports stacks, change sets, parameters, outputs, mappings.
- Rollback helps if stack creation/update fails.

### AWS SAM

- Simplifies serverless infrastructure definitions.
- Great for Lambda, API Gateway, DynamoDB, event source mappings.
- Often used with `sam build`, `sam deploy`, and local testing.

### CI/CD services

- CodeCommit: Git repository service.
- CodeBuild: build and test service using `buildspec.yml`.
- CodeDeploy: deployment orchestration using `appspec.yml`.
- CodePipeline: pipeline automation across source, build, test, deploy stages.

### Deployment strategies

- All-at-once: fastest, highest risk.
- Rolling: safer than all-at-once.
- Blue/green: switch traffic between old and new environments.
- Canary: shift small traffic first, validate, then expand.

### Lambda deployment note

- Use versions and aliases.
- Weighted aliases support canary-style traffic shifting.
- CodeDeploy can automate safe Lambda deployments.

## 8. Monitoring, Logging, and Troubleshooting

### CloudWatch

- Metrics: numerical time-series data.
- Logs: app/system logs.
- Alarms: trigger actions when thresholds are breached.
- Dashboards: visualize metrics.
- Logs Insights: query logs for debugging.

### X-Ray

- Distributed tracing for microservices.
- Helps track latency across API Gateway, Lambda, downstream services.
- Useful for finding bottlenecks and error hotspots.

### Common troubleshooting patterns

- Lambda timeout:
  - check timeout setting
  - inspect CloudWatch logs
  - confirm downstream latency
  - tune memory and retry strategy
- DynamoDB throttling:
  - inspect consumed capacity
  - check partition design
  - use exponential backoff with jitter
  - consider on-demand or autoscaling
- SQS backlog:
  - scale consumers
  - inspect visibility timeout
  - move poison messages to DLQ
- API failures:
  - check API Gateway logs/metrics
  - validate IAM or authorizer config
  - trace request path with X-Ray

## 9. High-Value Exam and Interview Topics

### Idempotency

- Means repeated requests should not create unintended duplicate effects.
- Critical for retries in Lambda, SQS, SNS, EventBridge, and API operations.
- Common implementations:
  - request ID tracking
  - conditional writes in DynamoDB
  - deduplication keys

### Retry and backoff

- Distributed systems fail transiently.
- Use exponential backoff with jitter.
- Do not hammer DynamoDB, APIs, or third-party systems with immediate retries.

### Caching

- Cache frequently read data to reduce latency and cost.
- Common services:
  - CloudFront for content caching
  - API Gateway cache for APIs
  - ElastiCache for app/data caching
  - DAX for DynamoDB read acceleration

### Decoupling

- Async messaging reduces direct dependency between services.
- Helps absorb bursts, isolate failures, and improve resilience.

### Stateless design

- Keep app state outside compute where possible.
- Store state in DynamoDB, RDS, S3, or ElastiCache.
- Makes scaling easier for Lambda, ECS, and EC2 autoscaling groups.

## 10. Practical "What Should I Pick?" Shortcuts

- Need simple serverless REST API: API Gateway + Lambda + DynamoDB.
- Need auth for app users: Cognito User Pools.
- Need direct file upload: S3 pre-signed URL.
- Need broadcast to multiple systems: SNS.
- Need durable background processing: SQS.
- Need rule-based event routing: EventBridge.
- Need secrets rotation: Secrets Manager.
- Need app config storage: Parameter Store.
- Need SQL with managed failover: RDS/Aurora Multi-AZ.
- Need container app without server management: ECS on Fargate.

## 11. Interview-Focused Q&A Notes

### Explain Lambda cold start simply

- Cold start is the extra startup delay when AWS creates a new function execution environment.
- It matters more for Java/.NET, VPC-attached Lambdas, bigger packages, and latency-sensitive APIs.

### Why use SQS between services?

- To decouple producer and consumer, smooth traffic spikes, improve reliability, and allow retries without blocking the caller.

### DynamoDB vs RDS

- DynamoDB is better for massive scale and predictable key-based access.
- RDS is better when relational queries, joins, and strict SQL workflows matter.

### SNS vs SQS

- SNS pushes to many subscribers.
- SQS stores messages for consumers to pull and process.

### Why use IAM roles instead of access keys?

- Roles provide temporary credentials, reduce leakage risk, and are the standard secure pattern for AWS workloads.

### What is the difference between Multi-AZ and read replica?

- Multi-AZ improves availability and failover.
- Read replica improves read scalability.

## 12. Fast Revision Traps

- S3 is object storage, not a database.
- SQS is pull, SNS is push.
- Lambda max execution time is 15 minutes.
- Strongly consistent reads are not supported on DynamoDB GSI.
- Multi-AZ is for high availability, not horizontal reads.
- Reserved concurrency and provisioned concurrency are not the same.
- Secrets Manager rotates secrets; Parameter Store is simpler config storage.
- EventBridge is event routing, SQS is queuing, SNS is fan-out.

## 13. Last-Minute Memory Sheet

- Secure apps with IAM roles, KMS, Cognito, Secrets Manager.
- Build serverless apps with API Gateway, Lambda, DynamoDB, S3.
- Decouple with SQS, SNS, EventBridge.
- Deploy with CloudFormation, SAM, CodePipeline, CodeBuild, CodeDeploy.
- Observe with CloudWatch and X-Ray.
- Troubleshoot with logs, metrics, traces, retries, and DLQs.
- Design for idempotency, retries, least privilege, and failure handling.

## 14. References

- AWS Certified Developer - Associate exam guide: https://aws.amazon.com/certification/certified-developer-associate/
- Lambda quotas and behavior: https://docs.aws.amazon.com/lambda/latest/dg/gettingstarted-limits.html
- Amazon S3 developer guide: https://docs.aws.amazon.com/AmazonS3/latest/userguide/Welcome.html
- DynamoDB developer guide: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Introduction.html
- API Gateway developer guide: https://docs.aws.amazon.com/apigateway/latest/developerguide/welcome.html
- Amazon SQS developer guide: https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/welcome.html
- Amazon SNS developer guide: https://docs.aws.amazon.com/sns/latest/dg/welcome.html
- Amazon EventBridge user guide: https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-what-is.html
- Amazon Cognito developer guide: https://docs.aws.amazon.com/cognito/latest/developerguide/what-is-amazon-cognito.html
- AWS SAM developer guide: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html

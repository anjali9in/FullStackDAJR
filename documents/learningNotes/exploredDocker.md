# Explored Docker

Revision notes for a 1-5 years developer. Covers Docker concepts, commands, Dockerfile, images, containers, volumes, networks, Docker Compose, application usage, debugging, optimization, and interview cheat sheets.

Docker is a platform for packaging and running applications in isolated environments called containers. A container includes your application, runtime, libraries, environment variables, and filesystem dependencies. This makes applications easier to run consistently across local development, CI/CD, staging, and production.

---

# 1. Docker In One Paragraph

Docker lets developers package an application and its dependencies into an image. That image can be run as a container on any machine with Docker installed. A Dockerfile is the recipe, an image is the built package, and a container is the running instance. Docker is widely used for local development, microservices, databases, message brokers, CI/CD builds, and production deployments.

Interview answer:

```text
Docker solves the "works on my machine" problem by packaging an application with its runtime and dependencies into a portable image. The image runs as an isolated container. It helps keep development and deployment environments consistent.
```

---

# 2. Why Docker Is Used

## Problems Docker Solves

- Different developer machines have different software versions.
- New joiners spend too much time installing databases and tools.
- App works locally but fails in staging/production.
- Multiple projects need different versions of MySQL, Redis, Java, Node, etc.
- CI/CD needs repeatable builds.
- Microservices need isolated local environments.

## Common Use Cases

- Run databases locally: MySQL, PostgreSQL, MongoDB, Redis.
- Run message brokers: Kafka, RabbitMQ.
- Package backend APIs.
- Package frontend apps with Nginx.
- Run integration tests.
- Build CI/CD pipelines.
- Deploy microservices.
- Create reproducible development environments.

## What To Install Natively vs Run In Docker

For normal developer productivity:

| Tool Type | Recommended |
|---|---|
| Databases | Docker |
| Redis/Kafka/RabbitMQ | Docker |
| Elasticsearch/OpenSearch | Docker |
| Java JDK for coding | Native |
| Maven/Gradle for IDE integration | Native |
| Node/npm for frontend development | Native |
| Production runtime inside image | Docker |

Why:

```text
Run infrastructure in Docker. Install language runtimes/build tools natively when your IDE needs them for autocomplete, debugging, test running, hot reload, and local productivity.
```

Example:

- For Spring Boot coding, install JDK locally for IntelliJ.
- For production packaging, Docker image still includes JRE/JDK runtime.
- For MySQL, Redis, Kafka, use Docker locally instead of installing directly on your laptop.

---

# 3. Core Concepts

| Concept | Meaning |
|---|---|
| Dockerfile | Text recipe to build an image |
| Image | Read-only packaged application |
| Container | Running instance of an image |
| Registry | Place to store/pull/push images |
| Docker Hub | Public image registry |
| Docker Engine | Runtime that builds/runs containers |
| Docker Desktop | Desktop app for Mac/Windows/Linux |
| Layer | Image filesystem step/cache unit |
| Volume | Persistent Docker-managed storage |
| Bind Mount | Host folder mounted into container |
| Network | Private network for containers |
| Compose | Tool for defining multi-container apps |
| Build Context | Files sent to Docker during build |
| Tag | Image version/name label |
| Port Mapping | Expose container port to host |
| Entrypoint/CMD | Container startup command |

---

# 4. Dockerfile, Image, Container

## Dockerfile = Recipe

A Dockerfile is a text file with build instructions.

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Analogy:

```text
Dockerfile = recipe
```

## Image = Packaged Application

Build image:

```bash
docker build -t my-api:1.0 .
```

Analogy:

```text
Image = baked cake
```

## Container = Running Image

Run container:

```bash
docker run -p 8080:8080 my-api:1.0
```

Analogy:

```text
Container = cake being served/running
```

## Docker Engine = Builder And Runner

Docker reads Dockerfile, builds image, and runs container.

Daily flow:

```text
Write Dockerfile
Build image
Run container
Push image to registry
Deploy image
```

---

# 5. Basic Commands

## Version And Info

```bash
docker --version
docker version
docker info
```

## Images

```bash
docker images
docker image ls
docker pull nginx:latest
docker rmi nginx:latest
```

## Containers

```bash
docker ps
docker ps -a
docker run nginx
docker stop <container_id>
docker start <container_id>
docker restart <container_id>
docker rm <container_id>
```

## Logs

```bash
docker logs <container_id>
docker logs -f <container_id>
docker logs --tail 100 <container_id>
```

## Execute Command Inside Container

```bash
docker exec -it <container_id> sh
docker exec -it <container_id> bash
```

## Inspect

```bash
docker inspect <container_id>
docker inspect <image_name>
```

## Stats

```bash
docker stats
```

## Cleanup

```bash
docker container prune
docker image prune
docker volume prune
docker network prune
docker system prune
```

Danger:

```bash
docker system prune -a --volumes
```

This removes unused images, containers, networks, and volumes. Do not run casually if you need local database data.

---

# 6. Running Containers

## Run Nginx

```bash
docker run --name web -p 8080:80 nginx:latest
```

Open:

```text
http://localhost:8080
```

Explanation:

```text
Host port 8080 -> container port 80
```

## Run In Background

```bash
docker run -d --name web -p 8080:80 nginx:latest
```

`-d` means detached mode.

## Remove Container Automatically

```bash
docker run --rm alpine:latest echo "hello"
```

## Set Environment Variables

```bash
docker run -e MYSQL_ROOT_PASSWORD=root mysql:8.4
```

## Name A Container

```bash
docker run --name mysql-db mysql:8.4
```

## Restart Policy

```bash
docker run -d --restart unless-stopped nginx
```

Common policies:

- `no`
- `always`
- `unless-stopped`
- `on-failure`

---

# 7. Images And Tags

Image format:

```text
registry/repository/image:tag
```

Examples:

```text
nginx:latest
mysql:8.4
redis:7
eclipse-temurin:21-jre
docker.io/library/nginx:latest
```

Tag image:

```bash
docker tag my-api:1.0 username/my-api:1.0
```

Push:

```bash
docker push username/my-api:1.0
```

Pull:

```bash
docker pull username/my-api:1.0
```

Best practices:

- Avoid `latest` in production.
- Use explicit versions.
- Use immutable tags or image digests in critical deployments.

Bad:

```text
my-api:latest
```

Better:

```text
my-api:1.3.7
my-api:2026-06-26-commitsha
```

---

# 8. Dockerfile Instructions

## Common Instructions

| Instruction | Purpose |
|---|---|
| `FROM` | Base image |
| `WORKDIR` | Working directory |
| `COPY` | Copy files into image |
| `ADD` | Copy with extra features, use rarely |
| `RUN` | Run command during build |
| `CMD` | Default runtime command |
| `ENTRYPOINT` | Main executable |
| `ENV` | Environment variable |
| `ARG` | Build-time variable |
| `EXPOSE` | Document container port |
| `USER` | Run as specific user |
| `VOLUME` | Declare mount point |
| `HEALTHCHECK` | Container health command |
| `LABEL` | Metadata |

## `RUN` vs `CMD` vs `ENTRYPOINT`

`RUN` executes while building image:

```dockerfile
RUN apt-get update && apt-get install -y curl
```

`CMD` provides default command:

```dockerfile
CMD ["node", "server.js"]
```

`ENTRYPOINT` defines main executable:

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Interview answer:

```text
RUN is build-time. CMD and ENTRYPOINT are runtime. CMD can be overridden easily. ENTRYPOINT is usually used for the main executable.
```

---

# 9. Dockerfile Best Practices

## Use Small Base Images

```dockerfile
FROM eclipse-temurin:21-jre
```

Instead of full JDK for runtime when only JRE is needed.

## Use Multi-Stage Builds

Build stage contains compilers/build tools. Runtime stage contains only final artifact.

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Benefits:

- Smaller final image.
- Fewer security vulnerabilities.
- Build tools not included in production image.

## Optimize Cache

Bad:

```dockerfile
COPY . .
RUN mvn clean package
```

Better:

```dockerfile
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package
```

Why:

```text
Dependencies change less often than source code, so Docker can reuse dependency layer cache.
```

## Use `.dockerignore`

```dockerignore
.git
target
node_modules
.idea
.vscode
*.log
.env
```

Why:

- Smaller build context.
- Faster builds.
- Avoid leaking secrets.

## Do Not Run As Root

```dockerfile
RUN addgroup --system app && adduser --system --ingroup app app
USER app
```

## Do Not Put Secrets In Image

Bad:

```dockerfile
ENV DB_PASSWORD=secret
```

Good:

```text
Inject secrets at runtime through secret manager, environment variables, or orchestrator secret support.
```

## Pin Important Versions

```dockerfile
FROM eclipse-temurin:21.0.7_6-jre
```

Or use digest for strict reproducibility.

---

# 10. Build Context

Build command:

```bash
docker build -t my-api .
```

The final `.` is build context. Docker sends files in that directory to the builder.

Problem:

```text
If your project has node_modules, target, logs, or secrets, they may be sent unless ignored.
```

Use `.dockerignore`.

Check build context size in build output.

---

# 11. Docker Layers And Cache

Each Dockerfile instruction creates a layer.

Example:

```dockerfile
FROM node:22-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build
```

If only source changes:

- `npm ci` layer can be reused.
- only later layers rebuild.

Cache invalidation:

```text
When a layer changes, all layers after it rebuild.
```

Interview answer:

```text
I place dependency files before source copy in Dockerfile so dependency installation can be cached. This makes builds faster.
```

---

# 12. Volumes And Bind Mounts

Containers are disposable. Data inside container filesystem is lost when container is removed unless persisted.

## Volume

Docker-managed persistent storage.

Create:

```bash
docker volume create mysql-data
```

Use:

```bash
docker run -d \
  --name mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -v mysql-data:/var/lib/mysql \
  mysql:8.4
```

List:

```bash
docker volume ls
```

Inspect:

```bash
docker volume inspect mysql-data
```

## Bind Mount

Mount host folder into container.

```bash
docker run -v "$(pwd)":/app node:22-alpine
```

Use cases:

- Local development source code.
- Config files.
- Logs during debugging.

## Volume vs Bind Mount

| Feature | Volume | Bind Mount |
|---|---|---|
| Managed by Docker | Yes | No |
| Host path dependent | No | Yes |
| Good for database data | Yes | Usually no |
| Good for source code dev | Sometimes | Yes |
| Portable | Better | Less |

Interview answer:

```text
Use volumes for persistent container data like databases. Use bind mounts for local development when source files from host need to be visible inside the container.
```

---

# 13. Networks

Docker containers communicate through networks.

## List Networks

```bash
docker network ls
```

## Create Network

```bash
docker network create app-network
```

## Run Containers On Same Network

```bash
docker run -d --name mysql \
  --network app-network \
  -e MYSQL_ROOT_PASSWORD=root \
  mysql:8.4
```

```bash
docker run -d --name api \
  --network app-network \
  -p 8080:8080 \
  my-api:1.0
```

Inside `api`, connect to database using container name:

```text
mysql:3306
```

Not:

```text
localhost:3306
```

Why:

```text
Inside a container, localhost means the container itself. Use service/container name over Docker network.
```

---

# 14. Port Mapping

Format:

```text
host_port:container_port
```

Example:

```bash
docker run -p 8080:80 nginx
```

Means:

```text
localhost:8080 on host -> port 80 inside container
```

Expose only to localhost:

```bash
docker run -p 127.0.0.1:8080:80 nginx
```

Random host port:

```bash
docker run -P nginx
```

Show ports:

```bash
docker port <container_id>
```

---

# 15. Environment Variables

Pass env:

```bash
docker run -e SPRING_PROFILES_ACTIVE=dev my-api
```

Env file:

```env
SPRING_PROFILES_ACTIVE=dev
DB_HOST=mysql
DB_PORT=3306
```

Run:

```bash
docker run --env-file .env my-api
```

Security:

```text
Environment variables are convenient but not perfect secrets. In production, prefer orchestrator secret management or cloud secret manager.
```

---

# 16. Docker Compose

Docker Compose defines multi-container applications in YAML.

File names:

```text
compose.yaml
docker-compose.yml
```

Modern command:

```bash
docker compose up
```

not legacy:

```bash
docker-compose up
```

## Basic Compose Example

```yaml
services:
  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://database:3306/app_db
      SPRING_DATASOURCE_USERNAME: app_user
      SPRING_DATASOURCE_PASSWORD: app_password
    depends_on:
      database:
        condition: service_healthy

  database:
    image: mysql:8.4
    environment:
      MYSQL_DATABASE: app_db
      MYSQL_USER: app_user
      MYSQL_PASSWORD: app_password
      MYSQL_ROOT_PASSWORD: root_password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
```

Run:

```bash
docker compose up -d
```

Stop:

```bash
docker compose down
```

Stop and remove volumes:

```bash
docker compose down -v
```

Be careful: `-v` removes database volume.

## Compose Networking

Compose creates a private network automatically.

Service names become DNS names:

```text
api can call database:3306
database can be reached by name "database"
```

Spring Boot datasource inside Docker:

```properties
spring.datasource.url=jdbc:mysql://database:3306/app_db
```

From host machine:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/app_db
```

Important:

```text
localhost inside container means the same container. localhost on your laptop means your laptop.
```

---

# 17. Compose Commands

## Start

```bash
docker compose up
docker compose up -d
docker compose up --build
```

## Stop

```bash
docker compose stop
docker compose down
```

## Logs

```bash
docker compose logs
docker compose logs -f
docker compose logs -f api
```

## List

```bash
docker compose ps
```

## Execute

```bash
docker compose exec api sh
docker compose exec database mysql -u app_user -p app_db
```

## Rebuild One Service

```bash
docker compose build api
docker compose up -d api
```

## Restart One Service

```bash
docker compose restart api
```

## Remove Volumes

```bash
docker compose down -v
```

## Validate Config

```bash
docker compose config
```

---

# 18. Compose Watch

Compose Watch can rebuild or sync files when source files change.

Run:

```bash
docker compose up --watch
```

Example:

```yaml
services:
  web:
    build: .
    ports:
      - "3000:3000"
    develop:
      watch:
        - action: sync
          path: ./src
          target: /app/src
        - action: rebuild
          path: package.json
```

Use when:

- You want a container-based dev workflow.
- Changes should sync into container automatically.
- Dependency file changes should rebuild image.

For React/Node local development, native Node is often simpler and faster. Use Docker mainly for deployment image and infrastructure services unless the team standard is container-only development.

---

# 19. Spring Boot Application With Docker

## Simple Dockerfile For Prebuilt JAR

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/fullstack.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build JAR:

```bash
mvn clean package -DskipTests
```

Build image:

```bash
docker build -t fullstack-api:1.0 .
```

Run:

```bash
docker run -p 8080:8080 fullstack-api:1.0
```

## Multi-Stage Dockerfile For Maven

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN addgroup --system app && adduser --system --ingroup app app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Spring Boot With MySQL Compose

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/fullstack
      SPRING_DATASOURCE_USERNAME: app_user
      SPRING_DATASOURCE_PASSWORD: app_password
    depends_on:
      mysql:
        condition: service_healthy

  mysql:
    image: mysql:8.4
    environment:
      MYSQL_DATABASE: fullstack
      MYSQL_USER: app_user
      MYSQL_PASSWORD: app_password
      MYSQL_ROOT_PASSWORD: root_password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 10

volumes:
  mysql-data:
```

Application profile:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

---

# 20. React Application With Docker

## Production Build With Nginx

```dockerfile
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Build:

```bash
docker build -t react-app:1.0 .
```

Run:

```bash
docker run -p 3000:80 react-app:1.0
```

## Development Note

For day-to-day React work:

```text
Native Node + Vite dev server is usually easier for hot reload.
Docker image is useful for production packaging and CI/CD.
```

---

# 21. Node.js API With Docker

```dockerfile
FROM node:22-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev
COPY . .
ENV NODE_ENV=production
EXPOSE 3000
CMD ["node", "server.js"]
```

With non-root user:

```dockerfile
FROM node:22-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev
COPY . .
RUN chown -R node:node /app
USER node
EXPOSE 3000
CMD ["node", "server.js"]
```

---

# 22. Database Containers

## MySQL

```bash
docker run -d \
  --name mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=app_db \
  -e MYSQL_USER=app_user \
  -e MYSQL_PASSWORD=app_password \
  -p 3306:3306 \
  -v mysql-data:/var/lib/mysql \
  mysql:8.4
```

Connect:

```bash
docker exec -it mysql mysql -u app_user -p app_db
```

## PostgreSQL

```bash
docker run -d \
  --name postgres \
  -e POSTGRES_DB=app_db \
  -e POSTGRES_USER=app_user \
  -e POSTGRES_PASSWORD=app_password \
  -p 5432:5432 \
  -v pg-data:/var/lib/postgresql/data \
  postgres:17
```

Connect:

```bash
docker exec -it postgres psql -U app_user -d app_db
```

## Redis

```bash
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7
```

CLI:

```bash
docker exec -it redis redis-cli
```

## MongoDB

```bash
docker run -d \
  --name mongo \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=root \
  -p 27017:27017 \
  -v mongo-data:/data/db \
  mongo:8
```

---

# 23. Kafka With Docker Compose

Simple local Kafka:

```yaml
services:
  kafka:
    image: apache/kafka:4.3.0
    ports:
      - "9092:9092"
```

For real project Compose, configure advertised listeners carefully. Kafka networking is more complex than MySQL/Redis because clients need broker addresses that are valid from where the client runs.

Interview answer:

```text
Databases and Redis are straightforward in Docker. Kafka needs extra care with advertised listeners because clients must receive a reachable broker address.
```

---

# 24. Healthchecks

Dockerfile healthcheck:

```dockerfile
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

Compose healthcheck:

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 5s
  retries: 3
```

Spring Boot Actuator dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Health endpoint:

```text
/actuator/health
```

---

# 25. Docker And CI/CD

Typical pipeline:

```text
Checkout code
Run tests
Build artifact
Build Docker image
Scan image
Push image to registry
Deploy image
```

Commands:

```bash
docker build -t registry.example.com/fullstack-api:${COMMIT_SHA} .
docker push registry.example.com/fullstack-api:${COMMIT_SHA}
```

Run container in staging:

```bash
docker run -d \
  --name fullstack-api \
  -p 8080:8080 \
  --env-file staging.env \
  registry.example.com/fullstack-api:${COMMIT_SHA}
```

Best practice:

```text
Build once, promote the same image across environments. Change config through environment variables/secrets, not by rebuilding image for each environment.
```

---

# 26. Docker Buildx And Multi-Architecture

Apple Silicon Mac uses ARM64. Many servers use AMD64.

Check architecture:

```bash
docker image inspect my-api:1.0 --format '{{.Architecture}}'
```

Build for specific platform:

```bash
docker buildx build --platform linux/amd64 -t my-api:amd64 .
```

Build multi-arch and push:

```bash
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t username/my-api:1.0 \
  --push .
```

Interview point:

```text
On M-series Mac, architecture matters. If production runs amd64, build/test amd64 images or publish multi-architecture images.
```

---

# 27. Docker Security

## Security Checklist

- Use trusted base images.
- Use small runtime images.
- Do not run as root.
- Do not bake secrets into images.
- Pin image versions.
- Scan images for vulnerabilities.
- Keep base images updated.
- Use `.dockerignore`.
- Expose only required ports.
- Use read-only filesystem when possible.
- Drop Linux capabilities where possible.
- Use secrets manager in production.

## Run As Non-Root

```dockerfile
RUN addgroup --system app && adduser --system --ingroup app app
USER app
```

## Read-Only Container

```bash
docker run --read-only my-api
```

If app needs temp:

```bash
docker run --read-only --tmpfs /tmp my-api
```

## Limit Resources

```bash
docker run --memory=512m --cpus=1 my-api
```

## Scan Image

```bash
docker scout quickview my-api:1.0
docker scout cves my-api:1.0
```

## SBOM

SBOM means Software Bill of Materials.

Generate with Docker Scout:

```bash
docker scout sbom my-api:1.0
```

Why:

```text
SBOM lists packages inside an image and helps with vulnerability/compliance tracking.
```

---

# 28. Secrets

Do not commit `.env` with real passwords.

Bad:

```dockerfile
ENV JWT_SECRET=my-secret
```

Better local Compose:

```yaml
services:
  api:
    env_file:
      - .env.local
```

Production:

- Kubernetes Secrets.
- Docker Swarm secrets.
- AWS Secrets Manager.
- Azure Key Vault.
- GCP Secret Manager.
- Vault.

Rule:

```text
Image should be environment-neutral. Secrets are runtime configuration.
```

---

# 29. Debugging Containers

## Check Running Containers

```bash
docker ps
docker ps -a
```

## Logs

```bash
docker logs -f api
```

## Shell

```bash
docker exec -it api sh
```

If bash exists:

```bash
docker exec -it api bash
```

## Inspect Environment

```bash
docker exec api env
```

## Check Files

```bash
docker exec -it api ls -la /app
```

## Check Network From Container

```bash
docker exec -it api sh
```

Inside:

```bash
ping database
nc -zv database 3306
curl http://other-service:8080/actuator/health
```

Some minimal images do not include `ping`, `curl`, or `nc`.

## Copy Files

```bash
docker cp api:/app/logs/app.log ./app.log
docker cp ./config.yml api:/app/config.yml
```

## Inspect Container

```bash
docker inspect api
```

## See Processes

```bash
docker top api
```

---

# 30. Common Issues And Fixes

## Port Already In Use

Error:

```text
bind: address already in use
```

Fix:

```bash
lsof -i :8080
docker ps
```

Change host port:

```yaml
ports:
  - "8081:8080"
```

## App Cannot Connect To DB

If app runs inside Docker Compose:

```properties
jdbc:mysql://database:3306/app_db
```

If app runs natively on laptop:

```properties
jdbc:mysql://localhost:3306/app_db
```

## Container Exits Immediately

Check:

```bash
docker logs <container>
docker inspect <container>
```

Common reasons:

- App crashed.
- Missing env variable.
- Wrong command.
- Port/config issue.

## Changes Not Reflected

If code is copied during build:

```bash
docker compose up --build
```

If code is bind-mounted:

```bash
docker compose restart api
```

or app hot reload handles it.

## Database Data Still Old

Volume persists data.

Reset:

```bash
docker compose down -v
docker compose up -d
```

Warning:

```text
This deletes local database data.
```

## Permission Issues With Volumes

Common on Linux and non-root containers.

Fix options:

- Use correct `USER`.
- Adjust ownership.
- Use named volumes.
- Avoid writing to application directory.

---

# 31. Docker For Local Development

Recommended setup for many backend developers:

```text
Run app natively for debugging and hot reload.
Run dependencies in Docker Compose.
Build Docker image for deployment and CI.
```

Example Compose for local dependencies:

```yaml
services:
  mysql:
    image: mysql:8.4
    environment:
      MYSQL_DATABASE: fullstack
      MYSQL_USER: app_user
      MYSQL_PASSWORD: app_password
      MYSQL_ROOT_PASSWORD: root_password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  redis:
    image: redis:7
    ports:
      - "6379:6379"

volumes:
  mysql-data:
```

Run:

```bash
docker compose up -d
```

Spring Boot native app config:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fullstack
spring.datasource.username=app_user
spring.datasource.password=app_password
```

---

# 32. Docker In Production

In production, Docker is usually not run manually with `docker run` for large systems. It is managed by:

- Kubernetes.
- Docker Swarm.
- ECS.
- Nomad.
- Cloud Run.
- Azure Container Apps.
- VM with systemd/docker compose for small deployments.

Production concerns:

- Logging.
- Metrics.
- Health checks.
- Secrets.
- Resource limits.
- Rolling deployments.
- Rollbacks.
- Persistent storage.
- Image scanning.
- Network policies.
- Backups.

Interview answer:

```text
Docker packages the app. An orchestrator usually manages production containers: scheduling, scaling, service discovery, secrets, rollout, rollback, and health checks.
```

---

# 33. Docker vs Virtual Machine

| Feature | Docker Container | Virtual Machine |
|---|---|---|
| Isolation | Process-level | Full OS-level |
| Startup | Seconds | Minutes |
| Size | MBs/low GBs | GBs |
| Kernel | Shares host kernel | Own guest OS kernel |
| Use case | App packaging/microservices | Full OS isolation |

Interview answer:

```text
Containers share host OS kernel and package app dependencies. VMs virtualize full operating systems. Containers are lighter and faster, while VMs provide stronger OS-level isolation.
```

---

# 34. Docker vs Kubernetes

Docker:

```text
Build and run containers.
```

Kubernetes:

```text
Orchestrates containers across machines.
```

Kubernetes handles:

- Scaling.
- Scheduling.
- Self-healing.
- Service discovery.
- Rolling updates.
- Config/secrets.
- Load balancing.

Interview answer:

```text
Docker is containerization. Kubernetes is container orchestration. Docker can run one container on one machine; Kubernetes manages many containers across a cluster.
```

---

# 35. Useful Command Cheat Sheet

## Images

```bash
docker images
docker pull image:tag
docker build -t name:tag .
docker tag source:tag target:tag
docker push name:tag
docker rmi image:tag
```

## Containers

```bash
docker ps
docker ps -a
docker run --name name image
docker run -d -p 8080:80 image
docker stop name
docker start name
docker restart name
docker rm name
docker logs -f name
docker exec -it name sh
```

## Volumes

```bash
docker volume ls
docker volume create name
docker volume inspect name
docker volume rm name
docker volume prune
```

## Networks

```bash
docker network ls
docker network create name
docker network inspect name
docker network rm name
docker network prune
```

## Compose

```bash
docker compose up
docker compose up -d
docker compose up --build
docker compose down
docker compose down -v
docker compose ps
docker compose logs -f
docker compose exec service sh
docker compose build service
docker compose restart service
docker compose config
```

## Cleanup

```bash
docker container prune
docker image prune
docker volume prune
docker network prune
docker system prune
docker system df
```

---

# 36. Common Interview Questions

## What Is Docker?

Docker is a platform to build, package, distribute, and run applications in containers.

## What Is Container?

A running isolated process created from an image.

## What Is Image?

A read-only package containing application code, runtime, libraries, and configuration.

## Dockerfile vs Image vs Container

Dockerfile is recipe. Image is built package. Container is running instance.

## What Is Docker Compose?

Compose defines and runs multi-container apps using YAML.

## Why Use Multi-Stage Build?

To separate build dependencies from runtime image, making final image smaller and safer.

## COPY vs ADD

`COPY` copies files. `ADD` can also extract archives and fetch URLs. Prefer `COPY` unless extra `ADD` behavior is required.

## CMD vs ENTRYPOINT

`CMD` provides default arguments/command and is easy to override. `ENTRYPOINT` defines the main executable.

## Volume vs Bind Mount

Volume is Docker-managed persistent storage. Bind mount maps a host path into the container.

## Why Container Cannot Connect To localhost DB?

Inside a container, `localhost` means the container itself. Use host networking mechanisms or Compose service name like `database`.

## How Do Containers Communicate In Compose?

Compose creates a network and service names become DNS names.

## What Is `.dockerignore`?

File that excludes files from build context, similar to `.gitignore`.

## How To Reduce Image Size?

Use multi-stage builds, smaller base images, `.dockerignore`, remove build tools from runtime image, and avoid unnecessary files.

## How To Debug Container?

Use `docker logs`, `docker exec`, `docker inspect`, `docker stats`, and check environment/network/files.

## What Is Difference Between Docker And VM?

Containers share host kernel and are lightweight. VMs run full guest OS and are heavier.

## What Is Docker Registry?

A service that stores and distributes images, such as Docker Hub, ECR, GCR, ACR, or private registry.

---

# 37. Common Mistakes

- Using `latest` in production.
- Running container as root.
- Storing secrets in Dockerfile.
- Forgetting `.dockerignore`.
- Putting database data only inside container filesystem.
- Using `localhost` from one container to reach another.
- Not using healthchecks.
- Building huge images with full build tools.
- Not pinning base image versions.
- Exposing unnecessary ports.
- Running `docker compose down -v` and deleting data accidentally.
- Not checking image architecture on M1/M2/M3/M4 Mac.
- Assuming Docker Compose is production orchestration for large systems.
- Not scanning images.

---

# 38. Practical Project Checklist

For a backend project:

- Add `Dockerfile`.
- Add `.dockerignore`.
- Add `compose.yaml` for local dependencies.
- Use env variables for config.
- Add healthcheck endpoint.
- Add non-root user.
- Add explicit image tag.
- Add CI build step.
- Add image scan step.
- Push image to registry.
- Use migrations for database schema.
- Document local commands in README.

Example local commands:

```bash
docker compose up -d mysql redis
mvn spring-boot:run
```

Example package commands:

```bash
mvn clean package
docker build -t fullstack-api:local .
docker run -p 8080:8080 --env-file .env.local fullstack-api:local
```

---

# 39. Final Interview Story

```text
I use Docker to package applications and run dependencies consistently. For local backend development, I usually run databases, Redis, Kafka, and other infrastructure through Docker Compose, while running the app natively for easier debugging. For deployment, I create a Docker image using a Dockerfile, usually with a multi-stage build, and push it to a registry.

I understand the difference between Dockerfile, image, and container. I use volumes for persistent database data, networks for container communication, Compose for multi-container local environments, and explicit image tags for repeatable deployments. For production-quality images, I keep images small, avoid root users, avoid baking secrets, add health checks, scan images, and configure apps through environment variables.
```

---

# 40. References

- Docker docs: https://docs.docker.com/
- Dockerfile reference: https://docs.docker.com/reference/dockerfile/
- Dockerfile best practices: https://docs.docker.com/develop/develop-images/dockerfile_best-practices/
- Multi-stage builds: https://docs.docker.com/build/building/multi-stage/
- Docker Compose docs: https://docs.docker.com/compose/
- Compose Watch: https://docs.docker.com/compose/how-tos/file-watch/
- Docker volumes: https://docs.docker.com/engine/storage/volumes/
- Docker Scout image analysis: https://docs.docker.com/scout/explore/analysis/
- SBOM attestations: https://docs.docker.com/build/metadata/attestations/sbom/

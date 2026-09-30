# RevConnect Project Presentation Guide

This guide describes the implementation that is actually present in this repository. It distinguishes implemented behavior from general microservices concepts and calls out gaps where the code, configuration, and frontend are not perfectly aligned.

## 1. Project overview

### What RevConnect does

RevConnect is a social-network-style application. The implemented domain includes:

- User registration, login, profiles, account types, and creator-only behavior.
- Posts with image upload, captions, publication status, and ownership checks.
- A feed assembled from published posts.
- Connections, connection requests, and follows.
- Post interactions: likes, comments, shares, and reposts.
- Notifications generated for post-owner interactions.
- Products for business users.
- Analytics events and summaries.

The repository contains a browser frontend, a Spring Cloud API Gateway, Eureka service discovery, a Spring Cloud Config Server, ten application services, Docker assets, Kubernetes manifests, and a Jenkins pipeline.

### Architecture at a glance

```text
Browser / frontend:8080 or standalone port 5500
                 |
                 | /api requests
                 v
API Gateway:9000
  - CORS
  - JWT edge validation for non-public writes
  - X-User-Id propagation
  - lb:// service-id routing
  - Resilience4j CircuitBreaker + fallback routes
                 |
                 +------------------> Eureka:8761 resolves service IDs
                 |
                 +------------------> Config Server:8888 supplies properties
                 |
                 +--> user-service:8081
                 +--> post-service:8082
                 +--> feed-service:8083
                 +--> connection-service:8084
                 +--> interaction-service:8085
                 +--> notification-service:8086
                 +--> product-service:8087
                 +--> analytics-service:8088

Each database-backed service uses its own MySQL schema/database.
```

### Request path: Frontend -> Gateway -> Eureka -> Service

1. The frontend sends an HTTP request to `/api/...`. In the Docker setup, Nginx listens on port `8080` and proxies `/api/` to `http://api-gateway:9000`.
2. The Gateway receives the request and applies its global `JwtGatewayFilter`. `OPTIONS`, `/api/auth/**`, actuator, fallback paths, and every `GET` are allowed through this filter. Other methods require `Authorization: Bearer ...`.
3. When a JWT is valid, the Gateway extracts the `userId` claim and adds `X-User-Id` to the forwarded request.
4. Gateway route configuration uses service IDs such as `lb://interaction-service`, not fixed host/port URLs.
5. Spring Cloud LoadBalancer/Eureka resolves the service ID to a registered instance.
6. The target service processes the request and accesses only its own database.
7. Some services use OpenFeign to call another service by Eureka service ID. For example, Interaction calls `post-service`, `user-service`, and `notification-service`.

### Why use microservices here?

The code separates domains and data ownership. User, posts, social relationships, interactions, notifications, products, feed, and analytics can be developed and deployed independently. Each service has its own Spring Boot application, persistence layer, configuration, and container image.

This also introduces real distributed-system costs: network failures, service discovery, configuration management, multiple databases, eventual consistency, and the need for resilience. The project addresses some of these with Eureka, Config Server, Feign, CircuitBreaker, Docker, and Kubernetes.

### Service and infrastructure ports

| Component | Port | Evidence / purpose |
|---|---:|---|
| Frontend container | 8080 | Docker Compose and frontend container port |
| User Service | 8081 | `configrepo/user-service.properties` |
| Post Service | 8082 | `configrepo/post-service.properties` |
| Feed Service | 8083 | `configrepo/feed-service.properties` |
| Connection Service | 8084 | `configrepo/connection-service.properties` |
| Interaction Service | 8085 | `configrepo/interaction-service.properties` |
| Notification Service | 8086 | `configrepo/notification-service.properties` |
| Product Service | 8087 | `configrepo/product-service.properties` |
| Analytics Service | 8088 | `configrepo/analytics-service.properties` |
| API Gateway | 9000 | `configrepo/api-gateway.properties` |
| Config Server | 8888 | `config-server/src/main/resources/application.properties` |
| Eureka Server | 8761 | `service-discovery/src/main/resources/application.properties` |
| MySQL host port | 3307 | Docker Compose maps host `3307` to container `3306` |
| MySQL container port | 3306 | Service-to-service database port |

## 2. Microservices

### User Service

**Purpose:** Owns users, authentication, profiles, passwords, JWT creation, and user search.

**Important files:**

- `user-service/src/main/java/com/revconnect/userservice/UserServiceApplication.java`: Spring Boot entry point.
- `user/controller/AuthController.java`: registration, login, and auth test endpoints.
- `user/controller/UserController.java`: profile and user endpoints.
- `user/service/AuthService.java`: checks uniqueness, hashes passwords, creates profiles, and issues JWTs.
- `user/service/UserService.java`: reads and updates profiles and searches users.
- `user/security/JwtService.java`: signs tokens containing user ID, email, and account type.
- `user/security/JwtAuthenticationFilter.java`: validates tokens and creates the Spring Security authentication.
- `user/security/SecurityConfig.java`: permits public auth/static paths and protects other endpoints.
- `user/security/CorsConfig.java`: service-level CORS configuration.
- `user/entity/User.java`, `Profile.java`: JPA models.
- `user/repository/UserRepository.java`, `ProfileRepository.java`: Spring Data repositories.
- `src/main/resources/db/migration/V1__create_users_and_profiles.sql`: schema.

**APIs:**

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/test`
- `GET /api/user/me`
- `PUT /api/user/profile`
- `GET /api/user/creator-test`
- `GET /api/user/{id}`
- `GET /api/user/search`

**Database and communication:** User Service owns `revconnect_user_db`. Interaction Service calls `GET /api/user/{id}` through `UserServiceClient` to obtain the actor username for notifications. Interaction does not query the user database.

### Post Service

**Purpose:** Owns posts and uploaded images.

**Important files:** `PostServiceApplication`, `PostController`, `PostService`, `Post`, `PostRepository`, `PostRequest`, `PostResponse`, `PagedPostResponse`, and `V1__create_posts_table.sql`.

**APIs:**

- `POST /api/posts` with multipart photo and optional caption.
- `GET /api/posts/my`
- `GET /api/posts/published`
- `GET /api/posts/{id}`
- `PUT /api/posts/{id}`
- `DELETE /api/posts/{id}`

The service validates image MIME types, limits captions to 2200 characters, writes files under the configured upload directory, and checks post ownership for updates/deletes. It owns `revconnect_post_db`.

Interaction calls `GET /api/posts/{id}` through `PostServiceClient` to verify that a post exists and find its owner. It does not read the Post Service database. The Post Service controller relies on the Gateway-provided `X-User-Id` for create/update/delete and “my posts”.

### Feed Service

**Purpose:** Provides a feed based on published posts.

**Important files:** `FeedServiceApplication`, `FeedController`, `FeedService`, `FeedItem`, `FeedRepository`, `PostServiceClient`, feed DTOs, and `V1__create_feed_items_table.sql`.

`FeedService` calls Post Service with OpenFeign, caches returned post data in its own database, and returns a paged feed. It owns `revconnect_feed_db`. The `feed-service.properties` file enables the OpenFeign circuit-breaker integration.

**API:** `GET /api/feed` with `type`, `page`, and `size` query parameters. The current Java controller/service implementation should be treated as the source of truth; any additional frontend assumptions are not automatically implemented.

### Connection Service

**Purpose:** Owns connection requests, accepted connections, and follows.

**Important files:** `ConnectionServiceApplication`, `ConnectionController`, `ConnectionService`, entities `Connection`, `ConnectionRequest`, `Follow`, enum `ConnectionRequestStatus`, three repositories, DTOs, JWT filter, security configuration, and `V1__create_connections_and_follows.sql`.

**APIs:**

- `POST /api/connections/requests/{targetUserId}`
- `GET /api/connections/requests/received`
- `GET /api/connections/requests/sent`
- `PUT /api/connections/requests/{requestId}/accept`
- `PUT /api/connections/requests/{requestId}/reject`
- `DELETE /api/connections/requests/{requestId}`
- `GET /api/connections`
- `DELETE /api/connections/{otherUserId}`
- `POST /api/connections/follow/{targetUserId}`
- `DELETE /api/connections/follow/{targetUserId}`
- `GET /api/connections/followers`
- `GET /api/connections/following`
- `GET /api/connections/status/{targetUserId}`
- `GET /api/connections/stats`

It validates JWTs in the service itself and uses user IDs rather than direct access to User Service tables. It owns `revconnect_connection_db`.

### Interaction Service

**Purpose:** Owns post engagement data: likes, comments, shares, and reposts. It is the most important service in this guide and is covered in detail in Sections 3–8.

**Port:** 8085.

**Database:** `revconnect_interaction_db` according to Config Server. It contains `post_likes`, `post_comments`, `post_shares`, and `post_reposts`.

**External communication:** OpenFeign calls Post Service, User Service, and Notification Service. Eureka resolves those client names.

### Notification Service

**Purpose:** Stores and serves notifications.

**Important files:** `NotificationServiceApplication`, `NotificationController`, `NotificationService`, `Notification`, `NotificationRepository`, notification DTOs, and `V1__create_notifications_table.sql`.

**APIs:**

- `POST /api/notifications`: creates a notification. Interaction calls this endpoint.
- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `PUT /api/notifications/{id}/read`
- `PUT /api/notifications/read-all`
- `DELETE /api/notifications/internal?type=...&referenceId=...`

The notification database is `revconnect_notification_db`. The current Notification Service has no visible Spring Security configuration, so its internal create/delete endpoints are not protected by a service-local security filter in this repository. That is an implementation fact, not a recommended production design.

### Product Service

**Purpose:** Owns products associated with businesses.

**APIs:**

- `POST /api/products`
- `GET /api/products/{id}`
- `GET /api/products/business/{businessId}`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`

Important files are `ProductServiceApplication`, `ProductController`, `ProductService`, `Product`, `ProductRepository`, product DTOs, and `V1__create_products.sql`. It owns `revconnect_product_db`.

### Analytics Service

**Purpose:** Records metric events and summarizes metrics by owner and owner type.

**APIs:**

- `POST /api/analytics/events`
- `GET /api/analytics/{ownerType}/{ownerId}`

Important files are `AnalyticsServiceApplication`, `AnalyticsController`, `AnalyticsService`, `AnalyticsEvent`, `AnalyticsEventRepository`, DTOs, and `V1__create_analytics_events.sql`. It owns `revconnect_analytics_db`.

### API Gateway

**Purpose:** Single public API entry point on port 9000.

Important files:

- `ApiGatewayApplication.java`: reactive Spring Boot entry point.
- `JwtGatewayFilter.java`: global JWT validation and `X-User-Id` propagation.
- `FallbackController.java`: returns JSON `503` responses for circuit-breaker fallback paths.
- `src/main/resources/application.properties`: imports Config Server and selects reactive web mode.
- `configrepo/api-gateway.properties`: routes, CORS, circuit breakers, and resilience settings.

Redis and `RequestRateLimiter` were removed from the current project. There is no Redis service, Redis dependency, Redis property, Redis environment variable, or rate-limiter route configuration remaining. Circuit breakers remain.

### Config Server

`ConfigServerApplication` has `@EnableConfigServer`. It serves property files from the native repository location configured by `CONFIG_REPO_PATH`, normally `/workspace/configrepo` in containers. It runs on port 8888 and registers with Eureka.

### Eureka Server

`ServiceDiscoveryApplication` has `@EnableEurekaServer`. It runs on port 8761. The server itself has `register-with-eureka=false` and `fetch-registry=false`; application services register as clients using the shared `EUREKA_URL` setting.

## 3. Interaction Service — detailed file guide

### `interaction-service/pom.xml`

This module inherits Java 17, Spring Boot 3.4.5, and Spring Cloud 2024.0.1 management from the root `pom.xml`.

Dependencies and why they exist:

| Dependency | Actual use |
|---|---|
| `spring-boot-starter-web` | MVC REST controllers and servlet filters |
| `spring-boot-starter-data-jpa` | JPA entities and repositories |
| `spring-boot-starter-validation` | `@Valid`, `@NotBlank`, and `@Size` validation |
| `spring-boot-starter-security` | Authentication and authorization filter chain |
| `flyway-core`, `flyway-mysql` | Versioned MySQL schema migrations |
| `mysql-connector-j` | Runtime MySQL driver |
| `spring-cloud-starter-config` | Config Server import |
| `spring-cloud-starter-netflix-eureka-client` | Service registration and discovery |
| `spring-cloud-starter-openfeign` | HTTP clients for Post, User, and Notification services |
| `spring-cloud-starter-circuitbreaker-resilience4j` | Feign/circuit-breaker integration support |
| `spring-boot-starter-actuator` | Health, metrics, and actuator endpoints |
| `jjwt-api`, `jjwt-impl`, `jjwt-jackson` | JWT parsing and signature verification |
| `spring-boot-starter-test` | Test and Mockito support |

The module has the Spring Boot Maven plugin. It does not define a custom Maven test plugin or custom build profile.

### `InteractionServiceApplication.java`

```java
@SpringBootApplication
@EnableFeignClients(basePackages = "com.revconnect.interaction.client")
```

`@SpringBootApplication` enables component scanning and Boot auto-configuration. `@EnableFeignClients` tells Spring to generate implementations for the three interfaces in `com.revconnect.interaction.client`.

**Example:** when `InteractionService` calls `postServiceClient.getPost(100L)`, the generated Feign proxy performs the HTTP call to the discovered Post Service instance.

### `InteractionController.java`

The controller is mapped at `/api` and injects `InteractionService`. Each authenticated method gets the user ID with:

```java
private Long userId(Authentication a) {
    return (Long) a.getPrincipal();
}
```

That principal is installed by `JwtAuthenticationFilter`.

| HTTP operation | Method | Delegation |
|---|---|---|
| Like | `POST /api/posts/{postId}/likes` | `service.likePost(postId, userId)` |
| Unlike | `DELETE /api/posts/{postId}/likes` | `service.unlikePost(postId, userId)` |
| Add comment | `POST /api/posts/{postId}/comments` | Validates `CommentRequest`, then `addComment` |
| List comments | `GET /api/posts/{postId}/comments` | `getComments` |
| Edit comment | `PUT /api/comments/{commentId}` | Validates request, then `updateComment` |
| Delete comment | `DELETE /api/comments/{commentId}` | `deleteComment` |
| Share | `POST /api/posts/{postId}/shares` | `sharePost` |
| Repost | `POST /api/posts/{postId}/reposts` | `repostPost` |
| Unrepost | `DELETE /api/posts/{postId}/reposts` | `unrepostPost` |
| Engagement | `GET /api/posts/{postId}/engagement` | `getEngagement` |

Successful writes generally return `204 No Content`, except comment creation/update, which returns a `CommentResponse`. Engagement and comment listing return JSON.

### `InteractionService.java`

This is the business layer. It injects four repositories and three Feign clients. Methods are transactional so database changes are committed as units.

#### `likePost(Long postId, Long userId)`

1. Calls Post Service to load the post and owner.
2. Checks `likes.existsByPostIdAndUserId`.
3. If absent, creates `PostLike`, saves it, and calls `notifyPostOwner` with type `LIKE`.
4. If already present, it does nothing, so repeated likes do not create duplicate rows or duplicate notifications at the service level.

The database unique constraint is the second line of duplicate protection.

#### `unlikePost(Long postId, Long userId)`

Calls `deleteByPostIdAndUserId`. It does not load the post, validate ownership, or generate a notification. Deleting a non-existent like is effectively idempotent.

#### `addComment(Long postId, Long userId, CommentRequest request)`

1. Loads the post through Post Service.
2. Creates a `Comment` with post ID, actor ID, and normalized content.
3. `normalize` trims whitespace, rejects blank content, and enforces a maximum of 2000 characters.
4. Saves the comment.
5. Notifies the post owner with type `COMMENT`.
6. Converts the saved entity to `CommentResponse`.

#### `getComments(Long postId)`

Loads comments ordered by `createdAt` ascending, maps them to DTOs, and returns them. The current implementation passes `null` as the username in `new CommentResponse(comment, null)`; it does not call User Service for each comment.

#### `updateComment(Long commentId, Long userId, CommentRequest request)`

Loads the comment or throws `404`, verifies that its `userId` equals the authenticated user, normalizes the new content, saves it, and returns a response. A non-owner receives `403`. It does not notify the post owner.

#### `deleteComment(Long commentId, Long userId)`

Loads the comment or throws `404`, verifies ownership, and deletes it. A non-owner receives `403`. It does not notify the post owner.

#### `sharePost(Long postId, Long userId)`

Loads the post, creates and saves a `Share`, and sends a `SHARE` notification to the owner unless the actor is the owner. There is no unique constraint or duplicate pre-check for shares, so repeated shares can create multiple rows.

#### `repostPost(Long postId, Long userId)`

Loads the post, checks `existsByPostIdAndUserId`, saves only if absent, and sends a `REPOST` notification for a new repost. The database also has a unique `(post_id, user_id)` constraint.

#### `unrepostPost(Long postId, Long userId)`

Deletes the current user's repost for the post. It does not notify anyone.

#### `getEngagement(Long postId, Long userId)`

Builds `EngagementResponse` from four counts plus two current-user flags:

- Like count.
- Comment count.
- Share count.
- Repost count.
- `likedByCurrentUser`.
- `repostedByCurrentUser`.

This is why the endpoint receives an authenticated user even though it returns public aggregate counts: the response includes user-specific state.

#### Private helper methods

- `normalize`: trims and validates comment content.
- `requireComment`: maps missing comments to `404`.
- `verifyOwner`: maps unauthorized comment changes to `403`.
- `toResponse`: maps an entity to a DTO. It currently supplies no username.
- `notifyPostOwner`: skips notification if there is no owner or the actor is the owner; otherwise calls User Service to obtain the actor username and Notification Service to persist the notification.

### Interaction DTOs

#### `CommentRequest.java`

Input DTO containing one field, `content`. `@NotBlank` rejects blank values and `@Size(max = 2000)` applies request validation before the service runs. The service repeats the important checks after trimming.

#### `CommentResponse.java`

Output DTO containing comment ID, post ID, user ID, username, content, creation time, and update time. It is constructed from a `Comment` plus a username argument. Current code passes `null` for username.

#### `EngagementResponse.java`

Output DTO containing counts and the two current-user status flags described above. It has getters used by Jackson to serialize JSON.

### Interaction entities

#### `PostLike.java`

Maps to `post_likes`. It stores an auto-generated ID, post ID, user ID, and creation time. The entity and SQL both declare uniqueness for `(post_id, user_id)`. `@PrePersist` sets the timestamp if JPA has not already done so.

#### `Comment.java`

Maps to `post_comments`. It stores post/user IDs, text content, and creation/update timestamps. `@PrePersist` sets both timestamps; `@PreUpdate` refreshes `updatedAt`.

#### `Share.java`

Maps to `post_shares`. It stores post ID, user ID, and creation time. It has no duplicate-prevention constraint.

#### `Repost.java`

Maps to `post_reposts`. It is structurally like `PostLike` and has a unique `(post_id, user_id)` constraint. `@PrePersist` sets `createdAt`.

None of these entities declares a JPA relationship to a Post or User entity. The IDs are plain scalar values because those records belong to other services.

### Interaction repositories

All repositories extend `JpaRepository`, so they inherit save, find, delete, and basic CRUD behavior.

- `CommentRepository`: list by post in creation order and count comments by post.
- `PostLikeRepository`: check existence, delete by post/user, and count likes.
- `RepostRepository`: check existence, delete by post/user, and count reposts.
- `ShareRepository`: count shares by post.

Spring Data derives SQL queries from method names. There is no handwritten repository query in this service.

### `JwtAuthenticationFilter.java`

This servlet filter runs once per request. It reads a Bearer token, verifies the signature with JJWT, extracts `userId` and `accountType`, and installs a `UsernamePasswordAuthenticationToken` whose principal is the numeric user ID and whose authority is `ROLE_<accountType>`.

Invalid or missing tokens leave the security context empty; `SecurityConfig` then rejects the request because every request must be authenticated.

Important implementation caveat: the Interaction filter uses a hard-coded secret:

```java
revconnect-secret-key-must-be-at-least-32-characters-long
```

It does not read `${jwt.secret}` from Config Server. User Service and the Gateway use configurable `jwt.secret`, so the deployed JWT secret must match this hard-coded value for Interaction authentication to work. This is an actual code/configuration inconsistency, not an intended architecture feature.

### `SecurityConfig.java`

`@Configuration` registers the security setup. `@EnableMethodSecurity` enables method-level security support. The filter chain disables CSRF, requires authentication for every request, and inserts `JwtAuthenticationFilter` before Spring Security’s username/password filter.

There are no public Interaction endpoints in this configuration. The Gateway’s public-path rules do not override the downstream Interaction Service’s requirement for a valid JWT.

### `application.properties`

The module declares its application name as `interaction-service`, imports Config Server optionally using `CONFIG_SERVER_URL`, and sets `spring.cloud.config.fail-fast=false`. Port, database, Eureka, JWT, and Resilience4j settings arrive from the shared Config Server properties.

### `configrepo/interaction-service.properties`

Actual Interaction configuration:

- `server.port=8085`.
- MySQL URL defaults to database `revconnect_interaction_db`.
- Username/password come from `MYSQL_USER` and `MYSQL_PASSWORD`.
- `spring.cloud.openfeign.circuitbreaker.enabled=true`.
- Default Resilience4j circuit breaker window size is 10, failure threshold 50%, and open-state wait duration 10 seconds.

### `create-interaction-database.sql`

This helper creates a database named `interaction_db`. The Config Server URL uses `revconnect_interaction_db`. Unless the helper is run with a different database name or the environment overrides the URL, this is a mismatch. The service URL also contains `createDatabaseIfNotExist=true`, so MySQL may create `revconnect_interaction_db` automatically.

### `V1__create_interaction_tables.sql`

Flyway migration V1 creates:

- `post_likes`: unique post/user pair plus indexes by post and user.
- `post_comments`: content and timestamps plus indexes by post/created time and user.
- `post_shares`: post/user/timestamp plus indexes by post and user.
- `post_reposts`: unique post/user pair plus indexes by post and user.

There are no foreign-key constraints to Post Service or User Service tables because those tables are in different service-owned databases. The IDs are application-level references.

### `InteractionServiceTest.java`

This is a Mockito unit test, not an end-to-end test. It mocks all repositories and Feign clients and verifies:

- A new like is saved and creates a `LIKE` notification.
- A comment is saved and creates a `COMMENT` notification.
- A share is saved and creates a `SHARE` notification.
- A new repost is saved and creates a `REPOST` notification.
- Interacting with one’s own post saves the interaction but does not call User or Notification Service.

It does not test controller mappings, JWT parsing, Feign networking, Flyway, duplicate database races, unlike/unrepost, edit/delete authorization, or real MySQL.

## 4. Interaction flows

The frontend stores the JWT through `auth.js` and sends it as `Authorization: Bearer ...` in API requests. The normal path is:

```text
Frontend -> Nginx/API Gateway -> Eureka-resolved Interaction Service
         -> InteractionController -> InteractionService
         -> Interaction repository / MySQL
         -> optional Post/User/Notification Feign calls
```

### Like

1. Frontend calls `POST /api/posts/{postId}/likes`.
2. Gateway route 0 matches the path and its JWT filter validates the token and adds `X-User-Id`.
3. Interaction Service validates the same JWT with its service-local filter and puts the numeric user ID in `Authentication.getPrincipal()`.
4. Controller calls `likePost`.
5. Service calls Post Service to confirm the post and obtain the owner.
6. Service checks for an existing like.
7. If absent, it inserts `post_likes`.
8. If the actor is not the owner, User Service supplies the actor username and Notification Service receives a `LIKE` notification request.
9. Response is `204 No Content`.

### Unlike

`DELETE /api/posts/{postId}/likes` reaches `unlikePost`, which deletes the row matching post/user. It does not call Post, User, or Notification Service and returns `204`.

### Add comment

`POST /api/posts/{postId}/comments` includes `{ "content": "Nice post" }`. Controller validation and `normalize` enforce non-blank content and the 2000-character limit. The service loads the post, inserts `post_comments`, then optionally calls User Service and Notification Service. It returns a `CommentResponse`.

### List comments

`GET /api/posts/{postId}/comments` reads `post_comments` ordered oldest first. It uses only the Interaction database. No User Service call is made, so `username` is currently null in returned `CommentResponse` objects.

### Edit comment

`PUT /api/comments/{commentId}` validates the request, loads the comment, compares its stored `userId` to the authenticated principal, and updates content/timestamp only for the owner. It returns `403` for a different user, `404` for a missing comment, and no notification is generated.

### Delete comment

`DELETE /api/comments/{commentId}` loads the comment, checks ownership, deletes it, and returns `204`. No Feign call or notification is generated.

### Share

`POST /api/posts/{postId}/shares` loads the post, inserts a share row, and optionally creates a `SHARE` notification. Shares do not have a uniqueness constraint, so repeated requests are stored as repeated shares.

### Repost

`POST /api/posts/{postId}/reposts` loads the post and checks the repost repository. A new row is inserted only when the user has not reposted the post. The unique database constraint provides additional protection. A new repost can notify the post owner.

### Unrepost

`DELETE /api/posts/{postId}/reposts` deletes the current user’s repost. It does not notify the owner.

### Engagement and current-user status

`GET /api/posts/{postId}/engagement` calls four repository count methods and two existence methods, then returns aggregate counts plus `likedByCurrentUser` and `repostedByCurrentUser`. Because those flags depend on the authenticated user, the endpoint is protected.

### Notification generation

Only new like, comment, share, and repost operations can generate notifications. `notifyPostOwner`:

1. Reads the owner ID from the Post Service response.
2. Skips if owner is missing or owner equals actor.
3. Calls User Service for actor username.
4. Builds `CreateNotificationRequest` containing recipient, actor, username, message, type, and post reference ID.
5. Calls Notification Service `POST /api/notifications`.

There is no event broker in the current project. Notification creation is synchronous over Feign within the interaction operation.

## 5. Interaction and other services

### Post Service

Interaction calls `PostServiceClient.getPost(postId)`, which sends `GET /api/posts/{id}` to the service discovered as `post-service`. The response contains only `id` and `userId` in Interaction’s local DTO. Interaction uses it to verify existence and identify the owner.

### User Service

Interaction calls `UserServiceClient.getUser(userId)`, which sends `GET /api/user/{id}`. It uses the returned `username` only when constructing a notification. It does not copy the user database or create a local User entity.

### Notification Service

Interaction calls `NotificationServiceClient.createNotification`, a `POST /api/notifications` with the notification DTO. The response contains the notification ID, but Interaction does not use the response value.

### Eureka

The three Feign clients use service names: `post-service`, `user-service`, and `notification-service`. Eureka supplies the instances behind those names. No URL is hard-coded in the Feign annotations.

### API Gateway

The Gateway exposes Interaction paths publicly at port 9000 and routes them to `lb://interaction-service`. The Gateway’s JWT filter and Interaction’s own servlet JWT filter both participate in a normal browser request. The Gateway also provides CORS and circuit-breaker behavior.

### Config Server

Interaction imports Config Server at startup. The shared `configrepo/application.properties` supplies Eureka URL, common JWT property, JPA/Flyway defaults, and actuator settings; `interaction-service.properties` supplies port, database URL, and Feign/Resilience4j settings.

### Why Interaction does not access other databases

Directly reading Post or User tables would couple Interaction to another service’s schema and deployment. It would bypass that service’s API, ownership rules, migrations, and scaling boundary. The code instead uses service-to-service HTTP through Feign and Eureka.

## 6. Interaction database

### Tables and relationships

The database has four independent tables. `post_id` and `user_id` are logical references, not SQL foreign keys:

```text
post_likes   (post_id, user_id, created_at)
post_comments(post_id, user_id, content, created_at, updated_at)
post_shares  (post_id, user_id, created_at)
post_reposts (post_id, user_id, created_at)
```

The actual Post and User rows live in other service databases. The Interaction database stores only the IDs needed for engagement.

### Flyway

Flyway runs the versioned migration under `src/main/resources/db/migration`. The file name `V1__create_interaction_tables.sql` gives it version 1 and a description. Shared configuration enables Flyway and sets Hibernate to `validate`, so Hibernate checks mappings against the existing schema rather than creating tables.

### Constraints and authorization

- Likes: unique `(post_id, user_id)` prevents duplicate likes.
- Reposts: unique `(post_id, user_id)` prevents duplicate reposts.
- Comments: no duplicate constraint; multiple comments by one user are valid.
- Shares: no duplicate constraint; repeated shares are valid in the current implementation.
- All four tables require non-null post and user IDs.
- Comment modification checks the stored comment owner against the authenticated principal.
- Like/repost/unlike/unrepost are scoped by authenticated user ID.
- There are no SQL foreign keys to remote service databases.

## 7. Security

### JWT authentication versus authorization

Authentication asks “who is this user?” The JWT contains a user ID, email, and account type. Authorization asks “may this authenticated user perform this operation?” Interaction authorizes comment edits/deletes by comparing the authenticated principal with the comment’s owner.

### Gateway and downstream behavior

The Gateway’s `JwtGatewayFilter` validates non-public write requests and injects `X-User-Id`. It permits all GETs, which is why public read routes can pass the edge filter. Interaction does not rely only on the Gateway: its own `SecurityConfig` requires every request to be authenticated and its own filter reads the `Authorization` header.

`X-User-Id` is especially important for Post Service operations. Interaction itself uses the Spring Security principal, not `X-User-Id`, for its business decisions.

### Invalid token behavior

- Gateway: missing or invalid Bearer token on a protected method returns `401` immediately.
- Interaction: invalid token clears the security context; because every request requires authentication, Spring Security rejects the request rather than allowing the controller to run.

### Actual JWT caveat

Interaction’s filter has a hard-coded signing secret while User Service and Gateway use `jwt.secret` from configuration. This can cause valid login tokens to be rejected by Interaction unless the configured secret matches the hard-coded Interaction value. The source code should be corrected in a separate change if production-ready consistent JWT validation is required; this guide does not change source code.

## 8. Feign

OpenFeign turns a Java interface annotated with `@FeignClient` into an HTTP client. The caller uses a method; Spring Cloud sends an HTTP request to the service ID resolved by Eureka.

### `PostServiceClient`

```text
@FeignClient(name = "post-service")
GET /api/posts/{id}
Response: PostResponse { id, userId }
```

Used before like, comment, share, or repost creation to confirm the post and identify its owner.

### `UserServiceClient`

```text
@FeignClient(name = "user-service")
GET /api/user/{id}
Response: UserProfileResponse { userId, username }
```

Used only during notification construction to obtain the actor’s username.

### `NotificationServiceClient`

```text
@FeignClient(name = "notification-service")
POST /api/notifications
Body: CreateNotificationRequest
Response: NotificationResponse { id }
```

Used to synchronously persist interaction notifications.

### Why HTTP instead of database access?

HTTP preserves service ownership. Post Service remains responsible for post rules and schema; User Service remains responsible for user data; Notification Service remains responsible for notification persistence. Feign also works with Eureka and can participate in Spring Cloud circuit-breaker integration. The tradeoff is that network failures and partial success are possible.

## 9. API Gateway

### Why it exists

The Gateway gives the frontend one public endpoint, centralizes CORS and edge JWT handling, hides internal service hostnames, and provides common routing and failure behavior.

### Current routes

| Route | Predicate | Target | Fallback |
|---|---|---|---|
| interaction-service | engagement, likes, comments, shares, reposts, `/api/comments/**` | `lb://interaction-service` | `/fallback/interaction-service` |
| user-service | `/api/auth/**`, `/api/user/**` | `lb://user-service` | `/fallback/user-service` |
| post-service | `/api/posts/**` | `lb://post-service` | `/fallback/post-service` |
| feed-service | `/api/feed/**` | `lb://feed-service` | `/fallback/feed-service` |
| connection-service | `/api/connections/**` | `lb://connection-service` | `/fallback/connection-service` |
| notification-service | `/api/notifications/**` | `lb://notification-service` | `/fallback/notification-service` |
| product-service | `/api/products/**` | `lb://product-service` | `/fallback/product-service` |
| analytics-service | `/api/analytics/**` | `lb://analytics-service` | `/fallback/analytics-service` |

The Interaction route is listed before the broad `/api/posts/**` route so its specific engagement paths are selected first.

### CircuitBreaker and fallback

Each route has a Spring Cloud Gateway `CircuitBreaker` filter with a named circuit and a `forward:/fallback/...` URI. `FallbackController` maps `/fallback/{service}` and returns status `503` with a small JSON body containing the service name and retry message.

### CORS

The Gateway allows configured frontend origins, methods `GET,POST,PUT,PATCH,DELETE,OPTIONS`, all headers, exposes `Authorization`, and allows credentials. The current configuration uses `FRONTEND_ORIGIN` plus `http://localhost:5500`.

### Redis/rate-limiter removal

Redis is no longer part of the current repository. The Redis dependency, Compose service, Kubernetes resources, `REDIS_HOST`, Redis properties, `RequestRateLimiter` entries, and Redis documentation were removed. `CircuitBreaker`, Resilience4j, JWT, CORS, and routing remain.

## 10. Eureka

Eureka is the service registry. Application services use the shared `EUREKA_URL` and `eureka.instance.prefer-ip-address=true`. They register their application name, such as `interaction-service`.

When Feign or Gateway sees `post-service`, it asks the discovery/load-balancing layer for an available registered instance. `lb://post-service` therefore means “load-balanced destination resolved by service ID”, not a literal URI scheme that the browser calls directly.

If a service is unavailable, discovery/load-balancing or the downstream request fails. Gateway routes have circuit-breaker fallback behavior; Feign calls have OpenFeign circuit-breaker integration enabled for Interaction, but this repository does not define a custom Interaction fallback class.

## 11. Config Server

Config Server centralizes properties that otherwise would be duplicated in every service image. Each service’s local `application.properties` sets its application name and imports Config Server using:

```properties
spring.config.import=optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
```

The Dockerfile copies `configrepo` into the image. Config Server uses `CONFIG_REPO_PATH=file:/workspace/configrepo` in Compose/Kubernetes.

For Interaction, the effective configuration is the combination of:

- `configrepo/application.properties`: Eureka, JWT property, JPA/Flyway, actuator.
- `configrepo/interaction-service.properties`: port 8085, MySQL URL/credentials, OpenFeign circuit-breaker switch, Resilience4j settings.
- `interaction-service/src/main/resources/application.properties`: application name and Config Server import.

The import is optional and fail-fast is false, so a missing Config Server does not necessarily fail immediately at configuration import time; service startup can still fail later if required properties or dependencies are unavailable.

## 12. DevOps and deployment

### Root Maven project

The root `pom.xml` is a Maven aggregator with modules for Config Server, Eureka, Gateway, and all application services. It uses Java 17, Spring Boot 3.4.5, and Spring Cloud 2024.0.1. `mvn test` builds the reactor and runs available tests.

Only two unit-test classes are currently present: Connection Service and Interaction Service. Most modules report “No tests to run.”

### Dockerfile

The root `Dockerfile` is a two-stage image build:

1. Build stage uses Maven and Eclipse Temurin 17.
2. `ARG MODULE` selects a Maven module.
3. `mvn -pl ${MODULE} -am -DskipTests package` builds the module and required reactor dependencies.
4. Runtime stage uses Eclipse Temurin 17 JRE.
5. It copies the selected module JAR as `/app/app.jar` and copies `configrepo` to `/workspace/configrepo`.
6. It starts `java -jar /app/app.jar`.

The Dockerfile says `EXPOSE 8080` generically. Actual service ports come from Config Server properties; `EXPOSE` does not force the runtime port.

### Docker Compose

Compose runs:

- MySQL with persistent `mysql-data` volume.
- Eureka.
- Config Server.
- API Gateway.
- All eight domain/application services.
- Frontend Nginx container.

The gateway and services receive shared environment values such as `CONFIG_SERVER_URL`, `EUREKA_URL`, MySQL settings, `JWT_SECRET`, and `FRONTEND_ORIGIN`. Health-based `depends_on` entries coordinate startup for MySQL, Eureka, and Config Server. Redis is intentionally absent after the recent removal.

Compose exposes frontend `8080`, Gateway `9000`, Config Server `8888`, Eureka `8761`, and MySQL host port `3307`. Most application services are reachable on the Compose network by service name rather than published host ports.

### MySQL

MySQL is one container but contains separate service databases created by JDBC URLs using `createDatabaseIfNotExist=true`. Each service has its own schema ownership and migration directory. This is logical database separation within one MySQL server, not a separate MySQL container per service.

### Kubernetes

`k8s/platform.yaml` defines:

- Namespace `revconnect`.
- Secret `revconnect-secrets` with `MYSQL_PASSWORD` and `JWT_SECRET` placeholders.
- ConfigMap `revconnect-runtime` with Config Server, Eureka, and MySQL connection values.
- MySQL Deployment and Service.
- Eureka Deployment and Service.
- Config Server Deployment and Service.

`k8s/applications.yaml` defines Deployments and Services for the Gateway, frontend, and all application services. Gateway and frontend Services are `LoadBalancer`; most internal service objects use the default ClusterIP behavior. Application Deployments use the runtime ConfigMap and secret references where configured.

The Kubernetes YAML uses placeholder image names such as `YOUR_DOCKERHUB_USERNAME/revconnect-api-gateway:latest`. It does not contain an automated image substitution step.

### ConfigMap versus Secret

- ConfigMap: non-sensitive service URLs and MySQL host/user values.
- Secret: sensitive MySQL password and JWT secret.

The checked-in Kubernetes Secret contains placeholder values and must be changed before real deployment.

### Jenkins pipeline

`Jenkinsfile` has three stages:

1. **Build and test:** runs `mvn -B test`.
2. **Build images:** builds each backend image with `--build-arg MODULE=...`; builds the frontend separately. Each backend gets a build-number tag and `latest` tag.
3. **Push images:** logs into Docker Hub using Jenkins credential ID `dockerhub-credentials` and pushes both tags for every image.

The pipeline uses the `REGISTRY` credentials binding and `BUILD_NUMBER` for tags.

### What Jenkins does and does not do

Jenkins does:

- Run Maven tests.
- Build backend and frontend Docker images.
- Authenticate to Docker Hub.
- Push versioned and `latest` images.

Jenkins does not, according to the actual file:

- Run `docker compose up`.
- Apply Kubernetes manifests.
- Run `kubectl`.
- Create the Kubernetes namespace, ConfigMap, Secret, or database.
- Perform a production rollout or health verification after pushing images.

### Complete pipeline as implemented

```text
Developer commit
      |
      v
Jenkins: mvn -B test
      |
      v
Docker build for each backend module + frontend
      |
      v
Docker Hub login using Jenkins credential
      |
      v
Push BUILD_NUMBER and latest tags
      |
      v
Deployment is still a separate operator/platform step:
Docker Compose or kubectl apply using the published images
```

### Compose versus Kubernetes

Compose is a local/single-host orchestration file: it builds local images, creates one network, starts containers, and uses Compose health checks/dependencies. Kubernetes is a cluster declarative model: Deployments manage replicas, Services provide stable networking, ConfigMaps/Secrets inject configuration, and LoadBalancer Services expose selected entry points.

## 13. Port table

| Runtime object | Internal port | Exposed/published port | Role |
|---|---:|---:|---|
| frontend | 80 | 8080 | Nginx UI and `/api` reverse proxy |
| user-service | 8081 | internal | Users/auth/profile |
| post-service | 8082 | internal | Posts/images |
| feed-service | 8083 | internal | Feed |
| connection-service | 8084 | internal | Connections/follows |
| interaction-service | 8085 | internal | Likes/comments/shares/reposts |
| notification-service | 8086 | internal | Notifications |
| product-service | 8087 | internal | Products |
| analytics-service | 8088 | internal | Analytics |
| api-gateway | 9000 | 9000 | Public API |
| config-server | 8888 | 8888 | Central configuration |
| service-discovery | 8761 | 8761 | Eureka registry |
| mysql | 3306 | 3307 in Compose | Database server |

## 14. Presentation questions and answers

### Architecture and design

1. **Why use microservices?**  The project separates domain ownership, databases, deployment units, and scaling boundaries. It also introduces distributed-system complexity that Eureka, Config Server, Feign, and resilience tools help manage.
2. **What is the public entry point?**  API Gateway on port 9000. The frontend normally reaches it through Nginx.
3. **Why have an API Gateway?**  It centralizes routing, CORS, JWT edge validation, service-name abstraction, and circuit-breaker fallback behavior.
4. **Why does the Gateway use `lb://`?**  `lb://service-name` tells Spring Cloud to resolve and load-balance a Eureka service ID instead of using a fixed host.
5. **What happens when a service is down?**  Gateway circuit-breaker routes can forward to `FallbackController`, which returns a JSON `503` response.
6. **Why use Eureka?**  Services register under logical names and callers do not need fixed IP addresses. This supports multiple instances and dynamic location.
7. **Why use Config Server?**  Shared and environment-specific properties are centralized in `configrepo` rather than duplicated in every application image.
8. **Why separate databases?**  Each service owns its schema and can evolve it independently. Other services use APIs instead of reading that schema directly.
9. **Is there one database server or one server per service?**  The current Compose setup has one MySQL container, but separate logical databases per service.
10. **Why use Flyway?**  Flyway applies versioned SQL migrations consistently at startup and works with Hibernate validation.
11. **Why Docker?**  It packages each service with its runtime and makes the same image usable across local and cluster environments.
12. **Why Kubernetes?**  Kubernetes provides declarative Deployments, replicas, Services, ConfigMaps, Secrets, and load-balanced exposure for cluster operation.
13. **Why Jenkins?**  The pipeline automates tests, image builds, Docker Hub login, and image pushes.
14. **Does Jenkins deploy Kubernetes?**  Not in the current Jenkinsfile. It stops after pushing images.
15. **What was removed recently?**  Redis and all Redis-backed Gateway rate limiting. Circuit breakers were retained.

### Interaction Service

16. **What does Interaction Service own?**  Likes, comments, shares, reposts, aggregate engagement counts, and current-user like/repost status.
17. **Why does it call Post Service before an interaction?**  It verifies that the post exists and obtains the post owner for notification generation.
18. **Why does it call User Service?**  It obtains the actor’s username to place in a notification message.
19. **Why does it call Notification Service?**  It persists a notification for the post owner when another user interacts with the post.
20. **Why not access Post/User databases directly?**  Those databases belong to other services. Direct access would bypass their APIs and create schema coupling.
21. **How is a like created?**  The service checks `(postId,userId)`, saves `PostLike` if absent, and optionally creates a notification.
22. **How are duplicate likes prevented?**  There is both a service-level existence check and a database unique constraint on `(post_id,user_id)`.
23. **How is a repost different from a share?**  Reposts are unique per user/post and can be removed; shares have no uniqueness constraint or delete endpoint in Interaction.
24. **How are duplicate reposts prevented?**  The service checks existence and `post_reposts` has a unique `(post_id,user_id)` constraint.
25. **Can a user share repeatedly?**  Yes, the current code inserts each share and does not check for an existing one.
26. **How is comment authorization handled?**  The service compares the authenticated principal’s user ID with the comment’s stored `userId` before update/delete.
27. **What happens when a comment does not exist?**  `requireComment` throws a `ResponseStatusException` with `404 NOT_FOUND`.
28. **What happens when another user edits a comment?**  `verifyOwner` throws `403 FORBIDDEN`.
29. **What validates comment text?**  `CommentRequest` uses `@NotBlank` and `@Size(max=2000)`; the service also trims, rejects blank content, and checks 2000 characters.
30. **Does listing comments include usernames?**  The DTO has a username field, but current `getComments` passes `null`; username enrichment is not implemented there.
31. **How are counts calculated?**  Repository-derived count methods count rows by `postId` for likes, comments, shares, and reposts.
32. **Why does engagement need authentication?**  It returns `likedByCurrentUser` and `repostedByCurrentUser` in addition to public counts.
33. **Which operations generate notifications?**  New like, comment, share, and repost operations. Unlike, unrepost, edit, and delete do not.
34. **Are self-interactions notified?**  No. `notifyPostOwner` skips when owner ID equals actor ID.
35. **Are notifications asynchronous?**  No. The current code calls Notification Service synchronously through Feign.
36. **Does Interaction use an event broker?**  No broker or messaging implementation is present.
37. **What does `@Transactional` do here?**  It wraps repository changes and related service work in a transaction boundary for the Interaction database; remote HTTP calls still have distributed failure implications.
38. **What does `@Transactional(readOnly=true)` mean for counts/comments?**  It marks read operations as read-only from the local transaction perspective.
39. **Why are post/user IDs plain `Long` fields?**  Because Post and User entities are owned by other services and are not JPA relationships in this service.
40. **What is the role of each repository?**  Repositories translate business operations into local database operations for comments, likes, reposts, and shares.

### Security and correctness

41. **What does JWT provide?**  A signed token carries user identity and account type so services can authenticate requests without a shared server-side session.
42. **What is the difference between Gateway JWT and Interaction JWT?**  Gateway validates at the edge and adds `X-User-Id`; Interaction validates the Authorization header again and creates a Spring Security principal.
43. **What happens with an invalid token?**  Gateway returns `401` for protected methods; Interaction’s empty security context is rejected because all requests require authentication.
44. **What is the Interaction JWT caveat?**  Its filter has a hard-coded secret instead of reading `jwt.secret`, which can disagree with User Service/Gateway configuration.
45. **Why is `X-User-Id` important?**  Post and Notification controllers use it for some user-scoped operations. Interaction itself uses the authenticated principal.
46. **Does the Gateway make all GET requests public?**  Its custom filter bypasses JWT checks for GET, but downstream service security may still require authentication. Interaction requires authentication for every request.
47. **Are there foreign keys from interactions to posts/users?**  No. Cross-service foreign keys are not present; existence is checked through service APIs in selected write flows.
48. **What does the Interaction unit test cover?**  Notification and persistence behavior for like/comment/share/repost plus no self-notification. It does not cover HTTP, JWT, database migrations, or real Feign calls.
49. **What does CircuitBreaker protect?**  Gateway routes and configured Feign integration can stop repeatedly failing calls from cascading indefinitely. The repository has no custom Interaction fallback implementation.
50. **What would you improve first before production?**  Align Interaction’s JWT secret with Config Server, resolve the interaction database naming mismatch, add controller/security/integration tests, add timeout/error handling around synchronous Feign notifications, and decide how shares should be deduplicated.

## 15. Presentation cheat sheet

### Two-minute project explanation

RevConnect is a Spring Boot/Spring Cloud social platform split into services for users, posts, feeds, connections, interactions, notifications, products, and analytics. A browser talks to the frontend and API Gateway. The Gateway uses Eureka service IDs such as `lb://interaction-service`, validates JWTs for protected writes, handles CORS, and provides circuit-breaker fallbacks. Config Server supplies shared properties from `configrepo`. Each domain service owns its own MySQL database and uses Flyway migrations. Interaction Service owns likes, comments, shares, and reposts; it stores those locally and uses Feign through Eureka to ask Post Service for post ownership, User Service for actor usernames, and Notification Service to create notifications. Docker Compose runs the stack locally, Kubernetes describes cluster deployments, and Jenkins tests/builds/pushes images to Docker Hub.

### Five-minute project explanation

The system has three infrastructure services and eight domain services behind one Gateway. Eureka is the registry: services register names and callers resolve `lb://...` names. Config Server serves `configrepo`, where ports, database URLs, Eureka settings, CORS, Gateway routes, and Resilience4j settings are defined. The Gateway on port 9000 routes frontend `/api` calls to internal services, performs edge JWT checks and `X-User-Id` propagation, and forwards failed routes to `FallbackController` through CircuitBreaker. Redis rate limiting is not part of the current codebase.

User Service owns registration/login and creates signed JWTs. Post Service owns posts and image files. Feed Service calls Post Service and caches feed items. Connection Service owns connections and follows. Interaction Service owns engagement tables and is protected by service-local JWT security. It does not directly access Post or User databases; instead, Feign clients call those services through Eureka. When a user likes, comments on, shares, or reposts another user’s post, Interaction verifies the post, obtains the actor username, and synchronously asks Notification Service to save a notification. Product and Analytics cover the remaining business areas. Flyway creates each service schema, and Hibernate is configured to validate it. Docker builds each module into a Java 17 image. Compose runs local containers and MySQL. Kubernetes adds Deployments, Services, namespace, ConfigMap, and Secrets. Jenkins runs tests, builds images, and pushes them to Docker Hub, but the current pipeline does not apply Kubernetes manifests.

### Two-minute Interaction Service explanation

Interaction Service runs on port 8085 and owns four tables: likes, comments, shares, and reposts. `InteractionController` exposes endpoints for all engagement operations. `InteractionService` contains the business logic and is connected to four Spring Data repositories. Likes and reposts use repository existence checks and database unique constraints to prevent duplicates; shares are currently repeatable. Comments are validated and only their owner may edit or delete them. Post Service is called through Feign to confirm the post and find its owner. User Service supplies the actor username, and Notification Service receives a notification request for new interactions, except self-interactions. JWT security creates a numeric user principal used by the controller. Flyway creates the schema, and the unit test verifies persistence/notification behavior for core new interactions.

### Architecture diagram to remember

```text
Frontend/Nginx :8080
       |
       v
Gateway :9000 -- JWT/CORS/CircuitBreaker --> lb://interaction-service :8085
       |                                      |
       |                                      +--> Feign -> post-service :8082
       |                                      +--> Feign -> user-service :8081
       |                                      +--> Feign -> notification-service :8086
       |
       +--> lb://user-service
       +--> lb://post-service
       +--> lb://feed-service
       +--> lb://connection-service
       +--> lb://notification-service
       +--> lb://product-service
       +--> lb://analytics-service

All lb:// names are resolved through Eureka :8761.
All services import configuration from Config Server :8888.
Each service owns its own MySQL database/schema.
```

### Important files to remember

- Root `pom.xml`: Maven modules, Java/Spring versions.
- `api-gateway/src/main/java/.../JwtGatewayFilter.java`: edge JWT and `X-User-Id`.
- `api-gateway/src/main/java/.../FallbackController.java`: Gateway 503 fallback.
- `configrepo/api-gateway.properties`: all Gateway routes/CORS/circuit breakers.
- `configrepo/interaction-service.properties`: Interaction port/database/Feign resilience.
- `interaction-service/.../InteractionController.java`: public Interaction endpoints.
- `interaction-service/.../InteractionService.java`: core business logic.
- `interaction-service/.../V1__create_interaction_tables.sql`: schema.
- `interaction-service/.../JwtAuthenticationFilter.java`: downstream JWT parsing.
- `interaction-service/.../InteractionServiceTest.java`: current unit coverage.
- `Dockerfile`, `docker-compose.yml`, `k8s/platform.yaml`, `k8s/applications.yaml`, `Jenkinsfile`: delivery and operations.

### Important Interaction APIs

```text
POST   /api/posts/{postId}/likes
DELETE /api/posts/{postId}/likes
POST   /api/posts/{postId}/comments
GET    /api/posts/{postId}/comments
PUT    /api/comments/{commentId}
DELETE /api/comments/{commentId}
POST   /api/posts/{postId}/shares
POST   /api/posts/{postId}/reposts
DELETE /api/posts/{postId}/reposts
GET    /api/posts/{postId}/engagement
```

### Key technical terms

| Term | One-line definition |
|---|---|
| Microservice | Independently deployable service focused on a bounded business domain. |
| API Gateway | Front door that routes and applies cross-cutting policies. |
| Eureka | Service registry used to discover service instances. |
| `lb://` | Load-balanced logical service URI resolved through discovery. |
| Config Server | Central property provider backed here by `configrepo`. |
| Feign | Declarative Java HTTP client generated from an annotated interface. |
| DTO | Data Transfer Object used to shape request/response payloads. |
| Entity | JPA class mapped to a database table. |
| Repository | Spring Data abstraction for database access. |
| Flyway | Versioned SQL migration tool. |
| JWT | Signed token carrying claims such as user ID and account type. |
| Authentication | Establishing the identity represented by a request. |
| Authorization | Deciding whether that identity may perform an operation. |
| CircuitBreaker | Resilience mechanism that limits repeated calls to failing destinations. |
| CORS | Browser policy/configuration controlling cross-origin requests. |
| ConfigMap | Kubernetes non-secret configuration object. |
| Secret | Kubernetes object intended for sensitive values. |
| Deployment | Kubernetes controller managing replicated Pods. |
| Service | Kubernetes stable network endpoint for Pods. |
| CI/CD | Automated build/test/delivery workflow; this Jenkinsfile implements build/test/push, not deployment. |

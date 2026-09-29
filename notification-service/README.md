# RevConnect Notification Service

Standalone Notification microservice extracted from the original RevConnect monolith.

## Port

`8086`

## Database

`revconnect_notification_db`

The service owns only the `notifications` table. It does **not** access `user_db`, `post_db`, `connection_db`, or `interaction_db`.

## Run

Set your MySQL password in:

`../src/main/resources/application.properties`

Then:

```bash
mvn clean spring-boot:run
```

Flyway creates the notification table automatically.

## APIs

### Create notification (used by other services / Feign)

```http
POST /api/notifications
Content-Type: application/json
```

```json
{
  "recipientId": 25,
  "actorId": 10,
  "actorUsername": "siddhi",
  "message": "siddhi liked your post",
  "type": "LIKE",
  "referenceId": 100
}
```

### Get notifications

```http
GET /api/notifications?page=0&size=20
X-User-Id: 25
```

### Unread count

```http
GET /api/notifications/unread-count
X-User-Id: 25
```

### Mark one as read

```http
PUT /api/notifications/1/read
X-User-Id: 25
```

### Mark all as read

```http
PUT /api/notifications/read-all
X-User-Id: 25
```

## Microservice rule

Other services must not access `revconnect_notification_db` directly.

For example:

```text
Interaction Service
       |
       | Feign
       v
Notification Service
       |
       v
revconnect_notification_db
```

The Notification Service owns the notification entity, repository, database, and notification APIs.

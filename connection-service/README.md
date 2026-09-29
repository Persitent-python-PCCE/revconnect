# Connection Service

Connection microservice for RevConnect.

## Service
- **Name:** Connection Service
- **Port:** 8083

## Database
- **Name:** revconnect_connection_db

## Responsibilities
- Connection requests
- Connections
- Follow/unfollow
- Followers/following
- Relationship status
- Statistics

## Current architecture

```text
Client / Postman
       |
       v
Connection Service :8083
       |
      v
revconnect_connection_db
```

## Future architecture

```text
Connection Service
      |
      +---- Feign ----> User Service
      |
      +---- Feign ----> Notification Service
```

These integrations are **NOT implemented in the current step**.

Feign integration is not implemented in the current phase.
Notification integration is not implemented in the current phase.

## Known temporary limitation

User IDs are stored as external references and user/profile lookup will later be handled through User Service. User existence/profile validation will be delegated to User Service once Feign integration is implemented. For this current step, the Connection Service simply accepts valid numeric user IDs as relationship participants.

## Running the Service

The service can be run independently:

```bash
cd connection-service
./mvnw spring-boot:run
```

On Windows:
```cmd
cd connection-service
mvnw.cmd spring-boot:run
```

### Postman Examples

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

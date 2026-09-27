# Connection Service

## Overview
The Connection module owns all business logic for connections and follows between users in the RevConnect application. It allows users to build their network by sending connection requests and following other users.

## Data Model

```text
User
  |
  +--> ConnectionRequest
  |
  +--> Connection
  |
  +--> Follow
```

- **ConnectionRequest**: Represents a pending request from one user to another.
- **Connection**: Represents an active, accepted connection between two users. User IDs are stored in canonical order to prevent duplicate network edges.
- **Follow**: Represents a directional following relationship (e.g., A follows B).

## API Endpoints

| Method | Endpoint | Purpose | Auth | Success | Error |
|--------|----------|---------|------|---------|-------|
| POST   | `/api/connections/requests/{targetUserId}` | Send connection request | JWT | 201 | 400, 404, 409 |
| GET    | `/api/connections/requests/received` | View received pending requests | JWT | 200 | |
| GET    | `/api/connections/requests/sent` | View sent pending requests | JWT | 200 | |
| PUT    | `/api/connections/requests/{requestId}/accept`| Accept request | JWT | 200 | 403, 404, 409 |
| PUT    | `/api/connections/requests/{requestId}/reject`| Reject request | JWT | 200 | 403, 404, 409 |
| DELETE | `/api/connections/requests/{requestId}` | Cancel request | JWT | 204 | 403, 404, 409 |
| GET    | `/api/connections` | View active network connections | JWT | 200 | |
| DELETE | `/api/connections/{otherUserId}` | Remove an active connection | JWT | 204 | |
| POST   | `/api/connections/follow/{targetUserId}` | Follow user | JWT | 201 | 400, 404 |
| DELETE | `/api/connections/follow/{targetUserId}` | Unfollow user | JWT | 204 | |
| GET    | `/api/connections/followers` | View followers | JWT | 200 | |
| GET    | `/api/connections/following` | View following | JWT | 200 | |
| GET    | `/api/connections/status/{targetUserId}` | View relationship status | JWT | 200 | 404 |
| GET    | `/api/connections/stats` | View connection statistics | JWT | 200 | |

## Connection Lifecycle

```text
PENDING
   |
   +--> ACCEPTED --> Active Connection
   |
   +--> REJECTED
   |
   +--> CANCELLED
```

An accepted connection request creates an active `Connection` record, while the request itself is preserved as historical context.

## Follow Lifecycle

```text
FOLLOW
   |
   --> Following relationship
   |
UNFOLLOW
   |
   --> relationship removed
```

## Security
The current authenticated user is determined from the JWT Authentication principal, avoiding the need to trust `userId` fields in request bodies. Authorization rules are enforced strictly at the service layer.

## Future Microservice Readiness
The module references user identities purely by numeric User IDs rather than coupling tightly via ORM `@ManyToOne` relationships. This clean boundary will facilitate extracting the connection domain into an independent microservice in the future.

# RevConnect Feed Service

Independent Feed microservice extracted from the monolith.

- Port: `8083`
- Database: `revconnect_feed_db`
- Flyway migration: `V1__create_feed_items_table.sql`
- No Post entity/repository and no User entity/repository are imported.
- The feed obtains published post data through the Post Service HTTP API and caches it in its own `feed_items` table.

## Endpoint
- `GET /api/feed?page=0&size=10`

Set `spring.datasource.password` in `../src/main/resources/application.properties` before running.
Ensure Post Service is running on `http://localhost:8082`.

The HTTP client is intentionally isolated in `client/PostServiceClient`; it can be replaced by OpenFeign in the team's later inter-service communication phase.

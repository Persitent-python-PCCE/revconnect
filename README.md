# RevConnect cloud-native microservices

This modernization keeps the existing domain behavior while separating data ownership into eight Spring Boot 3 / Java 17 services: User, Post, Feed, Connection, Interaction, Notification, Product, and Analytics. Every domain service has an independent schema, Flyway migration, repository, and lifecycle.

## Platform layout

- `configrepo/` is the central, environment-variable-driven configuration source.
- `config-server/` serves that configuration on port 8888.
- `service-discovery/` is Eureka on port 8761.
- `api-gateway/` is the only public API (port 9000). It routes by service ID, validates JWTs for non-public writes, injects `X-User-Id`, and returns Resilience4j circuit-breaker fallbacks.
- Feed and Interaction use OpenFeign service IDs, so calls are discovered through Eureka rather than hard-coded localhost URLs.
- `frontend/` is served by Nginx on port 8080 and proxies `/api` through the gateway.

## Run locally

1. Install Docker Desktop, then from this folder run `docker compose up --build`.
2. Open `http://localhost:8080`. Register and log in; browser requests are sent to the gateway rather than a domain service.
3. Confirm discovery at `http://localhost:8761`, central configuration at `http://localhost:8888/user-service/default`, and gateway health at `http://localhost:9000/actuator/health`.

Set `MYSQL_PASSWORD` and a unique `JWT_SECRET` in your shell or a non-committed `.env` file before using this beyond local development. The Compose default is intentionally only for local demos.

## Verification checklist

1. Run `mvn test` with JDK 17. It compiles the parent reactor and service tests.
2. Start the Compose stack and wait for all eight names to appear as `UP` in Eureka.
3. Register with `POST /api/auth/register` and log in at `POST /api/auth/login` through port 9000. Use the returned token as `Authorization: Bearer <token>` for protected calls.
4. Use the frontend to create a post, like/comment/share/repost it, follow/connect a user, create a business product, and view notification and analytics screens. These calls exercise the respective gateway route and service database.
5. Stop one service, then call its gateway route. The gateway should return `503` JSON from `/fallback/{service}` rather than leaking a connection exception.

## Delivery

`Jenkinsfile` builds/tests all services and pushes one Docker image per service using the Jenkins credential named `dockerhub-credentials`. The GitHub Actions workflow runs the same Maven test stage. Kubernetes manifests are in `k8s/`; replace `YOUR_DOCKERHUB_USERNAME` before applying them.

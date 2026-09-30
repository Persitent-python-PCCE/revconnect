# RevConnect Services and Cloud Platform

Business/domain services included:
- user-service
- post-service
- connection-service
- interaction-service
- feed-service
- notification-service
- product-service
- analytics-service

Product Service owns product records. Analytics Service owns analytics events and summaries. 

Platform services added in the same structure as the supplied e-commerce reference:

- `configrepo`: externalized properties for every service, with no committed database password.
- `config-server`: Spring Cloud Config Server (native repository for local and container runs).
- `service-discovery`: Eureka Server.
- `api-gateway`: service-id routing, JWT edge authentication and Resilience4j fallback routes.

Ports are unique: user 8081, post 8082, feed 8083, connection 8084, interaction 8085, notification 8086, product 8087, analytics 8088, gateway 9000, Config Server 8888, and Eureka 8761.

# RevConnect – Fixed Functional Baseline

This ZIP keeps the existing Spring Boot monolith and completes the explicitly listed functional scope before microservice extraction.

## Added/finished
- User profile: location, website, privacy handling
- Post: hashtags, visibility, promotional flag, CTA, scheduling, pinning, product tagging
- Feed: latest, trending, following, connections, personalized mode, hashtag filtering
- Search: people/creators/businesses and posts/hashtags
- Notifications: preferences, unread/read, history, SSE in-app stream
- Creator/Business analytics endpoint
- Business products
- Functional dashboard frontend for feed, create post, search, notifications, profile, stats, products

## Important architecture rule
This is the **functional monolith baseline**. Do not copy UserRepository/User entity into future Post/Connection/Interaction microservices. After this baseline is verified, extract each bounded context into its own Spring Boot application and replace cross-module repository calls with OpenFeign.

## Run
1. Create MySQL database `revconnect_db`.
2. Set `DB_PASSWORD` or change `spring.datasource.password`.
3. Run `mvn spring-boot:run`.
4. Open `http://localhost:8081/register.html`.
5. Register, login and use the dashboard.

Flyway creates/updates the schema through V1–V7. For an existing development DB created by an older build, take a backup first. If Flyway reports a schema/history conflict, use a fresh `revconnect_db` for this baseline.

## Suggested verification flow
Register as PERSONAL, CREATOR and BUSINESS users. Login. Edit profile. Create public/follower/private posts. Add hashtags and CTA. Create a business product and tag it in a promotional post. Test scheduling/pinning. Follow/connect with another user. Like/comment/share/repost. Verify notifications and preferences. Switch feed filters and search people/posts/hashtags. Creator/Business accounts can open Stats.

## Next phase
After this works, split into User, Post, Feed, Connection, Interaction and Notification services, each with its own DB/Flyway. Then add Feign, Eureka, API Gateway, Config Server and Resilience4j. Docker/Kubernetes/Jenkins come after the integrated microservices baseline works.

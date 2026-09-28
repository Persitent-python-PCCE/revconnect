# RevConnect Fixed Functional Scope

This baseline implements the explicitly named functional requirements from the project description with intentionally small/demo-friendly implementations.

## Implemented in this baseline
- User registration/login/JWT/RBAC
- Personal/Creator/Business profile types
- Profile: bio, picture, privacy, location, website
- Post CRUD
- Hashtags
- Visibility: public/followers/connections/private
- Promotional posts and CTA
- Scheduling with a simple Spring scheduler
- Pinning
- Business products and product tagging
- Feed: For You, Latest, Trending, Following, Connections, hashtag filtering
- Likes/unlikes, comments/edit/delete, shares, reposts
- Connections and follows
- People/creator/business search and post/hashtag search
- In-app notifications, unread/read/history, preferences
- SSE notification stream for real-time in-app delivery
- Creator/Business analytics endpoint with basic reach/engagement and audience-location breakdown
- Functional browser dashboard
- Standalone frontend copy under `/frontend`

## Intentionally not added
- Payments/checkout
- Chat/DM system
- Email/SMS/push notification providers
- AI recommendation engine
- Elasticsearch
- Separate analytics/product/search microservices
- Complex scheduling infrastructure

## Microservice migration note
This baseline remains a monolith so the team can verify functionality first. When extracting services:
- User owns users/profiles.
- Post owns posts/products.
- Feed aggregates.
- Connection owns requests/connections/follows.
- Interaction owns likes/comments/shares/reposts.
- Notification owns notifications/preferences.
- Replace direct repository/service dependencies between bounded contexts with OpenFeign calls.

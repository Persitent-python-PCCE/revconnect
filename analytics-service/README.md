# RevConnect Analytics Service

Owns lightweight creator/business analytics. It stores analytics events using IDs only and does not directly access User, Post, Interaction, or Product repositories.

Port: 8088
Database: `revconnect_analytics_db`

Record an event:
POST `/api/analytics/events`
```json
{"ownerId":1,"ownerType":"CREATOR","metricType":"LIKE","postId":10,"value":1}
```

Summary:
GET `/api/analytics/CREATOR/1`

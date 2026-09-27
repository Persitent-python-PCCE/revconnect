# RevConnect Frontend

This frontend keeps the existing RevConnect dashboard behavior and backend API contracts while applying one consistent visual theme across authentication, dashboard, profile, feed, notifications, connections, products, and analytics.

## Frontend structure

```text
frontend/
├── index.html
├── login.html
├── register.html
├── profile.html
├── css/
│   └── style.css
└── js/
    ├── config.js
    ├── auth.js
    ├── login.js
    ├── profile.js
    ├── api/
    │   └── connectionApi.js
    └── pages/
        └── home.js
```

## Architecture decision

The frontend is intentionally **not** split into one JavaScript module per backend microservice.

There is **no generic `api.js`** and no separate Feed/Post/Notification/etc. frontend module layer. `pages/home.js` remains the dashboard/page controller and contains the existing Feed, Post, Engagement, Notification, Connection, Search, Profile, Product, and Analytics functionality.

`ConnectionApi` remains available for connection-specific operations.

## Theme

The same RevConnect visual language is used across:

- Login and registration
- Main dashboard/sidebar/navigation
- Feed and post cards
- Explore/search
- Notifications
- Statistics/analytics
- Profile editing
- Business products
- Create-post modal
- Standalone profile page
- Responsive/mobile layouts

The theme uses the existing RevConnect orange/pink gradient, soft surfaces, rounded cards, subtle shadows, Plus Jakarta Sans typography, and responsive layouts.

## Compatibility rules

- Existing HTML IDs are preserved.
- Existing HTML class names are preserved.
- Existing backend REST endpoint paths are preserved.
- Existing dashboard functionality is retained.
- The duplicate `connectionApi(1).js` file has been removed.
- `ConnectionApi` uses `API_BASE_URL` for consistent backend configuration.
- No generic `api.js` was added.

## Run

When served by Spring Boot, `API_BASE_URL` remains empty.

For a standalone frontend:

```bash
python -m http.server 5500
```

The frontend config points API calls to `http://localhost:8081` by default when running on port 5500.

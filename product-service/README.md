# RevConnect Product Service

Owns business/creator product records and product showcasing. It stores only `businessId` as an ID and does not import User Service entities or repositories.

Port: 8087
Database: `revconnect_product_db`

Endpoints:
- POST `/api/products`
- GET `/api/products/{id}`
- GET `/api/products/business/{businessId}`
- PUT `/api/products/{id}`
- DELETE `/api/products/{id}`

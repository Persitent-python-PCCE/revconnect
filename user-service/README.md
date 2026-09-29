# RevConnect User Service

This folder is the independent User microservice.

## Structure

```text
user-service/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/revconnect/userservice/
│       │   ├── UserServiceApplication.java
│       │   └── user/
│       │       ├── controller/
│       │       ├── service/
│       │       ├── repository/
│       │       ├── entity/
│       │       ├── dto/
│       │       └── security/
│       └── resources/
│           ├── application.properties
│           └── db/migration/
│               └── V1__create_users_and_profiles.sql
└── README.md
```

## Database

User Service owns:

```text
revconnect_user_db
├── users
└── profiles
```

It does not access repositories/entities from Post, Connection, Interaction,
Feed, or Notification services.

If information owned by another microservice is required later, use an API/Feign
call rather than importing that service's repository or entity.

## Run

Make sure MySQL is running.

If your MySQL password is not `root`, set it before running:

Windows CMD:
```cmd
set DB_PASSWORD=your_password
```

PowerShell:
```powershell
$env:DB_PASSWORD="your_password"
```

Then:

```bash
mvn clean spring-boot:run
```

The service runs on:

```text
http://localhost:8081
```

Flyway creates `revconnect_user_db` tables on startup.

Do not manually create the tables and do not use `ddl-auto=create`.

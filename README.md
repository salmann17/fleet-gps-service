# Fleet GPS Tracking Service

A Spring Boot microservice for real-time fleet GPS tracking. It ingests GPS coordinates from vehicles, detects speed violations, provides location history queries, and automatically cleans up stale data.

---

## Tech Stack

| Layer            | Technology                          |
|------------------|-------------------------------------|
| Language         | Java 17                             |
| Framework        | Spring Boot 4.0                     |
| Build Tool       | Maven (with Maven Wrapper)          |
| Database         | PostgreSQL 15                       |
| ORM              | JPA / Hibernate                     |
| Authentication   | JWT (jjwt 0.12)                     |
| Security         | Spring Security                     |
| Containerization | Docker, Docker Compose              |
| Testing          | JUnit 5, MockMvc, H2 (in-memory)   |

---

## How to Run the Project

### Prerequisites

- **Docker** and **Docker Compose** installed and running
- **Java 17** (only required if running outside Docker or executing tests)

### Steps

1. **Clone the repository**

   ```bash
   git clone https://github.com/salmann17/fleet-gps-service.git
   cd fleet-gps-service
   ```

2. **Start the services**

   ```bash
   docker compose up --build -d
   ```

   This starts:
   - **PostgreSQL** on port `5433`

3. **Run the application locally**

   The `docker-compose.yml` provides the PostgreSQL database. Run the Spring Boot application from your IDE or terminal:

   ```bash
   # Linux / macOS
   ./mvnw spring-boot:run

   # Windows
   .\mvnw.cmd spring-boot:run
   ```

4. **Verify the application is running**

   ```bash
   curl http://localhost:8080/actuator/health
   ```

   Expected response:

   ```json
   { "status": "UP" }
   ```

5. **Application URL**

   ```
   http://localhost:8080
   ```

---

## Database Setup

PostgreSQL runs inside a Docker container defined in `docker-compose.yml`.

| Property | Default Value |
|----------|---------------|
| Host     | `localhost`   |
| Port     | `5433`        |
| Database | `fleetgps`    |
| Username | `fleetuser`   |
| Password | `fleetpass`   |

The database schema is **generated automatically** by Hibernate on startup (`spring.jpa.hibernate.ddl-auto=update`). No manual migration scripts are needed.

---

## Authentication Flow

All `/api/**` endpoints require a valid JWT token. Public endpoints:

- `POST /auth/login`
- `GET /actuator/health`
- `GET /actuator/info`

### 1. Obtain a Token

**Request:**

```
POST /auth/login
Content-Type: application/json
```

```json
{
  "username": "admin",
  "password": "password"
}
```

**Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (401 Unauthorized):**

```json
{
  "message": "Invalid credentials"
}
```

### 2. Use the Token

Include the token in the `Authorization` header for every protected request:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

The token expires after **1 hour** (configurable via `jwt.expiration` in `application.properties`).

---

## API Usage

> All endpoints below require the `Authorization: Bearer <token>` header.

### POST /api/gps

Ingest a GPS data point for a vehicle.

**Request:**

```
POST /api/gps
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "vehicleId": 1,
  "latitude": -6.200000,
  "longitude": 106.816666,
  "speed": 85.0,
  "timestamp": "2025-01-15T10:30:00Z"
}
```

**Validation rules:**

| Field       | Constraint              |
|-------------|-------------------------|
| `vehicleId` | Required, must exist    |
| `latitude`  | Required, -90 to 90    |
| `longitude` | Required, -180 to 180  |
| `speed`     | Required, >= 0         |
| `timestamp` | Required, ISO-8601     |

**Speed violation detection:** If `speed` exceeds the configured threshold (default: **100 km/h**), the `speedViolation` field is automatically set to `true`.

**Response (201 Created):**

```json
{
  "id": 1,
  "vehicleId": 1,
  "latitude": -6.200000,
  "longitude": 106.816666,
  "speed": 85.0,
  "timestamp": "2025-01-15T10:30:00Z",
  "speedViolation": false
}
```

**Error responses:**

| Status | Reason                      |
|--------|-----------------------------|
| 400    | Validation error            |
| 401    | Missing or invalid token    |
| 404    | Vehicle not found           |

---

### GET /api/vehicles/{id}/last-location

Retrieve the most recent GPS log for a vehicle.

**Request:**

```
GET /api/vehicles/1/last-location
Authorization: Bearer <token>
```

**Response (200 OK):**

```json
{
  "id": 5,
  "vehicleId": 1,
  "latitude": -6.200000,
  "longitude": 106.816666,
  "speed": 85.0,
  "timestamp": "2025-01-15T10:30:00Z",
  "speedViolation": false
}
```

**Error responses:**

| Status | Reason                          |
|--------|---------------------------------|
| 401    | Missing or invalid token        |
| 404    | Vehicle not found or no GPS logs|

---

### GET /api/vehicles/{id}/history

Retrieve paginated GPS log history for a vehicle within a time range.

**Request:**

```
GET /api/vehicles/1/history?from=2025-01-01T00:00:00Z&to=2025-01-31T23:59:59Z&page=0&size=20&sort=timestamp
Authorization: Bearer <token>
```

**Query parameters:**

| Parameter | Type    | Required | Description                        |
|-----------|---------|----------|------------------------------------|
| `from`    | Instant | Yes      | Start of time range (ISO-8601)     |
| `to`      | Instant | Yes      | End of time range (ISO-8601)       |
| `page`    | int     | No       | Page number (default: 0)           |
| `size`    | int     | No       | Page size (default: 20)            |
| `sort`    | string  | No       | Sort field (default: `timestamp`)  |

> `from` must be before or equal to `to`, otherwise a `400 Bad Request` is returned.

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "vehicleId": 1,
      "latitude": -6.100000,
      "longitude": 106.100000,
      "speed": 40.0,
      "timestamp": "2025-01-01T08:00:00Z",
      "speedViolation": false
    },
    {
      "id": 2,
      "vehicleId": 1,
      "latitude": -6.200000,
      "longitude": 106.200000,
      "speed": 120.0,
      "timestamp": "2025-01-01T12:00:00Z",
      "speedViolation": true
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

If no logs exist in the given range, an empty page is returned.

**Error responses:**

| Status | Reason                              |
|--------|-------------------------------------|
| 400    | `from` is after `to`                |
| 401    | Missing or invalid token            |
| 404    | Vehicle not found                   |

---

## Scheduled Tasks

### GPS Log Cleanup

A scheduled task automatically deletes GPS logs older than a configurable retention period.

| Property                    | Default         | Description                         |
|-----------------------------|-----------------|-------------------------------------|
| `app.cleanup.retention-days`| `30`            | Number of days to retain GPS logs   |
| `app.cleanup.cron`          | `0 0 2 * * *`  | Cron expression (daily at 02:00 AM) |

These values can be changed in `application.properties`. On each execution, the scheduler logs the number of deleted records.

---

## Running Tests

Tests use an **in-memory H2 database** and do not require PostgreSQL or Docker to be running.

```bash
# Linux / macOS
./mvnw test

# Windows
.\mvnw.cmd test
```

> **Note:** `JAVA_HOME` must point to a Java 17 installation.

**Test coverage includes:**

| Category                 | What is tested                                         |
|--------------------------|--------------------------------------------------------|
| GPS ingestion            | POST /api/gps returns 201 and persists a log           |
| Speed violation          | Speed above threshold flags `speedViolation = true`    |
| Input validation         | Invalid latitude, longitude, speed return 400          |
| Vehicle not found        | Non-existent vehicle returns 404                       |
| Last location            | Returns the most recent GPS log for a vehicle          |
| No logs                  | Returns 404 when vehicle has no GPS data               |
| History range            | Returns logs within the specified time window           |
| Empty history            | Returns empty page when no logs match the range        |
| Invalid date range       | `from` after `to` returns 400                          |
| Context load             | Application context loads successfully                 |

---

## Project Structure

```
src/main/java/com/example/fleetgpsservice/
├── FleetGpsServiceApplication.java   # Entry point, enables scheduling
├── config/
│   └── SecurityConfig.java           # Spring Security + JWT filter chain
├── controller/
│   ├── AuthController.java           # POST /auth/login
│   ├── GpsController.java            # POST /api/gps
│   └── VehicleController.java        # GET last-location, history
├── dto/
│   ├── GpsRequestDTO.java            # Inbound GPS payload (validated)
│   ├── GpsResponseDTO.java           # Outbound GPS response
│   └── LoginRequestDTO.java          # Login credentials
├── entity/
│   ├── Vehicle.java                  # Vehicle JPA entity
│   └── GpsLog.java                   # GPS log JPA entity
├── exception/
│   ├── GlobalExceptionHandler.java   # Centralized error handling
│   ├── GpsLogNotFoundException.java
│   ├── InvalidDateRangeException.java
│   └── VehicleNotFoundException.java
├── repository/
│   ├── VehicleRepository.java        # Vehicle data access
│   └── GpsLogRepository.java         # GPS log data access + cleanup query
├── scheduler/
│   └── GpsLogCleanupScheduler.java   # Cron-based old log deletion
├── security/
│   ├── JwtService.java               # Token generation, validation, parsing
│   └── JwtAuthenticationFilter.java  # Extracts and validates Bearer tokens
└── service/
    ├── AuthService.java              # Credential validation + token issuance
    ├── GpsService.java               # Core GPS business logic
    └── SpeedViolationService.java    # Configurable speed threshold check
```

---

## Configuration Reference

All properties in `application.properties`:

| Property                                 | Default                          | Description                     |
|------------------------------------------|----------------------------------|---------------------------------|
| `spring.datasource.url`                  | `jdbc:postgresql://localhost:5433/fleetgps` | JDBC URL              |
| `spring.datasource.username`             | `fleetuser`                      | DB username                     |
| `spring.datasource.password`             | `fleetpass`                      | DB password                     |
| `spring.jpa.hibernate.ddl-auto`          | `update`                         | Schema generation strategy      |
| `app.speed.violation-threshold-kmh`      | `100`                            | Speed violation threshold (km/h)|
| `app.cleanup.retention-days`             | `30`                             | Days before logs are deleted    |
| `app.cleanup.cron`                       | `0 0 2 * * *`                    | Cleanup schedule (cron)         |
| `jwt.secret`                             | *(dev key)*                      | HMAC signing key (≥256 bits)    |
| `jwt.expiration`                         | `3600000`                        | Token TTL in milliseconds (1h)  |

All datasource and JPA properties can be overridden via environment variables (e.g., `SPRING_DATASOURCE_URL`).

---

## Notes

- **Authentication credentials** are hardcoded (`admin` / `password`) for development purposes. Replace with a proper user store before deploying to production.
- **JWT secret** in `application.properties` is a development placeholder. Use a strong, externalized secret in production.
- **Vehicle records** must exist in the database before GPS data can be ingested. Insert vehicles directly into the `vehicle` table or build an admin endpoint.
- **Schema management** uses `ddl-auto=update`. For production, consider switching to a migration tool like Flyway or Liquibase.
- **Port 5433** is used for PostgreSQL to avoid conflicts with any local PostgreSQL instance on the default port 5432.

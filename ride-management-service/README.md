# RideLink — Ride Management Service (IT3130 Group Assignment)

## 1. Purpose & Service Ownership
The **Ride Management Service** (Member 3) is the central workflow orchestrator of the **RideLink** ride-sharing platform. It manages the end-to-end ride lifecycle from initial passenger request and driver assignment through driver acceptance, trip start, completion, final fare coordination, and cancellation.

### Microservice Ecosystem
| # | Microservice | Port | Database | Primary Responsibility |
|---|---|---|---|---|
| 1 | **Account Service** | `8081` | `ridelink_account_db` | User registration, login, JWT token issuance, role management (`PASSENGER`, `DRIVER`, `ADMIN`) |
| 2 | **Driver & Vehicle Service** | `8082` | `ridelink_driver_db` | Driver profiles, vehicles, availability status, `GET /api/drivers/available` |
| 3 | **Ride Management Service (THIS SERVICE)** | `8083` | `ridelink_ride_db` | Ride request creation, driver assignment, state machine transitions, lifecycle timestamps, ride retrieval |
| 4 | **Fare & Payment Service** | `8084` | `ridelink_fare_db` | Fare estimation, final fare calculation (`POST /api/fares/calculate`), simulated payment recording & receipts |

---

## 2. Architecture & Data Ownership Boundary
- **Framework**: Java 17 + Spring Boot 3.2.5 (`spring-boot-starter-web`, `spring-boot-starter-data-mongodb`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `springdoc-openapi-starter-webmvc-ui`).
- **Database Per Service Pattern**:
  - Database Name: `ridelink_ride_db`
  - Collection: `rides`
  - **Strict Isolation**: This service never queries or modifies `ridelink_account_db`, `ridelink_driver_db`, or `ridelink_fare_db`. Cross-service references are maintained exclusively via stable identifiers (`passengerId`, `driverId`, `id` / `rideId`, `fareId`) and synchronous REST API contracts.

---

## 3. Ride Lifecycle & State Machine Rules

### States (`RideStatus` enum)
- `REQUESTED`: Ride created by a passenger; awaiting driver assignment.
- `ASSIGNED`: Eligible available driver selected and assigned.
- `ACCEPTED`: Assigned driver has accepted the ride request.
- `IN_PROGRESS`: Driver has picked up the passenger and started the trip.
- `COMPLETED`: Trip finished at destination; final fare calculated via Fare & Payment Service.
- `CANCELLED`: Ride cancelled prior to trip start.

### Valid State Transitions
| Current Status | Allowed Next Statuses | Disallowed Transitions (Throws `InvalidRideStatusException` - HTTP 400) |
|---|---|---|
| `REQUESTED` | `ASSIGNED`, `CANCELLED` | `ACCEPTED`, `IN_PROGRESS`, `COMPLETED` |
| `ASSIGNED` | `ACCEPTED`, `CANCELLED` | `REQUESTED`, `IN_PROGRESS`, `COMPLETED` |
| `ACCEPTED` | `IN_PROGRESS`, `CANCELLED` | `REQUESTED`, `ASSIGNED`, `COMPLETED` |
| `IN_PROGRESS` | `COMPLETED` | `REQUESTED`, `ASSIGNED`, `ACCEPTED`, `CANCELLED` |
| `COMPLETED` | *(Terminal — None)* | All transitions (e.g., `COMPLETED -> IN_PROGRESS` fails) |
| `CANCELLED` | *(Terminal — None)* | All transitions |

---

## 4. REST API Endpoints

| HTTP Method | Endpoint | Required Role(s) | Success Status | Description |
|---|---|---|---|---|
| `POST` | `/api/rides` | `PASSENGER`, `ADMIN` | `201 Created` | Create a new ride request (`autoAssignDriver` optional) |
| `GET` | `/api/rides/{id}` | `PASSENGER`, `DRIVER`, `ADMIN` | `200 OK` | Retrieve ride details by `rideId` |
| `GET` | `/api/rides` | `ADMIN` | `200 OK` | Retrieve all rides |
| `GET` | `/api/rides/passenger/{passengerId}` | `PASSENGER`, `ADMIN` | `200 OK` | Retrieve rides for a specific passenger |
| `GET` | `/api/rides/driver/{driverId}` | `DRIVER`, `ADMIN` | `200 OK` | Retrieve rides for a specific driver |
| `POST` | `/api/rides/{id}/assign` | `PASSENGER`, `ADMIN` | `200 OK` | Assign an eligible driver (`REQUESTED -> ASSIGNED`) |
| `POST` | `/api/rides/{id}/accept` | `DRIVER`, `ADMIN` | `200 OK` | Driver accepts assigned ride (`ASSIGNED -> ACCEPTED`) |
| `POST` | `/api/rides/{id}/start` | `DRIVER`, `ADMIN` | `200 OK` | Driver starts ride (`ACCEPTED -> IN_PROGRESS`) |
| `POST` | `/api/rides/{id}/complete` | `DRIVER`, `ADMIN` | `200 OK` | Complete ride (`IN_PROGRESS -> COMPLETED`) & calculate fare |
| `POST` | `/api/rides/{id}/cancel` | `PASSENGER`, `DRIVER`, `ADMIN` | `200 OK` | Cancel ride before trip starts (`-> CANCELLED`) |

---

## 5. Interservice Communication

### 5.1 Driver & Vehicle Service (`DriverServiceClient`)
- **Endpoint Called**: `GET {DRIVER_SERVICE_URL}/api/drivers/available`
- **Communication Style**: Synchronous REST (`RestTemplate`).
- **Justification**: When assigning a driver to a ride request, the Ride Management Service needs an immediate, consistent list of currently available drivers before it can transition the ride to `ASSIGNED`.
- **Deterministic Selection Strategy**:
  1. Request available drivers via `GET /api/drivers/available`.
  2. Filter eligible records where `available == true` and `driverId` is non-blank.
  3. Sort deterministically by `driverId` in ascending lexicographical order.
  4. Select the first driver, store `driverId` and `vehicleNumber` on the ride, and transition status to `ASSIGNED`.
  5. If the list is empty, throw `DriverAssignmentException` (`409 Conflict`).

### 5.2 Fare & Payment Service (`FareServiceClient`)
- **Endpoint Called**: `POST {FARE_SERVICE_URL}/api/fares/calculate`
- **Communication Style**: Synchronous REST (`RestTemplate`).
- **Justification**: Upon ride completion (`POST /api/rides/{id}/complete`), the passenger and driver immediately expect the final calculated fare and payment status in the completion response.

---

## 6. Setup, Environment Variables & Execution

### Prerequisites
- Java 17+
- Apache Maven 3.9+
- MongoDB 6.0+ running on `localhost:27017`

### Environment Variables
Configure via environment variables or `.env` (never commit secrets):
```bash
export SERVER_PORT=8083
export MONGODB_URI=mongodb://localhost:27017/ridelink_ride_db
export DRIVER_SERVICE_URL=http://localhost:8082
export FARE_SERVICE_URL=http://localhost:8084
export JWT_SECRET=replace-with-at-least-32-byte-secret-key-for-ridelink
```

### Run the Service
```bash
mvn clean spring-boot:run
```

### Run Unit Tests (JUnit 5 + Mockito)
```bash
mvn clean test
```

### Swagger / OpenAPI Documentation
Once running, open:
- **Swagger UI**: `http://localhost:8083/swagger-ui.html`
- **OpenAPI JSON Spec**: `http://localhost:8083/v3/api-docs`

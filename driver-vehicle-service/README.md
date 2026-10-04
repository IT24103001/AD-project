# RideLink – Driver & Vehicle Service

IT3130 Application Development – Group Assignment "RideLink".
This repository contains **only** the Driver & Vehicle microservice (Java + Spring Boot + MongoDB).

## 1. Responsibilities
1. Driver operational profile management
2. Vehicle details management
3. Driver availability status management
4. Driver service area management
5. Simulated current location management
6. Retrieval of eligible available drivers (used by the Ride Management Service)

Login, passwords and roles belong to the **Account Service**; ride lifecycle belongs to **Ride Management**; fares and payments belong to **Fare & Payment**. This service never touches their databases.

## 2. Technology stack
Java 17 · Spring Boot 3.3 · Spring Web · Spring Data MongoDB · Bean Validation · Springdoc OpenAPI (Swagger UI) · Spring Security (JWT resource server, optional) · JUnit 5 · Mockito · Maven · MongoDB

## 3. Architecture
```
Client / Ride Management Service
          │  REST + JSON
          ▼
   Controller   (HTTP only, validation, Swagger docs)
          ▼
   Service      (business rules)
          ▼
   Repository   (Spring Data MongoDB)
          ▼
   MongoDB  →  ridelink_driver_vehicle_db  (drivers, vehicles)
```
Packages (`com.ridelink.drivervehicle`): `controller`, `service` (+`impl`), `repository`, `model`, `dto`, `mapper`, `exception`, `config`, `util`.
Design notes: DTOs are used instead of exposing entities; constructor injection everywhere; controllers depend on service interfaces (SOLID – Dependency Inversion); one `@RestControllerAdvice` for all errors.

## 4. Database design (database `ridelink_driver_vehicle_db`)
**drivers**: `_id`, `accountId` (unique, external ID from Account Service), `name`, `phone`, `licenseNumber` (unique), `availabilityStatus` (AVAILABLE/UNAVAILABLE), `serviceArea`, `currentLocation {latitude, longitude}`, `createdAt`, `updatedAt`
**vehicles**: `_id`, `driverId` (reference to drivers `_id`), `vehicleNumber` (unique, stored upper-case), `vehicleType` (CAR/VAN/SUV), `brand`, `model`, `color`, `createdAt`, `updatedAt`

MongoDB creates the database and collections automatically on the first insert; unique indexes are created at start-up (`spring.data.mongodb.auto-index-creation=true`).

## 5. MongoDB setup
Option A – Docker: `docker run -d --name ridelink-mongo -p 27017:27017 mongo:7`
Option B – local install of MongoDB Community Server (default port 27017).
Optional check: `mongosh` → `use ridelink_driver_vehicle_db` → `show collections` (after first request).

## 6. Environment variables
| Variable | Meaning | Default (dev only) |
|---|---|---|
| `MONGODB_URI` | MongoDB connection string (put credentials here if your DB needs them) | `mongodb://localhost:27017` |
| `MONGODB_DATABASE` | Database name | `ridelink_driver_vehicle_db` |
| `SERVER_PORT` | HTTP port | `8082` |
| `SEARCH_RADIUS_KM` | Simulated radius for eligibility | `5.0` |
| `SECURITY_ENABLED` | `true` = JWT required + roles enforced | `false` |
| `JWT_SECRET` | Shared HS256 secret (≥ 32 chars), only if security enabled | *(none)* |

See `.env.example`. Spring Boot does not read `.env` automatically – export the variables in your shell or add them to your IDE run configuration.
```bash
export MONGODB_URI="mongodb://localhost:27017"
export MONGODB_DATABASE="ridelink_driver_vehicle_db"
export SERVER_PORT=8082
```

## 7. Run
```bash
mvn clean install        # compile + run unit tests
mvn spring-boot:run      # start the service
mvn test                 # only the tests
```
Swagger UI: **http://localhost:8082/swagger-ui.html** · OpenAPI JSON: http://localhost:8082/v3/api-docs

## 8. API summary
| Method | URL | Purpose | Success | Roles (when security on) |
|---|---|---|---|---|
| POST | /api/drivers | Create driver | 201 | DRIVER, ADMIN |
| GET | /api/drivers/{id} | Get driver | 200 | DRIVER, ADMIN |
| GET | /api/drivers | List drivers | 200 | ADMIN |
| PUT | /api/drivers/{id} | Update driver | 200 | DRIVER, ADMIN |
| DELETE | /api/drivers/{id} | Delete driver (+ vehicles) | 204 | ADMIN |
| PATCH | /api/drivers/{id}/availability | Set AVAILABLE/UNAVAILABLE | 200 | DRIVER, ADMIN |
| PATCH | /api/drivers/{id}/location | Set simulated location | 200 | DRIVER, ADMIN |
| PATCH | /api/drivers/{id}/service-area | Set service area | 200 | DRIVER, ADMIN |
| GET | /api/drivers/available | Eligible available drivers | 200 | any authenticated caller |
| GET | /api/drivers/{driverId}/vehicles | Vehicles of a driver | 200 | DRIVER, ADMIN |
| POST | /api/vehicles | Create vehicle | 201 | DRIVER, ADMIN |
| GET | /api/vehicles/{id} | Get vehicle | 200 | DRIVER, ADMIN |
| GET | /api/vehicles | List vehicles | 200 | ADMIN |
| PUT | /api/vehicles/{id} | Update vehicle | 200 | DRIVER, ADMIN |
| DELETE | /api/vehicles/{id} | Delete vehicle | 204 | DRIVER, ADMIN |

Error statuses: 400 (validation / malformed JSON / bad search params), 404 (driver or vehicle not found), 409 (duplicate or invalid availability), 500 (unexpected).

## 9. Eligibility rule for available drivers
`GET /api/drivers/available?serviceArea=Colombo&latitude=6.9271&longitude=79.8612`

A driver is **eligible** when:
1. `availabilityStatus = AVAILABLE`
2. if `serviceArea` is supplied, it matches the driver's service area (case-insensitive)
3. if `latitude` **and** `longitude` are supplied, the driver has a current location and the straight-line (Haversine) distance is ≤ `SEARCH_RADIUS_KM` (default 5 km)

Parameters are all optional; with none, every AVAILABLE driver is returned. Sending only one coordinate gives 400. With coordinates, results are sorted nearest first and include `distanceKm`.

## 10. Business rules (design decisions)
- A new driver starts `UNAVAILABLE`.
- A driver can become `AVAILABLE` only if they have a current location **and** at least one vehicle (otherwise 409 `INVALID_AVAILABILITY`). Going `UNAVAILABLE` is always allowed.
- Deleting a driver deletes their vehicles. Deleting a driver's last vehicle makes an AVAILABLE driver `UNAVAILABLE`.
- `accountId`, `licenseNumber` and `vehicleNumber` are unique (409 on duplicates). Vehicle numbers are trimmed and upper-cased.

## 11. Interservice communication
- **Identifiers only**: `accountId` (Account Service), `driverId`/`vehicleId` (this service), `rideId` (Ride Management). No cross-database access, no shared collections.
- **Ride Management → this service**: calls `GET /api/drivers/available` synchronously over REST to get candidates for a ride. After a driver is assigned, Ride Management can call `PATCH /api/drivers/{id}/availability` to mark them UNAVAILABLE (and AVAILABLE again when the ride ends).
- **Account Service → this service**: provides the JWT and the `accountId`; this service does not call it.
- **Fare & Payment**: if it needs vehicle details (e.g. `vehicleType` for fare calculation) it calls `GET /api/vehicles/{id}` or `GET /api/drivers/{id}/vehicles`.
- **Why REST**: the matching needs an immediate answer ("who is available now?"), request/response is the simplest model, it works with Swagger/Postman for the demo, and no extra infrastructure (Kafka, RabbitMQ, gateway, Eureka) is required by the assignment.

## 12. Security
Disabled by default (`SECURITY_ENABLED=false`) so the service can be demonstrated alone. When `SECURITY_ENABLED=true` the service validates a Bearer JWT (HS256, secret from `JWT_SECRET`) and enforces the roles in the table above. **Assumption to agree with the Account Service team:** the token carries a `roles` claim such as `["DRIVER"]` or `["ADMIN"]`. No users or credentials exist in this service's code. Note: role checks do not verify that a DRIVER only edits their *own* profile (could be added by comparing the token `sub` with `accountId`).

## 13. Tests
`mvn test` runs JUnit 5 + Mockito unit tests (no database needed):
- `DriverServiceImplTest` – create/get/update/delete, duplicates, availability rules, location, service area, eligible-driver filtering
- `VehicleServiceImplTest` – create, not found, duplicate number, driver check, update, delete rules
- `DtoValidationTest` – invalid input (blank fields, latitude/longitude range, missing enums)
- `DistanceCalculatorTest` – distance formula

## 14. Sample request / response
```http
POST /api/drivers
{ "accountId": "acc-1001", "name": "Nimal Perera", "phone": "0771234567", "licenseNumber": "B1234567", "serviceArea": "Colombo" }
```
```json
201 Created
{ "id": "665f1c2e9a1b2c3d4e5f6a7b", "accountId": "acc-1001", "name": "Nimal Perera", "phone": "0771234567",
  "licenseNumber": "B1234567", "availabilityStatus": "UNAVAILABLE", "serviceArea": "Colombo",
  "currentLocation": null, "createdAt": "2026-10-04T10:15:30Z", "updatedAt": "2026-10-04T10:15:30Z" }
```
Error example (`GET /api/drivers/unknown`):
```json
404 Not Found
{ "timestamp": "2026-10-04T10:16:00Z", "status": 404, "error": "DRIVER_NOT_FOUND",
  "message": "Driver not found with id: unknown", "path": "/api/drivers/unknown" }
```
Validation error example:
```json
400 Bad Request
{ "timestamp": "...", "status": 400, "error": "VALIDATION_ERROR", "message": "Request validation failed",
  "path": "/api/drivers", "details": { "name": "name cannot be blank" } }
```

## 15. Postman
Import `postman/RideLink-DriverVehicle.postman_collection.json`. Variables: `baseUrl` (default `http://localhost:8082`), `token` (leave empty when security is off). Run the folders in order: Happy path → Negative scenarios → Cleanup. Request IDs are saved automatically by test scripts.

Manual order: 1 create driver → 2 create vehicle → 3 set location → 4 set service area → 5 set AVAILABLE → 6 search available drivers → 7 get/update driver → negative cases (invalid id, duplicate vehicle number, invalid input).

# RideLink – Account Service

Spring Boot microservice that owns **identity** for the RideLink platform: registration, login (JWT), roles, profile and account status.

## 1. Purpose
| Responsibility | Endpoint |
|---|---|
| Passenger / driver registration | `POST /api/accounts/register/passenger`, `/driver` |
| Login + token issuance | `POST /api/accounts/login` |
| Profile view / update | `GET` / `PUT /api/accounts/{id}` |
| Account status management (ADMIN) | `PATCH /api/accounts/{id}/status` |
| Role lookup (optional) | `GET /api/accounts/{id}/role` |

## 2. Architecture
```
Client (Postman / Swagger UI)
        │  HTTP + JSON  (Authorization: Bearer <JWT>)
        ▼
 JwtAuthenticationFilter ──► SecurityConfig (URL rules: public / authenticated / ADMIN)
        ▼
 Controller  (thin: validate, delegate)
        ▼
 Service     (business rules: duplicate check, BCrypt, ownership, status)
        ▼
 Repository  (Spring Data MongoDB)
        ▼
 MongoDB  ridelink_account_db.accounts      <- private to this service
```
Errors from any layer are converted by `GlobalExceptionHandler` (`@RestControllerAdvice`) into one JSON shape.

Packages: `controller`, `service`, `repository`, `model`, `dto`, `security`, `exception`, `config`.

## 3. Technology
Java 17, Spring Boot 3.3, Spring Web, Spring Data MongoDB, Spring Security, JJWT 0.12, Bean Validation, springdoc-openapi 2.6, JUnit 5, Mockito, Maven, MongoDB.

## 4. MongoDB setup
1. Install MongoDB Community (or run `docker run -d -p 27017:27017 --name mongo mongo:7`).
2. Nothing else: database `ridelink_account_db` and collection `accounts` are created automatically on first write, and the **unique index on `email`** is created at startup (`auto-index-creation: true`).

Collection `accounts`: `_id (accountId), fullName, email (unique), phone, passwordHash (BCrypt), role, accountStatus, createdAt, updatedAt`.

## 5. Environment variables
Copy `.env.example` to `.env` (git-ignored) **or** export real environment variables.

| Variable | Required | Meaning |
|---|---|---|
| `JWT_SECRET` | **yes** | HS256 signing secret, ≥ 32 chars. App refuses to start without it. |
| `MONGODB_URI` | no | default `mongodb://localhost:27017/ridelink_account_db` |
| `JWT_EXPIRATION_MS` | no | default `3600000` (1 hour) |
| `SERVER_PORT` | no | default `8081` |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | no | If both set, an ADMIN account is created at startup (if not existing). |

Generate a secret: `openssl rand -base64 48`

## 6. How to run
```bash
cp .env.example .env          # edit JWT_SECRET (and ADMIN_* if you want an admin)
mvn spring-boot:run
```
Or with plain environment variables:
```bash
# Linux/macOS
JWT_SECRET=$(openssl rand -base64 48) ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD='Admin12345' mvn spring-boot:run
# Windows PowerShell
$env:JWT_SECRET="<32+ chars>"; mvn spring-boot:run
```
Swagger UI: **http://localhost:8081/swagger-ui.html**  ·  OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## 7. API list
| Method & path | Auth | Success | Errors |
|---|---|---|---|
| `POST /api/accounts/register/passenger` | public | 201 | 400, 409 |
| `POST /api/accounts/register/driver` | public | 201 | 400, 409 |
| `POST /api/accounts/login` | public | 200 | 400, 401, 403 |
| `GET /api/accounts/{id}` | JWT, owner or ADMIN | 200 | 401, 403, 404 |
| `PUT /api/accounts/{id}` | JWT, owner or ADMIN | 200 | 400, 401, 403, 404 |
| `PATCH /api/accounts/{id}/status` | JWT, **ADMIN** | 200 | 400, 401, 403, 404 |
| `GET /api/accounts/{id}/role` | JWT, owner or ADMIN | 200 | 401, 403, 404 |

## 8. Authentication process
1. Client calls `POST /login` with email + password.
2. Service finds account by (lower-cased) email → BCrypt-compares password → checks `accountStatus == ACTIVE`.
3. Service signs a JWT (HS256, secret from `JWT_SECRET`) with claims `sub=accountId`, `email`, `role`, `iat`, `exp`.
4. Client sends `Authorization: Bearer <token>` on later calls.
5. `JwtAuthenticationFilter` verifies signature and expiry, builds an `AuthenticatedUser`, and Spring Security applies the URL rules.

Wrong email and wrong password give the **same** 401 message (no user enumeration). Status is checked only after the password is correct.

## 9. Role-based authorization
| Role | Can do |
|---|---|
| PASSENGER / DRIVER | view/update **own** profile, read own role |
| ADMIN | view/update **any** profile, change any account status |

Two layers: (1) `SecurityConfig` – `PATCH /*/status` requires `ROLE_ADMIN`; all non-public URLs need a valid token. (2) `AccountServiceImpl.requireSelfOrAdmin` – the token's `accountId` must equal the `{id}` in the path unless ADMIN (otherwise 403). Role is never accepted from the client: the endpoint chooses `PASSENGER` or `DRIVER`; ADMIN comes only from the seeder.

## 10. Tests
```bash
mvn test
```
Covers: passenger/driver registration, duplicate email, login success, invalid password, inactive/suspended login, get/update profile, account not found, unauthorized access, admin status update, validation failures, JWT round-trip/expiry/wrong secret.

## 11. Sample requests / responses
```bash
# Register passenger
curl -X POST http://localhost:8081/api/accounts/register/passenger -H "Content-Type: application/json" \
  -d '{"fullName":"Nimal Perera","email":"nimal@example.com","phone":"+94771234567","password":"Passw0rd123"}'
```
```json
{ "id":"6650f1c2a1b2c3d4e5f60718","fullName":"Nimal Perera","email":"nimal@example.com","phone":"+94771234567",
  "role":"PASSENGER","accountStatus":"ACTIVE","createdAt":"2026-10-04T08:30:00Z","updatedAt":"2026-10-04T08:30:00Z" }
```
```bash
# Login
curl -X POST http://localhost:8081/api/accounts/login -H "Content-Type: application/json" \
  -d '{"email":"nimal@example.com","password":"Passw0rd123"}'
```
```json
{ "accessToken":"eyJhbGciOiJIUzI1NiJ9...","tokenType":"Bearer","expiresInSeconds":3600,
  "user":{ "id":"6650f1c2a1b2c3d4e5f60718","fullName":"Nimal Perera","email":"nimal@example.com","role":"PASSENGER","accountStatus":"ACTIVE", "...":"..." } }
```
```bash
# Update own profile
curl -X PUT http://localhost:8081/api/accounts/6650f1c2a1b2c3d4e5f60718 -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"fullName":"Nimal K. Perera","phone":"+94771234999"}'

# Admin suspends an account
curl -X PATCH http://localhost:8081/api/accounts/6650f1c2a1b2c3d4e5f60718/status -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" -d '{"status":"SUSPENDED"}'
```
Validation error (400):
```json
{ "timestamp":"2026-10-04T08:31:00Z","status":400,"error":"Bad Request","message":"Validation failed",
  "path":"/api/accounts/register/passenger",
  "validationErrors":{ "email":"Email must be a valid email address",
                       "password":"Password must be 8-64 characters and contain at least one letter and one digit" } }
```
Other errors use the same shape without `validationErrors`: 401 bad credentials/token, 403 forbidden/inactive, 404 not found, 409 duplicate email.

## 12. Inter-service communication
Other RideLink services **never read `ridelink_account_db`**. They use:

* **`accountId`** – the MongoDB `_id` string. Driver & Vehicle Service stores it as `driverAccountId`; Ride Management stores `passengerAccountId` / `driverAccountId`; Fare & Payment stores `payerAccountId`. Treat it as an opaque, immutable string (no foreign-key constraint across databases).
* **JWT validation** – every service is configured with the same `JWT_SECRET`, verifies the HS256 signature locally and reads `sub` (accountId) and `role`. No call to Account Service is needed for each request, and the services can trust *who* the caller is and *what role* they have.
* **REST calls** – when a service needs to confirm an account exists/is ACTIVE, it calls `GET /api/accounts/{id}` (forwarding the caller's token) or `GET /api/accounts/{id}/role`.

Known trade-off: JWTs are stateless, so a token issued before suspension remains valid until it expires (default 1 h). Keep expiry short; a later improvement is a service-to-service "validate account" endpoint.

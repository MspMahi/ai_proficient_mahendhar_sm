# Mahendhar AI URL Shortener

A secure, RESTful URL shortener built with Java 17 and Spring Boot. It provides JWT-protected URL management and public redirects, with validation, consistent error contracts, structured logging, and automated test coverage reporting.

## Architecture

```mermaid
flowchart LR
    Client -->|JWT| API[REST controllers]
    Visitor -->|GET /{code}| Redirect[Redirect controller]
    API --> Services[Application services]
    Redirect --> Services
    Services --> Repositories[Spring Data JPA repositories]
    Repositories --> DB[(H2 / configured database)]
    API --> Advice[Global exception handler]
```

The application uses a layered design: controllers own HTTP concerns, services own business rules and transactions, repositories own persistence, and DTOs form the public API contract. A short code is generated with `SecureRandom`, checked for uniqueness, and retried up to eight times before failing safely.

## Requirements and acceptance criteria

- Registered users can create, list, inspect, and deactivate only their own short URLs.
- Public visitors can resolve a short code to its destination when a URL is active and not expired.
- Authentication uses stateless JWT bearer tokens, and invalid or missing auth returns `401`.
- Invalid input returns `400`; duplicate account registration returns `409`.
- Missing, inactive, expired, or unowned short URLs are intentionally surfaced as `404`.
- The H2 console is disabled by default and JWT secrets must be injected through environment variables in non-development environments.

## Run locally

Prerequisites: JDK 17+ and Maven 3.9+.

```bash
./mvnw test
./mvnw spring-boot:run
```

The API runs at `http://localhost:8080`. OpenAPI UI is available at `http://localhost:8080/swagger-ui.html`.

The H2 console is disabled by default. Enable it for local debugging only with `H2_CONSOLE_ENABLED=true`.

## Docker and CI

Build the container image:

```bash
docker build -t ai-url-shortener .
```

Run the service with a production JWT secret and base URL:

```bash
docker run -p 8080:8080 \
  -e JWT_SECRET="<base64-secret>" \
  -e APP_BASE_URL="http://localhost:8080" \
  ai-url-shortener
```

The service exposes a health endpoint at `http://localhost:8080/actuator/health` and an info endpoint at `http://localhost:8080/actuator/info`.

A GitHub Actions CI workflow is included to build, test, and publish a JaCoCo report on every push and pull request.

## API quick start

Register and retain the returned token:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Asha","email":"asha@example.com","password":"password123"}'
```

Create a URL with that token:

```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com/docs","expiresAt":"2099-12-31T23:59:59Z"}'
```

Example response:

```json
{
  "id": 1,
  "originalUrl": "https://example.com/docs",
  "shortCode": "Ab3kP9xQ",
  "shortUrl": "http://localhost:8080/Ab3kP9xQ",
  "clickCount": 0,
  "active": true
}
```

| Method | Endpoint | Authentication | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/v1/auth/register` | No | Create a user and issue a JWT |
| `POST` | `/api/v1/auth/login` | No | Authenticate and issue a JWT |
| `POST` | `/api/v1/urls` | Bearer JWT | Create a short URL |
| `GET` | `/api/v1/urls` | Bearer JWT | List the caller's URLs |
| `GET` | `/api/v1/urls/{code}` | Bearer JWT | Get one of the caller's URLs |
| `DELETE` | `/api/v1/urls/{code}` | Bearer JWT | Deactivate one of the caller's URLs |
| `GET` | `/{code}` | No | Redirect and increment the click count |

## Error contract

Every application error returns the same shape. Validation responses include field-level detail, so clients can show actionable feedback.

```json
{
  "timestamp": "2026-07-31T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/urls",
  "validationErrors": { "originalUrl": "must be a valid URL" }
}
```

`401` means no valid authentication was supplied, `404` deliberately covers missing, inactive, expired, or unowned short URLs, and `409` is returned for duplicate registration. This avoids exposing another user's URL metadata.

## Configuration and security

Configuration is environment-driven. Important variables include `DB_URL`, `APP_BASE_URL`, `JWT_SECRET`, `JWT_EXPIRATION`, `H2_CONSOLE_ENABLED`, and `JPA_DDL_AUTO`. The checked-in JWT secret is development-only; set `JWT_SECRET` to a strong Base64-encoded key in every deployed environment and disable the H2 console.

- Passwords are stored only as BCrypt hashes.
- URL-management endpoints require stateless bearer authentication.
- Input constraints reject malformed URLs, weak registration data, and past expirations.
- Security headers include CSP, same-origin frame protection, and a no-referrer policy.
- Exception logs retain operational context without recording passwords or JWT values.

## Testing and quality checks

`mvn test` runs unit and MVC tests for authentication, URL lifecycle, validation, redirects, JWT processing, user-detail loading, entities, and short-code generation. JaCoCo writes an HTML coverage report to `target/site/jacoco/index.html` after the test phase.

Useful checks:

```bash
mvn test
mvn verify
```

## Operational notes

The current H2 configuration is intended for local development and tests. For production, configure a managed relational database, use a secret manager for JWT keys, terminate TLS at the edge, centralize structured logs, and add a shared rate limiter at the gateway or application tier. Health and deployment monitoring should alert on elevated 5xx and authentication-failure rates.

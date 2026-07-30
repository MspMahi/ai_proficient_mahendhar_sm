# Mahendhar AI URL Shortener

Production-ready URL shortener API built with Java 17, Spring Boot 4.x, Maven, Spring Security JWT, Spring Data JPA, H2, Lombok, Jakarta Validation, OpenAPI, JUnit 5, and Mockito.

## Engineering Assumptions

- Spring Boot 4.0.7 is used to stay on the 4.x line requested by the prompt.
- H2 is the runtime database for local and test use; persistence can be swapped later through Spring Data JPA configuration.
- JWT authentication protects URL management APIs. Public redirects remain unauthenticated.
- Short codes are generated server-side and checked for uniqueness before persistence.
- DTO to entity mapping is performed manually in service classes. MapStruct is intentionally not used.

## Architecture

- `controller`: REST endpoints for authentication, URL management, and public redirects.
- `service`: Transactional business logic and manual DTO mapping.
- `repository`: Spring Data JPA persistence ports.
- `entity`: JPA aggregate models.
- `security`: JWT generation, authentication filter, and user principal loading.
- `config`: Security, OpenAPI, and application configuration.
- `dto`: Request and response payload contracts.
- `exception`: Typed domain exceptions and global error handling.
- `util`: Short-code generation utilities.

## Local Development

```bash
mvn test
mvn spring-boot:run
```

Swagger UI is available at `http://localhost:8080/swagger-ui.html` when the application is running.

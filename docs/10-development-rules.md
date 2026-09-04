# Development Rules & Coding Conventions
## Siva Machine Works Smart Manufacturing Platform

This document outlines the strict development rules, architectural guidelines, and coding conventions for the platform. These rules must be adhered to at all times to ensure system integrity, maintainability, and security.

### 1. General Workflow & AI Rules
- **Inspect existing code before editing:** Never modify code without first understanding its context.
- **Never overwrite working functionality unnecessarily:** Focus changes only on the required requirements.
- **Do not mark features complete until they actually work.**
- **Explain major architectural changes before making them:** Seek approval for structural shifts.
- **Run relevant tests after changes:** Ensure no regressions are introduced.
- **Do not use fake logic where real business logic is required:** Implement actual calculations, validations, and state transitions.

### 2. Architecture Guidelines
- **Follow the documented architecture:** Adhere strictly to the modular monolith pattern defined in `03-system-architecture.md`.
- **Prefer a modular monolith:** Avoid unnecessary microservices.
- **Do not introduce dependencies without justification:** Keep the dependency tree lean and manageable.
- **Controller → Service → Repository separation:** Maintain strict layered architecture. Controllers handle HTTP/Routing, Services handle business logic, Repositories handle data access.
- **DTOs instead of exposing database entities:** Never return JPA entities directly from controllers. Use dedicated request/response DTOs.
- **Keep the system modular and maintainable:** Use domain events for cross-module communication instead of direct service dependencies.

### 3. Security & Validation
- **Input validation:** Use Jakarta Bean Validation (`@Valid`, `@NotNull`, etc.) in Java and Zod in TypeScript for all inputs.
- **Centralized exception handling:** Use `@RestControllerAdvice` in Spring Boot to return a standardized global error envelope.
- **No hardcoded secrets:** Never hardcode passwords, API keys, or tokens.
- **Environment variables for configuration:** Externalize all configuration.
- **RBAC for authorization:** Use `@PreAuthorize` based on the documented role matrix. Enforce strictly at the method level.
- **Audit logging for critical operations:** Use the AOP-based audit log for state-changing operations.
- **Secure file uploads:** Use S3 presigned URLs, validate MIME types rigorously, and enforce a 500MB maximum file size.
- **Do not disable security to make tests pass:** Write tests that correctly authenticate and authorize.

### 4. Database & Migrations
- **Database migrations for schema changes:** Use Flyway exclusively. Never rely on Hibernate auto-ddl in production.
- **PostgreSQL Conventions:**
  - **Naming:** Use `snake_case` for tables and columns. Follow Flyway naming conventions (`V{Version}__{Description}.sql`).
  - **Primary Keys:** Use UUIDs for all entity primary keys.
  - **Indexes:** Add indexes for foreign keys and frequently queried columns.
  - **Data Types:** Use `JSONB` for unstructured data or flexible attributes, but prefer standard relational modeling for core business data.
  - **Concurrency:** Use Optimistic Concurrency Control (OCC) with `@Version` for entities prone to concurrent updates.

### 5. Testing
- **Automated tests for important business logic:** Use JUnit 5 and Mockito for unit tests.
- **Integration testing:** Use Testcontainers for testing repository interactions with a real PostgreSQL database.

### 6. AI Integration Rules
- **Secure AI integration:** Use LangChain4j. Ensure role-scoped RAG retrieval (users can only query data they have access to).
- **Citations:** Always provide source citations in AI responses.
- **Rate limiting:** Apply strict rate limiting to AI endpoints.
- **No direct unrestricted database access by LLMs:** The AI must only access data through strictly defined, authorized RAG pipelines.

### 7. Coding Conventions

#### Java (Backend)
- **Java 25:** Leverage modern features.
- **Records:** Use `record` for DTOs and immutable data carriers.
- **Sealed classes:** Use sealed classes/interfaces for finite hierarchies and state machines.
- **Pattern matching:** Use switch expressions and pattern matching to simplify logic.
- **Immutability:** Prefer final fields and immutable collections wherever possible.

#### Spring Boot (Backend)
- **Spring Boot 3.x:** Adhere to the latest Spring Boot standards.
- **Constructor injection:** Use required args constructors (or Lombok `@RequiredArgsConstructor`) for dependency injection. Never use `@Autowired` on fields.
- **Spring Events:** Use `ApplicationEventPublisher` for cross-module communication.
- **Transaction management:** Use `@Transactional` explicitly at the service layer boundaries.
- **Error envelope:** Always return the standardized JSON error envelope for failures.

#### TypeScript (Frontend)
- **Strict mode:** Enable and enforce `strict: true` in `tsconfig.json`.
- **No `any`:** Avoid `any` completely. Use `unknown` or specific types.
- **Type Interfaces:** Ensure TypeScript interfaces and Zod schemas perfectly match the backend API contracts.

#### Next.js (Frontend)
- **App Router:** Use the Next.js App Router paradigm.
- **Server/Client components:** Clearly separate Server Components (data fetching) from Client Components (interactivity, using `"use client"`).
- **Tailwind CSS:** Use Tailwind for all styling.
- **API client interceptors:** Use Axios or fetch interceptors to seamlessly handle JWT inclusion and token refresh logic.

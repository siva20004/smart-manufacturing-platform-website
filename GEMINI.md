# GEMINI SYSTEM PROMPT & PROJECT INSTRUCTIONS

You are operating within the **Siva Machine Works Smart Manufacturing Platform** project. You must strictly follow the development rules, architectural patterns, and conventions of this project.

## Core Directives

1. **Architecture Compliance:**
   - Adhere strictly to the **Modular Monolith** architecture.
   - Maintain strict separation of concerns: `Controller → Service → Repository`.
   - Never expose Database Entities (JPA) via APIs; always map to `DTOs`.
   - Modules communicate internally via Spring Events, not direct service calls.
   - Avoid unnecessary microservices.

2. **Code Quality & Validation:**
   - Implement robust input validation (Jakarta Bean Validation for Java, Zod for TypeScript).
   - Ensure centralized exception handling using `@RestControllerAdvice` returning a standard error envelope.
   - Utilize Java 25 features (Records, Sealed classes, Pattern matching, Immutability).
   - Use Spring Boot 3.x with constructor injection and explicit transaction management.

3. **Security First:**
   - Use environment variables for all configuration. NO hardcoded secrets.
   - Enforce RBAC at the method level (`@PreAuthorize`) based on the role matrix.
   - Ensure audit logging for all critical, state-changing operations via AOP.
   - Secure file uploads (S3 presigned URLs, MIME validation, 500MB max limit).
   - Do not disable security configurations to make tests pass.

4. **Data & AI:**
   - Use Flyway for all database schema migrations.
   - AI integrations (LangChain4j) must strictly respect role-scoped RAG retrieval, provide citations, and enforce rate limiting.
   - The LLM/AI layer must NEVER have direct, unrestricted access to the database.

5. **Operational Integrity:**
   - Inspect existing code thoroughly before making any edits.
   - Never overwrite working functionality unnecessarily.
   - Implement real business logic; do not use fake logic.
   - Run relevant tests (JUnit 5, Mockito, Testcontainers) after code changes.
   - Do not mark tasks or features as complete until they are fully functional.
   - Explain major architectural changes before executing them.

Refer to `docs/10-development-rules.md` and `AGENTS.md` for extended guidelines.

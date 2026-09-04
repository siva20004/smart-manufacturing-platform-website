# Architecture & Technical Interview Guide

**1. Why this architecture?**  
We built a Modular Monolith with Spring Boot and Next.js, backed by PostgreSQL. This minimizes distributed complexity while enforcing strict domain separation, making the code maintainable, scalable, and operationally lightweight.

**2. Why modular monolith?**  
It provides the best of both worlds: the strict API boundaries and decoupled domain logic of microservices, but with the simplicity, in-memory speed, and transactional safety of a single deployed artifact.

**3. Why PostgreSQL?**  
It is the world's most advanced open-source relational database, offering absolute ACID compliance required for Inventory ledgers, plus native vector similarity search via `pgvector` for our AI RAG pipelines.

**4. Why PDM?**  
Product Data Management (PDM) is the single source of truth for engineering. Without PDM, CAD files and drawings scatter across hard drives, leading to manufacturing errors, superseded revisions being built, and massive financial waste.

**5. What is eBOM?**  
The Engineering Bill of Materials (eBOM) defines the product *as designed* by engineering. It includes the structural hierarchy of assemblies and parts but does not include manufacturing consumables or routing steps.

**6. What is mBOM?**  
The Manufacturing Bill of Materials (mBOM) defines the product *as built*. It takes the eBOM and adds packaging, grease, paint, routing locations, and assembly sequences necessary for the shop floor.

**7. Why is eBOM → mBOM transformation necessary?**  
Engineering designs for function; manufacturing optimizes for assembly. A single physical part might be split across different assembly stations, requiring a structural transformation from the pure engineering view to the physical routing view.

**8. How does ERP integrate with PDM?**  
ERP orchestrates the business (Sales, Finance, Inventory). It pulls the finalized, released eBOM/mBOM from PDM to calculate exact material requirements, costs, and lead times when a customer orders a machine.

**9. How does inventory interact with procurement?**  
When production demands materials (via mBOM explosion) that exceed available stock, the system flags a shortage and automatically generates a Purchase Request (PR) in the Procurement module to restock the inventory.

**10. How does production interact with BOM?**  
Production relies on the mBOM to issue routing instructions to work centers and to reserve/consume specific quantities of raw materials from the warehouse as assembly progresses.

**11. How is AI integrated?**  
AI is integrated directly into the Backend as an Assistant service. It answers natural language queries regarding production delays, inventory shortages, and engineering document summaries.

**12. How is AI prevented from accessing unauthorized data?**  
Through Role-Scoped RAG. The AI NEVER queries the database directly. The user queries the backend, the backend authenticates the user, fetches only the specific records/vectors the user's role permits, and passes *that bounded context* to the AI to synthesize an answer. 

**13. How is RBAC implemented?**  
Using Spring Security's `@PreAuthorize` annotations on every API controller. Users are issued JWTs containing specific roles (e.g., `ROLE_ENG_USER`, `ROLE_PROD_USER`). If a Production user tries to approve a PDM design, the system rejects it with a 403 Forbidden.

**14. How is auditability achieved?**  
Through immutable event logging. Critical actions (like approving a BOM or confirming a Sales Order) are intercepted by Spring AOP (Aspect-Oriented Programming) and persisted to an `audit_logs` table, recording the actor, action, timestamp, and payload.

**15. How would this scale globally?**  
The backend is completely stateless (JWT auth). We would deploy ECS containers behind Application Load Balancers across multiple AWS Regions, relying on Aurora PostgreSQL Global Databases for low-latency read replicas worldwide.

**16. How would you migrate this system to microservices if required?**  
Because the system is a *modular* monolith, the domain boundaries (e.g., `inventory`, `crm`) do not illegally share database tables or invoke each other's repositories. We would simply extract a domain package into a new Spring Boot app, replace internal Spring Events with an event bus (Kafka/RabbitMQ), and point it to its own database schema.

**17. How would you deploy it globally?**  
Using GitHub Actions to build and push Docker images to GHCR, then deploying via Infrastructure as Code (Terraform) to AWS ECS Fargate, leveraging Route 53 for latency-based DNS routing.

**18. What would you improve in a real enterprise implementation?**  
- Add OAuth2/OIDC integration (Okta/Active Directory).
- Implement Apache Kafka for async event sourcing instead of in-memory Spring Events.
- Add an API Gateway (Kong/Apigee) for rate limiting and WAF protection.
- Implement comprehensive APM tracing (Datadog/OpenTelemetry) across the stack.

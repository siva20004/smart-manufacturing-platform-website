# Development Roadmap
## Smart Manufacturing Digital Transformation Platform

### N. Development Phases

#### Phase 1: Foundation (Weeks 1-2)
**Goals**: Project setup, infrastructure, authentication, core entities

**Backend**:
- Spring Boot project initialization with Maven
- Module package structure setup
- PostgreSQL schema setup with Flyway migrations
- Shared kernel: Base entities, audit columns, exception handling
- Authentication: JWT login, refresh, logout
- RBAC: Users, Roles, Permissions, Spring Security integration
- Audit logging infrastructure (AOP-based)
- Health check and actuator endpoints
- Docker Compose: PostgreSQL, Redis, MinIO, Spring Boot
- OpenAPI/Swagger configuration
- Structured logging setup

**Frontend**:
- Next.js project initialization with TypeScript + Tailwind CSS
- Authentication pages (Login, session management)
- App layout with sidebar navigation
- Role-based UI rendering (hide/show based on permissions)
- API client setup with Axios/fetch + JWT interceptor
- Reusable components: DataTable, Form, Modal, Notification

**Milestones**:
- [ ] User can log in and see role-appropriate dashboard
- [ ] Docker Compose brings up entire stack with one command
- [ ] API documentation accessible at /swagger-ui
- [ ] Audit log captures all auth events

---

#### Phase 2: Core Business Modules (Weeks 3-5)
**Goals**: CRM, ERP/Sales Orders, Products, basic Inventory

**Backend**:
- Product master CRUD
- CRM: Customer, Contact, Opportunity, Quotation CRUD
- Quotation → Sales Order conversion
- Sales Order lifecycle (Draft → Confirmed → In Production → Shipped → Invoiced)
- Basic Inventory: Warehouse, Stock tracking
- Goods Receipt and Goods Issue
- Inventory reservation on SO confirmation
- Domain events: OrderConfirmedEvent, StockUpdatedEvent

**Frontend**:
- Product catalog UI
- CRM pages: Customer list/detail, Opportunity board, Quotation form
- Sales Order pages: List, Create, Detail with status workflow
- Inventory dashboard: Stock levels, warehouse view
- Goods Receipt / Issue forms

**Milestones**:
- [ ] Full CRM → Quotation → Sales Order flow working
- [ ] Inventory updates on Goods Receipt/Issue
- [ ] Sales Order confirmation reserves inventory

---

#### Phase 3: Engineering & BOM (Weeks 6-8)
**Goals**: PDM, eBOM, mBOM, eBOM→mBOM transformation

**Backend**:
- PDM: Document upload to S3/MinIO, version control, metadata
- Document search (full-text via PostgreSQL tsvector)
- Engineering Change Order workflow
- eBOM: Hierarchical BOM CRUD, multi-level tree structure
- eBOM versioning and revision management
- eBOM approval workflow
- mBOM: Structure derived from eBOM
- eBOM → mBOM transformation engine
  - Rule-based transformation (add consumables, substitute parts)
  - Routing/operation definition
  - Transformation audit trail
- mBOM approval and release workflow
- Domain events: EBOMApprovedEvent, MBOMReleasedEvent

**Frontend**:
- PDM: Document library, upload UI, version history
- eBOM: Tree view editor, BOM comparison view
- eBOM approval workflow UI
- mBOM: Tree view with manufacturing-specific fields
- eBOM→mBOM transformation wizard
- Transformation review and approval UI

**Milestones**:
- [ ] Upload document → attach to eBOM item → full traceability
- [ ] Create eBOM → approve → transform to mBOM → approve → release
- [ ] Transformation rules correctly add manufacturing items
- [ ] BOM tree renders correctly with unlimited depth

---

#### Phase 4: Production & Supply Chain (Weeks 9-11)
**Goals**: Procurement, Production, Quality

**Backend**:
- Simplified MRP: Material requirement calculation from mBOM + SO quantity
- Purchase Requisition generation for shortages
- Procurement: Vendor management, PO creation, PO approval
- PO → Goods Receipt matching
- Production: Work Order creation from SO + mBOM
- Work Order operations tracking
- Material consumption recording
- Production completion and output
- Quality: Inspection plans, Inspections, NCR
- Quality checkpoints integrated with production operations
- Shipment recording
- Invoice generation
- Domain events: WorkOrderCompletedEvent, InspectionPassedEvent, ShipmentCreatedEvent

**Frontend**:
- Procurement: Vendor list, PO creation/approval, PO tracking
- Production: Work Order list, Kanban/list view, operation progress
- Production dashboard: WIP, throughput metrics
- Quality: Inspection forms, NCR list, quality dashboard
- Shipment and Invoice pages

**Milestones**:
- [ ] Full Order-to-Cash flow: Customer → SO → BOM → Inventory → Procurement → Production → Quality → Shipment → Invoice
- [ ] MRP correctly identifies material shortages
- [ ] Quality gate blocks shipment of failed inspections

---

#### Phase 5: AI & Analytics (Weeks 12-14)
**Goals**: Business analytics, AI/RAG operations assistant

**Backend**:
- Analytics: Aggregation queries for Sales, Inventory, Production, Procurement
- Analytics API endpoints with date range filtering
- AI Document Processor: Text extraction from uploaded documents (Apache Tika)
- Chunking engine: Split documents into semantic chunks
- Embedding generation via OpenAI API
- pgvector setup and embedding storage
- RAG query pipeline: embed query → vector search → context assembly → LLM call
- Authorization-aware retrieval (filter by user's accessible modules)
- Source citation in responses
- Query logging and feedback mechanism
- LangChain4j integration

**Frontend**:
- Analytics dashboards: Sales (revenue, pipeline), Inventory (turnover, aging), Production (OEE, throughput)
- Dashboard widgets with charts (Chart.js or Recharts)
- AI Assistant: Chat interface with streaming responses
- Source citation display with links to original documents
- Query history sidebar
- Feedback buttons (thumbs up/down)

**Milestones**:
- [ ] Analytics dashboards showing real-time business metrics
- [ ] AI Assistant answers manufacturing questions with source citations
- [ ] AI respects RBAC — users only get answers from authorized data
- [ ] Document processing pipeline: upload → extract → chunk → embed → searchable

---

#### Phase 6: Polish & Deployment (Weeks 15-16)
**Goals**: CI/CD, cloud deployment, documentation, final testing

**Backend**:
- Comprehensive unit tests (80%+ coverage on service layer)
- Integration tests with Testcontainers
- API contract tests
- Performance testing for critical endpoints
- Security hardening review
- Dockerfile optimization (multi-stage build)
- GitHub Actions CI pipeline: lint → test → build → Docker → push

**Frontend**:
- UI polish and responsive design review
- Error handling and loading states
- Component tests with React Testing Library
- Accessibility review
- Production build optimization

**Infrastructure**:
- AWS infrastructure setup (ECS, RDS, S3, ElastiCache, CloudFront)
- GitHub Actions CD pipeline: deploy to staging → deploy to production
- Environment configuration management
- Monitoring and alerting setup (CloudWatch)
- Final documentation update

**Milestones**:
- [ ] All tests passing in CI
- [ ] One-command local setup via Docker Compose
- [ ] Staging environment deployed on AWS
- [ ] README with full setup instructions
- [ ] Architecture documentation complete

---

### N.2 Phase Summary Table

| Phase | Name | Duration | Key Deliverables | Dependencies |
|---|---|---|---|---|
| 1 | Foundation | Weeks 1-2 | Auth, RBAC, Audit, Docker, Project skeleton | None |
| 2 | Core Business | Weeks 3-5 | CRM, ERP, Sales Orders, Inventory | Phase 1 |
| 3 | Engineering & BOM | Weeks 6-8 | PDM, eBOM, mBOM, Transformation | Phase 1 |
| 4 | Production & SCM | Weeks 9-11 | Procurement, Production, Quality, MRP | Phase 2, 3 |
| 5 | AI & Analytics | Weeks 12-14 | Dashboards, RAG Assistant, Doc Processing | Phase 2, 3, 4 |
| 6 | Polish & Deploy | Weeks 15-16 | CI/CD, AWS, Testing, Documentation | All |

Note that Phase 2 and Phase 3 can be worked on in parallel.

---

### O. Risks and Trade-offs

| # | Risk | Impact | Probability | Mitigation |
|---|---|---|---|---|
| 1 | Scope creep — too many modules for a portfolio project | High | High | Strict phase gates. Each module has MVP scope defined. Cut features, not quality. |
| 2 | BOM transformation complexity | Medium | Medium | Start with simple rule-based transformation. Avoid over-engineering. Support manual override. |
| 3 | AI/RAG accuracy and hallucination | Medium | Medium | Clear source citations. Disclaimer on AI responses. Feedback loop for improvement. |
| 4 | Performance with complex BOM trees | Medium | Low | Lazy loading for BOM trees. Pagination. Database indexing. Limit demo data to realistic sizes. |
| 5 | LLM API costs during development | Low | High | Use mock LLM responses for tests. Cache embeddings. Use smaller models during dev. |
| 6 | Security vulnerabilities | High | Low | OWASP checklist. Spring Security defaults. Input validation. Code review. |
| 7 | Docker/AWS deployment complexity | Medium | Medium | Start with Docker Compose locally. AWS deployment is Phase 6. Use Infrastructure as Code. |
| 8 | Modular monolith coupling | Medium | Medium | Enforce module boundaries via code reviews. No cross-module direct DB access. Events only. |
| 9 | Frontend-backend integration issues | Medium | Medium | OpenAPI contract-first. TypeScript types generated from API spec. Integration tests. |
| 10 | Data modeling errors discovered late | High | Low | Design DB schema upfront. Use Flyway migrations. Review schema before coding. |

#### Key Trade-offs

1. **Modular Monolith vs. Microservices**
   - **Chose**: Modular Monolith
   - **Reason**: Single developer project. Simpler deployment, debugging, and transactions. Same domain boundaries can be extracted to microservices later.
   - **Trade-off**: Less impressive from a distributed systems perspective, but more realistic and practical.

2. **PostgreSQL for Everything vs. Specialized Databases**
   - **Chose**: PostgreSQL (relational + pgvector + full-text search)
   - **Reason**: Reduces operational complexity. PostgreSQL is extremely capable. pgvector eliminates need for separate vector DB.
   - **Trade-off**: May not scale as well as dedicated vector DB for very large embedding collections, but sufficient for portfolio scope.

3. **Spring Events vs. Message Broker**
   - **Chose**: Spring ApplicationEvents (in-process)
   - **Reason**: No need for eventual consistency in a monolith. Simpler. Synchronous within the same transaction when needed.
   - **Trade-off**: Cannot distribute events across services. Acceptable for current architecture.

4. **Simplified MRP vs. Full MRP**
   - **Chose**: Simplified MRP (single-level, on-demand)
   - **Reason**: Full MRP (MRP II, time-phased) is extremely complex. Simplified version demonstrates the concept without months of development.
   - **Trade-off**: Not production-ready MRP, but demonstrates the integration pattern.

5. **JWT vs. Session-based Auth**
   - **Chose**: JWT with Redis-backed refresh tokens
   - **Reason**: Stateless API servers. Industry standard for SPAs. Refresh token in Redis enables revocation.
   - **Trade-off**: JWT tokens cannot be invalidated before expiry (mitigated by short expiry + refresh flow).

6. **Seed Data vs. Empty Database**
   - **Chose**: Rich seed data with realistic Japanese manufacturing context
   - **Reason**: Portfolio project must be demo-ready. Empty systems don't impress.
   - **Trade-off**: Seed data maintenance overhead. Must keep seed data consistent across modules.

---

### O.2 Open Considerations

The following items are intentionally deferred or simplified for the portfolio scope:
- Multi-tenant architecture (not needed for single company)
- Internationalization (simplified to English with Japanese context)
- Real-time notifications (WebSocket) — can be added later
- Advanced reporting (OLAP cube, data warehouse) — analytics module uses direct queries
- Mobile app — responsive web design covers mobile use cases
- Advanced workflow engine (BPMN) — simplified status-based workflows sufficient

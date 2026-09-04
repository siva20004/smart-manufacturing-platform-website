# Business Requirements
## Smart Manufacturing Digital Transformation Platform

### A. Business Objectives

1. **Unify all business operations onto a single cloud platform replacing legacy systems**: Eliminate data silos by transitioning from fragmented legacy tools to a unified, cohesive cloud-based ERP ecosystem.
2. **Establish a digital thread from customer order through engineering, procurement, production, quality, to shipment**: Ensure end-to-end traceability and visibility of product data and lifecycle across all business units.
3. **Automate eBOM to mBOM transformation reducing manual errors and cycle time**: Streamline the handover from engineering to manufacturing by automating bill of materials translation and augmenting it with manufacturing context.
4. **Enable data-driven decision making through real-time analytics dashboards**: Provide actionable insights across all operational tiers via unified and highly customizable performance metrics and KPIs.
5. **Deploy AI-powered operations assistant for manufacturing knowledge retrieval**: Leverage RAG (Retrieval-Augmented Generation) technology to empower employees with instant, context-aware answers derived from corporate documents and data.
6. **Implement enterprise-grade security with role-based access and full audit trails**: Protect intellectual property and sensitive customer data with robust authentication, fine-grained RBAC, and immutable audit logging.
7. **Achieve cloud-native deployment with automated CI/CD pipelines**: Guarantee rapid, reliable, and consistent software delivery through modern DevOps practices and containerization.
8. **Demonstrate integration patterns common in enterprise manufacturing IT**: Showcase best practices in service boundaries, event-driven architectures, and API design tailored to manufacturing constraints.
9. **Reduce order-to-delivery cycle time by 60%**: Optimize the entire value chain through automated workflows, instant data availability, and proactive supply chain management.
10. **Build a scalable foundation that could evolve from modular monolith to microservices**: Architect the system with clear module boundaries and interfaces to allow seamless future decomposition as scale demands.

### B. User Personas

| Persona | Role | Department | Key Activities | System Modules Used |
| :--- | :--- | :--- | :--- | :--- |
| **Sales Representative** | Individual Contributor | Sales | Manage customer relations, draft quotes, enter sales orders | CRM, ERP Core |
| **Sales Manager** | Manager | Sales | Review pipelines, approve quotes/orders, analyze sales performance | CRM, ERP Core, ANA |
| **Design Engineer** | Individual Contributor | Engineering | Create product designs, upload CAD files, build eBOMs, initiate ECOs | PDM, BOM (eBOM) |
| **Engineering Manager** | Manager | Engineering | Approve eBOMs, review eBOM→mBOM handover, oversee PLM | PDM, BOM, AIR |
| **Production Planner** | Individual Contributor | Production | Consume mBOMs, generate work orders, schedule shop floor activities | BOM (mBOM), PRD, INV |
| **Shop Floor Operator** | Individual Contributor | Production | Execute work orders, log production time/materials, perform in-process quality checks | PRD, QMS, AIR |
| **Procurement Officer** | Individual Contributor | Supply Chain | Process purchase requisitions, issue RFQs, create purchase orders, manage vendors | SCM, INV |
| **Warehouse Manager** | Manager | Supply Chain | Manage inventory, process goods receipts/issues, conduct cycle counts | INV, SCM |
| **Quality Inspector** | Individual Contributor | Quality Control | Perform material and final inspections, log non-conformance, track corrective actions | QMS, PRD, INV |
| **Finance Clerk** | Individual Contributor | Finance | Generate invoices, track payments, monitor cash flow | ERP Core, SCM |
| **Plant Manager** | Executive | Management | Monitor production KPIs, oversee plant efficiency, query AI for operational insights | ANA, PRD, QMS, AIR |
| **IT Administrator** | System Admin | IT | Manage users/roles, configure system settings, monitor health and audit logs | ADM |
| **Executive (C-Suite)** | Executive | Management | View cross-functional dashboards, drive strategic decisions based on data and AI insights | ANA, AIR |

### C. Functional Requirements

#### 1. CRM Module
*   Customer master management (company, contacts, addresses)
*   Opportunity tracking with pipeline stages
*   Quotation generation linked to product catalog
*   Quotation to Sales Order conversion
*   Customer interaction history
*   Customer analytics (revenue, order history)

#### 2. ERP Core / Sales Order Module
*   Sales Order creation, editing, approval workflow
*   SO line items linked to product/BOM
*   Order status tracking (Draft → Confirmed → In Production → Shipped → Invoiced)
*   Automatic inventory reservation on confirmation
*   Integration with Production and Procurement modules
*   Invoice generation from shipped orders

#### 3. PDM / PLM Module
*   Document upload and version control (CAD files, specs, drawings)
*   Document metadata management (part number, revision, status)
*   Document categorization and tagging
*   Engineering Change Order (ECO) workflow
*   Document search (full-text + metadata)
*   S3-compatible storage integration
*   Access control per document/project

#### 4. Engineering BOM (eBOM) Module
*   Hierarchical eBOM creation (parent-child structure)
*   Multi-level BOM with unlimited depth
*   BOM versioning and revision history
*   Link BOM items to PDM documents
*   BOM comparison (diff between revisions)
*   Engineering approval workflow
*   BOM export capabilities

#### 5. Manufacturing BOM (mBOM) Module
*   mBOM derived from eBOM via transformation
*   Manufacturing-specific additions (consumables, packaging, tooling)
*   Routing/operation sequence definition
*   Work center assignment
*   mBOM versioning independent of eBOM
*   Integration with Production module

#### 6. eBOM → mBOM Transformation
*   Rule-based transformation engine
*   Add manufacturing-specific items (welding wire, lubricant, fixtures)
*   Substitute engineering parts with manufacturing equivalents
*   Define routing steps and operations
*   Transformation review and approval workflow
*   Transformation audit trail
*   Support for partial transformation (incremental)

#### 7. Inventory Management Module
*   Multi-warehouse inventory tracking
*   Real-time stock levels (on-hand, reserved, available)
*   Goods Receipt processing
*   Goods Issue processing
*   Stock transfer between warehouses
*   Cycle count and inventory adjustment
*   Minimum stock / reorder point alerts
*   Inventory valuation

#### 8. Procurement / SCM Module
*   Vendor/Supplier master management
*   Purchase Requisition from MRP or manual
*   RFQ process (optional, simplified)
*   Purchase Order creation and approval
*   PO to Goods Receipt matching
*   Vendor performance tracking
*   Material Requirement Planning (simplified MRP)

#### 9. Production Management Module
*   Work Order creation from Sales Order + mBOM
*   Work Order scheduling (simple Gantt or list view)
*   Operation tracking (status per routing step)
*   Material consumption recording
*   Production completion and output recording
*   Quality checkpoint integration
*   Production dashboard (WIP, throughput, efficiency)

#### 10. Quality Management Module
*   Inspection plan definition
*   Incoming material inspection
*   In-process quality checks
*   Final inspection before shipment
*   Non-conformance report (NCR) creation
*   Corrective action tracking
*   Quality metrics dashboard

#### 11. Business Analytics Module
*   Sales analytics (revenue, pipeline, conversion)
*   Inventory analytics (turnover, aging, accuracy)
*   Production analytics (OEE, throughput, yield)
*   Procurement analytics (spend, lead time, vendor rating)
*   Executive dashboard with KPIs
*   Configurable date range filters
*   Export to CSV/PDF

#### 12. AI / RAG Module
*   Manufacturing Operations Assistant chatbot
*   RAG-based retrieval from: PDM documents, production records, quality records, BOM data
*   Document processing: extract text from uploaded PDFs/documents
*   Generate embeddings and store in vector database (pgvector)
*   Context-aware query with source citations
*   Role-based data access in AI queries (users only see what they're authorized to see)
*   Query history and feedback mechanism

#### 13. Administration Module
*   User management (create, update, deactivate)
*   Role management with granular permissions
*   RBAC enforcement across all modules
*   Audit log viewer with filters
*   System health monitoring
*   Configuration management

### D. Non-Functional Requirements

#### 1. Performance
*   API response < 500ms for standard operations
*   Dashboard load < 3s
*   Support 100 concurrent users
*   Pagination for large datasets

#### 2. Security
*   JWT-based authentication with refresh tokens
*   RBAC with module-level and action-level permissions
*   All API endpoints authenticated (except health check)
*   Input validation and SQL injection prevention
*   CORS configuration
*   Secrets management via environment variables
*   HTTPS in production

#### 3. Reliability
*   Graceful error handling with meaningful error messages
*   Database transaction management
*   Idempotent API operations where applicable
*   Data backup strategy

#### 4. Scalability
*   Modular monolith with clean boundaries enabling future decomposition
*   Stateless application tier (session in Redis)
*   Horizontal scaling capability via container orchestration
*   Database connection pooling

#### 5. Maintainability
*   Clean code with consistent coding standards
*   Comprehensive API documentation (OpenAPI/Swagger)
*   Module-level separation of concerns
*   Structured logging with correlation IDs

#### 6. Compliance & Audit
*   Every data mutation logged with who/what/when
*   Immutable audit log
*   Data retention policies
*   GDPR-aware design (data export, deletion capability)

#### 7. Observability
*   Health check endpoints
*   Application metrics via Spring Actuator
*   Structured JSON logging
*   Request tracing with correlation IDs

### E. Business Modules Summary

| Module Name | Module Code | Description | Priority |
| :--- | :--- | :--- | :--- |
| ERP Core / Sales | ERP | Central hub for sales orders, invoicing, and cross-functional linkages | Core |
| BOM Management | BOM | Engine for managing engineering (eBOM) and manufacturing (mBOM) structures | Core |
| Inventory | INV | Real-time tracking of stock across multiple warehouses | Core |
| Production | PRD | Work order execution, shop floor tracking, and production scheduling | Core |
| Administration | ADM | User management, RBAC, system config, and audit | Core |
| CRM | CRM | Customer relationship, opportunity, and quotation management | Important |
| Supply Chain | SCM | Procurement, vendor management, and simplified MRP | Important |
| PDM / PLM | PDM | Product data management, CAD storage, and ECO workflows | Important |
| Quality Management | QMS | Inspection plans, quality checks, and non-conformance tracking | Important |
| Analytics | ANA | Data-driven dashboards and cross-functional reporting | Enhancement |
| AI / RAG | AIR | AI operations assistant with document and data retrieval | Enhancement |

### F. Business Workflows

#### Workflow 1: Order-to-Cash (Main Business Flow)

**Flow:** Customer Inquiry → CRM Opportunity → Quotation → Sales Order → BOM Resolution → Inventory Check → Material Requirement → Procurement (if shortage) → Production Work Order → Manufacturing Execution → Quality Inspection → Goods Issue → Shipment → Invoice → Payment

*   **Step 1: Inquiry to Quote**
    *   *Actor*: Sales Representative
    *   *System Module*: CRM
    *   *Input*: Customer details, product requirements
    *   *Output*: Formal Quotation
    *   *Business Rules*: Quote pricing must respect product catalog margins.
*   **Step 2: Order Creation**
    *   *Actor*: Sales Manager
    *   *System Module*: ERP Core
    *   *Input*: Approved Quotation
    *   *Output*: Confirmed Sales Order
    *   *Business Rules*: Order confirmation triggers inventory reservation.
*   **Step 3: Material Planning & Procurement**
    *   *Actor*: Procurement Officer / Production Planner
    *   *System Module*: SCM, INV
    *   *Input*: SO material requirements vs. On-hand inventory
    *   *Output*: Purchase Requisitions/Orders (if shortages exist)
    *   *Business Rules*: POs require managerial approval above predefined thresholds.
*   **Step 4: Production Execution**
    *   *Actor*: Shop Floor Operator
    *   *System Module*: PRD
    *   *Input*: Work Order (derived from SO and mBOM)
    *   *Output*: Finished Goods
    *   *Business Rules*: Materials must be consumed sequentially per routing.
*   **Step 5: Quality & Fulfillment**
    *   *Actor*: Quality Inspector / Warehouse Manager / Finance Clerk
    *   *System Module*: QMS, INV, ERP Core
    *   *Input*: Finished Goods
    *   *Output*: Inspection report, Goods Issue, Shipment, Invoice
    *   *Business Rules*: Shipment is blocked if final quality inspection fails.

#### Workflow 2: Engineering-to-Manufacturing (BOM Transformation)

**Flow:** Product Design → PDM Document Upload → eBOM Creation → Multi-level BOM Assembly → Engineering Review & Approval → eBOM → mBOM Transformation → Add Manufacturing Items → Define Routing & Operations → mBOM Review & Approval → mBOM Available for Production

*   **Step 1: Product Definition**
    *   *Actor*: Design Engineer
    *   *System Module*: PDM, BOM (eBOM)
    *   *Input*: CAD files, engineering specs
    *   *Output*: Draft eBOM linked to documents
    *   *Business Rules*: Every eBOM component must have a valid part number.
*   **Step 2: Engineering Approval**
    *   *Actor*: Engineering Manager
    *   *System Module*: BOM (eBOM)
    *   *Input*: Draft eBOM
    *   *Output*: Approved/Released eBOM
    *   *Business Rules*: Changes post-approval require a formal Engineering Change Order (ECO).
*   **Step 3: Transformation to mBOM**
    *   *Actor*: Production Planner / Manufacturing Engineer
    *   *System Module*: BOM (mBOM)
    *   *Input*: Released eBOM
    *   *Output*: Draft mBOM (including consumables and routings)
    *   *Business Rules*: eBOM structural integrity must be maintained during mapping.
*   **Step 4: Manufacturing Release**
    *   *Actor*: Engineering Manager / Production Manager
    *   *System Module*: BOM (mBOM)
    *   *Input*: Draft mBOM
    *   *Output*: Released mBOM
    *   *Business Rules*: Released mBOM becomes active for new Work Orders.

#### Workflow 3: AI-Powered Operations Assistant

**Flow:** User Query → Authentication & Authorization Check → Query Analysis → Authorized Data Scope Determination → Vector Search (pgvector) → Document Retrieval → Context Assembly → LLM Generation → Response with Source Citations → User Feedback

*   **Step 1: Query Processing**
    *   *Actor*: Any authorized user (e.g., Plant Manager)
    *   *System Module*: AIR, ADM
    *   *Input*: Natural language question (e.g., "What is the standard tolerance for part X?")
    *   *Output*: Authenticated query context
    *   *Business Rules*: System checks user's RBAC profile to restrict data access.
*   **Step 2: Information Retrieval**
    *   *Actor*: System (RAG Engine)
    *   *System Module*: AIR
    *   *Input*: User query embeddings
    *   *Output*: Relevant document snippets from pgvector
    *   *Business Rules*: Only retrieve documents the user is explicitly authorized to view.
*   **Step 3: Response Generation**
    *   *Actor*: System (LLM)
    *   *System Module*: AIR
    *   *Input*: Query + Retrieved Context
    *   *Output*: Generated answer with citations
    *   *Business Rules*: The LLM must explicitly cite the source documents used in the response.
*   **Step 4: Feedback Loop**
    *   *Actor*: User
    *   *System Module*: AIR
    *   *Input*: Thumbs up/down, optional text
    *   *Output*: Feedback logged in DB
    *   *Business Rules*: Negative feedback flags the query for administrator review to improve RAG accuracy.

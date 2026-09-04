# Smart Manufacturing Digital Transformation Platform
## Siva Machine Works — Enterprise Digital Transformation

### 1. Executive Summary
- Siva Machine Works is a fictional global industrial machinery manufacturer headquartered in Osaka, Japan, with operations across Asia, Europe, and North America
- The company manufactures precision CNC machines, hydraulic press systems, and industrial automation equipment
- This platform simulates a full enterprise IT transformation: replacing legacy siloed systems with a unified cloud-native platform
- Covers ERP, SCM, PDM/PLM, CRM, Production, Quality, and AI-powered operations assistant
- Demonstrates practical enterprise systems engineering competence for manufacturing domain

### 2. Company Profile (Fictional)
- **Company**: Siva Machine Works Co., Ltd. (シヴァ・マシン・ワークス株式会社)
- **Founded**: 1968
- **Headquarters**: Osaka, Japan
- **Employees**: ~2,500 globally
- **Revenue**: ¥45 billion annually
- **Products**: CNC Machining Centers, Hydraulic Press Systems, Industrial Robots, Automation Controllers
- **Facilities**: 3 manufacturing plants (Osaka, Nagoya, Penang), 5 regional offices
- **Customers**: Automotive OEMs, Aerospace suppliers, Electronics manufacturers, General industrial

### 3. Business Problem Statement
Siva Machine Works currently operates with:
- A legacy on-premise ERP (custom-built, 15+ years old)
- Disconnected departmental systems (spreadsheet-based BOM management, paper-based quality records)
- No unified customer management — sales teams use local tools
- Engineering documents stored on shared network drives with no version control
- No analytics capability beyond basic reporting
- Manual processes for eBOM to mBOM transformation prone to errors
- No AI/ML capabilities for operational intelligence

This results in:
- 3-5 day delays in order-to-production handoff
- 15% inventory inaccuracy
- Frequent BOM discrepancies between engineering and manufacturing
- Limited visibility into cross-functional operations
- Inability to scale operations efficiently

### 4. Project Vision
Deliver a unified, cloud-native digital platform that:
1. Integrates all core business functions (ERP, SCM, CRM, PDM/PLM, Production)
2. Automates the engineering-to-manufacturing BOM transformation
3. Provides real-time operational visibility through analytics dashboards
4. Leverages AI/RAG for intelligent document retrieval and operations assistance
5. Implements enterprise-grade security with RBAC and audit logging
6. Runs on cloud infrastructure with CI/CD automation

### 5. Scope
#### In Scope
- Cloud ERP core (Sales Orders, Purchase Orders, Invoicing)
- Supply Chain Management & Procurement
- Inventory Management (multi-warehouse)
- Production Management (Work Orders, Routing, Shop Floor)
- PDM/PLM (Document management, CAD metadata, Revision control)
- Engineering BOM (eBOM) management
- Manufacturing BOM (mBOM) management
- eBOM → mBOM transformation engine
- CRM (Customer management, Opportunities, Quotations)
- Business Analytics dashboards
- AI-powered Manufacturing Operations Assistant (RAG-based)
- AI Document Processing (extraction, classification)
- Authentication & Authorization (JWT + RBAC)
- Comprehensive audit logging
- Docker containerization
- CI/CD with GitHub Actions
- AWS cloud deployment

#### Out of Scope
- Actual CAD file rendering/editing
- Real-time IoT/SCADA integration
- Multi-currency/multi-language (simplified to JPY + English/Japanese labels)
- Full accounting/GL module (simplified invoicing only)
- Mobile native apps (responsive web only)

### 6. Technology Stack

| Layer | Technology |
|---|---|
| Frontend | Next.js, TypeScript, Tailwind CSS |
| Backend | Java 25, Spring Boot 3.x, Maven |
| Database | PostgreSQL 16 |
| Object Storage | S3-compatible (MinIO for local, AWS S3 for cloud) |
| AI/ML | OpenAI API (or compatible), pgvector, LangChain4j |
| Search | PostgreSQL full-text search + pgvector |
| Caching | Redis (session + cache) |
| Messaging | Spring Events (in-process, modular monolith) |
| Auth | Spring Security + JWT |
| Containerization | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Cloud | AWS (ECS/Fargate, RDS, S3, CloudFront) |
| Monitoring | Spring Actuator, structured logging |

### 7. Architecture Philosophy
- **Modular Monolith**: Start with a single deployable unit with clean module boundaries
- **Domain-Driven Design**: Each business module is a bounded context
- **Event-Driven Internal Communication**: Modules communicate through domain events
- **API-First**: REST APIs with OpenAPI documentation
- **Security by Default**: Every endpoint is authenticated and authorized
- **Cloud-Ready**: 12-factor app principles, environment-based configuration
- **Pragmatic AI**: AI features solve real business problems, not technology for technology's sake

### 8. Key Stakeholders (Fictional)

| Role | Name | Responsibility |
|---|---|---|
| CEO | Tanaka Hiroshi | Executive sponsor |
| CTO | Yamamoto Kenji | Technology strategy |
| VP Engineering | Suzuki Akira | Engineering & PLM |
| VP Manufacturing | Nakamura Yuki | Production & Quality |
| VP Sales | Watanabe Mei | CRM & Sales Operations |
| IT Director | Kimura Takeshi | Platform delivery |
| Head of Procurement | Ishida Rina | SCM & Vendor Management |

### 9. Success Metrics
- Order-to-production handoff reduced from 3-5 days to < 1 day
- Inventory accuracy improved from 85% to 98%+
- eBOM to mBOM transformation time reduced from 2 days to < 2 hours
- Single source of truth for all business data
- AI assistant response accuracy > 85% for manufacturing queries
- 100% audit trail coverage for compliance
- Zero-downtime deployments via CI/CD

### 10. Document Index
| Document | Description |
|---|---|
| 01-project-overview.md | This document |
| 02-business-requirements.md | User personas, functional & non-functional requirements |
| 03-system-architecture.md | Technical architecture, database, API, AI, security design |
| 04-development-roadmap.md | Phased development plan, milestones, risks |

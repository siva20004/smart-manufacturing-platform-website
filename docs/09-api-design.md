# API Design Specification - Smart Manufacturing Digital Transformation Platform

## 1. Introduction
This document defines the REST API specification for Siva Machine Works' Smart Manufacturing Digital Transformation Platform. 

## 2. API Standards and Conventions

### 2.1 Base Path
All API endpoints are prefixed with `/api/v1/`.

### 2.2 Standard Request & Response Envelopes
All responses follow a standard envelope format to ensure consistency.

**Success Response Envelope:**
```json
{
  "success": true,
  "timestamp": "2026-09-02T10:00:00Z",
  "data": { ... },
  "pagination": {
    "page": 1,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

**Error Response Envelope:**
```json
{
  "success": false,
  "timestamp": "2026-09-02T10:00:00Z",
  "error": {
    "code": "DOMAIN_ERR_001",
    "message": "Human readable error message",
    "details": [
      {
        "field": "username",
        "issue": "must not be blank"
      }
    ]
  },
  "correlationId": "abc-123-def"
}
```

### 2.3 Global Error Handling & HTTP Status Codes
- **200 OK**: Success.
- **201 Created**: Resource created successfully.
- **202 Accepted**: Request accepted for processing (async).
- **204 No Content**: Successful update/delete with no response body.
- **400 Bad Request**: Validation error, missing required fields.
- **401 Unauthorized**: Missing or invalid authentication token.
- **403 Forbidden**: Authenticated user lacks required permissions.
- **404 Not Found**: Resource does not exist.
- **409 Conflict**: State transition not allowed, unique constraint violation, optimistic lock failure.
- **422 Unprocessable Entity**: Business rule violation (e.g., eBOM not approved).
- **500 Internal Server Error**: Unexpected server error.

### 2.4 Optimistic Concurrency Control
All entities support optimistic concurrency control via a `version` field.
**Request Header:** `If-Match: <version>`
If the version in the database is newer, a `409 Conflict` is returned.

### 2.5 Security & Rate Limiting
- **Authentication**: JWT Bearer Tokens required (`Authorization: Bearer <token>`).
- **Rate Limiting**: Applied per tenant/IP (e.g., 100 requests per minute).

---

## 3. Endpoints by Domain

### 3.1 Auth (`/auth`)

**POST /api/v1/auth/login**
- **Purpose**: Authenticate user and issue JWT token.
- **Auth**: No
- **Role**: N/A
- **Request DTO**: 
  ```json
  { "username": "admin", "password": "password123" }
  ```
- **Response DTO**: 
  ```json
  { "accessToken": "eyJ...", "refreshToken": "eyJ...", "expiresIn": 3600 }
  ```

**POST /api/v1/auth/refresh**
- **Purpose**: Get a new access token using a refresh token.
- **Auth**: Yes
- **Role**: N/A

**POST /api/v1/auth/logout**
- **Purpose**: Invalidate the current session/token.
- **Auth**: Yes
- **Role**: N/A

**GET /api/v1/auth/me**
- **Purpose**: Get the current authenticated user's profile and permissions.
- **Auth**: Yes
- **Role**: N/A

**PUT /api/v1/auth/change-password**
- **Purpose**: Update current user's password.
- **Auth**: Yes
- **Role**: N/A

---

### 3.2 Users (`/users`)

**GET /api/v1/users**
- **Purpose**: List all users.
- **Auth**: Yes
- **Role**: `ADMIN`
- **Query Params**: `page`, `size`, `role`, `status`

**POST /api/v1/users**
- **Purpose**: Create a new user.
- **Auth**: Yes
- **Role**: `ADMIN`

**PUT /api/v1/users/{id}**
- **Purpose**: Update an existing user.
- **Auth**: Yes
- **Role**: `ADMIN`

**PUT /api/v1/users/{id}/deactivate**
- **Purpose**: Soft delete/deactivate a user.
- **Auth**: Yes
- **Role**: `ADMIN`

**PUT /api/v1/users/{id}/assign-roles**
- **Purpose**: Update user roles.
- **Auth**: Yes
- **Role**: `ADMIN`

---

### 3.3 Customers (`/customers`)

**GET /api/v1/customers**
- **Purpose**: List customers.
- **Auth**: Yes
- **Role**: `SALES_REP`, `SALES_MANAGER`, `ADMIN`
- **Query Params**: `page`, `size`, `search`, `status`

**GET /api/v1/customers/{id}**
- **Purpose**: Get customer details.
- **Auth**: Yes
- **Role**: `SALES_REP`, `SALES_MANAGER`

**POST /api/v1/customers**
- **Purpose**: Create a new customer.
- **Auth**: Yes
- **Role**: `SALES_MANAGER`, `ADMIN`
- **Request DTO**: `name`, `taxId`, `currency`, `billingAddress`

**PUT /api/v1/customers/{id}**
- **Purpose**: Update customer.
- **Auth**: Yes
- **Role**: `SALES_MANAGER`
- **Headers**: `If-Match`

**PUT /api/v1/customers/{id}/activate**
- **Purpose**: Change customer status to active.
- **Auth**: Yes
- **Role**: `SALES_MANAGER`

**PUT /api/v1/customers/{id}/hold**
- **Purpose**: Place customer on credit/financial hold.
- **Auth**: Yes
- **Role**: `FINANCE_MANAGER`

**PUT /api/v1/customers/{id}/credit-limit**
- **Purpose**: Update customer credit limit.
- **Auth**: Yes
- **Role**: `FINANCE_MANAGER`

**GET, POST, PUT /api/v1/customers/{id}/contacts**
- **Purpose**: Manage customer contacts.
- **Auth**: Yes
- **Role**: `SALES_REP`

---

### 3.4 Products (`/products`)

**GET /api/v1/products**
- **Purpose**: List standard products/parts.
- **Auth**: Yes
- **Role**: Any

**GET /api/v1/products/{id}**
- **Purpose**: Get product details.
- **Auth**: Yes
- **Role**: Any

**POST /api/v1/products**
- **Purpose**: Create a new product.
- **Auth**: Yes
- **Role**: `ENGINEERING_MANAGER`

**PUT /api/v1/products/{id}**
- **Purpose**: Update product metadata.
- **Auth**: Yes
- **Role**: `ENGINEERING_MANAGER`
- **Headers**: `If-Match`

**GET /api/v1/products/{id}/cost-rollup**
- **Purpose**: Perform multi-level cost rollup based on active BOM and routings.
- **Auth**: Yes
- **Role**: `FINANCE_MANAGER`, `ENGINEERING_MANAGER`

**GET /api/v1/products/{id}/active-bom**
- **Purpose**: Fetch the currently active/released manufacturing BOM.
- **Auth**: Yes
- **Role**: Any

**GET, POST, PUT /api/v1/products/{id}/revisions**
- **Purpose**: Manage product revisions (list, create, release).
- **Auth**: Yes
- **Role**: `ENGINEERING_MANAGER`

---

### 3.5 PDM (Product Data Management) (`/pdm`)

**GET /api/v1/pdm/documents**
- **Purpose**: List engineering documents (drawings, specs).
- **Auth**: Yes
- **Role**: Any

**POST /api/v1/pdm/documents/upload**
- **Purpose**: Upload document metadata and get presigned S3 URL for file upload.
- **Auth**: Yes
- **Role**: `ENGINEER`

**GET /api/v1/pdm/documents/{id}/download**
- **Purpose**: Get temporary presigned URL to download document.
- **Auth**: Yes
- **Role**: Any

**GET /api/v1/pdm/documents/{id}/versions**
- **Purpose**: List document versions.
- **Auth**: Yes
- **Role**: Any

**PUT /api/v1/pdm/documents/{id}/approve**
- **Purpose**: Approve a document version.
- **Auth**: Yes
- **Role**: `ENGINEERING_MANAGER`

**PUT /api/v1/pdm/documents/{id}/release**
- **Purpose**: Release an approved document.
- **Auth**: Yes
- **Role**: `ENGINEERING_MANAGER`

**GET, POST, PUT /api/v1/pdm/ecos**
- **Purpose**: Engineering Change Orders (list, create, approve, implement).
- **Auth**: Yes
- **Role**: `ENGINEERING_MANAGER`

---

### 3.6 BOM (Bill of Materials) (`/bom`)

**GET, POST /api/v1/bom/ebom**
- **Purpose**: Manage Engineering BOMs (list, create).
- **Auth**: Yes
- **Role**: `ENGINEER`

**GET /api/v1/bom/ebom/{id}/tree**
- **Purpose**: Get hierarchical eBOM structure.
- **Auth**: Yes
- **Role**: Any

**POST /api/v1/bom/ebom/{id}/items**
- **Purpose**: Add items to an eBOM.
- **Auth**: Yes
- **Role**: `ENGINEER`

**PUT /api/v1/bom/ebom/{id}/approve**
- **Purpose**: Approve eBOM.
- **Auth**: Yes
- **Role**: `ENGINEERING_MANAGER`

**GET /api/v1/bom/ebom/{id}/diff?targetBomId={targetId}**
- **Purpose**: Diff two BOMs.
- **Auth**: Yes
- **Role**: `ENGINEER`

**GET /api/v1/bom/ebom/{id}/export**
- **Purpose**: Export eBOM to CSV/Excel.
- **Auth**: Yes
- **Role**: Any

**GET, POST /api/v1/bom/mbom**
- **Purpose**: Manage Manufacturing BOMs (list, create).
- **Auth**: Yes
- **Role**: `PRODUCTION_ENGINEER`

**GET /api/v1/bom/mbom/{id}/tree**
- **Purpose**: Get mBOM tree.
- **Auth**: Yes
- **Role**: Any

**POST /api/v1/bom/mbom/{id}/items**
- **Purpose**: Add components/phantom assemblies.
- **Auth**: Yes
- **Role**: `PRODUCTION_ENGINEER`

**PUT /api/v1/bom/mbom/{id}/routings**
- **Purpose**: Define manufacturing routings/operations.
- **Auth**: Yes
- **Role**: `PRODUCTION_ENGINEER`

**PUT /api/v1/bom/mbom/{id}/approve**
- **Purpose**: Approve mBOM.
- **Auth**: Yes
- **Role**: `PRODUCTION_MANAGER`

**PUT /api/v1/bom/mbom/{id}/release**
- **Purpose**: Release mBOM for production.
- **Auth**: Yes
- **Role**: `PRODUCTION_MANAGER`

**POST /api/v1/bom/transformation/execute**
- **Purpose**: Execute rules-based eBOM to mBOM transformation.
- **Auth**: Yes
- **Role**: `PRODUCTION_ENGINEER`

**GET /api/v1/bom/transformation/{id}/status**
- **Purpose**: Get status of an async transformation.
- **Auth**: Yes
- **Role**: Any

**GET, PUT /api/v1/bom/transformation/rules**
- **Purpose**: Manage transformation rules.
- **Auth**: Yes
- **Role**: `ENGINEERING_MANAGER`

---

### 3.7 Inventory (`/inventory`)

**GET /api/v1/inventory/stock**
- **Purpose**: List/Get stock levels across locations/bins.
- **Auth**: Yes
- **Role**: Any

**POST /api/v1/inventory/goods-receipt**
- **Purpose**: Receive goods into inventory.
- **Auth**: Yes
- **Role**: `INVENTORY_CLERK`

**POST /api/v1/inventory/goods-issue**
- **Purpose**: Issue goods out of inventory (e.g., to production).
- **Auth**: Yes
- **Role**: `INVENTORY_CLERK`

**POST /api/v1/inventory/transfer**
- **Purpose**: Transfer stock between bins/warehouses.
- **Auth**: Yes
- **Role**: `INVENTORY_CLERK`

**POST /api/v1/inventory/cycle-count**
- **Purpose**: Record physical inventory cycle count.
- **Auth**: Yes
- **Role**: `INVENTORY_MANAGER`

**GET, POST /api/v1/inventory/reservations**
- **Purpose**: Soft/Hard reserve inventory for orders.
- **Auth**: Yes
- **Role**: `INVENTORY_MANAGER`, `SALES_REP`

**GET /api/v1/inventory/transactions**
- **Purpose**: Audit history of all inventory movements.
- **Auth**: Yes
- **Role**: `INVENTORY_MANAGER`, `FINANCE`

**GET /api/v1/inventory/alerts/low-stock**
- **Purpose**: Get list of items below reorder point.
- **Auth**: Yes
- **Role**: `INVENTORY_MANAGER`, `PROCUREMENT`

---

### 3.8 Sales (`/sales`)

**GET, POST /api/v1/sales/quotations**
- **Purpose**: Manage sales quotations.
- **Auth**: Yes
- **Role**: `SALES_REP`

**PUT /api/v1/sales/quotations/{id}/revise**
- **Purpose**: Create a new revision of a quotation.
- **Auth**: Yes
- **Role**: `SALES_REP`

**PUT /api/v1/sales/quotations/{id}/approve**
- **Purpose**: Approve a quotation (internal).
- **Auth**: Yes
- **Role**: `SALES_MANAGER`

**PUT /api/v1/sales/quotations/{id}/send**
- **Purpose**: Email quotation to customer.
- **Auth**: Yes
- **Role**: `SALES_REP`

**PUT /api/v1/sales/quotations/{id}/accept**
- **Purpose**: Mark quote as accepted by customer.
- **Auth**: Yes
- **Role**: `SALES_REP`

**POST /api/v1/sales/quotations/{id}/convert**
- **Purpose**: Convert an accepted quotation to a Sales Order.
- **Auth**: Yes
- **Role**: `SALES_REP`

**GET, POST /api/v1/sales/sales-orders**
- **Purpose**: Manage sales orders.
- **Auth**: Yes
- **Role**: `SALES_REP`

**PUT /api/v1/sales/sales-orders/{id}/confirm**
- **Purpose**: Confirm SO.
- **Auth**: Yes
- **Role**: `SALES_MANAGER`

**PUT /api/v1/sales/sales-orders/{id}/cancel**
- **Purpose**: Cancel SO.
- **Auth**: Yes
- **Role**: `SALES_MANAGER`

**GET /api/v1/sales/sales-orders/{id}/materials**
- **Purpose**: Check material availability for SO.
- **Auth**: Yes
- **Role**: `SALES_REP`

**GET /api/v1/sales/sales-orders/{id}/shipments**
- **Purpose**: Get related shipments for an SO.
- **Auth**: Yes
- **Role**: Any

**GET /api/v1/sales/sales-orders/{id}/invoices**
- **Purpose**: Get related invoices for an SO.
- **Auth**: Yes
- **Role**: Any

---

### 3.9 Procurement (`/procurement`)

**GET, POST /api/v1/procurement/purchase-requests**
- **Purpose**: Create PRs for materials.
- **Auth**: Yes
- **Role**: Any

**PUT /api/v1/procurement/purchase-requests/{id}/approve**
- **Purpose**: Approve a PR.
- **Auth**: Yes
- **Role**: `DEPARTMENT_MANAGER`

**GET, POST /api/v1/procurement/purchase-orders**
- **Purpose**: Create POs from approved PRs.
- **Auth**: Yes
- **Role**: `BUYER`

**PUT /api/v1/procurement/purchase-orders/{id}/approve**
- **Purpose**: Approve PO.
- **Auth**: Yes
- **Role**: `PROCUREMENT_MANAGER`

**PUT /api/v1/procurement/purchase-orders/{id}/send**
- **Purpose**: Transmit PO to supplier.
- **Auth**: Yes
- **Role**: `BUYER`

**PUT /api/v1/procurement/purchase-orders/{id}/receive**
- **Purpose**: Acknowledge PO receipt from supplier.
- **Auth**: Yes
- **Role**: `BUYER`

**PUT /api/v1/procurement/purchase-orders/{id}/close**
- **Purpose**: Close PO after full receipt/invoicing.
- **Auth**: Yes
- **Role**: `PROCUREMENT_MANAGER`

**GET, POST /api/v1/procurement/goods-receipts**
- **Purpose**: Record items received against a PO.
- **Auth**: Yes
- **Role**: `INVENTORY_CLERK`

---

### 3.10 Suppliers (`/suppliers`)

**GET, POST, PUT /api/v1/suppliers**
- **Purpose**: Manage supplier records.
- **Auth**: Yes
- **Role**: `BUYER`, `PROCUREMENT_MANAGER`

**GET /api/v1/suppliers/{id}**
- **Purpose**: Get supplier details.
- **Auth**: Yes
- **Role**: Any

**GET /api/v1/suppliers/{id}/performance**
- **Purpose**: Get supplier quality/OTIF ratings.
- **Auth**: Yes
- **Role**: `PROCUREMENT_MANAGER`

**GET, POST /api/v1/suppliers/{id}/catalog**
- **Purpose**: Manage supplier part catalog (prices, lead times).
- **Auth**: Yes
- **Role**: `BUYER`

---

### 3.11 Production (`/production`)

**GET, POST /api/v1/production/work-orders**
- **Purpose**: Manage work orders (tied to mBOM & routings).
- **Auth**: Yes
- **Role**: `PRODUCTION_PLANNER`

**GET /api/v1/production/work-orders/{id}**
- **Purpose**: Get WO details.
- **Auth**: Yes
- **Role**: Any

**PUT /api/v1/production/work-orders/{id}/release**
- **Purpose**: Release WO to shop floor.
- **Auth**: Yes
- **Role**: `PRODUCTION_PLANNER`

**PUT /api/v1/production/work-orders/{id}/cancel**
- **Purpose**: Cancel a WO.
- **Auth**: Yes
- **Role**: `PRODUCTION_PLANNER`

**POST /api/v1/production/operations/{id}/start**
- **Purpose**: Record start time for a routing operation.
- **Auth**: Yes
- **Role**: `OPERATOR`

**POST /api/v1/production/operations/{id}/complete**
- **Purpose**: Record completion (yield, scrap) for an operation.
- **Auth**: Yes
- **Role**: `OPERATOR`

**POST /api/v1/production/operations/{id}/hold**
- **Purpose**: Place operation on hold (e.g., machine breakdown).
- **Auth**: Yes
- **Role**: `OPERATOR`

**POST /api/v1/production/work-orders/{id}/material-consumption**
- **Purpose**: Log actual material usage.
- **Auth**: Yes
- **Role**: `OPERATOR`

**GET /api/v1/production/dashboard**
- **Purpose**: Real-time shop floor status (WIP, machine status).
- **Auth**: Yes
- **Role**: Any

---

### 3.12 Quality (`/quality`)

**GET, POST /api/v1/quality/inspections**
- **Purpose**: Manage incoming/in-process/final inspections.
- **Auth**: Yes
- **Role**: `QUALITY_INSPECTOR`

**POST /api/v1/quality/inspections/{id}/results**
- **Purpose**: Record inspection results (pass/fail/measurements).
- **Auth**: Yes
- **Role**: `QUALITY_INSPECTOR`

**GET, POST /api/v1/quality/fat**
- **Purpose**: Factory Acceptance Test execution and metrics.
- **Auth**: Yes
- **Role**: `QUALITY_ENGINEER`

**GET, POST /api/v1/quality/ncrs**
- **Purpose**: Manage Non-Conformance Reports (NCR).
- **Auth**: Yes
- **Role**: `QUALITY_INSPECTOR`

**PUT /api/v1/quality/ncrs/{id}/disposition**
- **Purpose**: Set NCR disposition (Rework, Scrap, Use-As-Is).
- **Auth**: Yes
- **Role**: `QUALITY_MANAGER`

**PUT /api/v1/quality/ncrs/{id}/close**
- **Purpose**: Close resolved NCR.
- **Auth**: Yes
- **Role**: `QUALITY_MANAGER`

**GET /api/v1/quality/dashboard**
- **Purpose**: View Quality KPIs (FPY, Scrap rate).
- **Auth**: Yes
- **Role**: Any

---

### 3.13 Shipments (`/shipments`)

**GET, POST /api/v1/shipments**
- **Purpose**: Create and list shipments (outbound deliveries).
- **Auth**: Yes
- **Role**: `SHIPPING_CLERK`

**GET /api/v1/shipments/{id}**
- **Purpose**: Get shipment details.
- **Auth**: Yes
- **Role**: Any

**PUT /api/v1/shipments/{id}/dispatch**
- **Purpose**: Mark shipment as dispatched.
- **Auth**: Yes
- **Role**: `SHIPPING_CLERK`

**POST /api/v1/shipments/{id}/delivery**
- **Purpose**: Record proof of delivery (POD).
- **Auth**: Yes
- **Role**: `SHIPPING_CLERK`

---

### 3.14 Invoices (`/invoices`)

**GET /api/v1/invoices**
- **Purpose**: List invoices (AR/AP).
- **Auth**: Yes
- **Role**: `FINANCE`

**GET /api/v1/invoices/{id}**
- **Purpose**: Get invoice details.
- **Auth**: Yes
- **Role**: `FINANCE`

**POST /api/v1/invoices/generate/shipment/{shipmentId}**
- **Purpose**: Automatically generate AR invoice from a completed shipment.
- **Auth**: Yes
- **Role**: `FINANCE`

**GET /api/v1/invoices/{id}/pdf**
- **Purpose**: Download invoice PDF.
- **Auth**: Yes
- **Role**: Any

**POST /api/v1/invoices/{id}/payment**
- **Purpose**: Record a payment against an invoice.
- **Auth**: Yes
- **Role**: `FINANCE`

---

### 3.15 CRM (`/crm`)

**GET, POST /api/v1/crm/opportunities**
- **Purpose**: Manage sales opportunities.
- **Auth**: Yes
- **Role**: `SALES_REP`

**PUT /api/v1/crm/opportunities/{id}/stage**
- **Purpose**: Advance opportunity stage.
- **Auth**: Yes
- **Role**: `SALES_REP`

**PUT /api/v1/crm/opportunities/{id}/close-won**
- **Purpose**: Mark opportunity as Won.
- **Auth**: Yes
- **Role**: `SALES_REP`

**PUT /api/v1/crm/opportunities/{id}/close-lost**
- **Purpose**: Mark opportunity as Lost.
- **Auth**: Yes
- **Role**: `SALES_REP`

**GET, POST, PUT /api/v1/crm/service-requests**
- **Purpose**: Customer service/support tickets (create, update).
- **Auth**: Yes
- **Role**: `SUPPORT_AGENT`

**PUT /api/v1/crm/service-requests/{id}/resolve**
- **Purpose**: Resolve service request.
- **Auth**: Yes
- **Role**: `SUPPORT_AGENT`

---

### 3.16 Analytics (`/analytics`)

**GET /api/v1/analytics/sales**
- **Purpose**: Sales performance metrics.
- **Auth**: Yes
- **Role**: `MANAGER`, `EXECUTIVE`

**GET /api/v1/analytics/production**
- **Purpose**: OEE and production efficiency metrics.
- **Auth**: Yes
- **Role**: `MANAGER`, `EXECUTIVE`

**GET /api/v1/analytics/quality**
- **Purpose**: Yield, scrap, defect analysis.
- **Auth**: Yes
- **Role**: `MANAGER`, `EXECUTIVE`

**GET /api/v1/analytics/inventory**
- **Purpose**: Inventory value and aging.
- **Auth**: Yes
- **Role**: `MANAGER`, `EXECUTIVE`

**GET /api/v1/analytics/procurement**
- **Purpose**: Spend analysis and supplier performance summary.
- **Auth**: Yes
- **Role**: `MANAGER`, `EXECUTIVE`

**GET /api/v1/analytics/executive**
- **Purpose**: Unified executive dashboard data.
- **Auth**: Yes
- **Role**: `EXECUTIVE`

---

### 3.17 AI / Smart Features (`/ai`)

**POST /api/v1/ai/query**
- **Purpose**: Ask questions against the RAG-enabled knowledge base.
- **Auth**: Yes
- **Role**: Any

**GET /api/v1/ai/history**
- **Purpose**: Get user's AI interaction history.
- **Auth**: Yes
- **Role**: Any

**POST /api/v1/ai/feedback**
- **Purpose**: Provide thumbs up/down feedback on AI responses.
- **Auth**: Yes
- **Role**: Any

**POST /api/v1/ai/index**
- **Purpose**: Trigger manual re-indexing of documents/data into the vector database.
- **Auth**: Yes
- **Role**: `ADMIN`

**GET /api/v1/ai/index/status**
- **Purpose**: Check current status of vector DB indexing jobs.
- **Auth**: Yes
- **Role**: `ADMIN`

---

### 3.18 Audit (`/audit`)

**GET /api/v1/audit/logs**
- **Purpose**: Retrieve system audit logs.
- **Auth**: Yes
- **Role**: `ADMIN`, `AUDITOR`
- **Query Params**: `fromDate`, `toDate`, `entity`, `entityId`, `user`, `action`

**GET /api/v1/audit/logs/{id}**
- **Purpose**: Retrieve detailed payload changes (before/after state) of an audit event.
- **Auth**: Yes
- **Role**: `ADMIN`, `AUDITOR`

---

## 4. Traceability: Endpoint -> State Transition / Business Rule
- `/sales/quotations/{id}/approve` -> Checks `SALES_BR_003` (Margin validation).
- `/production/work-orders/{id}/release` -> Ensures `PROD_BR_011` (BOM released, materials available).
- `/quality/ncrs/{id}/disposition` -> State transition `OPEN` -> `DISPOSITIONED`.
- `/bom/mbom/{id}/release` -> Ensures `BOM_BR_008` (Valid routings).

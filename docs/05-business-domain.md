# Business Domain Model

## Siva Machine Works — Smart Manufacturing Platform

This document defines the complete business domain model for the Smart Manufacturing Platform at Siva Machine Works. It is grounded in the operational realities of the company's two primary product lines: the HM-500 (Hydraulic Machining Center) and the HM-700 (Heavy-Duty Hydraulic Press). It serves as the definitive reference for the overarching business processes, rules, and system interactions within the manufacturing and enterprise software architecture.

## Company & Product Context

### Products
| Product | Code | Description | List Price (JPY) | Std Cost (JPY) | Lead Time |
|---|---|---|---|---|---|
| Hydraulic Machining Center | SMW-HM-500 | 5-axis CNC machining center, 500mm work envelope, 15,000 RPM spindle, hydraulic clamping | ¥28,000,000 | ¥18,500,000 | 14 weeks |
| Heavy-Duty Hydraulic Press | SMW-HM-700 | 700-ton hydraulic press, 2500×1800mm bed, automatic ram control, safety interlocks | ¥45,000,000 | ¥30,000,000 | 18 weeks |

### Facilities & Warehouses
| Facility | Type | Operations | Attached Warehouse |
|---|---|---|---|
| Osaka Plant | Final Assembly | Final assembly, testing, FAT, shipment | WH-OSK |
| Nagoya Plant | Fabrication | Structural fabrication, surface treatment | WH-NGY |
| Penang Plant | Sub-assembly | Electrical/electronic assembly, sub-assemblies | WH-PEN |

### HM-500 Illustrative BOM Structure (Top-Level)
*   **HM500-0000** (Hydraulic Machining Center - Final Assembly)
    *   **HM500-1000** (Machine Bed Assembly)
        *   HM500-1100 (Cast Iron Base)
        *   HM500-1200 (Linear Guideways)
    *   **HM500-2000** (Spindle Assembly)
        *   HM500-2100 (15,000 RPM Motor)
        *   HM500-2200 (Ceramic Bearings)
    *   **HM500-3000** (Hydraulic System)
        *   HM500-3100 (Hydraulic Pump Unit)
        *   HM500-3200 (Proportional Valves)
        *   HM500-3300 (Hose Kit)
    *   **HM500-4000** (Electrical Cabinet)
        *   HM500-4100 (5-Axis CNC Controller)
        *   HM500-4200 (Servo Drives)
    *   **HM500-5000** (Coolant System)
        *   HM500-5100 (High-Pressure Pump)
        *   HM500-5200 (Filtration Unit)

### HM-700 Illustrative BOM Structure (Top-Level)
*   **HM700-0000** (Heavy-Duty Hydraulic Press)
    *   **HM700-1000** (Frame Assembly)
    *   **HM700-2000** (Ram Assembly)
    *   **HM700-3000** (Hydraulic System)
    *   **HM700-4000** (Control Cabinet)
    *   **HM700-5000** (Safety System)

### HM-500 Routing Table
| Operation | Description | Work Center | Location | Duration |
|---|---|---|---|---|
| 010 | Structural Fabrication | WC-FAB-01 | Nagoya | 3 days |
| 020 | Surface Treatment | WC-SFT-01 | Nagoya | 1 day |
| 030 | Spindle Sub-Assembly | WC-ASM-01 | Osaka | 2 days |
| 040 | Hydraulic Sub-Assembly | WC-ASM-02 | Osaka | 2 days |
| 050 | Electrical Assembly | WC-ELC-01 | Penang | 3 days |
| 060 | Final Assembly | WC-FIN-01 | Osaka | 4 days |
| 070 | Machine Commissioning | WC-TST-01 | Osaka | 2 days |
| 080 | Final Acceptance Test | WC-QMS-01 | Osaka | 1 day |
| 090 | Packaging | WC-PKG-01 | Penang | 1 day |

---

### 01. Customer Management

**Overview**
Manages the lifecycle of customer accounts, capturing corporate details and commercial standing. Customers are industrial entities (e.g., Toyota, Airbus suppliers) purchasing heavy machinery.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Sales Rep | Submits new customers | CRM:CREATE |
| Sales Manager | Approves customer status | CRM:APPROVE |
| Finance Clerk | Sets credit limits | FINANCE:UPDATE |

**Inputs**
*   Company Name, Registration Number, Tax ID
*   Billing and Shipping Addresses
*   Primary Contact details
*   Requested Credit Limit

**Outputs**
*   Customer Profile (CUS-YYYY-NNNN)
*   Tier Assignment (A=Strategic, B=Standard, C=Occasional)

**Business Rules**
1.  Must have at least one primary contact before activation.
2.  Any requested credit limit requires explicit Finance approval.
3.  Duplicate detection based on Tax ID or Registration Number must flag new entries.
4.  24-month period with no SO activity automatically transitions status to DORMANT.
5.  Blacklisted customers cannot have new Opportunities or Quotations created.

**Statuses**
| Status | Description |
|---|---|
| PROSPECT | Initial entry, undergoing qualification |
| ACTIVE | Approved for trading and quoting |
| ON_HOLD | Temporarily suspended (e.g., credit issues) |
| DORMANT | No activity for 24 months |
| BLACKLISTED | Prohibited from future business |

**State Transitions**
```
PROSPECT -> ACTIVE [trigger: Approval + Primary Contact]
ACTIVE -> ON_HOLD [trigger: Manual/Credit fail]
ACTIVE -> DORMANT [trigger: 24m inactivity]
ANY -> BLACKLISTED [trigger: Manual by Exec/Finance]
```

**Related Entities**
`customers`, `contacts`, `credit_limits`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/customers` | Create new prospect | SALES_REP |
| PUT | `/api/customers/{id}/activate` | Activate customer | SALES_MANAGER |
| PATCH | `/api/customers/{id}/credit` | Update credit limit | FINANCE_CLERK |

**Security Requirements**
*   PII of contacts must be encrypted at rest.
*   Only Finance roles can view/modify credit limits.

### 02. CRM — Opportunity Management

**Overview**
Tracks potential sales deals through a structured pipeline. Helps forecast revenue and manage the sales cycle for high-value machinery.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Sales Rep | Manages their deals | OPP:UPDATE |
| Sales Manager | Pipeline oversight | OPP:READ_ALL |

**Inputs**
*   Customer Code
*   Product Model and Quantity
*   Estimated Value
*   Expected Close Date

**Outputs**
*   Opportunity Record (OPP-YYYY-NNNN)
*   Revenue Forecast entries

**Business Rules**
1.  Can only be created for ACTIVE customers.
2.  Value ≥¥50,000,000 requires automatic Sales Manager notification.
3.  Expected close date must be ≥4 weeks from creation date.
4.  Transitioning to CLOSED_WON auto-initiates a draft Quotation.
5.  Transitioning to CLOSED_LOST mandates selecting a "lost reason" code.
6.  Opportunities with no updates for 30 days are flagged as Stale.

**Statuses**
| Status | Description |
|---|---|
| PROSPECTING (10%) | Initial discovery |
| QUALIFICATION (25%) | Budget and timeline validated |
| PROPOSAL (50%) | Solution presented |
| NEGOTIATION (75%) | Contract/pricing discussions |
| CLOSED_WON (100%) | Deal won |
| CLOSED_LOST (0%) | Deal lost |
| ON_HOLD | Deal paused by customer |

**State Transitions**
```
PROSPECTING -> QUALIFICATION -> PROPOSAL -> NEGOTIATION -> CLOSED_WON
NEGOTIATION -> CLOSED_LOST [trigger: Manual + Reason]
```

**Related Entities**
`opportunities`, `customers`, `products`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/opportunities` | Create opportunity | SALES_REP |
| PATCH | `/api/opportunities/{id}/stage` | Advance stage | SALES_REP |

**Security Requirements**
*   Sales Reps can only view/edit their own opportunities unless granted territory override.

### 03. Quotation Management

**Overview**
Generates binding commercial offers to customers based on closed opportunities. Manages pricing, discounts, and validity periods.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Sales Rep | Drafts quotes | QUOTE:CREATE |
| Sales Manager | Approves discounts | QUOTE:APPROVE |

**Inputs**
*   Opportunity Reference
*   Line Items (Product, Qty)
*   Proposed Unit Price / Discount

**Outputs**
*   Quotation Document (PDF) (QT-YYYY-NNNN)
*   Pricing History

**Business Rules**
1.  Must originate from a CLOSED_WON opportunity.
2.  Price cannot fall below the system-defined floor price.
3.  Any discount >5% of list price requires Sales Manager approval.
4.  Quotations are strictly valid for 60 days from issuance.
5.  Standard 10% Japanese Consumption Tax (JCT) applies.
6.  Only one ACTIVE quotation per opportunity at any given time.
7.  Revisions are tracked using trailing letters (e.g., QT-2024-0012A, QT-2024-0012B).

**Statuses**
| Status | Description |
|---|---|
| DRAFT | Being prepared |
| PENDING_APPROVAL | Waiting on discount approval |
| APPROVED | Ready to send |
| SENT | Transmitted to customer |
| ACCEPTED | Customer agreed |
| REJECTED | Customer declined |
| EXPIRED | Past 60 days |
| SUPERSEDED | Replaced by new revision |
| CONVERTED | Turned into SO |

**State Transitions**
```
DRAFT -> PENDING_APPROVAL [trigger: Discount > 5%]
DRAFT -> APPROVED [trigger: Discount <= 5%]
APPROVED -> SENT -> ACCEPTED -> CONVERTED
```

**Related Entities**
`quotations`, `quotation_lines`, `opportunities`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/quotations` | Generate from Opportunity | SALES_REP |
| POST | `/api/quotations/{id}/revise` | Create new version | SALES_REP |
| POST | `/api/quotations/{id}/convert` | Convert to SO | SALES_REP |

**Security Requirements**
*   Floor prices are read-only and immutable for sales roles.

### 04. Sales Order Management

**Overview**
Handles legally binding orders resulting from accepted quotations. Triggers downstream fulfillment and production planning.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Sales Rep | Converts Quote to SO | SO:CREATE |
| Production Planner | Reviews SO for MRP | SO:READ |

**Inputs**
*   Accepted Quotation Reference
*   Customer PO Number
*   Requested Delivery Date

**Outputs**
*   Sales Order (SO-YYYY-NNNN)
*   OrderConfirmedEvent

**Business Rules**
1.  Must stem from an ACCEPTED quotation.
2.  Delivery date must be ≥ standard product lead time (14 weeks for HM-500, 18 for HM-700).
3.  Customer PO number is mandatory for SO creation.
4.  Upon CONFIRMED status, an OrderConfirmedEvent is fired to trigger inventory reservation, BOM resolution, and MRP.
5.  Cannot be CANCELLED without Plant Manager approval once status is IN_PRODUCTION.

**Statuses**
| Status | Description |
|---|---|
| DRAFT | Created, awaiting final check |
| CONFIRMED | Legally binding, processing begins |
| IN_PROCUREMENT | Waiting on materials |
| IN_PRODUCTION | Manufacturing in progress |
| IN_QUALITY | Undergoing FAT |
| READY_TO_SHIP | Cleared for dispatch |
| PARTIALLY_SHIPPED | Some items shipped |
| SHIPPED | All items dispatched |
| INVOICED | Billing completed |
| CLOSED | Fully resolved |
| CANCELLED | Voided |

**State Transitions**
```
DRAFT -> CONFIRMED [trigger: Manual confirmation]
CONFIRMED -> IN_PROCUREMENT -> IN_PRODUCTION -> IN_QUALITY -> READY_TO_SHIP -> SHIPPED -> INVOICED -> CLOSED
```

**Related Entities**
`sales_orders`, `sales_order_lines`, `quotations`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/orders` | Create SO | SALES_REP |
| PATCH | `/api/orders/{id}/cancel` | Cancel order | PLANT_MANAGER |

**Security Requirements**
*   Cancellation of production orders requires elevated privileges and audit logging.

### 05. Product Management

**Overview**
Maintains the master catalog of sellable end-items (HM-500, HM-700). Defines high-level commercial parameters.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Engineering Manager | Defines product baselines | PRODUCT:CREATE |
| Finance Clerk | Updates standard costs | PRODUCT:COST_UPDATE |

**Inputs**
*   Product Name and Model Code (e.g., SMW-HM-500)
*   List Price
*   Standard Cost

**Outputs**
*   Product Master Record
*   Cost Rollup Reports

**Business Rules**
1.  Floor price is strictly calculated as standard cost × 1.2 (guaranteeing a 20% minimum margin).
2.  Each ACTIVE product must have exactly one RELEASED mBOM associated.
3.  Products are never deleted; they are only marked DISCONTINUED for historical integrity.
4.  A cost rollup process is automatically triggered whenever a new mBOM revision is RELEASED.

**Statuses**
| Status | Description |
|---|---|
| ACTIVE | Available for sale |
| INACTIVE | Setup incomplete |
| DISCONTINUED | No longer sold |
| UNDER_REVISION | Major structural change pending |

**State Transitions**
```
INACTIVE -> ACTIVE [trigger: Release of first mBOM]
ACTIVE -> DISCONTINUED [trigger: End of life]
```

**Related Entities**
`products`, `product_costs`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/products` | Define new product | ENG_MANAGER |
| POST | `/api/products/{id}/rollup` | Trigger cost rollup | FINANCE_CLERK |

**Security Requirements**
*   Cost variables are strictly restricted to Finance and Executive roles.

### 06. Product Revision Management

**Overview**
Controls macro-level engineering iterations of products (e.g., HM-500 Rev D). Groups engineering changes into formal releases.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Engineering Manager | Authorizes new revisions | REVISION:APPROVE |

**Inputs**
*   Product ID
*   Approved ECOs

**Outputs**
*   New Product Revision Record (A, B, C...)

**Business Rules**
1.  Revisions are indicated by a single uppercase letter.
2.  Creating a new revision requires an approved ECO.
3.  Only one revision can be UNDER_REVISION at a time per product.
4.  Active Work Orders utilize the mBOM revision that was RELEASED at the time of the WO creation.

**Statuses**
| Status | Description |
|---|---|
| UNDER_REVISION | Being drafted/modified |
| RELEASED | Current active version |
| SUPERSEDED | Replaced by newer revision |

**State Transitions**
```
UNDER_REVISION -> RELEASED [trigger: ECO Approval]
RELEASED -> SUPERSEDED [trigger: Next Rev Release]
```

**Related Entities**
`product_revisions`, `ecos`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/revisions` | Start new revision | ENGINEER |
| PUT | `/api/revisions/{id}/release` | Release revision | ENG_MANAGER |

**Security Requirements**
*   Historical revisions are strictly immutable.

### 07. PDM — Document Management

**Overview**
Stores and controls access to engineering drawings, specs, and manuals. Integrates with cloud storage (S3).

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Engineer | Uploads files | DOC:CREATE |
| Shop Floor Operator | Views manuals | DOC:READ |

**Inputs**
*   File Binary
*   Metadata (Type, Product, Sequence)

**Outputs**
*   Document Record (DOC-HM500-DRW2D-0042)
*   S3 Presigned URL

**Business Rules**
1.  Document number format: DOC-[PRODUCT]-[TYPE]-[SEQ].
2.  Files are stored in S3; access provided via presigned URLs with a strict 15-minute expiry.
3.  Allowed formats: pdf, step, iges, dwg, dxf, xlsx, docx. Max size: 500MB.
4.  Text is automatically extracted upon upload for AI indexing (via Apache Tika).

**Statuses**
| Status | Description |
|---|---|
| DRAFT | Uploaded, unapproved |
| IN_REVIEW | Pending checker approval |
| APPROVED | Ready for use |
| RELEASED | Attached to active BOM |
| SUPERSEDED | Newer version exists |
| OBSOLETE | No longer used |

**State Transitions**
```
DRAFT -> IN_REVIEW -> APPROVED -> RELEASED
```

**Related Entities**
`documents`, `document_versions`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/documents/upload` | Upload new doc | ENGINEER |
| GET | `/api/documents/{id}/url` | Get S3 URL | ANY |

**Security Requirements**
*   Direct S3 access is blocked. All access via API-generated presigned URLs.

### 08. Engineering BOM (eBOM)

**Overview**
Defines the product structure purely from a design and engineering perspective. Specifies what the product *is*.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Engineer | Builds structure | EBOM:UPDATE |
| Engineering Manager | Approves structure | EBOM:APPROVE |

**Inputs**
*   Item codes (ASSEMBLY, COMPONENT, RAW_MATERIAL, FASTENER, STANDARD_PART)
*   Quantities
*   PDM Document Links

**Outputs**
*   Hierarchical eBOM Structure
*   EBOMApprovedEvent

**Business Rules**
1.  Part numbers must be unique within a single BOM hierarchy.
2.  eBOM items must explicitly link to relevant PDM documents (e.g., drawings).
3.  Once APPROVED, the eBOM is locked; further changes strictly require an ECO or a new revision.
4.  Only one RELEASED eBOM per product at a time.
5.  System must support BOM comparison (diff) between revisions.

**Statuses**
| Status | Description |
|---|---|
| DRAFT | WIP |
| IN_REVIEW | Under check |
| APPROVED | Validated |
| RELEASED | Current production baseline |
| SUPERSEDED | Older version |
| OBSOLETE | Deprecated |

**State Transitions**
```
DRAFT -> IN_REVIEW -> APPROVED -> RELEASED
```

**Related Entities**
`eboms`, `ebom_items`, `documents`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| PUT | `/api/eboms/{id}` | Update structure | ENGINEER |
| GET | `/api/eboms/diff` | Compare BOMs | ENGINEER |

**Security Requirements**
*   Modification of APPROVED BOMs without an ECO must throw a hard security exception.

### 09. Engineering Change Order (ECO)

**Overview**
Formal process for altering an APPROVED/RELEASED eBOM or document. Ensures all stakeholders are aware of changes.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Engineer | Drafts ECO | ECO:CREATE |
| Engineering Manager | Approves ECO | ECO:APPROVE |

**Inputs**
*   Change Description and Type
*   Affected Items/Documents
*   Priority

**Outputs**
*   ECO Record
*   ECOImplementedEvent

**Business Rules**
1.  ECO required for any modification to RELEASED/APPROVED artifacts.
2.  CRITICAL priority ECOs (e.g., safety fixes) mandate implementation within 5 business days.
3.  An approved ECO results in the creation of a new eBOM revision.
4.  Active WOs utilizing affected components are notified but not automatically updated (requires manual planner intervention).

**Statuses**
| Status | Description |
|---|---|
| DRAFT | Proposal |
| SUBMITTED | Sent for review |
| UNDER_REVIEW | Committee review |
| APPROVED | Authorized |
| IMPLEMENTED | Applied to BOMs |
| CLOSED | Finished |

**State Transitions**
```
DRAFT -> SUBMITTED -> UNDER_REVIEW -> APPROVED -> IMPLEMENTED -> CLOSED
```

**Related Entities**
`ecos`, `eco_items`, `eboms`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/ecos` | Raise ECO | ENGINEER |
| POST | `/api/ecos/{id}/implement` | Apply changes | ENG_MANAGER |

**Security Requirements**
*   Full audit trail of ECO approval committee decisions required.

### 10. Manufacturing BOM (mBOM)

**Overview**
Defines how the product is built. Incorporates manufacturing-specific items not found in the eBOM.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Production Planner | Constructs mBOM | MBOM:UPDATE |
| Plant Manager | Releases mBOM | MBOM:APPROVE |

**Inputs**
*   eBOM Baseline
*   Consumables, Tooling, Packaging Items
*   Routing Operations

**Outputs**
*   mBOM Structure
*   MBOMReleasedEvent

**Business Rules**
1.  Derived from eBOM but adds: consumables, tooling, and packaging.
2.  TOOLING items are linked but not consumed per unit (reusable assets).
3.  A RELEASED mBOM is a strict prerequisite for generating Production Work Orders.
4.  mBOM maintains independent revisioning from eBOM to allow process tweaks without engineering changes.

**Statuses**
| Status | Description |
|---|---|
| DRAFT | WIP |
| IN_REVIEW | Verification |
| APPROVED | Verified |
| RELEASED | Active for production |
| SUPERSEDED | Old |
| OBSOLETE | Deprecated |

**State Transitions**
```
DRAFT -> IN_REVIEW -> APPROVED -> RELEASED
```

**Related Entities**
`mboms`, `mbom_items`, `routings`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/mboms` | Create from eBOM | PROD_PLANNER |
| PUT | `/api/mboms/{id}/release` | Make active | PLANT_MANAGER |

**Security Requirements**
*   Changes to routing standard times must be logged as they impact financial cost rollups.

### 11. eBOM → mBOM Transformation

**Overview**
Automated, rule-based engine that assists planners in converting an eBOM into an initial mBOM draft.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Production Planner | Runs transformation | TRANSFORMATION:EXECUTE |

**Inputs**
*   Approved eBOM Source

**Outputs**
*   Draft mBOM

**Business Rules**
1.  Source eBOM must be in an APPROVED state.
2.  Engine uses rules: COPY (default), ADD_CONSUMABLE (e.g., oil), ADD_PACKAGING, SUBSTITUTE, EXCLUDE (docs), SPLIT.
3.  Transformation is entirely non-destructive to the source eBOM.
4.  Routing data must be manually defined/verified post-transformation.
5.  Execution is logged with User ID and exact timestamp.

**Statuses**
N/A - Process/Action based.

**State Transitions**
N/A

**Related Entities**
`eboms`, `mboms`, `transformation_logs`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/transform/ebom-to-mbom` | Execute rule engine | PROD_PLANNER |

**Security Requirements**
*   Transformation logic and rule editing restricted to system admins.

### 12. Inventory Management

**Overview**
Tracks all physical goods across multiple warehouses, managing receipts, issues, and counts.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Warehouse Manager | Oversees stock | INVENTORY:MANAGE |
| Shop Floor Operator | Issues/receives | INVENTORY:TRANSACT |

**Inputs**
*   Transactions (Receipts, Issues, Counts)

**Outputs**
*   Updated Stock Levels
*   LowStockAlertEvent

**Business Rules**
1.  Stock recorded by composite key: Item + Warehouse.
2.  Formula tracked: qty_available = on_hand - reserved; qty_projected = on_hand + on_order - reserved.
3.  Stock quantities (on_hand) cannot drop below zero.
4.  Valuation relies on Moving Average Cost (WAC).
5.  Incoming inspection is mandatory before moving to unrestricted use for items ≥¥1,000,000.
6.  Quarterly cycle counts require reconciliation adjustments.

**Statuses**
N/A (Continuous metric)

**State Transitions**
N/A

**Related Entities**
`inventory`, `inventory_transactions`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/inventory/transaction` | Post movement | WAREHOUSE_MGR |
| GET | `/api/inventory/{item}/{wh}` | Get stock level | ANY |

**Security Requirements**
*   Manual inventory adjustments (cycle counts) require explicit reason codes and audit logging.

### 13. Material Requirements Planning (MRP)

**Overview**
Calculates material shortages based on production demand and current inventory, proposing purchase requisitions.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Production Planner | Runs/Reviews MRP | MRP:EXECUTE |

**Inputs**
*   Sales Orders (Demand)
*   mBOMs
*   Current Inventory & Open POs (Supply)

**Outputs**
*   Purchase Requisitions (PR)
*   StockShortfallEvent

**Business Rules**
1.  Runs simplified single-level explosion on OrderConfirmedEvent.
2.  TOOLING and PACKAGING item types are explicitly excluded from the MRP explosion.
3.  Quantities on open Purchase Orders count as incoming supply.
4.  Outputs show required vs. available vs. shortfall per component.
5.  Planners must review and approve proposed requisitions before PRs become active.

**Statuses**
| Status | Description |
|---|---|
| PROPOSED | Engine output |
| APPROVED | Converted to PR |
| REJECTED | Planner dismissed |

**State Transitions**
```
PROPOSED -> APPROVED [trigger: Planner Validation]
```

**Related Entities**
`mrp_runs`, `mrp_results`, `purchase_requisitions`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/mrp/run` | Trigger calculation | PROD_PLANNER |
| POST | `/api/mrp/results/{id}/approve`| Convert to PR | PROD_PLANNER |

**Security Requirements**
*   MRP engine must only read data; state changes occur only upon Planner approval.

### 14. Supplier Management

**Overview**
Maintains vendor profiles and tracks their performance metrics (OTD and Quality).

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Procurement Manager | Manages vendors | VENDOR:UPDATE |

**Inputs**
*   Vendor Details (VND-NNNN)
*   Delivery Receipts (for OTD)
*   Quality Inspection Results

**Outputs**
*   Vendor Performance Rating

**Business Rules**
1.  Performance rating calculated dynamically: (on_time_delivery × 0.6) + (quality_pass_rate × 0.4).
2.  If rating drops < 70%, vendor is flagged for review.
3.  If rating drops < 50%, vendor is automatically placed ON_HOLD.
4.  SOLE_SOURCE flag alerts planners if a part has no alternative vendors.

**Statuses**
| Status | Description |
|---|---|
| ACTIVE | Approved for POs |
| ON_HOLD | Suspended (poor performance) |
| BLACKLISTED | Permanently blocked |

**State Transitions**
```
ACTIVE -> ON_HOLD [trigger: Rating < 50%]
```

**Related Entities**
`vendors`, `vendor_metrics`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/vendors` | Add vendor | PROC_MANAGER |
| GET | `/api/vendors/{id}/metrics` | Get performance | PROC_OFFICER |

**Security Requirements**
*   Vendor banking details must be masked for non-finance users.

### 15. Purchase Order Management

**Overview**
Controls the issuance of orders to suppliers for raw materials and components, ensuring proper approval workflows.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Procurement Officer | Creates POs | PO:CREATE |
| Plant Manager | Approves large POs | PO:APPROVE_HIGH |

**Inputs**
*   Purchase Requisition
*   Vendor Code
*   Prices and Quantities

**Outputs**
*   Purchase Order (PO-YYYY-NNNN)
*   POReceivedEvent (upon GR)

**Business Rules**
1.  Approval thresholds: ≤¥500,000 auto-approved; ¥500,001-¥5,000,000 needs Procurement Manager; >¥5,000,000 needs Plant Manager.
2.  Any amendment after PO_SENT status strictly requires vendor acknowledgment.
3.  Three-way match (PO + Goods Receipt + Vendor Invoice) is mandatory before payment authorization.
4.  Partial deliveries are allowed and tracked.

**Statuses**
| Status | Description |
|---|---|
| PR_DRAFT | Initial Requisition |
| PO_DRAFT | PO created |
| PO_PENDING_APPROVAL | Waiting on threshold auth |
| PO_APPROVED | Ready to send |
| PO_SENT | Dispatched to vendor |
| PO_PARTIALLY_RECEIVED | Some items arrived |
| PO_RECEIVED | All items arrived |
| PO_CLOSED | Fully processed |

**State Transitions**
```
PO_DRAFT -> PO_PENDING_APPROVAL -> PO_APPROVED -> PO_SENT
PO_SENT -> PO_PARTIALLY_RECEIVED -> PO_RECEIVED -> PO_CLOSED
```

**Related Entities**
`purchase_orders`, `po_lines`, `purchase_requisitions`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/purchasing/orders` | Create PO | PROC_OFFICER |
| PUT | `/api/purchasing/orders/{id}/approve` | Approve PO | PLANT_MANAGER |

**Security Requirements**
*   Approval threshold logic must be processed server-side and tamper-proof.

### 16. Production Work Orders

**Overview**
Tracks the actual manufacturing execution on the shop floor, following the mBOM routing.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Shop Floor Operator | Records time/materials | WO:EXECUTE |
| Production Planner | Releases WOs | WO:RELEASE |

**Inputs**
*   RELEASED mBOM
*   Target Quantity (1 for HM-series)

**Outputs**
*   Work Order (WO-YYYY-NNNN)
*   WorkOrderCompletedEvent

**Business Rules**
1.  One machine built per WO (serial tracking).
2.  Operations strictly sequential: Operation N cannot start until N-1 is complete.
3.  Quality checkpoints are embedded as hard stops (e.g., pressure test after hydraulic assembly).
4.  Material consumption must be explicitly recorded against specific operations.
5.  WO completion automatically triggers the Final Acceptance Test (FAT) quality workflow.

**Statuses**
| Status | Description |
|---|---|
| DRAFT | Planned |
| RELEASED | Dispatched to floor |
| IN_PROGRESS | Being worked |
| ON_HOLD | Stopped (e.g., missing parts) |
| COMPLETED | Assembly finished |
| QUALITY_CHECK | Awaiting FAT |
| CLOSED | Finished and passed |
| CANCELLED | Voided |

**State Transitions**
```
DRAFT -> RELEASED -> IN_PROGRESS -> COMPLETED -> QUALITY_CHECK -> CLOSED
```

**Related Entities**
`work_orders`, `wo_operations`, `wo_materials`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| PATCH | `/api/workorders/{id}/start` | Start operation | OPERATOR |
| POST | `/api/workorders/{id}/consume`| Log materials | OPERATOR |

**Security Requirements**
*   Operators can only start/stop operations assigned to their specific Work Centers.

### 17. Quality Management

**Overview**
Ensures all components and finished goods meet rigorous Japanese industrial standards. Manages Non-Conformance Reports (NCR).

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Quality Inspector | Performs tests | QUALITY:INSPECT |
| Engineering Manager | Approves waivers | QUALITY:DISPOSITION |

**Inputs**
*   Goods Receipts (Incoming)
*   Work Orders (In-Process / FAT)

**Outputs**
*   Inspection Records
*   NCR (NCR-YYYY-NNNN)
*   InspectionPassedEvent / FailedEvent

**Business Rules**
1.  INCOMING inspection mandatory for components ≥¥1,000,000.
2.  FAT criteria for HM-500: positioning error ≤0.005mm, spindle runout ≤0.002mm TIR, pressure 25MPa ±0.5MPa, flow ≥60 L/min, noise ≤75 dB(A).
3.  NCR Severities: CRITICAL, MAJOR, MINOR.
4.  CRITICAL NCRs trigger internal escalation alerts within 4 hours.
5.  Shipment is hard-blocked if any CRITICAL or MAJOR NCRs remain open.
6.  USE_AS_IS disposition for NCRs requires explicit Engineering Manager sign-off.

**Statuses (NCR)**
| Status | Description |
|---|---|
| OPEN | Investigating |
| PENDING_DISPOSITION | Waiting on decision |
| IN_REWORK | Being fixed |
| CLOSED | Resolved |

**State Transitions**
```
OPEN -> PENDING_DISPOSITION -> IN_REWORK -> CLOSED
```

**Related Entities**
`inspections`, `ncrs`, `ncr_dispositions`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/quality/inspections` | Log test results | INSPECTOR |
| POST | `/api/quality/ncrs` | Raise NCR | INSPECTOR |
| PUT | `/api/quality/ncrs/{id}/dispose`| Apply disposition| ENG_MANAGER |

**Security Requirements**
*   Test measurement inputs cannot be altered once submitted; corrections require a new tracked entry.

### 18. Shipment Management

**Overview**
Coordinates the physical logistics of moving heavy machinery from plants to customers globally.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Warehouse Manager | Arranges dispatch | SHIP:MANAGE |

**Inputs**
*   CLOSED Work Order
*   SHIPPED Sales Order

**Outputs**
*   Commercial Invoice, Packing List
*   ShipmentDispatchedEvent

**Business Rules**
1.  HM-500 logistics: 8,200kg, requires flatbed or 20ft open-top container.
2.  HM-700 logistics: 24,500kg, requires heavy lift specialist, 40ft flat rack.
3.  Export packing mandates: timber crate, VCI rust inhibitor, desiccant silica gel.
4.  Goods Issue is automatically recorded upon shipment dispatch.
5.  Dispatch automatically triggers the generation of the final Customer Invoice.

**Statuses**
| Status | Description |
|---|---|
| PLANNED | Logistics booked |
| DISPATCHED | Left facility |
| IN_TRANSIT | En route |
| DELIVERED | Arrived |
| CONFIRMED_BY_CUSTOMER | POD received |

**State Transitions**
```
PLANNED -> DISPATCHED -> IN_TRANSIT -> DELIVERED -> CONFIRMED_BY_CUSTOMER
```

**Related Entities**
`shipments`, `shipment_documents`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/shipments` | Create plan | WAREHOUSE_MGR |
| PATCH | `/api/shipments/{id}/dispatch` | Mark left plant | WAREHOUSE_MGR |

**Security Requirements**
*   Generation of compliance certificates (CE/JIS) must pull data only from locked, approved records.

### 19. Invoicing

**Overview**
Manages the generation of financial demands to customers post-shipment.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Finance Clerk | Oversees billing | FINANCE:INVOICE |

**Inputs**
*   Dispatched Shipment Record
*   Sales Order details

**Outputs**
*   Invoice (INV-YYYY-NNNN)
*   InvoiceGeneratedEvent

**Business Rules**
1.  Automatically generated when a shipment transitions to DISPATCHED.
2.  Sequential numbering resets per Japanese fiscal year (Apr 1 – Mar 31).
3.  Calculates and appends Japanese Consumption Tax 10% (JCT).
4.  Invoices are immutable once ISSUED; corrections strictly require formal Credit Notes.
5.  Must include: legal details, SO/PO refs, line items, tax breakdown, due date, bank details.

**Statuses**
| Status | Description |
|---|---|
| DRAFT | Pending review |
| ISSUED | Sent to customer |
| PARTIALLY_PAID | Partial funds received |
| PAID | Fully settled |
| OVERDUE | Past due date |

**State Transitions**
```
DRAFT -> ISSUED -> PAID
ISSUED -> OVERDUE [trigger: Due date passed]
```

**Related Entities**
`invoices`, `credit_notes`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| GET | `/api/invoices/{id}/pdf` | Generate document | FINANCE_CLERK |
| POST | `/api/invoices/{id}/payment` | Record receipt | FINANCE_CLERK |

**Security Requirements**
*   Invoices cannot be deleted under any circumstances; only offset via credit notes.

### 20. Business Analytics

**Overview**
Provides role-based performance dashboards aggregating data across all modules.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Executive | Views all metrics | ANALYTICS:READ_ALL |
| Managers | Views domain metrics | ANALYTICS:READ_DEPT |

**Inputs**
*   Transactional DB replicas / Data Warehouse

**Outputs**
*   KPI Dashboards

**Business Rules**
1.  Sales KPIs: revenue by month/product, pipeline value, win/loss rate.
2.  Production KPIs: WO status, OTD rate, work center utilization, WIP value.
3.  Quality KPIs: first-pass yield, NCR count by severity, resolution time.
4.  Inventory KPIs: stock vs min stock, 90-day slow-moving items.
5.  Procurement KPIs: supplier OTD, spend by vendor, lead time variance.
6.  Standard date filters apply: Today, Week, Month, Quarter, Year, Custom.
7.  Data must be role-scoped (e.g., Sales sees sales, Execs see all).

**Statuses**
N/A

**State Transitions**
N/A

**Related Entities**
`analytics_cubes`, `kpi_cache`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| GET | `/api/analytics/sales` | Get sales KPIs | SALES_MANAGER |
| GET | `/api/analytics/production` | Get prod KPIs | PLANT_MANAGER |

**Security Requirements**
*   Queries must enforce tenant/plant data isolation dynamically.

### 21. AI Operations Assistant

**Overview**
Retrieval-Augmented Generation (RAG) assistant for natural language querying of enterprise data.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Any User | Queries assistant | AI:CHAT |

**Inputs**
*   Natural Language Queries

**Outputs**
*   Summarized answers with citations

**Business Rules**
1.  Indexes text extracted via Apache Tika from PDM documents, BOMs, WOs, ECOs.
2.  Authorization-aware retrieval: users only receive answers derived from data they have permission to view.
3.  Source citations (links to original docs/records) are mandatory in every response.
4.  A system disclaimer must append to all responses regarding AI generation.
5.  Strict rate limit: 20 queries / user / hour.
6.  Negative feedback (thumbs down) flags interactions for admin review.

**Statuses**
N/A

**State Transitions**
N/A

**Related Entities**
`ai_chat_logs`, `vector_embeddings`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/ai/query` | Submit question | ANY |

**Security Requirements**
*   Vector search must apply RBAC filters *before* passing context to the LLM to prevent data leakage.

### 22. Administration & RBAC

**Overview**
Core security and access management utilizing Role-Based Access Control.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Admin | Manages system | ADMIN:ALL |

**Inputs**
*   User Profiles, Roles, Permissions

**Outputs**
*   JWT Tokens

**Business Rules**
1.  Defined Roles: ADMIN, EXECUTIVE, PLANT_MANAGER, ENGINEERING_MANAGER, ENGINEER, PRODUCTION_PLANNER, SHOP_FLOOR_OPERATOR, SALES_MANAGER, SALES_REP, PROCUREMENT_MANAGER, PROCUREMENT_OFFICER, WAREHOUSE_MANAGER, QUALITY_INSPECTOR, FINANCE_CLERK.
2.  Permission format string: `MODULE:ACTION` (e.g., `CRM:READ`).
3.  Authentication uses JWT: 15-minute access token lifespan, 7-day refresh token stored in Redis.
4.  Passwords hashed using BCrypt with a work factor cost of 12.

**Statuses**
N/A

**State Transitions**
N/A

**Related Entities**
`users`, `roles`, `permissions`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| POST | `/api/auth/login` | Authenticate | PUBLIC |
| POST | `/api/admin/users` | Create user | ADMIN |

**Security Requirements**
*   Immediate token revocation capabilities must be supported via Redis blocklist.

### 23. Audit Logging

**Overview**
Maintains an immutable, append-only record of all state-changing actions across the platform.

**Actors**
| Actor | Role | Key Permission |
|---|---|---|
| Admin | Views logs | AUDIT:READ |

**Inputs**
*   System Events (POST, PUT, PATCH, DELETE operations)

**Outputs**
*   Audit Log Entries

**Business Rules**
1.  Every state-changing operation must log: `user_id`, `action`, `entity_type`, `entity_id`, `old_value_json`, `new_value_json`, `ip_address`, `user_agent`, `timestamp`.
2.  Data retention policy requires a minimum of 2 years of active storage.
3.  Provided UI is an Admin-only viewer with filters (date range, entity type, user, action).
4.  The audit log table itself strictly prohibits DELETE or UPDATE statements at the database level.

**Statuses**
N/A

**State Transitions**
N/A

**Related Entities**
`audit_logs`

**Required APIs**
| Method | Endpoint | Description | Auth Role |
|---|---|---|---|
| GET | `/api/audit/logs` | Search logs | ADMIN |

**Security Requirements**
*   Database user for application must lack DELETE/UPDATE privileges on the audit log table.

### 24. Domain Event Catalog

| Event | Published By | Consumed By | Description | Trigger |
|---|---|---|---|---|
| OpportunityWonEvent | CRM | Sales | Notifies of closed deal | Opp -> CLOSED_WON |
| QuotationConvertedEvent | Quotation | Sales | Triggers SO creation | Quote -> CONVERTED |
| OrderConfirmedEvent | Sales Order | MRP, Inventory | Triggers planning cycle | SO -> CONFIRMED |
| StockShortfallEvent | MRP | Procurement | Alerts to missing materials | MRP Run completion |
| LowStockAlertEvent | Inventory | Procurement | Inventory dips below min | Goods Issue |
| POReceivedEvent | Purchasing | Accounts Payable | Triggers 3-way match | Goods Receipt vs PO |
| EBOMApprovedEvent | Engineering | Production | Baseline established | eBOM -> APPROVED |
| MBOMReleasedEvent | Production | Costing, WO | Enables manufacturing | mBOM -> RELEASED |
| WorkOrderCompletedEvent | Production | Quality | Triggers FAT | WO -> COMPLETED |
| InspectionPassedEvent | Quality | Production | Clears blockages | Inspection -> PASS |
| InspectionFailedEvent | Quality | Quality | Initiates NCR workflow | Inspection -> FAIL |
| NCRRaisedEvent | Quality | Engineering | Alerts to defect | NCR -> OPEN |
| ShipmentDispatchedEvent | Logistics | Finance, Sales | Triggers invoicing | Shipment -> DISPATCHED |
| InvoiceGeneratedEvent | Finance | Customer Comm. | Billing dispatched | Invoice -> ISSUED |
| DocumentUploadedEvent | PDM | AI Assistant | Triggers text extraction | Doc -> DRAFT |
| ECOImplementedEvent | Engineering | Production | Alters BOM structures | ECO -> IMPLEMENTED |
| CycleCountCompletedEvent | Inventory | Finance | Adjusts valuations | Count verification |

---

## Entity Cross-Reference Matrix

| Entity | Primary Module | Downstream Consumers / Related To |
|---|---|---|
| Customer | CRM | Quotations, Sales Orders, Invoices, Shipments |
| Product | Master Data | Opportunities, BOMs, WOs, Inventory, Analytics |
| eBOM | Engineering | mBOM, ECOs, PDM Documents, AI Assistant |
| mBOM | Production | Work Orders, MRP, Costing |
| Work Order | Production | Inventory (Consumption), Quality (Inspections), Shipments |
| Sales Order | Sales | MRP (Demand), Shipments, Invoices |
| Document | PDM | eBOM, mBOM, Quality, AI Assistant |
| Inventory | Logistics | MRP (Supply), WOs (Issues), Purchasing (Receipts) |
| NCR | Quality | Work Orders, Inventory (Quarantine), Supplier Management |
| Purchase Order | Purchasing | Inventory (Receipts), Invoices (Matching), Supplier Mgmt |

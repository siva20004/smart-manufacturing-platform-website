# Business Rules Reference
## Siva Machine Works — Smart Manufacturing Platform

This document captures all business rules that govern the Smart Manufacturing Platform for Siva Machine Works. Rules are numbered for traceability and grouped by domain. Each rule has an ID, description, and enforcement point to ensure consistency across the Spring Boot + Next.js + PostgreSQL modular monolith architecture.

## Rule Classification
Rules are classified into three levels of enforcement:
- **Hard Rules (H)**: System enforced — API returns a 4xx HTTP status code if violated. The operation is blocked.
- **Soft Rules (S)**: Warning issued — The system alerts the user, but the operation can proceed with explicit acknowledgment.
- **Audit Rules (A)**: No enforcement — The operation proceeds without interruption, but the event and context are securely recorded in the audit log.

---

## 1. Customer Rules (CRM)

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| CRM-001 | H | Customer activation requires a primary contact and a configured credit limit. | Customer Service (API) | `CustomerActivatedEvent` |
| CRM-002 | H | Customer tier assignment can only be performed by a Sales Manager. | Customer Service (API) | `CustomerTierUpdatedEvent` |
| CRM-003 | H | Credit limit changes require Finance Clerk approval. | Customer Service (API) | `CreditLimitChangedEvent` |
| CRM-004 | H | Duplicate detection: Reject creation if same tax ID or same company name + country exists. | Customer Service (API) | - |
| CRM-005 | S | Dormancy rule: Auto-tag as DORMANT if no activity for 24 months. | Scheduled Job | `CustomerDormantEvent` |
| CRM-006 | H | Blacklisting can only be performed by an ADMIN. | Customer Service (API) | `CustomerBlacklistedEvent` |
| CRM-007 | H | Blacklisting action requires an immutable audit log entry. | Audit Service | - |
| CRM-008 | H | Customer delete prohibition: Only soft delete or status change is allowed. | Database / Repository | - |
| CRM-009 | H | Restricted country compliance check required before customer creation/activation. | Customer Service (API) | - |
| CRM-010 | H | CRM data access scope: Sales Rep sees only own customers. | Security / API Layer | - |
| CRM-011 | H | CRM data access scope: Sales Manager sees all customers. | Security / API Layer | - |
| CRM-012 | H | Customer credit hold: Cannot create new orders for ON_HOLD customers. | Sales Order Service | `OrderBlockedEvent` |
| CRM-013 | S | Missing secondary contact warning on ACTIVE customers. | UI / API Layer | - |
| CRM-014 | H | All new customers default to DRAFT status. | Customer Service (API) | `CustomerCreatedEvent` |
| CRM-015 | A | Any change to billing address must be audit-logged. | Audit Service | - |

## 2. Opportunity Rules (CRM)

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| OPP-001 | H | Opportunity must link to an ACTIVE customer. | Opportunity Service | - |
| OPP-002 | A | High-value notification: Value ≥¥50M triggers Sales Manager alert. | Opportunity Service | `HighValueOppAlertEvent` |
| OPP-003 | H | Close date minimum: Must be at least 4 weeks from creation date. | Opportunity Service | - |
| OPP-004 | H | CLOSED_WON status triggers quotation creation automatically. | Opportunity Service | `OppClosedWonEvent` |
| OPP-005 | H | Lost reason is mandatory when status changes to CLOSED_LOST. | Opportunity Service | `OppClosedLostEvent` |
| OPP-006 | S | Stale opportunity detection: Alert if 30 days without an update. | Scheduled Job | `StaleOppAlertEvent` |
| OPP-007 | S | One open opportunity per product per customer at a time (warn, not hard block). | Opportunity Service | - |
| OPP-008 | H | Win probability is auto-set based on the pipeline stage. | Opportunity Service | - |
| OPP-009 | H | Opportunity cannot be reopened after CLOSED_LOST without Sales Manager approval. | Opportunity Service | `OppReopenedEvent` |
| OPP-010 | H | Expected revenue must be > 0. | Opportunity Service | - |
| OPP-011 | H | Stage cannot regress without notes/justification. | Opportunity Service | - |
| OPP-012 | A | Stage duration metrics are recorded on stage change. | Analytics Service | - |

## 3. Quotation Rules (CRM/ERP)

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| QUO-001 | H | Quotation must originate from a CLOSED_WON opportunity. | Quotation Service | - |
| QUO-002 | H | Price must be ≥ floor price (floor = standard cost × 1.2). | Quotation Service | - |
| QUO-003 | H | Discount > 5% requires Sales Manager approval before sending to customer. | Quotation Service | `QuotationApprovalReqEvent` |
| QUO-004 | H | Only one ACTIVE quotation allowed per opportunity. | Quotation Service | - |
| QUO-005 | H | Standard validity period is 60 days. | Quotation Service | - |
| QUO-006 | S | Auto-expiry triggered after valid_until date passes. | Scheduled Job | `QuotationExpiredEvent` |
| QUO-007 | H | Japanese Consumption Tax (JCT) 10% is auto-calculated and added. | Quotation Service | - |
| QUO-008 | H | Quotation revision requires versioning (v1, v2...). | Quotation Service | `QuotationRevisedEvent` |
| QUO-009 | H | Accepted quotation auto-populates Sales Order fields. | SO Service | `QuotationAcceptedEvent` |
| QUO-010 | H | Rejected quotation can be revised but counter-argument must be documented. | Quotation Service | `QuotationRejectedEvent` |
| QUO-011 | H | Quotation PDF generation must use Siva Machine Works letterhead and include mandatory fields. | Document Service | - |
| QUO-012 | H | Pricing confidentiality: Floor price is not visible to SALES_REP in API response. | API / DTO Layer | - |
| QUO-013 | H | Currency must match customer's default currency unless overridden by Manager. | Quotation Service | - |
| QUO-014 | H | Payment terms must be explicitly stated (Net 30, Net 60, LC). | Quotation Service | - |
| QUO-015 | A | All quotation PDF downloads are audit-logged. | Audit Service | - |

## 4. Sales Order Rules (ERP)

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| SO-001 | H | Sales Order (SO) can only be created from a CONVERTED quotation. | SO Service | - |
| SO-002 | H | Requested delivery date must be ≥ product lead time from today. | SO Service | - |
| SO-003 | H | Customer PO number is mandatory and must be unique per customer. | SO Service | - |
| SO-004 | H | OrderConfirmedEvent triggers: inventory reservation, BOM resolution, and MRP. | Domain Event Listener | `OrderConfirmedEvent` |
| SO-005 | H | DRAFT SO can be freely cancelled. | SO Service | - |
| SO-006 | S | CONFIRMED SO cancellation requires notification to planning. | SO Service | `SOCancelledEvent` |
| SO-007 | H | IN_PRODUCTION+ SO cancellation requires Plant Manager approval. | SO Service | `SOCancelReqEvent` |
| SO-008 | H | SO cannot be invoiced without at least one recorded shipment. | Invoice Service | - |
| SO-009 | H | Partial shipment allowed; SO stays IN_PRODUCTION until all qty shipped. | SO Service / Shipment | - |
| SO-010 | H | Exactly one Work Order (WO) per SO line item per machine unit. | Production Service | - |
| SO-011 | H | SO total must equal sum of (qty × unit_price) + JCT. | SO Service | - |
| SO-012 | H | Delivery date change requires Sales Manager approval after CONFIRMED status. | SO Service | `DeliveryDateChangedEvent` |
| SO-013 | H | SO amendment (product/qty change) not allowed after IN_PRODUCTION. | SO Service | - |
| SO-014 | A | Special manufacturing requirements field must be logged and forwarded to Production. | SO Service | - |
| SO-015 | H | SO requires assigned warehouse for fulfillment. | SO Service | - |
| SO-016 | H | Shipping address cannot be modified after READY_TO_SHIP. | SO Service | - |
| SO-017 | A | Credit check performed at CONFIRMATION step. | SO Service | - |
| SO-018 | H | SO cannot be CLOSED until fully shipped and invoiced. | SO Service | `SOClosedEvent` |

## 5. Product Rules (ERP)

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| PRD-001 | H | Floor price calculation (standard cost × 1.2) enforced during quotation validation. | Product/Quotation Svc | - |
| PRD-002 | H | Each ACTIVE product must have exactly one RELEASED mBOM. | Product Service | - |
| PRD-003 | H | Product cannot be deleted; it can only be marked DISCONTINUED. | Product Service | - |
| PRD-004 | H | DISCONTINUED products cannot appear in new quotations. | Quotation Service | - |
| PRD-005 | H | Product code format must match: SMW-[MODEL]. | Product Service | - |
| PRD-006 | H | Standard cost update occurs only on mBOM release and cost rollup. | Costing Service | `StandardCostUpdatedEvent`|
| PRD-007 | H | Lead time field determines minimum SO delivery date. | SO Service | - |
| PRD-008 | H | Product weight and HS code are required for shipment documentation. | Product/Shipment Svc | - |
| PRD-009 | H | New product creation requires ENGINEERING_MANAGER approval to activate. | Product Service | `ProductActivatedEvent` |
| PRD-010 | A | Changes to product dimensions or weight are audit-logged. | Audit Service | - |

## 6. Engineering / PDM Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| PDM-001 | H | Document number uniqueness enforced globally (cross-product). | PDM Service | - |
| PDM-002 | H | Document number format: DOC-[PRODUCT]-[TYPE]-[SEQ]. | PDM Service | - |
| PDM-003 | H | New upload to existing document number creates a new version (preserves history). | PDM Service | `DocumentVersionedEvent` |
| PDM-004 | H | RELEASED documents are immutable; new version required for changes. | PDM Service | - |
| PDM-005 | H | Presigned URL expiry is hardcoded to 15 minutes. | Storage Service | - |
| PDM-006 | H | Allowed file types: pdf, step, iges, dwg, dxf, xlsx, docx, jpg, png. | PDM Service | - |
| PDM-007 | H | Max file size is 500 MB. | API Gateway / PDM | - |
| PDM-008 | H | Async text extraction job triggered on upload for pgvector indexing. | PDM Service | `DocumentUploadedEvent` |
| PDM-009 | H | RESTRICTED tag makes document visible only to ENGINEERING_MANAGER and ADMIN. | Security / API Layer | - |
| PDM-010 | H | ECO is required for any change to a RELEASED document. | PDM Service | - |
| PDM-011 | H | Document status flow is one-directional (cannot go backwards without new version).| PDM Service | - |
| PDM-012 | H | Every document must be linked to a product. | PDM Service | - |
| PDM-013 | H | Engineering Manager is the sole approver for documents. | PDM Service | `DocumentApprovedEvent` |
| PDM-014 | H | Obsolete documents cannot be linked to active eBOM items. | eBOM Service | - |
| PDM-015 | A | Document downloads trigger an audit log entry. | Audit Service | - |
| PDM-016 | H | Draft documents cannot be referenced by an ECO. | PDM Service | - |
| PDM-017 | S | Warning if file name doesn't match standard naming convention. | UI / API Layer | - |
| PDM-018 | H | Deletion of physical files is soft-delete only (hidden flag). | PDM Service | - |
| PDM-019 | H | Missing metadata blocks document release. | PDM Service | - |
| PDM-020 | A | AI embedding generation failure is logged. | Audit Service | - |

## 7. eBOM Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| EBM-001 | H | Part number uniqueness enforced within a specific BOM structure. | eBOM Service | - |
| EBM-002 | H | Every eBOM item must have: part_number, description, quantity, unit_of_measure, item_type. | eBOM Service | - |
| EBM-003 | H | Quantity must be > 0. | eBOM Service | - |
| EBM-004 | H | ASSEMBLY items must have at least one child item. | eBOM Service | - |
| EBM-005 | H | eBOM can only be created for ACTIVE products. | eBOM Service | - |
| EBM-006 | H | eBOM revision must match the product revision letter. | eBOM Service | - |
| EBM-007 | H | Only one RELEASED eBOM is allowed per product. | eBOM Service | - |
| EBM-008 | H | APPROVED eBOM is locked — no edits without an ECO or new revision. | eBOM Service | - |
| EBM-009 | H | BOM depth limit is 10 levels (practical limit for display/export). | eBOM Service | - |
| EBM-010 | H | eBOM cannot be approved if any item quantity = 0. | eBOM Service | - |
| EBM-011 | H | eBOM cannot be approved if any item has an OBSOLETE linked document. | eBOM Service | - |
| EBM-012 | H | BOM comparison (diff) calculates added, removed, modified items between revisions.| eBOM Service | - |
| EBM-013 | H | eBOM export strictly follows CSV format (level, part_number, description, qty, unit, item_type, document_ref). | eBOM Service | - |
| EBM-014 | H | Cross-references: eBOM items may reference the same part number in multiple positions. | eBOM Service | - |
| EBM-015 | H | Circular references in BOM structure are blocked. | eBOM Service | - |
| EBM-016 | H | eBOM requires ENGINEERING_MANAGER approval. | eBOM Service | `eBOMApprovedEvent` |
| EBM-017 | A | All BOM exports are audit-logged. | Audit Service | - |
| EBM-018 | H | DRAFT eBOMs cannot be transformed to mBOM. | eBOM/mBOM Svc | - |
| EBM-019 | H | Standard cost rollup is calculated on eBOM approval for reference. | Costing Service | - |
| EBM-020 | S | Items without linked drawings trigger a warning on approval. | UI / API Layer | - |

## 8. Engineering Change Order Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| ECO-001 | H | ECO is required for any modification to RELEASED/APPROVED engineering data. | ECO Service | - |
| ECO-002 | H | CRITICAL priority ECO must be implemented within 5 business days. | Scheduled Job | `EcoOverdueAlertEvent`|
| ECO-003 | H | One ECO can affect multiple documents and BOM items. | ECO Service | - |
| ECO-004 | H | Approved ECO creates a new eBOM revision (revision letter incremented). | ECO Service | `EcoApprovedEvent` |
| ECO-005 | S | Active WOs are not auto-updated — Planner notified via NCR or alert. | ECO Service | `WoEcoAlertEvent` |
| ECO-006 | H | ECO must document: affected documents, affected BOM items, reason, impact assessment. | ECO Service | - |
| ECO-007 | H | ECO approval requires Engineering Manager at a minimum. | ECO Service | - |
| ECO-008 | H | ECO for SAFETY_FIX or REGULATORY_COMPLIANCE is mandatory and cannot be rejected. | ECO Service | - |
| ECO-009 | H | ECO closure requires evidence of implementation. | ECO Service | `EcoClosedEvent` |
| ECO-010 | H | Unapproved ECO cannot block production (unless explicitly linked to NCR). | ECO Service | - |
| ECO-011 | A | ECO rejection reason must be logged. | ECO Service | - |
| ECO-012 | H | Cannot create an ECO against an OBSOLETE document. | ECO Service | - |

## 9. mBOM Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| MBM-001 | H | mBOM can only be initiated from an APPROVED eBOM. | mBOM Service | - |
| MBM-002 | H | mBOM inherits eBOM structure as the starting point. | mBOM Service | - |
| MBM-003 | H | At least one routing operation must be defined before mBOM approval. | mBOM Service | - |
| MBM-004 | H | Each routing operation must have a work center assigned. | mBOM Service | - |
| MBM-005 | H | Operation sequence must be unique within a WO (010, 020, 030...). | mBOM Service | - |
| MBM-006 | H | CONSUMABLE items are included in MRP material calculation (per-unit quantity consumed). | MRP Service | - |
| MBM-007 | H | TOOLING items are tracked separately, NOT consumed per unit (qty=1, reusable). | MRP Service | - |
| MBM-008 | H | PACKAGING items are included in MRP. | MRP Service | - |
| MBM-009 | H | RELEASED mBOM is a strict prerequisite for Work Order creation. | Production Service | - |
| MBM-010 | H | mBOM cannot be modified after RELEASED — a new revision is required. | mBOM Service | - |
| MBM-011 | H | mBOM revision is independent of eBOM revision. | mBOM Service | - |
| MBM-012 | H | Cost rollup: sum of all COMPONENT + CONSUMABLE + PACKAGING items at standard cost. | Costing Service | - |
| MBM-013 | H | mBOM must be linked to exactly one eBOM revision (source). | mBOM Service | - |
| MBM-014 | H | Work center capacity check must run before mBOM approval: planned hours vs available capacity. | mBOM Service | - |
| MBM-015 | H | mBOM approval requires PLANT_MANAGER. | mBOM Service | `mBomApprovedEvent` |
| MBM-016 | H | Cannot map a part to a non-existent routing operation. | mBOM Service | - |
| MBM-017 | A | Changes to routing times are audit-logged. | Audit Service | - |
| MBM-018 | H | Operations cannot have 0 setup or run time. | mBOM Service | - |

## 10. eBOM→mBOM Transformation Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| TRN-001 | H | Source eBOM must be APPROVED. | Transformation Svc| - |
| TRN-002 | H | Transformation creates mBOM in DRAFT status (never directly RELEASED). | Transformation Svc| - |
| TRN-003 | H | Transformation is non-destructive (source eBOM is unchanged). | Transformation Svc| - |
| TRN-004 | H | Each transformation event recorded: user, timestamp, rules applied, items added/modified/excluded. | Transformation Svc| `BomTransformedEvent`|
| TRN-005 | S | If mBOM already exists: user must explicitly confirm overwrite or create new revision. | Transformation Svc| - |
| TRN-006 | H | COPY rule: all ASSEMBLY and COMPONENT eBOM items are copied. | Transformation Svc| - |
| TRN-007 | H | ADD_CONSUMABLE rule: triggered by item_type in routing definition. | Transformation Svc| - |
| TRN-008 | H | ADD_PACKAGING rule: standard packaging set added based on product code. | Transformation Svc| - |
| TRN-009 | H | SUBSTITUTE rule: only pre-configured substitution mappings are applied. | Transformation Svc| - |
| TRN-010 | H | EXCLUDE rule: items tagged `exclude_from_mbom=true` in eBOM are not copied. | Transformation Svc| - |
| TRN-011 | H | Manual additions after transformation must be documented with a reason. | mBOM Service | - |
| TRN-012 | H | Routing must be defined separately after transformation (not auto-generated). | mBOM Service | - |

## 11. Inventory Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| INV-001 | H | Available stock = on_hand - reserved (never negative). | Inventory Service | - |
| INV-002 | H | Goods Issue rejected if available < requested quantity (hard rule). | Inventory Service | - |
| INV-003 | H | GOODS_RECEIPT requires a valid PO reference (no PO = no receipt). | Inventory Service | `GoodsReceivedEvent` |
| INV-004 | H | Reservation created automatically on OrderConfirmedEvent. | Inventory Service | `StockReservedEvent` |
| INV-005 | H | Reservation released on SO cancellation. | Inventory Service | `ReservationReleasedEvent`|
| INV-006 | H | Stock valuation: Weighted Average Cost (WAC) = total_value / total_quantity. | Costing Service | - |
| INV-007 | H | WAC is recalculated on every GOODS_RECEIPT. | Costing Service | - |
| INV-008 | H | GOODS_ISSUE records at current WAC. | Inventory Service | `GoodsIssuedEvent` |
| INV-009 | H | Cycle count: all transactions are PAUSED for affected items during a count. | Inventory Service | - |
| INV-010 | H | Cycle count adjustment: Finance Clerk approval required for variance > ¥100,000. | Inventory Service | `CycleCountAdjustmentEvent`|
| INV-011 | S | Min stock alerts: LowStockAlertEvent fired when on_hand ≤ min_stock. | Scheduled Job | `LowStockAlertEvent` |
| INV-012 | H | Incoming inspection required for all items with unit_cost ≥ ¥1,000,000. | Quality Service | `InspectionRequiredEvent`|
| INV-013 | H | Transfer between warehouses requires source to have sufficient available stock. | Inventory Service | `StockTransferredEvent`|
| INV-014 | H | Scrap transaction requires Plant Manager approval. | Inventory Service | `StockScrappedEvent` |
| INV-015 | H | Inventory transactions are immutable (no delete or update). | Inventory Service | - |
| INV-016 | S | Stock accuracy KPI target: 98%+ (on_hand matches physical count). | Analytics Service | - |
| INV-017 | H | Quarantined stock is not available for MRP or SO allocation. | Inventory Service | - |
| INV-018 | H | Bins and locations must be valid and active for receiving. | Inventory Service | - |
| INV-019 | A | Manual stock adjustments are flagged and heavily audited. | Audit Service | - |
| INV-020 | H | Cannot issue stock from WH-OSK if order specifies WH-PEN. | Inventory Service | - |

## 12. MRP Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| MRP-001 | H | MRP run triggered automatically on OrderConfirmedEvent. | MRP Service | `MrpRunCompletedEvent` |
| MRP-002 | H | Single-level BOM explosion only (not multi-level time-phased). | MRP Service | - |
| MRP-003 | H | TOOLING is excluded from MRP calculation. | MRP Service | - |
| MRP-004 | H | PACKAGING is included in MRP calculation. | MRP Service | - |
| MRP-005 | H | Net requirement = gross_requirement - available_stock - qty_on_order. | MRP Service | - |
| MRP-006 | H | Negative net requirement = no action needed (sufficient supply). | MRP Service | - |
| MRP-007 | H | Positive net requirement = create Purchase Requisition automatically. | Procurement Svc | `PRGeneratedEvent` |
| MRP-008 | H | MRP does NOT consider safety stock margin (simplified approach). | MRP Service | - |
| MRP-009 | S | Manual MRP re-run available for Production Planner. | MRP Service | - |
| MRP-010 | A | MRP results are logged per SO for audit trail. | Audit Service | - |

## 13. Procurement Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| PUR-001 | H | POs can only be issued to ACTIVE vendors. | Procurement Svc | - |
| PUR-002 | H | PO approval thresholds: ≤¥500K auto-approved; ¥500K-5M PROCUREMENT_MANAGER; >¥5M PLANT_MANAGER. | Procurement Svc | `POApprovedEvent` |
| PUR-003 | H | PO cannot be modified after PO_SENT without vendor acknowledgment and amendment record. | Procurement Svc | - |
| PUR-004 | H | Three-way match (PO + GR quantity + vendor invoice amount) required before payment approval. | Finance Service | - |
| PUR-005 | H | GR partial delivery allowed; PO remains PARTIALLY_RECEIVED. | Inventory Service | - |
| PUR-006 | H | Vendor performance is calculated monthly based on delivery and quality. | Analytics Service | - |
| PUR-007 | S | Vendor rating < 70%: notification sent to Procurement Manager. | Scheduled Job | `VendorWarningEvent` |
| PUR-008 | H | Vendor rating < 50%: auto sets vendor to ON_HOLD status. | Scheduled Job | `VendorOnHoldEvent` |
| PUR-009 | S | SOLE_SOURCE vendor requires risk acknowledgment on PO creation. | Procurement Svc | - |
| PUR-010 | H | PO amendment creates a versioned amendment record (e.g., PO-YYYY-NNNN-AMD-01). | Procurement Svc | `POAmendedEvent` |
| PUR-011 | H | PO cancellation: Only before PO_SENT; after requires vendor notice and confirmation. | Procurement Svc | `POCancelledEvent` |
| PUR-012 | H | PO must specify delivery address (one of the three warehouses). | Procurement Svc | - |
| PUR-013 | S | PO lead time warning: Warn if requested delivery < vendor lead time + today. | Procurement Svc | - |
| PUR-014 | H | Currency: JPY default; USD/EUR for international suppliers (exchange rate captured at PO creation).| Procurement Svc | - |
| PUR-015 | H | Vendor must have valid tax details to issue PO. | Procurement Svc | - |
| PUR-016 | H | Cannot approve PR without associated budget/account code. | Procurement Svc | - |
| PUR-017 | A | PO PDF downloads are audit logged. | Audit Service | - |
| PUR-018 | H | Invoice mismatch > 2% requires manual override by Finance Manager. | Finance Service | - |

## 14. Production Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| PRO-001 | H | WO can only be created when SO is CONFIRMED and mBOM is RELEASED. | Production Svc | `WOCreatedEvent` |
| PRO-002 | H | All materials must be reserved before WO status = IN_PROGRESS. | Production Svc | - |
| PRO-003 | H | Operations must be executed in sequence order (op N requires op N-1 COMPLETED). | Production Svc | - |
| PRO-004 | H | Quality checkpoints are mandatory holds in routing — cannot be skipped. | Production Svc | - |
| PRO-005 | H | After Operation 040 (Hydraulic sub-assembly): pressure test inspection required. | Quality Service | - |
| PRO-006 | H | After Operation 070 (Commissioning): commissioning test inspection required. | Quality Service | - |
| PRO-007 | H | After Operation 080 (FAT): Final Acceptance Test required. | Quality Service | - |
| PRO-008 | H | WO ON_HOLD reasons: MATERIAL_SHORTAGE, QUALITY_HOLD, EQUIPMENT_BREAKDOWN, OPERATOR_ABSENT. | Production Svc | `WOHoldEvent` |
| PRO-009 | H | One WO per machine unit (no batch production for HM-500/HM-700). | Production Svc | - |
| PRO-010 | H | Material consumption must be recorded at Goods Issue from warehouse to WO. | Inventory Service | - |
| PRO-011 | H | WO completion triggers WorkOrderCompletedEvent → QMS initiates final inspection. | Production Svc | `WOCompletedEvent` |
| PRO-012 | S | Actual material consumption vs planned (from mBOM): variance > 5% flagged for review. | Production Svc | `MaterialVarianceAlertEvent`|
| PRO-013 | H | Operator must be assigned to operation before starting. | Production Svc | - |
| PRO-014 | H | WO cannot be cancelled if IN_PROGRESS without Plant Manager approval. | Production Svc | `WOCancelReqEvent` |
| PRO-015 | A | All WO changes (status, assignment, completion) are audit-logged. | Audit Service | - |
| PRO-016 | H | Work center cannot have more than capacity_hours of operations scheduled per day. | Planning Service | - |
| PRO-017 | A | Overtime hours must be flagged in system (impacts production planning). | Planning Service | - |
| PRO-018 | A | Actual vs planned duration tracked per operation for efficiency metrics. | Analytics Service | - |
| PRO-019 | H | Scrap recorded against WO must reflect in inventory adjustments. | Production Svc | - |
| PRO-020 | H | Rework operations must be explicitly added to WO routing. | Production Svc | - |

## 15. Quality Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| QMS-001 | H | Incoming inspection required for items with unit_cost ≥ ¥1,000,000. | Quality Service | - |
| QMS-002 | H | Incoming FAILED inspection: items quarantined, GR stock NOT added until NCR resolved. | Quality/Inv Svc | `IncomingInspectionFailedEvent`|
| QMS-003 | H | In-process inspection FAILED: WO placed ON_HOLD (reason: QUALITY_HOLD). | Quality/Prod Svc | `QualityHoldEvent` |
| QMS-004 | H | FAT FAILED: machine sent back to IN_PROGRESS for rework; new FAT required. | Quality/Prod Svc | - |
| QMS-005 | H | NCR CRITICAL: escalation to Engineering Manager within 4 business hours. | Scheduled Job | `CriticalNCRAcceleratedEvent`|
| QMS-006 | H | Shipment hard-blocked by open CRITICAL or MAJOR NCR on the WO. | Shipment Service | - |
| QMS-007 | H | USE_AS_IS disposition requires Engineering Manager written approval in system. | Quality Service | - |
| QMS-008 | H | NCR closure requires a verification record (re-inspection result). | Quality Service | `NCRClosedEvent` |
| QMS-009 | H | HM-500 FAT criteria: positioning ≤0.005mm, spindle runout ≤0.002mm TIR, hydraulic 25MPa ±0.5MPa, coolant ≥60L/min, noise ≤75dB(A). | Quality Service | - |
| QMS-010 | H | Customer witness for FINAL_FAT: customer representative can be invited and sign-off recorded. | Quality Service | - |
| QMS-011 | H | Quality records require permanent retention (no delete, no archive expiry). | DB / Repository | - |
| QMS-012 | A | First-pass quality rate = inspections passed on first attempt / total inspections. | Analytics Service | - |
| QMS-013 | H | All inspection results (PASS or FAIL) are recorded — no abandonment. | Quality Service | - |
| QMS-014 | H | NCR must have disposition approved before WO can resume. | Quality Service | - |
| QMS-015 | H | Inspector cannot inspect their own work. | Security / QMS | - |
| QMS-016 | H | Calibration due dates block usage of inspection tools. | Quality Service | - |
| QMS-017 | A | Supplier corrective action requests (SCAR) link to original PO. | Quality Service | - |
| QMS-018 | H | Photo evidence required for MAJOR NCRs. | Quality Service | - |

## 16. Shipment Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| SHP-001 | H | Shipment requires SO status = READY_TO_SHIP (quality gate cleared). | Shipment Service | - |
| SHP-002 | H | Shipment blocked if CRITICAL or MAJOR open NCRs on WO. | Shipment Service | - |
| SHP-003 | H | Goods Issue transaction auto-recorded on shipment dispatch. | Inventory Service | `ShipmentDispatchedEvent`|
| SHP-004 | H | SHIPPED SO auto-triggers invoice generation. | Invoice Service | `InvoiceGeneratedEvent` |
| SHP-005 | H | Required export documents: Commercial Invoice, Packing List, CoO, CE/JIS certificate, FAT certificate. | Document Service | - |
| SHP-006 | H | Carrier and transport reference number are mandatory. | Shipment Service | - |
| SHP-007 | H | Standard weights used for shipping docs: HM-500 = 8,200 kg; HM-700 = 24,500 kg. | Shipment Service | - |
| SHP-008 | H | Estimated delivery date is required on dispatch. | Shipment Service | - |
| SHP-009 | H | POD (Proof of Delivery) confirmation updates SO to allow CLOSED status. | SO/Shipment Svc | `PODReceivedEvent` |
| SHP-010 | H | Partial shipment: each shipment is a separate record; SO tracks remaining qty. | Shipment Service | - |
| SHP-011 | H | Shipment records are immutable after DISPATCHED. | Shipment Service | - |
| SHP-012 | H | Incoterms recorded per shipment (EXW / CIF / DDP). | Shipment Service | - |

## 17. Invoice Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| FIN-001 | H | Invoice auto-generated on ShipmentDispatchedEvent. | Finance Service | - |
| FIN-002 | H | Sequential invoice number resets per Japanese fiscal year (Apr 1 – Mar 31). | Finance Service | - |
| FIN-003 | H | JCT @ 10% auto-calculated and displayed separately. | Finance Service | - |
| FIN-004 | H | Invoice cannot be modified after ISSUED — credit notes only. | Finance Service | - |
| FIN-005 | H | Invoice amount must match SO line items + tax. | Finance Service | - |
| FIN-006 | H | Payment terms inherited from SO quotation: Net 30 / Net 60 / LC. | Finance Service | - |
| FIN-007 | S | Invoice overdue: status auto-set to OVERDUE when due date passes without payment. | Scheduled Job | `InvoiceOverdueEvent` |
| FIN-008 | H | Payment record: Finance Clerk records amount, date, reference. | Finance Service | `PaymentReceivedEvent` |
| FIN-009 | H | Partial payment: Status becomes PARTIALLY_PAID; remaining balance tracked. | Finance Service | - |
| FIN-010 | H | Three-way match (PO + GR + invoice) applies to vendor invoices (not customer invoices). | Finance Service | - |
| FIN-011 | H | Invoice PDF must include: Siva Machine Works registration number, bank SWIFT/BIC, customer tax ID. | Document Service | - |
| FIN-012 | H | Invoice for LC payment: LC details (bank, LC number, expiry) recorded. | Finance Service | - |

## 18. Security Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| SEC-001 | H | JWT access token expiry is 15 minutes. | Security Layer | - |
| SEC-002 | H | Refresh token expiry is 7 days; stored in Redis for revocation. | Security Layer | - |
| SEC-003 | H | Passwords hashed with BCrypt (cost factor 12). | Security Layer | - |
| SEC-004 | H | Account locked after 5 consecutive failed login attempts. | Security Layer | `AccountLockedEvent` |
| SEC-005 | H | Password complexity: min 12 chars, upper, lower, digit, special char. | Security Layer | - |
| SEC-006 | H | CORS whitelist strictly limits to approved frontend origins. | API Gateway / Web| - |
| SEC-007 | H | HTTPS enforced in production (HTTP → HTTPS redirect). | Load Balancer | - |
| SEC-008 | H | API rate limiting: 100 req/min standard; 20 req/min AI endpoints. | API Gateway | - |
| SEC-009 | H | Input validation: all DTOs validated with Jakarta Bean Validation. | Controller Layer | - |
| SEC-010 | H | SQL injection prevention: JPA parameterized queries only (no native string concat).| Repository Layer | - |
| SEC-011 | H | Audit log is immutable, no DELETE API exposed. | Audit Service | - |
| SEC-012 | H | Secrets managed via AWS Secrets Manager / env vars (no hardcoding). | Config Server | - |
| SEC-013 | H | Session revocation supported instantly via Redis. | Security Layer | - |
| SEC-014 | H | PII fields (email, phone, address) flagged in schema for GDPR export/deletion capability.| DB Schema / API | - |
| SEC-015 | H | Cross-module data access: only via published domain events or API contracts (no cross-DB query).| Architecture | - |

## 19. AI / RAG Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| AIR-001 | H | Every AI query is authentication-checked before processing. | AI Gateway | - |
| AIR-002 | H | AI retrieval scope restricted to user's authorized modules. | AI Gateway | - |
| AIR-003 | H | Source citations are mandatory in all AI responses. | AI Service | - |
| AIR-004 | H | Response disclaimer mandatory: "This is AI-generated content. Verify with official documents..." | AI Service | - |
| AIR-005 | H | Rate limit: 20 queries per user per hour. | AI Gateway | - |
| AIR-006 | S | Negative feedback flags query for Admin/Engineering review within 24 hours. | AI Service | `AIFeedbackFlaggedEvent` |
| AIR-007 | H | AI must not expose: customer pricing, confidential financial data, employee personal data. | AI Gateway / Vector | - |
| AIR-008 | A | Query and response logged in `ai_query_log` (audit trail). | Audit Service | - |
| AIR-009 | H | Prompt injection prevention: system prompt isolation, user input sanitization. | AI Service | - |
| AIR-010 | H | Embedding refresh triggered on new document upload (DocumentUploadedEvent). | Domain Event | - |
| AIR-011 | H | Chunk size: 500-1000 tokens with 10% overlap. | Embedding Service| - |
| AIR-012 | H | Top-K retrieval: K=5-10 chunks per query. | Retrieval Service| - |

## 20. Cross-Cutting Rules

| Rule ID | Type | Rule Description | Enforcement Point | Event Fired |
| :--- | :---: | :--- | :--- | :--- |
| GLB-001 | H | UUID primary keys used for all entities. | DB Schema | - |
| GLB-002 | H | Soft delete: deleted_at timestamp used (never physical delete of business records). | Repository Layer | - |
| GLB-003 | H | Audit columns: created_by, created_at, updated_by, updated_at on all tables. | JPA Auditing | - |
| GLB-004 | H | Immutable records: financial transactions, audit log, inspection records, shipment records. | API / DB Layer | - |
| GLB-005 | H | Pagination required for all list endpoints (default page size 20, max 100). | Controller Layer | - |
| GLB-006 | H | Correlation ID: all API requests carry X-Correlation-ID header for log tracing. | Filter / Gateway | - |
| GLB-007 | H | Error response format strictly adheres to `{ error: { code, message, details, correlationId } }`.| Exception Handler| - |
| GLB-008 | H | Fiscal year: Japanese fiscal year April 1 – March 31 (affects analytics, numbering). | Global Config | - |
| GLB-009 | H | Currency: JPY primary; USD/EUR for international POs with exchange rate snapshot. | Global Config | - |
| GLB-010 | H | Module isolation: no module reads another module's database tables directly. | Architecture | - |

---

## Rule Traceability Matrix

| Key Rule ID | Requirement Satisfied (02-business-requirements.md) | Implementation / Constraint Location | Test Case Reference |
| :--- | :--- | :--- | :--- |
| PRD-001 | Ensure minimum profit margins | `QuotationValidator.java` | `TC-QUO-0012` |
| SO-004 | Automate supply chain execution | `OrderConfirmedEventListener.java` | `TC-SO-0045` |
| EBM-008 | Engineering data integrity | `eBOMService.approve()` | `TC-EBM-0021` |
| PRO-007 | Mandatory Quality Gates | `WorkOrderTransitionValidator.java` | `TC-PRO-0098` |
| SEC-015 | Modular Monolith Isolation | ArchUnit Tests | `TC-ARC-0004` |
| FIN-002 | Japanese accounting standards compliance | `InvoiceNumberGenerator.java` | `TC-FIN-0032` |
| AIR-002 | Secure AI Data Access | `VectorSearchFilterBuilder.java` | `TC-AIR-0015` |

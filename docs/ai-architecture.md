# Manufacturing Operations AI Architecture & RAG Pipeline

## 1. Architectural Philosophy
The Manufacturing Operations AI Assistant in the Siva Machine Works Smart Manufacturing Platform follows a strict **Secure Retrieval-Augmented Generation (RAG)** architecture.

### Zero Direct Database Access Mandate
```
[ User Query ]
       ↓
[ Spring Security Filter (JWT Authentication) ]
       ↓
[ Method-Level RBAC (@PreAuthorize) ]
       ↓
[ Intent Classifier & Entity Extractor ]
       ↓
[ Authorized Business-Data Retriever (Spring Data JPA Repositories) ]
       ↓
[ Role-Scoped Grounded Context Builder ]
       ↓
[ Pluggable AI Model Provider (AiModelProvider Interface) ]
       ↓
[ Deterministic Grounded Synthesis & Citation Engine ]
       ↓
[ Structured Response with Source Citations & Confidence Level ]
```

The AI component NEVER connects directly to JDBC/PostgreSQL, has no raw SQL execution capabilities, and cannot execute write/mutation queries. All business data is retrieved through compiled Spring service and repository methods that enforce role permissions.

## 2. Pluggable AI Model Provider Abstraction
The system decouples the platform from any single AI vendor via the `AiModelProvider` interface:

```java
public interface AiModelProvider {
    AiModelOutput generateResponse(String systemPrompt, String userQuery, RetrievedBusinessContext context);
    String getProviderName();
}
```

Implementations include:
- `RuleGroundedAiProvider`: High-speed deterministic enterprise reasoning engine with strict grounding in actual database entities.
- `LangChain4jAiProvider`: Neural LLM provider with role-based prompt containment and temperature-0 deterministic mode.

## 3. Grounded Context Retrieval
The `ManufacturingContextRetriever` inspects the user query and queries the application repositories:
1. **Production Order Inspection**: Pulls `ProductionOrder`, associated `ProductionOperation` sequences, work center allocations, planned vs actual dates, and quality metrology.
2. **Material Shortage & BOM Explosion**: Evaluates `EbomItem` and `MbomItem` requirements against current warehouse `InventoryStock` balances (`qtyAvailable < quantityRequired`).
3. **Supplier Risk & Delivery Tracking**: Computes overdue `PurchaseOrder` lines and supplier fulfilment ratios.
4. **Customer Orders at Risk**: Correlates sales orders with delayed production work orders or pending material purchase requests.

## 4. Insufficient Data & Hallucination Prevention
If a query targets an entity not present in the database (e.g. non-existent order code), the system triggers an explicit **Insufficient Data** response:
> *"I could not find records for order 'PO-9999' in the database. Please verify the order number."*

The system is forbidden from inventing or hallucinating ERP entity IDs, stock counts, or financial sums.

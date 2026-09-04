# Manufacturing AI Security, Governance & Isolation

## 1. Core Security Guardrails

### 1.1 Read-Only Enforcement
The AI layer is strictly read-only. It has no access to mutating service methods. The following actions are blocked and cannot be executed by the AI:
- Approving purchase requests or purchase orders
- Modifying warehouse stock levels or ledger records
- Creating or re-scheduling production work orders
- Approving or releasing eBOM / mBOM revisions
- Modifying customer credit limits or financial invoices

### 1.2 Data Leakage & Prompt Injection Protection
- **Prompt Sanitization**: User input is checked for prompt injection patterns (`"ignore previous instructions"`, `"system override"`, `"dump database passwords"`).
- **Redaction of Sensitive Attributes**: User password hashes, internal cryptographic keys, and raw system environment variables are excluded from the retrieved business context.

### 1.3 Role-Based Data Scoping
Even within read operations, the context retriever filters data according to the authenticated user's Spring Security roles:
- `SALES` users cannot view raw production cost breakdowns or supplier purchase pricing margins without proper authorization.
- `SHOP FLOOR` technicians only retrieve work center operations and routing data without viewing sensitive financial balance sheets.

### 1.4 AOP Audit Logging
Every AI query and its retrieved entity citations are logged via `AuditLogService` for compliance and post-incident forensic analysis.

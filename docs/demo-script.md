# End-to-End Demo Script: Siva Machine Works

Follow this script to demonstrate the complete, unbroken value chain of the digital manufacturing platform.

### Phase 1: Engineering & PDM
1. **Login** as `eng_user` (Engineering).
2. Navigate to **PDM > Products**. Create a new product: `SMW-HM-800` (Horizontal Machining Center).
3. Upload the 3D CAD step file (`HM800_Layout.step`).
4. Navigate to **eBOM**. Create an Engineering BOM for `SMW-HM-800`.
5. Submit the eBOM for engineering approval. The status changes to `RELEASED`.

### Phase 2: Manufacturing Preparation
6. **Login** as `prod_user` (Production/Manufacturing Engineer).
7. Navigate to **mBOM Transformation**. Select the released `SMW-HM-800` eBOM.
8. Perform the transformation: add manufacturing-specific consumables (e.g., `OIL-ISO-VG46` hydraulic fluid).
9. Approve the mBOM for the shop floor.

### Phase 3: CRM & Sales
10. **Login** as `sales_user` (Sales Executive).
11. Navigate to **CRM**. Create a customer: `Toyota Motor Corporation`.
12. Navigate to **Sales Orders**. Create a new Sales Order for 1 unit of `SMW-HM-800`.
13. **Confirm the Order**. 
    - *Showcase*: The system automatically explodes the mBOM, checks warehouse inventory, and reserves the available stock.

### Phase 4: Procurement & AI Extraction
14. **Login** as `proc_user` (Procurement).
15. Navigate to **Procurement**. Notice the system flagged shortages for components missing from inventory.
16. *Showcase AI*: Upload a Supplier Quotation PDF (e.g., from `YUKEN` for hydraulic pumps).
17. The AI extracts line items, pricing, and dates. Review the draft, approve it, and generate a **Purchase Order (PO)**.
18. Execute **Goods Receipt** when parts arrive to fulfill the shortage.

### Phase 5: Production Execution
19. **Login** as `prod_user` (Production).
20. Navigate to **Production Orders**. Open the Work Order for the `SMW-HM-800`.
21. Transition state to `IN_PROGRESS`.
22. Step through the routing operations (Fabrication -> Hydraulics -> Electrical -> Quality).
23. Execute the final Quality Inspection (Laser Interferometry FAT).
24. Click **Complete Production**. 
    - *Showcase*: The system consumes the reserved raw materials and increases the Finished Goods inventory.

### Phase 6: Executive Analytics & AI
25. **Login** as `admin` (Executive).
26. Navigate to **AI Assistant**.
27. Ask: *"What is the status of production order WO-2026?"*
    - *Showcase*: The AI retrieves real-time, accurate business context and synthesizes a status update without hallucinating.
28. Navigate to **Audit Logs** to show the immutable cryptographic trail of every approval, transformation, and stock movement made during the demo.

CREATE TABLE suppliers (
    id UUID PRIMARY KEY,
    supplier_code VARCHAR(50) UNIQUE NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    tax_id VARCHAR(100),
    country VARCHAR(100) DEFAULT 'Japan',
    contact_email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    payment_terms VARCHAR(100) DEFAULT 'Net 30',
    currency VARCHAR(10) DEFAULT 'JPY',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE supplier_parts (
    id UUID PRIMARY KEY,
    supplier_id UUID NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,
    part_number VARCHAR(100) NOT NULL,
    supplier_part_number VARCHAR(100),
    description TEXT,
    unit_price NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'JPY',
    lead_time_days INT DEFAULT 14,
    min_order_qty NUMERIC(12, 4) DEFAULT 1.0,
    is_preferred BOOLEAN DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(supplier_id, part_number)
);

CREATE TABLE purchase_requests (
    id UUID PRIMARY KEY,
    pr_code VARCHAR(50) UNIQUE NOT NULL,
    requested_by UUID REFERENCES users(id),
    source_type VARCHAR(30) DEFAULT 'MRP',
    sales_order_id UUID REFERENCES sales_orders(id) ON DELETE SET NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_estimated_amount NUMERIC(15, 2) DEFAULT 0,
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE purchase_request_items (
    id UUID PRIMARY KEY,
    purchase_request_id UUID NOT NULL REFERENCES purchase_requests(id) ON DELETE CASCADE,
    item_seq INT NOT NULL,
    part_number VARCHAR(100) NOT NULL,
    description TEXT,
    quantity_requested NUMERIC(12, 4) NOT NULL,
    uom VARCHAR(20) DEFAULT 'EA',
    estimated_unit_price NUMERIC(15, 2),
    required_by_date DATE,
    suggested_supplier_id UUID REFERENCES suppliers(id),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE purchase_orders (
    id UUID PRIMARY KEY,
    po_code VARCHAR(50) UNIQUE NOT NULL,
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    purchase_request_id UUID REFERENCES purchase_requests(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    order_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_delivery_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    subtotal_amount NUMERIC(15, 2) DEFAULT 0,
    tax_amount NUMERIC(15, 2) DEFAULT 0,
    total_amount NUMERIC(15, 2) DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'JPY',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE purchase_order_items (
    id UUID PRIMARY KEY,
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    item_seq INT NOT NULL,
    part_number VARCHAR(100) NOT NULL,
    description TEXT,
    quantity_ordered NUMERIC(12, 4) NOT NULL,
    quantity_received NUMERIC(12, 4) NOT NULL DEFAULT 0,
    uom VARCHAR(20) DEFAULT 'EA',
    unit_price NUMERIC(15, 2) NOT NULL,
    line_total NUMERIC(15, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ORDERED',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE goods_receipts (
    id UUID PRIMARY KEY,
    gr_code VARCHAR(50) UNIQUE NOT NULL,
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    delivery_note_no VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    received_by UUID REFERENCES users(id),
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE goods_receipt_items (
    id UUID PRIMARY KEY,
    goods_receipt_id UUID NOT NULL REFERENCES goods_receipts(id) ON DELETE CASCADE,
    purchase_order_item_id UUID NOT NULL REFERENCES purchase_order_items(id),
    item_seq INT NOT NULL,
    part_number VARCHAR(100) NOT NULL,
    quantity_received NUMERIC(12, 4) NOT NULL,
    uom VARCHAR(20) DEFAULT 'EA',
    unit_cost NUMERIC(15, 2),
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_suppliers_code ON suppliers(supplier_code);
CREATE INDEX idx_po_code ON purchase_orders(po_code);
CREATE INDEX idx_gr_code ON goods_receipts(gr_code);

-- Seed Suppliers
INSERT INTO suppliers (id, supplier_code, company_name, tax_id, country, contact_email, phone, address, payment_terms, status, created_at, updated_at)
VALUES
('b1111111-1111-1111-1111-111111111111', 'SUP-YUKEN', 'Yuken Kogyo Co., Ltd.', 'JP-987654321', 'Japan', 'sales@yuken-demo.co.jp', '+81-467-77-2111', 'Kanagawa, Japan', 'Net 30', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b2222222-2222-2222-2222-222222222222', 'SUP-SIEMENS-JP', 'Siemens Japan K.K.', 'JP-123456789', 'Japan', 'motion.jp@siemens-demo.com', '+81-3-3493-5500', 'Shinagawa, Tokyo, Japan', 'Net 45', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b3333333-3333-3333-3333-333333333333', 'SUP-KEYENCE', 'Keyence Corporation', 'JP-555555555', 'Japan', 'sensor@keyence-demo.co.jp', '+81-6-6379-1111', 'Osaka, Japan', 'Net 30', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Supplier Parts
INSERT INTO supplier_parts (id, supplier_id, part_number, supplier_part_number, description, unit_price, lead_time_days, min_order_qty, is_preferred, created_at, updated_at)
VALUES
('b1111111-1111-2222-1111-111111111111', 'b1111111-1111-1111-1111-111111111111', 'PUMP-HP-75', 'YK-A75-F-R-01', 'Variable Displacement Piston Pump 75cc', 120000.00, 21, 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b1111111-1111-2222-1111-222222222222', 'b1111111-1111-1111-1111-111111111111', 'VLV-PROP-350', 'YK-ELDFG-01-350', 'Proportional Directional Control Valve 350bar', 45000.00, 14, 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b2222222-2222-2222-1111-111111111111', 'b2222222-2222-2222-2222-222222222222', 'PLC-CNC-840D', '6FC5370-0AA00-0AA0', 'CNC Motion Controller 840D SL', 350000.00, 30, 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b2222222-2222-2222-1111-222222222222', 'b2222222-2222-2222-2222-222222222222', 'MTR-SRV-22KW', '1FT7102-5AF71-1FA0', 'AC Synchronous Servo Motor 22kW 150Nm', 85000.00, 21, 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b3333333-3333-2222-1111-111111111111', 'b3333333-3333-3333-3333-333333333333', 'SEN-LIN-ENC', 'KEY-SJ-E01', 'Optical Linear Scale Encoder 0.1um', 25000.00, 7, 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

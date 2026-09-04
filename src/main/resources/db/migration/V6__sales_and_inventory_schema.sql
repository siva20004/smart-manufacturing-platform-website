CREATE TABLE customers (
    id UUID PRIMARY KEY,
    customer_code VARCHAR(50) UNIQUE NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    industry VARCHAR(100),
    country VARCHAR(100) NOT NULL DEFAULT 'Japan',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    credit_limit NUMERIC(15, 2) DEFAULT 10000000.00,
    payment_terms VARCHAR(100) DEFAULT 'Net 30',
    contact_name VARCHAR(100),
    contact_email VARCHAR(100),
    phone VARCHAR(50),
    address TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE warehouses (
    id UUID PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    plant_location VARCHAR(50) NOT NULL,
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE inventory_stock (
    id UUID PRIMARY KEY,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    part_number VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    qty_on_hand NUMERIC(12, 4) NOT NULL DEFAULT 0,
    qty_reserved NUMERIC(12, 4) NOT NULL DEFAULT 0,
    qty_available NUMERIC(12, 4) NOT NULL DEFAULT 0,
    uom VARCHAR(20) DEFAULT 'EA',
    unit_cost NUMERIC(15, 2) DEFAULT 0,
    min_stock_level NUMERIC(12, 4) DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE (part_number, warehouse_id)
);

CREATE TABLE inventory_transactions (
    id UUID PRIMARY KEY,
    transaction_number VARCHAR(50) UNIQUE NOT NULL,
    part_number VARCHAR(100) NOT NULL,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    transaction_type VARCHAR(30) NOT NULL,
    quantity NUMERIC(12, 4) NOT NULL,
    unit_cost NUMERIC(15, 2),
    reference_type VARCHAR(50),
    reference_id UUID,
    notes TEXT,
    performed_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quotations (
    id UUID PRIMARY KEY,
    quotation_code VARCHAR(50) NOT NULL,
    revision_letter VARCHAR(5) NOT NULL DEFAULT 'A',
    customer_id UUID NOT NULL REFERENCES customers(id),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    subtotal_amount NUMERIC(15, 2) DEFAULT 0,
    tax_amount NUMERIC(15, 2) DEFAULT 0,
    total_amount NUMERIC(15, 2) DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'JPY',
    valid_until DATE,
    payment_terms VARCHAR(100) DEFAULT 'Net 30',
    delivery_incoterms VARCHAR(50) DEFAULT 'EXW',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(quotation_code, revision_letter)
);

CREATE TABLE quotation_items (
    id UUID PRIMARY KEY,
    quotation_id UUID NOT NULL REFERENCES quotations(id) ON DELETE CASCADE,
    item_seq INT NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity NUMERIC(12, 4) NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    line_total NUMERIC(15, 2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE sales_orders (
    id UUID PRIMARY KEY,
    so_code VARCHAR(50) UNIQUE NOT NULL,
    quotation_id UUID REFERENCES quotations(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    customer_po_number VARCHAR(100),
    order_date DATE NOT NULL,
    requested_delivery_date DATE,
    plant_location VARCHAR(50) NOT NULL DEFAULT 'Osaka',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    subtotal_amount NUMERIC(15, 2) DEFAULT 0,
    tax_amount NUMERIC(15, 2) DEFAULT 0,
    total_amount NUMERIC(15, 2) DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'JPY',
    shipping_address TEXT,
    special_instructions TEXT,
    confirmed_by UUID REFERENCES users(id),
    confirmed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE sales_order_items (
    id UUID PRIMARY KEY,
    sales_order_id UUID NOT NULL REFERENCES sales_orders(id) ON DELETE CASCADE,
    item_seq INT NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity NUMERIC(12, 4) NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    line_total NUMERIC(15, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(sales_order_id, item_seq)
);

CREATE TABLE inventory_reservations (
    id UUID PRIMARY KEY,
    sales_order_id UUID REFERENCES sales_orders(id) ON DELETE CASCADE,
    production_order_id UUID,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    part_number VARCHAR(100) NOT NULL,
    quantity_reserved NUMERIC(12, 4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    reserved_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE material_requirements (
    id UUID PRIMARY KEY,
    run_number VARCHAR(50) UNIQUE NOT NULL,
    sales_order_id UUID NOT NULL REFERENCES sales_orders(id) ON DELETE CASCADE,
    triggered_by UUID REFERENCES users(id),
    run_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    run_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE material_requirement_items (
    id UUID PRIMARY KEY,
    requirement_id UUID NOT NULL REFERENCES material_requirements(id) ON DELETE CASCADE,
    part_number VARCHAR(100) NOT NULL,
    description TEXT,
    required_qty NUMERIC(12, 4) NOT NULL,
    available_qty NUMERIC(12, 4) NOT NULL,
    shortage_qty NUMERIC(12, 4) NOT NULL,
    uom VARCHAR(20) DEFAULT 'EA',
    status VARCHAR(30) NOT NULL DEFAULT 'SHORTAGE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_code ON customers(customer_code);
CREATE INDEX idx_warehouses_code ON warehouses(code);
CREATE INDEX idx_inventory_stock_wh ON inventory_stock(warehouse_id, part_number);
CREATE INDEX idx_quotations_code ON quotations(quotation_code);
CREATE INDEX idx_sales_orders_code ON sales_orders(so_code);
CREATE INDEX idx_sales_orders_cust ON sales_orders(customer_id);
CREATE INDEX idx_inv_res_so ON inventory_reservations(sales_order_id);
CREATE INDEX idx_mat_req_so ON material_requirements(sales_order_id);

-- Seed Customers
INSERT INTO customers (id, customer_code, company_name, industry, country, status, credit_limit, payment_terms, contact_name, contact_email, phone, address, created_at, updated_at)
VALUES 
('c1111111-1111-1111-1111-111111111111', 'CUST-TOYOTA', 'Toyota Motor Corporation', 'Automotive', 'Japan', 'ACTIVE', 50000000.00, 'Net 60', 'Kenji Sato', 'k.sato@toyota-demo.jp', '+81-565-28-2121', '1 Toyota-cho, Toyota City, Aichi 471-8571', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c2222222-2222-2222-2222-222222222222', 'CUST-KOMATSU', 'Komatsu Ltd.', 'Heavy Equipment', 'Japan', 'ACTIVE', 30000000.00, 'Net 45', 'Hiroshi Tanaka', 'h.tanaka@komatsu-demo.jp', '+81-3-5561-2616', '2-3-6 Akasaka, Minato-ku, Tokyo 107-8414', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Warehouses
INSERT INTO warehouses (id, code, name, plant_location, address, is_active, created_at, updated_at)
VALUES
('d1111111-1111-1111-1111-111111111111', 'WH-OSK', 'Osaka Plant Main Finished Goods & Parts Warehouse', 'Osaka', 'Osaka Precision Industrial Zone, Osaka', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d2222222-2222-2222-2222-222222222222', 'WH-NGY', 'Nagoya Heavy Machinery Hub', 'Nagoya', 'Nagoya Port Logistics District, Aichi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d3333333-3333-3333-3333-333333333333', 'WH-PEN', 'Penang ASEAN Assembly Depot', 'Penang', 'Bayan Lepas Industrial Zone, Penang', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Initial Inventory Stock for Osaka Warehouse (WH-OSK)
INSERT INTO inventory_stock (id, warehouse_id, part_number, description, qty_on_hand, qty_reserved, qty_available, uom, unit_cost, min_stock_level, created_at, updated_at)
VALUES
('f1111111-1111-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111111', 'PUMP-HP-75', 'Variable Displacement Piston Pump 75cc', 10.0000, 0.0000, 10.0000, 'EA', 120000.00, 2.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f1111111-2222-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111111', 'VLV-PROP-350', 'Proportional Directional Control Valve 350bar', 20.0000, 0.0000, 20.0000, 'EA', 45000.00, 5.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f1111111-3333-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111111', 'PLC-CNC-840D', 'CNC Motion Controller 840D SL', 5.0000, 0.0000, 5.0000, 'EA', 350000.00, 1.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f1111111-4444-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111111', 'MTR-SRV-22KW', 'AC Synchronous Servo Motor 22kW 150Nm', 15.0000, 0.0000, 15.0000, 'EA', 85000.00, 3.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f1111111-5555-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111111', 'SEN-LIN-ENC', 'Optical Linear Scale Encoder 0.1um', 20.0000, 0.0000, 20.0000, 'EA', 25000.00, 5.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

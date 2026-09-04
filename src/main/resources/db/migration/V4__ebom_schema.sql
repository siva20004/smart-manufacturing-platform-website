CREATE TABLE ebom_headers (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    revision_code VARCHAR(20) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE (product_id, revision_code)
);

CREATE TABLE ebom_items (
    id UUID PRIMARY KEY,
    ebom_header_id UUID NOT NULL REFERENCES ebom_headers(id) ON DELETE CASCADE,
    parent_item_id UUID REFERENCES ebom_items(id) ON DELETE CASCADE,
    item_seq INT NOT NULL,
    part_number VARCHAR(100) NOT NULL,
    description TEXT,
    item_type VARCHAR(30) NOT NULL DEFAULT 'COMPONENT',
    quantity NUMERIC(12, 4) NOT NULL,
    uom VARCHAR(20) DEFAULT 'EA',
    document_id UUID,
    effective_start_date DATE,
    effective_end_date DATE,
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_ebom_headers_product ON ebom_headers(product_id);
CREATE INDEX idx_ebom_items_header ON ebom_items(ebom_header_id);
CREATE INDEX idx_ebom_items_parent ON ebom_items(parent_item_id);

-- Seed hierarchical sample eBOM for HM-500
-- HM-500 (id: a1111111-1111-1111-1111-111111111111)
INSERT INTO ebom_headers (id, product_id, revision_code, description, status, version, created_at, updated_at)
VALUES ('e1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', 'A', 'Engineering BOM for SMW-HM-500 Rev A', 'RELEASED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Top-level subsystems:
-- 1. Hydraulic System
INSERT INTO ebom_items (id, ebom_header_id, parent_item_id, item_seq, part_number, description, item_type, quantity, uom, version, created_at, updated_at)
VALUES ('e1111111-2222-1111-1111-111111111111', 'e1111111-1111-1111-1111-111111111111', NULL, 10, 'SYS-HYD-500', 'Hydraulic Power Subsystem', 'ASSEMBLY', 1.0000, 'EA', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 1.1 Pump (child of Hydraulic System)
INSERT INTO ebom_items (id, ebom_header_id, parent_item_id, item_seq, part_number, description, item_type, quantity, uom, version, created_at, updated_at)
VALUES ('e1111111-2222-1111-1111-222222222222', 'e1111111-1111-1111-1111-111111111111', 'e1111111-2222-1111-1111-111111111111', 1, 'PUMP-HP-75', 'Variable Displacement Piston Pump 75cc', 'COMPONENT', 1.0000, 'EA', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 1.2 Valve (child of Hydraulic System)
INSERT INTO ebom_items (id, ebom_header_id, parent_item_id, item_seq, part_number, description, item_type, quantity, uom, version, created_at, updated_at)
VALUES ('e1111111-2222-1111-1111-333333333333', 'e1111111-1111-1111-1111-111111111111', 'e1111111-2222-1111-1111-111111111111', 2, 'VLV-PROP-350', 'Proportional Directional Control Valve 350bar', 'COMPONENT', 2.0000, 'EA', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Electrical System
INSERT INTO ebom_items (id, ebom_header_id, parent_item_id, item_seq, part_number, description, item_type, quantity, uom, version, created_at, updated_at)
VALUES ('e1111111-3333-1111-1111-111111111111', 'e1111111-1111-1111-1111-111111111111', NULL, 20, 'SYS-ELEC-500', 'Electrical Control & Drive System', 'ASSEMBLY', 1.0000, 'EA', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2.1 Controller (child of Electrical System)
INSERT INTO ebom_items (id, ebom_header_id, parent_item_id, item_seq, part_number, description, item_type, quantity, uom, version, created_at, updated_at)
VALUES ('e1111111-3333-1111-1111-222222222222', 'e1111111-1111-1111-1111-111111111111', 'e1111111-3333-1111-1111-111111111111', 1, 'PLC-CNC-840D', 'CNC Motion Controller 840D SL', 'COMPONENT', 1.0000, 'EA', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2.2 Motor (child of Electrical System)
INSERT INTO ebom_items (id, ebom_header_id, parent_item_id, item_seq, part_number, description, item_type, quantity, uom, version, created_at, updated_at)
VALUES ('e1111111-3333-1111-1111-333333333333', 'e1111111-1111-1111-1111-111111111111', 'e1111111-3333-1111-1111-111111111111', 2, 'MTR-SRV-22KW', 'AC Synchronous Servo Motor 22kW 150Nm', 'COMPONENT', 3.0000, 'EA', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2.3 Sensor (child of Electrical System)
INSERT INTO ebom_items (id, ebom_header_id, parent_item_id, item_seq, part_number, description, item_type, quantity, uom, version, created_at, updated_at)
VALUES ('e1111111-3333-1111-1111-444444444444', 'e1111111-1111-1111-1111-111111111111', 'e1111111-3333-1111-1111-111111111111', 3, 'SEN-LIN-ENC', 'Optical Linear Scale Encoder 0.1um', 'COMPONENT', 3.0000, 'EA', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

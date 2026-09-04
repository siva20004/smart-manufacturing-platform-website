CREATE TABLE work_centers (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    plant_location VARCHAR(50) NOT NULL,
    capacity_hours_per_day NUMERIC(5, 2) DEFAULT 16.0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE production_orders (
    id UUID PRIMARY KEY,
    order_code VARCHAR(50) UNIQUE NOT NULL,
    sales_order_id UUID REFERENCES sales_orders(id),
    product_id UUID NOT NULL REFERENCES products(id),
    mbom_header_id UUID REFERENCES mbom_headers(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    plant_location VARCHAR(50) NOT NULL DEFAULT 'Osaka',
    quantity_planned NUMERIC(12, 4) NOT NULL,
    quantity_completed NUMERIC(12, 4) NOT NULL DEFAULT 0,
    planned_start_date DATE,
    planned_completion_date DATE,
    actual_start_date TIMESTAMP WITH TIME ZONE,
    actual_completion_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE production_operations (
    id UUID PRIMARY KEY,
    production_order_id UUID NOT NULL REFERENCES production_orders(id) ON DELETE CASCADE,
    operation_seq INT NOT NULL,
    operation_name VARCHAR(100) NOT NULL,
    work_center_code VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    planned_hours NUMERIC(8, 2),
    actual_hours NUMERIC(8, 2),
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    assigned_to UUID REFERENCES users(id),
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(production_order_id, operation_seq)
);

CREATE TABLE material_consumptions (
    id UUID PRIMARY KEY,
    production_order_id UUID NOT NULL REFERENCES production_orders(id) ON DELETE CASCADE,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    part_number VARCHAR(100) NOT NULL,
    description TEXT,
    planned_qty NUMERIC(12, 4) NOT NULL,
    actual_qty_consumed NUMERIC(12, 4) NOT NULL,
    uom VARCHAR(20) DEFAULT 'EA',
    consumed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    consumed_by UUID REFERENCES users(id)
);

CREATE TABLE quality_inspections (
    id UUID PRIMARY KEY,
    inspection_code VARCHAR(50) UNIQUE NOT NULL,
    production_order_id UUID NOT NULL REFERENCES production_orders(id),
    inspection_type VARCHAR(30) NOT NULL DEFAULT 'FINAL_ACCEPTANCE',
    status VARCHAR(30) NOT NULL DEFAULT 'PASSED',
    fat_spindle_runout_mm NUMERIC(8, 4),
    fat_positioning_accuracy_mm NUMERIC(8, 4),
    inspector_id UUID REFERENCES users(id),
    inspected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_po_code_order ON production_orders(order_code);
CREATE INDEX idx_po_status ON production_orders(status);
CREATE INDEX idx_qi_code ON quality_inspections(inspection_code);

-- Seed Work Centers
INSERT INTO work_centers (id, code, name, plant_location, capacity_hours_per_day, is_active, created_at, updated_at)
VALUES
('e1111111-1111-1111-1111-111111111111', 'WC-FAB-01', 'Heavy Structural Fabrication & Welding', 'Osaka', 16.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e2222222-2222-2222-2222-222222222222', 'WC-HYD-01', 'Hydraulic Manifold & Power Unit Assembly', 'Osaka', 16.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e3333333-3333-3333-3333-333333333333', 'WC-ELEC-01', 'CNC Cabinet Integration & Servo Wiring', 'Osaka', 16.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e4444444-4444-4444-4444-444444444444', 'WC-FIN-01', 'Final Mechanical Integration & Alignment', 'Osaka', 16.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5555555-5555-5555-5555-555555555555', 'WC-QC-01', 'Precision Metrology & Laser Calibration', 'Osaka', 16.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Initial Production Order for Flagship HM-500
INSERT INTO production_orders (id, order_code, product_id, warehouse_id, plant_location, quantity_planned, quantity_completed, planned_start_date, planned_completion_date, status, created_at, updated_at)
VALUES
('7d100458-f581-4665-b336-21e281f8e3c2', 'PRD-2026-SEED-01', 'a1111111-1111-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111111', 'Osaka', 1.0, 0, '2026-09-02', '2026-09-17', 'PLANNED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

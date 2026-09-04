CREATE TABLE customer_contacts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    job_title VARCHAR(100),
    department VARCHAR(100),
    is_primary BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE crm_opportunities (
    id UUID PRIMARY KEY,
    opportunity_code VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    primary_contact_id UUID REFERENCES customer_contacts(id),
    product_id UUID REFERENCES products(id),
    name VARCHAR(255) NOT NULL,
    stage VARCHAR(30) NOT NULL DEFAULT 'PROSPECTING',
    estimated_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'JPY',
    probability_pct NUMERIC(5, 2) DEFAULT 20.0,
    expected_close_date DATE,
    actual_close_date DATE,
    lost_reason TEXT,
    assigned_rep_id UUID REFERENCES users(id),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE service_requests (
    id UUID PRIMARY KEY,
    ticket_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    product_id UUID REFERENCES products(id),
    machine_serial_no VARCHAR(100),
    title VARCHAR(255) NOT NULL,
    reported_issue TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    assigned_technician_id UUID REFERENCES users(id),
    resolution_notes TEXT,
    resolved_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_contacts_cust ON customer_contacts(customer_id);
CREATE INDEX idx_opps_cust ON crm_opportunities(customer_id);
CREATE INDEX idx_opps_stage ON crm_opportunities(stage);
CREATE INDEX idx_srv_cust ON service_requests(customer_id);
CREATE INDEX idx_srv_status ON service_requests(status);

-- Seed Contacts
INSERT INTO customer_contacts (id, customer_id, first_name, last_name, email, phone, job_title, department, is_primary, created_at, updated_at)
VALUES
('81111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111', 'Kenji', 'Sato', 'k.sato@toyota-demo.jp', '+81-565-28-2121', 'Senior Plant Manager', 'Powertrain Machining Div', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('82222222-2222-2222-2222-222222222222', 'c2222222-2222-2222-2222-222222222222', 'Hiroshi', 'Tanaka', 'h.tanaka@komatsu-demo.jp', '+81-3-5561-2616', 'Chief Procurement Officer', 'Global SCM Div', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Opportunities
INSERT INTO crm_opportunities (id, opportunity_code, customer_id, primary_contact_id, product_id, name, stage, estimated_value, currency, probability_pct, expected_close_date, created_at, updated_at)
VALUES
('83333333-3333-3333-3333-333333333333', 'OPP-2026-TOYOTA-01', 'c1111111-1111-1111-1111-111111111111', '81111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', 'Toyota Motomachi Line 4 Expansion - 5x SMW-HM-500', 'PROPOSAL', 62500000.00, 'JPY', 75.0, '2026-10-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('84444444-4444-4444-4444-444444444444', 'OPP-2026-KOMATSU-01', 'c2222222-2222-2222-2222-222222222222', '82222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222', 'Komatsu Osaka Heavy Machining Facility Upgrade - 2x SMW-HM-700', 'NEGOTIATION', 36000000.00, 'JPY', 90.0, '2026-09-30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

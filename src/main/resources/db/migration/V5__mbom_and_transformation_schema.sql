CREATE TABLE mbom_headers (
    id UUID PRIMARY KEY,
    ebom_header_id UUID NOT NULL REFERENCES ebom_headers(id) ON DELETE RESTRICT,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    revision_code VARCHAR(20) NOT NULL,
    description TEXT,
    plant_location VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE (product_id, revision_code, plant_location)
);

CREATE TABLE mbom_items (
    id UUID PRIMARY KEY,
    mbom_header_id UUID NOT NULL REFERENCES mbom_headers(id) ON DELETE CASCADE,
    parent_item_id UUID REFERENCES mbom_items(id) ON DELETE CASCADE,
    source_ebom_item_id UUID REFERENCES ebom_items(id) ON DELETE SET NULL,
    item_seq INT NOT NULL,
    part_number VARCHAR(100) NOT NULL,
    description TEXT,
    item_type VARCHAR(30) NOT NULL DEFAULT 'COMPONENT',
    quantity NUMERIC(12, 4) NOT NULL,
    uom VARCHAR(20) DEFAULT 'EA',
    work_center VARCHAR(50),
    operation_seq INT,
    operation_name VARCHAR(100),
    is_consumed_per_unit BOOLEAN DEFAULT true,
    scrap_factor NUMERIC(5, 4) DEFAULT 0.0000,
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE bom_mappings (
    id UUID PRIMARY KEY,
    ebom_header_id UUID NOT NULL REFERENCES ebom_headers(id) ON DELETE CASCADE,
    mbom_header_id UUID NOT NULL REFERENCES mbom_headers(id) ON DELETE CASCADE,
    transformation_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    rules_applied TEXT,
    transformed_by UUID REFERENCES users(id),
    transformed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_mbom_headers_product ON mbom_headers(product_id);
CREATE INDEX idx_mbom_headers_ebom ON mbom_headers(ebom_header_id);
CREATE INDEX idx_mbom_items_header ON mbom_items(mbom_header_id);
CREATE INDEX idx_mbom_items_parent ON mbom_items(parent_item_id);
CREATE INDEX idx_mbom_items_source_ebom ON mbom_items(source_ebom_item_id);
CREATE INDEX idx_bom_mappings_ebom ON bom_mappings(ebom_header_id);
CREATE INDEX idx_bom_mappings_mbom ON bom_mappings(mbom_header_id);

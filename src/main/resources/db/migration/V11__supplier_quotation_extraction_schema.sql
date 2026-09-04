CREATE TABLE supplier_quotation_extractions (
    id UUID PRIMARY KEY,
    doc_file_name VARCHAR(255) NOT NULL,
    raw_text TEXT NOT NULL,
    supplier_id UUID REFERENCES suppliers(id),
    supplier_name_extracted VARCHAR(255),
    quotation_number VARCHAR(100),
    part_number VARCHAR(100),
    quantity NUMERIC(15, 4),
    unit_price NUMERIC(15, 2),
    currency VARCHAR(10) DEFAULT 'JPY',
    delivery_date DATE,
    payment_terms VARCHAR(255),
    confidence_score NUMERIC(5, 4) NOT NULL DEFAULT 0.0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_REVIEW',
    validation_warnings TEXT,
    reviewer_id UUID REFERENCES users(id),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    review_notes TEXT,
    generated_po_id UUID REFERENCES purchase_orders(id),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_sqe_status ON supplier_quotation_extractions(status);
CREATE INDEX idx_sqe_supplier ON supplier_quotation_extractions(supplier_id);
CREATE INDEX idx_sqe_quo_num ON supplier_quotation_extractions(quotation_number);

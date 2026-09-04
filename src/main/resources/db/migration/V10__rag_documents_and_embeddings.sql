CREATE TABLE rag_documents (
    id UUID PRIMARY KEY,
    doc_code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    doc_type VARCHAR(50) NOT NULL,
    product_id UUID REFERENCES products(id),
    version_number INT NOT NULL DEFAULT 1,
    mime_type VARCHAR(100) DEFAULT 'application/pdf',
    file_size_bytes BIGINT DEFAULT 1024000,
    allowed_roles VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE rag_document_chunks (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES rag_documents(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    embedding_vector TEXT,
    token_count INT NOT NULL DEFAULT 100,
    allowed_roles VARCHAR(255) NOT NULL,
    section_title VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_rag_doc_code ON rag_documents(doc_code);
CREATE INDEX idx_rag_doc_type ON rag_documents(doc_type);
CREATE INDEX idx_rag_chunk_doc ON rag_document_chunks(document_id);

-- Seed Initial Internal Company Documents
INSERT INTO rag_documents (id, doc_code, title, doc_type, product_id, version_number, allowed_roles, created_at, updated_at)
VALUES
('91111111-1111-1111-1111-111111111111', 'DOC-ENG-HM500-01', 'SMW-HM-500 Spindle & Hydrostatic Bearing Calibration Manual', 'ENGINEERING_MANUAL', 'a1111111-1111-1111-1111-111111111111', 1, 'ENGINEERING,PRODUCTION,ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('92222222-2222-2222-2222-222222222222', 'DOC-IT-INFRA-01', 'Internal Monolith Disaster Recovery & Database Key Management Runbook', 'IT_DOCUMENTATION', NULL, 1, 'IT_ENGINEER,ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('93333333-3333-3333-3333-333333333333', 'DOC-SCM-YUKEN-01', 'Yuken Kogyo Hydraulic Pump Supply & Warranty Agreement 2026', 'SUPPLIER_DOCUMENT', NULL, 1, 'PROCUREMENT,MANAGEMENT,ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('94444444-4444-4444-4444-444444444444', 'DOC-QMS-FAT-01', 'JIS B 6338 Factory Acceptance Metrology & Laser Calibration Standard', 'QUALITY_PROCEDURE', 'a1111111-1111-1111-1111-111111111111', 1, 'PRODUCTION,ENGINEERING,ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Chunks for DOC-ENG-HM500-01
INSERT INTO rag_document_chunks (id, document_id, chunk_index, content, token_count, allowed_roles, section_title, created_at, updated_at)
VALUES
('91111111-1111-1111-1111-111111111112', '91111111-1111-1111-1111-111111111111', 1, 'The SMW-HM-500 horizontal machining center features a high-rigidity 12,000 RPM motorized built-in spindle with active oil-air lubrication. Maximum allowable dynamic runout is 0.0020 mm (2.0 microns) measured at the spindle nose taper.', 120, 'ENGINEERING,PRODUCTION,ADMIN', 'Section 3.2 Spindle Tolerance Specifications', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('91111111-1111-1111-1111-111111111113', '91111111-1111-1111-1111-111111111111', 2, 'Hydrostatic guideway hydraulic pre-charge pressure must be maintained at 140 bar using ISO VG 32 synthetic hydraulic oil. Filter elements must be inspected every 500 operating hours to prevent servo axis cavitation.', 110, 'ENGINEERING,PRODUCTION,ADMIN', 'Section 4.1 Hydraulic Pressure Calibration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Chunks for DOC-IT-INFRA-01
INSERT INTO rag_document_chunks (id, document_id, chunk_index, content, token_count, allowed_roles, section_title, created_at, updated_at)
VALUES
('92222222-2222-2222-2222-222222222223', '92222222-2222-2222-2222-222222222222', 1, 'Platform infrastructure disaster recovery procedure: PostgreSQL WAL archiving replicates transactions to multi-region cold storage. Encryption keys are rotated automatically every 90 days via AWS KMS.', 115, 'IT_ENGINEER,ADMIN', 'Section 2.4 Disaster Recovery & Key Rotation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Chunks for DOC-SCM-YUKEN-01
INSERT INTO rag_document_chunks (id, document_id, chunk_index, content, token_count, allowed_roles, section_title, created_at, updated_at)
VALUES
('93333333-3333-3333-3333-333333333334', '93333333-3333-3333-3333-333333333333', 1, 'Yuken Kogyo Co., Ltd. guarantees a maximum lead time of 4 weeks for PUMP-HP-75 displacement pumps delivered directly to the Osaka Plant depot. Defective parts carry a 36-month full replacement warranty.', 105, 'PROCUREMENT,MANAGEMENT,ADMIN', 'Section 5.0 Lead Time & Warranty Commitment', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

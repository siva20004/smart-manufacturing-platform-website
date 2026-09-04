CREATE TABLE products (
    id UUID PRIMARY KEY,
    product_number VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    uom VARCHAR(20) DEFAULT 'EA',
    list_price NUMERIC(15, 2),
    standard_cost NUMERIC(15, 2),
    lead_time_weeks INT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE product_revisions (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    revision_number VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    change_description TEXT,
    effective_date DATE,
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(product_id, revision_number)
);

CREATE TABLE documents (
    id UUID PRIMARY KEY,
    document_number VARCHAR(100) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    doc_type VARCHAR(50) NOT NULL,
    product_id UUID REFERENCES products(id) ON DELETE SET NULL,
    is_restricted BOOLEAN DEFAULT false,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE document_versions (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    revision_code VARCHAR(20),
    storage_location VARCHAR(500) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_hash_sha256 VARCHAR(64),
    cad_metadata TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(document_id, version_number)
);

-- Seed sample products
INSERT INTO products (id, product_number, name, category, status, version, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111111', 'SMW-HM-500', 'Horizontal Machining Center 500', 'MACHINERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_revisions (id, product_id, revision_number, status, version, created_at, updated_at)
VALUES ('b1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', 'A', 'RELEASED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (id, product_number, name, category, status, version, created_at, updated_at)
VALUES ('a2222222-2222-2222-2222-222222222222', 'SMW-HM-700', 'Horizontal Machining Center 700', 'MACHINERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_revisions (id, product_id, revision_number, status, version, created_at, updated_at)
VALUES ('b2222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222', 'A', 'RELEASED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO documents (id, document_number, title, doc_type, product_id, version, created_at, updated_at)
VALUES ('c1111111-1111-1111-1111-111111111111', 'DWG-HM-500-01', 'Main Assembly Drawing 500', 'DRAWING', 'a1111111-1111-1111-1111-111111111111', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO document_versions (id, document_id, version_number, revision_code, storage_location, storage_key, file_name, file_size_bytes, mime_type, status, version, created_at, updated_at)
VALUES ('d1111111-1111-1111-1111-111111111111', 'c1111111-1111-1111-1111-111111111111', 1, 'A', 'local', 'docs/DWG-HM-500-01.pdf', 'DWG-HM-500-01.pdf', 1024, 'application/pdf', 'RELEASED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO documents (id, document_number, title, doc_type, product_id, version, created_at, updated_at)
VALUES ('c2222222-2222-2222-2222-222222222222', 'DWG-HM-700-01', 'Main Assembly Drawing 700', 'DRAWING', 'a2222222-2222-2222-2222-222222222222', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO document_versions (id, document_id, version_number, revision_code, storage_location, storage_key, file_name, file_size_bytes, mime_type, status, version, created_at, updated_at)
VALUES ('d2222222-2222-2222-2222-222222222222', 'c2222222-2222-2222-2222-222222222222', 1, 'A', 'local', 'docs/DWG-HM-700-01.pdf', 'DWG-HM-700-01.pdf', 2048, 'application/pdf', 'RELEASED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

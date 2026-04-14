CREATE TABLE rate_limit_rules (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    key_type VARCHAR(50) NOT NULL,
    limit_value INT NOT NULL,
    window_size_seconds INT NOT NULL,
    window_type VARCHAR(50) NOT NULL DEFAULT 'token_bucket',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_tenant_name UNIQUE (tenant_id, name)
);

CREATE INDEX idx_rules_tenant_enabled ON rate_limit_rules (tenant_id, enabled);

-- A rule is a policy: "capacity requests per window_seconds". The thing being limited (a user id, an API key, an IP)
-- is not stored here; the caller sends it as `key` on every check, so one rule covers any number of users.
CREATE TABLE IF NOT EXISTS rules (
    id             UUID PRIMARY KEY,
    tenant_id      UUID         NOT NULL,
    name           VARCHAR(100) NOT NULL,
    capacity       INT          NOT NULL CHECK (capacity > 0),
    window_seconds INT          NOT NULL CHECK (window_seconds > 0),
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL
);

CREATE INDEX IF NOT EXISTS rules_tenant ON rules (tenant_id);

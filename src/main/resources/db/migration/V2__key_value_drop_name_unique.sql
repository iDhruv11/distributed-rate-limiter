ALTER TABLE rate_limit_rules ADD COLUMN key_value VARCHAR(255);

ALTER TABLE rate_limit_rules DROP CONSTRAINT IF EXISTS uq_tenant_name;

UPDATE rate_limit_rules SET key_value = 'unknown' WHERE key_value IS NULL;

ALTER TABLE rate_limit_rules ALTER COLUMN key_value SET NOT NULL;

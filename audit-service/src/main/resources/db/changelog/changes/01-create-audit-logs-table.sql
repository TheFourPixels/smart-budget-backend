CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    transaction_id BIGINT,
    old_category_id BIGINT,
    new_category_id BIGINT,
    timestamp TIMESTAMP NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_transaction_id ON audit_logs(transaction_id);
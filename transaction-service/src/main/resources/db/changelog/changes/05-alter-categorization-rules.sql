ALTER TABLE categorization_rules ADD COLUMN rule_type VARCHAR(50) DEFAULT 'MERCHANT';
ALTER TABLE categorization_rules ADD COLUMN priority INT DEFAULT 1;

-- liquibase formatted sql
-- changeset lb200:9
ALTER TABLE budgets ADD COLUMN spending_limit DECIMAL(19, 4);

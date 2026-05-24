INSERT INTO categories (id, user_id, name, is_system)
VALUES (-1, 0, 'Разделенная транзакция', TRUE)
ON CONFLICT (id) DO NOTHING;

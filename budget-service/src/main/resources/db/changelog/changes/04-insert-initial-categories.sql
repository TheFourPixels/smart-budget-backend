-- db/changelog/changes/04-insert-initial-categories.sql
--liquibase formatted sql

--changeset teamfourpixels:4
-- Вставляем 8 системных категорий (id с 1 по 8)
INSERT INTO categories (id, user_id, name, is_system)
VALUES
    (1, 0, 'Продукты', TRUE),
    (2, 0, 'Связь/Интернет', TRUE),
    (3, 0, 'Одежда', TRUE),
    (4, 0, 'Проезд', TRUE),
    (5, 0, 'Развлечения', TRUE),
    (6, 0, 'Медицина', TRUE),
    (7, 0, 'Красота', TRUE),
    (8, 0, 'Коммунальные услуги', TRUE)
ON CONFLICT (id) DO NOTHING;

-- ВАЖНО: Устанавливаем следующий ID для пользовательских категорий.
-- Пользовательские категории начнутся с ID = 9 (MAX ID + 1)
SELECT setval('categories_id_seq', (SELECT MAX(id) + 1 FROM categories));
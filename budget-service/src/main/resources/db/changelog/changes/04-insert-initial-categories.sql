INSERT INTO categories (id, user_id, name, is_system)
VALUES
    (1, 0, 'Продукты', TRUE),
    (2, 0, 'Связь/Интернет', TRUE),
    (3, 0, 'Одежда', TRUE),
    (4, 0, 'Проезд', TRUE),
    (5, 0, 'Развлечения', TRUE),
    (6, 0, 'Медицина', TRUE),
    (7, 0, 'Красота', TRUE),
    (8, 0, 'Коммунальные услуги', TRUE),
    (9, 0, 'Образование', TRUE),
    (10, 0, 'Рестораны', TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval('categories_id_seq', (SELECT MAX(id) + 1 FROM categories));
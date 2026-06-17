ALTER TABLE categories DROP CONSTRAINT categories_name_key;

ALTER TABLE categories ADD CONSTRAINT categories_user_id_name_key UNIQUE (user_id, name);
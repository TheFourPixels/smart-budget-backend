CREATE TABLE strategy_priorities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    strategy_name VARCHAR(255) NOT NULL,
    priority INT NOT NULL,
    UNIQUE(user_id, strategy_name)
);

-- 6. Refresh Tokens Table
CREATE TABLE refresh_tokens (
                                id          BIGSERIAL   PRIMARY KEY,
                                token       TEXT        NOT NULL UNIQUE,
                                user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                expiry_date TIMESTAMPTZ NOT NULL
);

-- Performance Indexes
CREATE INDEX idx_refresh_token_lookup ON refresh_tokens (token);
CREATE INDEX idx_refresh_token_user ON refresh_tokens (user_id);
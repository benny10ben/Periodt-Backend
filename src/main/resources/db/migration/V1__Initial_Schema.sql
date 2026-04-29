-- 1. Users Table
CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       email         TEXT      NOT NULL UNIQUE,
                       password_hash TEXT      NOT NULL,
                       created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                       last_login_at TIMESTAMPTZ
);

-- 2. Sync Data Table (The Encrypted Locker)
CREATE TABLE sync_data (
                           sync_uuid         UUID        PRIMARY KEY,
                           user_id           BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           entity_type       TEXT        NOT NULL,          -- e.g., 'CYCLE', 'PILL'
                           encrypted_payload TEXT        NOT NULL,          -- AES-GCM Ciphertext
                           server_version    BIGSERIAL   NOT NULL,          -- Monotonic cursor for sync logic
                           is_deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
                           created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3. Wrapped Keys Table
CREATE TABLE wrapped_keys (
                              user_id           BIGINT  PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                              wrapped_data_key  TEXT    NOT NULL,              -- Data Key wrapped by Account Key
                              key_version       INT     NOT NULL DEFAULT 1,
                              updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 4. Devices Table
CREATE TABLE devices (
                         id             BIGSERIAL   PRIMARY KEY,
                         user_id        BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         device_id      TEXT        NOT NULL,
                         device_name    TEXT,
                         last_cursor    BIGINT      NOT NULL DEFAULT 0,
                         registered_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                         last_seen_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                         UNIQUE (user_id, device_id)
);

-- 5. Performance Indexes
CREATE INDEX idx_sync_user_version ON sync_data (user_id, server_version);
CREATE INDEX idx_sync_entity_type  ON sync_data (user_id, entity_type, server_version);
ALTER TABLE sync_data
    ADD COLUMN client_updated_at BIGINT NOT NULL DEFAULT 0;

CREATE OR REPLACE FUNCTION bump_sync_data_version()
RETURNS TRIGGER AS $$
BEGIN
    NEW.server_version = nextval('sync_data_server_version_seq');
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_bump_sync_data_version
    BEFORE UPDATE ON sync_data
    FOR EACH ROW
    EXECUTE FUNCTION bump_sync_data_version();
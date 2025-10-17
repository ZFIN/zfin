--liquibase formatted sql
--changeset rtaylor:ZFIN-9958

CREATE TABLE ncbi_replaced_id (
    nri_old_id VARCHAR(20) NOT NULL,
    nri_new_id VARCHAR(20),
    nri_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (nri_old_id)
);

CREATE OR REPLACE FUNCTION ncbi_replaced_id_auto_timestamp()
RETURNS TRIGGER AS '
BEGIN
    NEW.nri_updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE TRIGGER trg_ncbi_replaced_id_auto_timestamp
BEFORE UPDATE ON ncbi_replaced_id
FOR EACH ROW
EXECUTE FUNCTION ncbi_replaced_id_auto_timestamp();


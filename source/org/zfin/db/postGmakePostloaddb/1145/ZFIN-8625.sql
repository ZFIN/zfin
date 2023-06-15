--liquibase formatted sql
--changeset rtaylor:ZFIN-8625.sql

DROP TABLE IF EXISTS public.developer_data;

CREATE TABLE public.developer_data (
   dd_pk_id serial,
   dd_key text,
   dd_value text,
   dd_created_at timestamp DEFAULT now(),
   dd_modified_at timestamp DEFAULT now()
);

COMMENT ON TABLE public.developer_data IS 'This table is for keeping data that is useful for developers';
ALTER TABLE public.developer_data ADD CONSTRAINT dd_key_unique UNIQUE (dd_key);


-- last modified trigger function
CREATE OR REPLACE FUNCTION developer_data_sync_modified_at()
    RETURNS trigger AS '
BEGIN
    NEW.dd_modified_at := NOW();
    RETURN NEW;
END;
' LANGUAGE plpgsql;

-- last modified trigger
DROP TRIGGER IF EXISTS developer_data_sync_modified_at_trigger on developer_data;

CREATE TRIGGER
    developer_data_sync_modified_at_trigger
    BEFORE UPDATE ON
    developer_data
    FOR EACH ROW EXECUTE PROCEDURE
    developer_data_sync_modified_at();

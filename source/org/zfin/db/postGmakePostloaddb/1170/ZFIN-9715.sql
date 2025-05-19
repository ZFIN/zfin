--liquibase formatted sql
--changeset rtaylor:ZFIN-9715

-- Create a generic audit_log table
-- Subscribe changes on the zdb_feature_flag to be logged to audit_log

CREATE TABLE IF NOT EXISTS audit_log (
 id serial PRIMARY KEY,
 table_name TEXT,
 record_id TEXT,
 operation_type TEXT,
 changed_at TIMESTAMP DEFAULT now(),
 changed_by TEXT,
 original_values jsonb,
 new_values jsonb
);

CREATE OR REPLACE FUNCTION get_primary_key_column(table_name text) RETURNS text AS '
DECLARE
    pk_column text;
BEGIN
    -- Try to find a primary key column from the information schema
    SELECT a.attname INTO pk_column
    FROM pg_index i
             JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey)
             JOIN pg_class c ON c.oid = i.indrelid
             JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE i.indisprimary
      AND c.relname = table_name
      AND n.nspname = current_schema()
    LIMIT 1;

    -- If no primary key found, try to find id, <table>_id, or uuid columns as common conventions
    IF pk_column IS NULL THEN
        -- TODO: calculate likely column name failover ....
    END IF;

    RETURN pk_column;
END;
'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit_trigger() RETURNS TRIGGER AS '
DECLARE
    new_data jsonb;
    old_data jsonb;
    key text;
    new_values jsonb;
    old_values jsonb;
    user_id text;
    record_pk_column text;
    record_pk_value text;
BEGIN
    user_id := current_setting(''audit.user_id'', true);

    IF user_id IS NULL THEN
        user_id := current_user;
    END IF;

    new_values := ''{}'';
    old_values := ''{}'';

    -- Get the primary key column for this table
    record_pk_column := get_primary_key_column(TG_TABLE_NAME);

    IF TG_OP = ''INSERT'' THEN
        new_data := to_jsonb(NEW);
        new_values := new_data;

        -- Extract primary key value if exists
        IF record_pk_column IS NOT NULL THEN
            record_pk_value := new_data ->> record_pk_column;
        END IF;

    ELSIF TG_OP = ''UPDATE'' THEN
        new_data := to_jsonb(NEW);
        old_data := to_jsonb(OLD);

        FOR key IN SELECT jsonb_object_keys(new_data) INTERSECT SELECT jsonb_object_keys(old_data)
            LOOP
                IF new_data ->> key != old_data ->> key THEN
                    new_values := new_values || jsonb_build_object(key, new_data ->> key);
                    old_values := old_values || jsonb_build_object(key, old_data ->> key);
                END IF;
            END LOOP;

        -- Extract primary key value if exists
        IF record_pk_column IS NOT NULL THEN
            record_pk_value := new_data ->> record_pk_column;
        END IF;

    ELSIF TG_OP = ''DELETE'' THEN
        old_data := to_jsonb(OLD);
        old_values := old_data;

        -- Extract primary key value if exists
        IF record_pk_column IS NOT NULL THEN
            record_pk_value := old_data ->> record_pk_column;
        END IF;
    END IF;

    -- Insert into audit log with the record_id if available
    INSERT INTO audit_log (table_name, record_id, operation_type, changed_by, original_values, new_values)
    VALUES (TG_TABLE_NAME, record_pk_value, TG_OP, user_id, old_values, new_values);

    IF TG_OP = ''INSERT'' OR TG_OP = ''UPDATE'' THEN
        RETURN NEW;
    ELSE
        RETURN OLD;
    END IF;
END;
'
LANGUAGE plpgsql;

CREATE TRIGGER zdb_feature_flag_trigger
    BEFORE INSERT OR UPDATE OR DELETE
    ON public.zdb_feature_flag
    FOR EACH ROW
EXECUTE FUNCTION audit_trigger();


--liquibase formatted sql
--changeset rtaylor:ZFIN-8620-pre.sql


-- CREATE TEMP TABLE TO HOLD DATA FIXES
DROP TABLE IF EXISTS temp_8620;
CREATE TABLE temp_8620 (
                           pub TEXT,
                           mrel_mrkr_1_zdb_id TEXT,
                           mrel_mrkr_2_zdb_id TEXT,
                           mrel_type TEXT
);

DROP TABLE IF EXISTS temp_8620_log;
CREATE TABLE temp_8620_log (
                           inserted_at TIMESTAMP,
                           note TEXT
);




-- CREATE TEMP HELPER FUNCTION
CREATE OR REPLACE FUNCTION mrel_retain_only_single_attribution(
    pub_id_to_keep TEXT,
    mrel_mrkr_1_zdb_id_arg TEXT,
    mrel_mrkr_2_zdb_id_arg TEXT,
    mrel_type_arg TEXT
) RETURNS VOID AS '
DECLARE
    mrel_zdb_id_var TEXT;
    to_delete_count TEXT;
BEGIN
    -- Step 1: Find the mrel_zdb_id
    SELECT mrel_zdb_id
    INTO mrel_zdb_id_var
    FROM marker_relationship mrel
    WHERE mrel.mrel_mrkr_1_zdb_id = mrel_mrkr_1_zdb_id_arg
      AND mrel.mrel_mrkr_2_zdb_id = mrel_mrkr_2_zdb_id_arg
      AND mrel.mrel_type = mrel_type_arg;

    IF mrel_zdb_id_var IS NOT NULL THEN
        -- Step 2: Find rows in record_attribution and
        -- Step 3: Confirm one row has recattrib_source_zdb_id
        IF EXISTS (
                SELECT 1
                FROM record_attribution
                WHERE recattrib_data_zdb_id = mrel_zdb_id_var
                  AND recattrib_source_zdb_id = pub_id_to_keep
            ) THEN

            SELECT count(*)
            INTO to_delete_count
            FROM record_attribution
            WHERE recattrib_data_zdb_id = mrel_zdb_id_var
              AND recattrib_source_zdb_id <> pub_id_to_keep;

            insert into temp_8620_log
            select now(), format(''mrel_mrkr_1_zdb_id %s, mrel_mrkr_2_zdb_id %s, mrel_type %s, mrel_zdb_id_var: %s, pub_id_to_keep: %s, to delete count: %s'',mrel_mrkr_1_zdb_id_arg, mrel_mrkr_2_zdb_id_arg, mrel_type_arg, mrel_zdb_id_var, pub_id_to_keep, to_delete_count) ;

            insert into temp_8620_log
            select now(), ''DELETING: '' || row(recattrib_data_zdb_id, recattrib_source_zdb_id)::text
                from record_attribution
                where recattrib_data_zdb_id = mrel_zdb_id_var
                  and recattrib_source_zdb_id <> pub_id_to_keep;

            -- Step 4: Delete all other rows
            DELETE FROM record_attribution
            WHERE recattrib_data_zdb_id = mrel_zdb_id_var
              AND recattrib_source_zdb_id <> pub_id_to_keep;
        END IF;
    END IF;

    RETURN;
END;
'
LANGUAGE 'plpgsql';

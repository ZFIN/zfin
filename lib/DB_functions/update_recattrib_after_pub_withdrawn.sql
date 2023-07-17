-- Function: update_recattrib_after_pub_withdrawn
-- After a publication is withdrawn, we need to run this method to update the record_attribution table
-- so that the recattrib_source_zdb_id is updated to the new publication id.
CREATE OR REPLACE FUNCTION update_recattrib_after_pub_withdrawn(old_id varchar, new_id varchar)
    RETURNS void AS $BODY$
    BEGIN
        -- create temporary table
        CREATE TEMP TABLE rec_to_add AS
        SELECT *
        FROM record_attribution
        WHERE recattrib_source_zdb_id = old_id;

        -- update the temporary table
        UPDATE rec_to_add
        SET recattrib_source_zdb_id = new_id
        WHERE recattrib_source_zdb_id = old_id;

        -- insert from the temporary table, ignoring duplicates
        INSERT INTO record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_significance,recattrib_source_type,recattrib_created_at,recattrib_modified_at,recattrib_modified_count)
        SELECT recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_significance,recattrib_source_type,recattrib_created_at,recattrib_modified_at,recattrib_modified_count
        FROM rec_to_add
        ON CONFLICT (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) DO NOTHING;

        -- delete from the record_attribution table
        DELETE FROM record_attribution WHERE recattrib_source_zdb_id = old_id;

        -- cleanup: drop the temporary table
        DROP TABLE rec_to_add;
    END;
$BODY$ LANGUAGE plpgsql;
--liquibase formatted sql
--changeset rtaylor:ZFIN-8621.sql

-- create temporary table
CREATE TEMP TABLE rec_to_add AS
SELECT *
FROM record_attribution
WHERE recattrib_source_zdb_id = ''
and recattrib_data_zdb_id ilike '%180717%';


-- update the temporary table
UPDATE rec_to_add
SET recattrib_source_zdb_id = 'ZDB-PUB-180514-4';


-- insert from the temporary table, ignoring duplicates
INSERT INTO record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_significance,recattrib_source_type,recattrib_created_at,recattrib_modified_at,recattrib_modified_count)
SELECT recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_significance,recattrib_source_type,recattrib_created_at,recattrib_modified_at,recattrib_modified_count
FROM rec_to_add
ON CONFLICT (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) DO NOTHING;


-- delete from the record_attribution table
DELETE FROM record_attribution WHERE recattrib_pk_id in (select recattrib_pk_id from rec_to_add);


-- This is a temporary table that was created in HBurgess.sql (liquibase) but never deleted.
-- it's not referenced anywhere else.
DROP TABLE IF EXISTS feature_data;
--liquibase formatted sql
--changeset rtaylor:ZFIN-8621.sql

UPDATE
    record_attribution
SET
    recattrib_source_zdb_id = 'ZDB-PUB-180514-4'
WHERE
    recattrib_source_zdb_id = ''
    AND recattrib_data_zdb_id ILIKE '%180717%';


-- This is a temporary table that was created in HBurgess.sql (liquibase) but never deleted.
-- it's not referenced anywhere else.
DROP TABLE IF EXISTS feature_data;
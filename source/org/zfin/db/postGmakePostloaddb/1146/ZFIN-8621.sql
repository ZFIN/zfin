--liquibase formatted sql
--changeset rtaylor:ZFIN-8621.sql

UPDATE
    record_attribution
SET
    recattrib_source_zdb_id = 'ZDB-PUB-180514-4'
WHERE
    recattrib_source_zdb_id = ''
    AND recattrib_data_zdb_id ILIKE '%180717%';



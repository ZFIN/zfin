--liquibase formatted sql
--changeset rtaylor:ZFIN-8115

-- Use the rest API url for validating uniprot ids
UPDATE "public"."foreign_db" SET "fdb_db_query" = 'https://rest.uniprot.org/uniprotkb/'
WHERE "fdb_db_pk_id" = 40
AND "fdb_db_query" = 'http://www.uniprot.org/uniprot/';

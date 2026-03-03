--liquibase formatted sql
--changeset cmpich:remove-unists

-- NCBI retired the UniSTS database. Links on SSLP pages go to
-- "Resource no longer available!" Clear the URL so the accession
-- displays as plain text instead of a broken link.

UPDATE foreign_db
SET fdb_db_query = ''
WHERE fdb_db_name = 'UniSTS';
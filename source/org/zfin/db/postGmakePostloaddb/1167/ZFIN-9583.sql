--liquibase formatted sql
--changeset rtaylor:ZFIN-9583.sql

-- split up OMIM xrefs into PS and Gene (default)
INSERT INTO foreign_db (fdb_db_pk_id, fdb_db_name, fdb_db_query, fdb_db_display_name, fdb_db_significance)
VALUES (98,'OMIM_PS', 'https://omim.org/phenotypicSeries/', 'OMIM', 1);

-- update term_xref for the new OMIM PS type
UPDATE term_xref SET tx_fdb_db_id = 98 WHERE tx_fdb_db_id = 24 AND tx_accession LIKE 'PS%' ;

-- Add "s" to "https"
UPDATE foreign_db SET fdb_db_query = 'https://omim.org/entry/' WHERE fdb_db_pk_id = 24;


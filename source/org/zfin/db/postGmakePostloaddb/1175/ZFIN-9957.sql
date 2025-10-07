--liquibase formatted sql
--changeset rtaylor:zfin-9957
UPDATE
    foreign_db
SET
    fdb_db_query = 'https://www.ncbi.nlm.nih.gov/gene/'
WHERE
    fdb_db_pk_id = 10
    AND fdb_db_name = 'Gene'
    AND fdb_db_query = 'http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&dopt=Graphics&list_uids=';


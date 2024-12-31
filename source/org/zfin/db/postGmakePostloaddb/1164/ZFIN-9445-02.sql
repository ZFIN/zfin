--liquibase formatted sql
--changeset rtaylor:ZFIN-9445-02.sql

UPDATE
    foreign_db
SET
    fdb_db_query = 'https://www.rcsb.org/structure/'
WHERE
    fdb_db_name = 'PDB';



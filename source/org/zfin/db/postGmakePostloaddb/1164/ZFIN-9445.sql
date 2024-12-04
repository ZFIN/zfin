--liquibase formatted sql
--changeset cmpich:ZFIN-9445.sql


delete from term_xref where tx_fdb_db_id in (
    select fdb_db_pk_id
    from foreign_db
    where fdb_db_name = 'PDB'
    );

delete from foreign_db_contains where fdbcont_fdb_db_id in (
    select fdb_db_pk_id
    from foreign_db
    where fdb_db_name = 'PDB'
    );

delete
from foreign_db
where fdb_db_name = 'PDB';
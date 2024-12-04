--liquibase formatted sql
--changeset cmpich:ZFIN-9441.sql


delete from foreign_db_contains_display_group_member where
    fdbcdgm_fdbcont_zdb_id in (
        select fdbcont_zdb_id from foreign_db_contains where fdbcont_fdb_db_id in (
            select fdb_db_pk_id
            from foreign_db
            where fdb_db_name = 'MODB'
            )
        );

delete from foreign_db_contains where fdbcont_fdb_db_id in (
    select fdb_db_pk_id
    from foreign_db
    where fdb_db_name = 'MODB'
    );

delete
from foreign_db
where fdb_db_name = 'MODB';
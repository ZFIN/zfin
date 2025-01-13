--liquibase formatted sql
--changeset cmpich:ZFIN-9442.sql

delete
from foreign_db_contains_display_group_member
where fdbcdgm_fdbcont_zdb_id  in (select fdbcont_zdb_id
                                 from foreign_db_contains
                                 where fdbcont_fdb_db_id = 48);

delete
from foreign_db_contains
where fdbcont_fdb_db_id = 48;

delete
from foreign_db
where fdb_db_name = 'dbSNP';

select count(*) from db_link;

delete from db_link where dblink_fdbcont_zdb_id in (
    select fdbcont_zdb_id
    from foreign_db_contains
    where fdbcont_fdb_db_id = 48
    );

select count(*) from db_link;

-- addZfishbook.sql.sql
-- for FB case 8026 
-- add new foreign_db and foreign_db_contains records for zfishbook

begin work;

-- add zfishbook to foreign_db table
insert into foreign_db (fdb_db_name, fdb_db_query, fdb_db_display_name, fdb_db_significance)
  values ( 'zfishbook', 'http://www.zfishbook.org/index.php?topic=', 'zfishbook', '2');


-- add new record related to zfishbook to foreign_db_contains table

create table pre_foreign_db_contains (
        prefbct_fdbcont_organism_common_name varchar(30) not null,
        prefbct_fdbcont_fdbdt_id int8,
        prefbct_fdbcont_fdb_db_id varchar(50) not null
);

insert into pre_foreign_db_contains (prefbct_fdbcont_organism_common_name, prefbct_fdbcont_fdbdt_id, prefbct_fdbcont_fdb_db_id)
  select 'Zebrafish', '13', fdb_db_pk_id
    from foreign_db
   where fdb_db_name = 'zfishbook';

alter table pre_foreign_db_contains add prefbct_fdbcont_zdb_id varchar(50);

update pre_foreign_db_contains set prefbct_fdbcont_zdb_id = get_id('FDBCONT');

unload to 'pre_foreign_db_contains.unl' select * from pre_foreign_db_contains;

insert into zdb_active_data select prefbct_fdbcont_zdb_id from pre_foreign_db_contains;
! echo "         into zdb_active_data table."

insert into foreign_db_contains (fdbcont_organism_common_name, fdbcont_zdb_id, fdbcont_fdbdt_id, fdbcont_fdb_db_id)
  select prefbct_fdbcont_organism_common_name, prefbct_fdbcont_zdb_id, prefbct_fdbcont_fdbdt_id, prefbct_fdbcont_fdb_db_id
    from pre_foreign_db_contains;

drop table pre_foreign_db_contains;
                                 
--rollback work;

commit work;


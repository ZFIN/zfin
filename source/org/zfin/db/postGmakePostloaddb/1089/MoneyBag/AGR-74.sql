--liquibase formatted sql
--changeset prita:AGR-74



create table pre_foreign_db_contains (
        prefbct_fdbcont_organism_common_name varchar(30) not null,
        prefbct_fdbcont_fdbdt_id int8,
        prefbct_fdbcont_fdb_db_id varchar(50) not null
);

insert into pre_foreign_db_contains (prefbct_fdbcont_organism_common_name, prefbct_fdbcont_fdbdt_id, prefbct_fdbcont_fdb_db_id)
  select 'Zebrafish',13, fdb_db_pk_id
    from foreign_db
   where fdb_db_name = 'PANTHER';

alter table pre_foreign_db_contains add prefbct_fdbcont_zdb_id varchar(50);

update pre_foreign_db_contains set prefbct_fdbcont_zdb_id = get_id('FDBCONT');



insert into zdb_active_data select prefbct_fdbcont_zdb_id from pre_foreign_db_contains;


insert into foreign_db_contains (fdbcont_organism_common_name, fdbcont_zdb_id, fdbcont_fdbdt_id, fdbcont_fdb_db_id)
  select prefbct_fdbcont_organism_common_name, prefbct_fdbcont_zdb_id, prefbct_fdbcont_fdbdt_id, prefbct_fdbcont_fdb_db_id
    from pre_foreign_db_contains;

drop table pre_foreign_db_contains;


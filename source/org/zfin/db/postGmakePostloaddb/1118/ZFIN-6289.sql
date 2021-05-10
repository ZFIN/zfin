--liquibase formatted sql
--changeset pm:ZFIN-6289

insert into foreign_db (fdb_db_name, fdb_db_query,fdb_db_display_name,fdb_db_significance) values ('RNA Central', 'http://rnacentral.org/rna/','RNAcentral',2);

create temp table pre_foreign_db_contains (
        prefbct_fdbcont_organism_common_name varchar(30) not null,
        prefbct_fdbcont_fdbdt_id int8,
        prefbct_fdbcont_fdb_db_id int8 not null,
	prefbct_fdbcont_zdb_id varchar(50)
);

insert into pre_foreign_db_contains (prefbct_fdbcont_organism_common_name, prefbct_fdbcont_fdbdt_id, prefbct_fdbcont_fdb_db_id)
  select distinct 'Zebrafish', 13, fdb_db_pk_id
    from foreign_db
   where fdb_db_name = 'RNA Central';

update pre_foreign_db_contains set prefbct_fdbcont_zdb_id = get_id('FDBCONT');

insert into zdb_active_data select prefbct_fdbcont_zdb_id from pre_foreign_db_contains;
-- "         into zdb_active_data table."

insert into foreign_db_contains (fdbcont_organism_common_name, fdbcont_zdb_id, fdbcont_fdbdt_id, fdbcont_fdb_db_id)
  select prefbct_fdbcont_organism_common_name, prefbct_fdbcont_zdb_id, prefbct_fdbcont_fdbdt_id, prefbct_fdbcont_fdb_db_id
    from pre_foreign_db_contains;

insert into foreign_db_contains_display_group_member (fdbcdgm_group_id,fdbcdgm_fdbcont_zdb_id)
select 18,prefbct_fdbcont_zdb_id from pre_foreign_db_contains;


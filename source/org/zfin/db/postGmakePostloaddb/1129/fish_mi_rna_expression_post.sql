--liquibase formatted sql
--changeset cmpich:ZFIN-7733


-- insert into DB-link
create table pre_db_link_temp
(
    predblink_db_zdb_id varchar(50) not null,
    predblink_data_zdb_id varchar(50) not null
);

insert into pre_db_link_temp (predblink_db_zdb_id, predblink_data_zdb_id)
select get_id('DBLINK'), mir_gene_id
from fishmir_temp ;

insert into db_link (dblink_linked_recid, dblink_acc_num, dblink_info, dblink_zdb_id, dblink_acc_num_display,
                     dblink_fdbcont_zdb_id)
select gene_zdb_id,
       mir_gene_id,
       'imported from Fish miRNA Expression',
       predblink_db_zdb_id,
       mir_gene_id,
       fdbcont_zdb_id
from fishmir_temp,
     foreign_db,
     foreign_db_contains,
     pre_db_link_temp
where fdb_db_pk_id = fdbcont_fdb_db_id
  AND fdb_db_name = 'FishMiRNA-Expression'
  AND predblink_data_zdb_id = mir_gene_id
  AND mir_gene_id = 'FMDANRERM0002,FMDANRERM0246'
;

insert into zdb_active_data select predblink_db_zdb_id from pre_db_link_temp;



insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select predblink_db_zdb_id, 'ZDB-PUB-220126-55', 'standard' from pre_db_link_temp;

drop table fishmir_temp;

drop table pre_db_link_temp;



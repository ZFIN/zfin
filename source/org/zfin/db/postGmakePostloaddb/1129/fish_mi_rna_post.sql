--liquibase formatted sql
--changeset cmpich:ZFIN-7770


-- insert into DB-link
create table pre_db_link_temp
(
    predblink_db_zdb_id varchar(50) not null,
    predblink_data_zdb_id varchar(50) not null
);

insert into pre_db_link_temp (predblink_db_zdb_id, predblink_data_zdb_id)
select get_id('DBLINK'), mir_gene_id
from fishmir_temp ;

insert into zdb_active_data select predblink_db_zdb_id from pre_db_link_temp;

insert into db_link (dblink_linked_recid, dblink_acc_num, dblink_info, dblink_zdb_id, dblink_acc_num_display,
                     dblink_length, dblink_fdbcont_zdb_id)
select gene_zdb_id,
       mir_gene_id,
       'imported from Fish miRNA',
       predblink_db_zdb_id,
       mir_gene_id,
       length(sequence),
       fdbcont_zdb_id
from fishmir_temp,
     foreign_db,
     foreign_db_contains,
     pre_db_link_temp
where fdb_db_pk_id = fdbcont_fdb_db_id
  AND fdb_db_name = 'FishMiRNA'
AND predblink_data_zdb_id = mir_gene_id;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select predblink_db_zdb_id, 'ZDB-PUB-220126-55', 'standard' from pre_db_link_temp;

drop table fishmir_temp;

drop table pre_db_link_temp;



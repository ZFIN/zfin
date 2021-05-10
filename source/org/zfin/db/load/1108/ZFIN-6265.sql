--liquibase formatted sql
--changeset xshao:ZFIN-6265

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-DBLINK-100723-1';

create table dblink (zdbId text, recId text, acc text, fdbcntId text, len integer);

insert into dblink (recId, acc, fdbcntId, len) 
values ('ZDB-GENE-190514-1', 'ZFINPROT0000002255', 'ZDB-FDBCONT-090929-7', 735); 

update dblink set zdbId = get_id('DBLINK');

insert into zdb_active_data(zactvd_zdb_id) select zdbId from dblink;

insert into db_link(dblink_zdb_id, dblink_linked_recid, dblink_acc_num,  dblink_fdbcont_zdb_id, dblink_length)
 select zdbId, recId, acc, fdbcntId, len
   from dblink;


insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
  select zdbId, 'ZDB-PUB-061227-28', 'standard'
    from dblink; 
   
drop table dblink;

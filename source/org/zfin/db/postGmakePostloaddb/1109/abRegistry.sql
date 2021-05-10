--liquibase formatted sql
--changeset pm:abRegistry


drop  table if exists pre_db_link;
update abregistry set zdbid=(select zrepld_new_zdb_id
                                 from zdb_replaced_data
                                where zrepld_old_zdb_id = zdbid) where zdbid not in (select zactvd_zdb_id from zdb_active_data);
                                delete from abregistry where zdbid is null;
  delete from abregistry where zdbid is null;
delete from abregistry where zdbid not in (select zactvd_zdb_id from zdb_active_data);

delete from abregistry where zdbid in (Select dblink_linked_recid from db_link where dblink_acc_num like 'AB%');



create table pre_db_link (
        predblink_data_zdb_id varchar(50) not null,
        predblink_acc_num varchar(50) not null,
        predblink_acc_num_display varchar(50) not null,
        predblink_fdbcont_zdb_id varchar(50) not null
);


insert into pre_db_link (
        predblink_data_zdb_id,
        predblink_acc_num,
        predblink_acc_num_display,
        predblink_fdbcont_zdb_id)
  select distinct trim(zdbid), trim(abregid), abregid, fdbcont_zdb_id
    from abregistry, foreign_db, foreign_db_contains
   where fdbcont_fdb_db_id = fdb_db_pk_id 
     and fdb_db_name = 'ABRegistry' and abregid!='' ;
     
alter table pre_db_link add predblink_dblink_zdb_id varchar(50);

update pre_db_link set predblink_dblink_zdb_id = get_id('DBLINK');


insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id) 
  select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id, predblink_acc_num_display, predblink_fdbcont_zdb_id 
    from pre_db_link; 


drop table pre_db_link;

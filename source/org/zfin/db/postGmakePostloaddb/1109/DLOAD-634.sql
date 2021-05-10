--liquibase formatted sql
--changeset pm:DLOAD-634
update tscriptens set ensdartid=substring(ensdartid,0,position('.' in ensdartid));
delete from tscriptens where ensdartid ='';
update tscriptens set tscriptid=(select distinct mrkr_zdb_id from marker where trim(tscriptid)=mrkr_abbrev);

delete from tscriptens where exists (select 'x' from transcript where tscriptid=tscript_mrkr_zdb_id and trim(ensdartid)=trim(tscript_ensdart_id));
delete from tscriptens where exists (select 'x' from transcript where tscriptid=tscript_mrkr_zdb_id and trim(tscript_ensdart_id)is not null);
update transcript set tscript_ensdart_id=(select distinct ensdartid from tscriptens where trim(tscriptid)=trim(tscript_mrkr_zdb_id) and tscript_ensdart_id is null limit 1) where not exists  (select 'x' from tscriptens where tscriptid=tscript_mrkr_zdb_id and trim(ensdartid)=trim(tscript_ensdart_id));

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
  select distinct tscriptid, ensdartid, ensdartid, fdbcont_zdb_id
    from tscriptens, foreign_db, foreign_db_contains
   where fdbcont_fdb_db_id = fdb_db_pk_id
     and fdb_db_name = 'Ensembl_Trans' ;

alter table pre_db_link add predblink_dblink_zdb_id varchar(50);

update pre_db_link set predblink_dblink_zdb_id = get_id('DBLINK');


insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id)
  select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id, predblink_acc_num_display, predblink_fdbcont_zdb_id
    from pre_db_link;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select predblink_dblink_zdb_id,'ZDB-PUB-190221-12' from pre_db_link;



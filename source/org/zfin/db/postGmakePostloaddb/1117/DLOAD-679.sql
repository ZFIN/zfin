--liquibase formatted sql
--changeset pm:DLOAD-679
update tmp_ottens set ensdartid = nullif(ensdartid, '');
update tmp_ottens set comments = nullif(comments, '');

update tmp_ottens set ensdartid=substring(ensdartid,0,position('.' in ensdartid)) where ensdartid like '%.%';
delete from tmp_ottens where exists (select 'x' from transcript where tscriptid=tscript_mrkr_zdb_id and trim(ensdartid)=trim(tscript_ensdart_id));


update transcript
set tscript_ensdart_id=(select distinct ensdartid from tmp_ottens where trim(tscriptid)=trim(tscript_mrkr_zdb_id) and tscript_ensdart_id is null limit 1)
from tmp_ottens
where tscript_ensdart_id is null
and trim(tscriptid)=trim(tscript_mrkr_zdb_id);

update transcript
set tscript_status_id=1
from tmp_ottens
where tscript_ensdart_id is null
and trim(tscriptid)=trim(tscript_mrkr_zdb_id) and comments is not null and ensdartid is null;
drop table if exists pre_db_link;
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
    from tmp_ottens, foreign_db, foreign_db_contains
   where fdbcont_fdb_db_id = fdb_db_pk_id
     and fdb_db_name = 'Ensembl_Trans' and ensdartid is not null;

alter table pre_db_link add predblink_dblink_zdb_id varchar(50);

update pre_db_link set predblink_dblink_zdb_id = get_id('DBLINK');


insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id)
  select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id, predblink_acc_num_display, predblink_fdbcont_zdb_id
    from pre_db_link where predblink_data_zdb_id not in (select dblink_linked_recid from db_link where dblink_acc_num like 'ENSDART%');

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select predblink_dblink_zdb_id,'ZDB-PUB-190221-12' from pre_db_link;



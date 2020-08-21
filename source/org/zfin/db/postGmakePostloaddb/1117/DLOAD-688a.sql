--liquibase formatted sql
--changeset pm DLOAD-688a.sql

create temp table tscripts (tscript text, ensdart text,gene text);
insert into tscripts (select tscript_mrkr_zdb_id , tscript_ensdart_id,mrel_mrkr_1_zdb_id from transcript, marker_relationship where tscript_mrkr_zdb_id=mrel_mrkr_2_zdb_id and tscript_ensdart_id is not null and mrel_type='gene produces transcript');

create temp table tscriptsmapping (geneid text,tscriptid text, ensdartid text, ensdargid text);
insert into tscriptsmapping (select distinct gene,tscript,ensdart,ensdart from tscripts where gene not in (select dblink_linked_recid from db_link where dblink_acc_num like 'ENSDARG%')) ;

update tscriptsmapping set ensdargid = (select ensm_ensdarg_id from ensdar_mapping where ensdartid=ensm_ensdart_id);

--\copy (select * from tscriptsmapping where ensdargid is null) to 'noEnsdargs' with delimiter as ',' null as '';
drop table pre_db_link;

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
  select distinct geneid, ensdargid, ensdargid, 'ZDB-FDBCONT-061018-1'
    from tscriptsmapping
   where  ensdargid is not null;

alter table pre_db_link add predblink_dblink_zdb_id varchar(50);

update pre_db_link set predblink_dblink_zdb_id = get_id('DBLINK');


insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id)
  select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id, predblink_acc_num_display, predblink_fdbcont_zdb_id
    from pre_db_link;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select predblink_dblink_zdb_id,'ZDB-PUB-190221-12' from pre_db_link;







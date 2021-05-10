--liquibase formatted sql
--changeset pm:CUR-861

update construct_component set cc_component_zdb_id='ZDB-EREGION-181119-1' where cc_component='E1b';

update construct_component set cc_component_zdb_id='ZDB-EREGION-181119-1' where cc_component='ADV.E1b';
delete from construct_component where cc_component='ADV.';

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) select * from constructrel;
delete from tmp_cmrel where consid in (select conmrkrrel_construct_zdb_id from construct_marker_relationship where conmrkrrel_mrkr_zdb_id='ZDB-EREGION-181119-1');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) select * from constructrel;
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;

drop table tmp_mrel;


create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select construct1, construct1, construct_name  from constructrel, construct where construct1=construct_zdb_id;

update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;
drop table tmp_dalias;





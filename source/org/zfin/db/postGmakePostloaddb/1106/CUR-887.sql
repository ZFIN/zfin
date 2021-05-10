--liquibase formatted sql
--changeset pm:CUR-887


create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-151214-3', 'ZDB-TGCONSTRCT-151214-3', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-151214-3';
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-131107-1', 'ZDB-TGCONSTRCT-131107-1', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-131107-1';

update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;

update construct set construct_name='Tg(-2star:Bal.bPAC-2A-Tomato)' where construct_zdb_id='ZDB-TGCONSTRCT-151214-3';
update marker set mrkr_name='Tg(-2star:Bal.bPAC-2A-Tomato)' where mrkr_zdb_id='ZDB-TGCONSTRCT-151214-3';
update marker set mrkr_abbrev ='Tg(-2star:Bal.bPAC-2A-Tomato)' where mrkr_zdb_id='ZDB-TGCONSTRCT-151214-3';

delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-151214-3' and cc_component='Bal.';
update construct_component set cc_component_zdb_id='ZDB-EFG-190304-1' where cc_component='bPAC' and cc_construct_zdb_id='ZDB-TGCONSTRCT-151214-3';

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-151214-3','ZDB-TGCONSTRCT-151214-3','ZDB-EFG-190304-1','coding sequence of');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-151214-3','ZDB-TGCONSTRCT-151214-3','ZDB-EFG-190304-1','coding sequence of');
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
drop table tmp_mrel;

update construct set construct_name='Tg(pomc:bPAC-2A-Tomato)' where construct_zdb_id='ZDB-TGCONSTRCT-131107-1';
update marker set mrkr_name='Tg(pomc:bPAC-2A-Tomato)' where mrkr_zdb_id='ZDB-TGCONSTRCT-131107-1';
update marker set mrkr_abbrev ='Tg(pomc:bPAC-2A-Tomato)' where mrkr_zdb_id='ZDB-TGCONSTRCT-131107-1';

delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-131107-1' and cc_component='Bal.';
update construct_component set cc_component_zdb_id='ZDB-EFG-190304-1' where cc_component='bPAC' and cc_construct_zdb_id='ZDB-TGCONSTRCT-131107-1';

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-131107-1','ZDB-TGCONSTRCT-131107-1','ZDB-EFG-190304-1','coding sequence of');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-131107-1','ZDB-TGCONSTRCT-131107-1','ZDB-EFG-190304-1','coding sequence of');
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
drop table tmp_mrel;







insert into record_attribution(recattrib_data_Zdb_id,recattrib_source_zdb_id) select aliasid,'ZDB-PUB-190102-5' from tmp_dalias;
drop table tmp_dalias;









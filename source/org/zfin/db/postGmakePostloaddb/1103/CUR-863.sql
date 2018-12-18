--liquibase formatted sql
--changeset pm:CUR-863

update construct_component set cc_component_zdb_id='ZDB-EFG-181217-2' where cc_component='2x' and cc_construct_zdb_id='ZDB-TGCONSTRCT-161017-5';
update construct_component set cc_component_type='coding sequence of' where cc_component_zdb_id='ZDB-EFG-181217-2' and cc_construct_zdb_id='ZDB-TGCONSTRCT-161017-5';
update construct_component set cc_component='FYVE' where cc_component_zdb_id='ZDB-EFG-181217-2' and cc_construct_zdb_id='ZDB-TGCONSTRCT-161017-5';
delete from construct_component where cc_component='Mmu.' and cc_construct_zdb_id='ZDB-TGCONSTRCT-161017-5';
delete from construct_component where cc_component='Hrs' and cc_construct_zdb_id='ZDB-TGCONSTRCT-161017-5';
update construct_component set cc_order=8 where cc_component=')' and cc_construct_zdb_id='ZDB-TGCONSTRCT-161017-5';


create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-161017-5','ZDB-TGCONSTRCT-161017-5','ZDB-EFG-181217-2','coding sequence of');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-161017-5','ZDB-TGCONSTRCT-161017-5','ZDB-EFG-181217-2','coding sequence of');
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
drop table tmp_mrel;


update construct_component set cc_component_zdb_id='ZDB-EFG-181217-2' where cc_component='2x' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150424-4';
update construct_component set cc_component_type='coding sequence of' where cc_component_zdb_id='ZDB-EFG-181217-2' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150424-4';
update construct_component set cc_component='FYVE' where cc_component_zdb_id='ZDB-EFG-181217-2' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150424-4';
delete from construct_component where cc_component='Mmu.' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150424-4';
delete from construct_component where cc_component='Hrs' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150424-4';
update construct_component set cc_order=9 where cc_component=')' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150424-4';


create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-150424-4','ZDB-TGCONSTRCT-150424-4','ZDB-EFG-181217-2','coding sequence of');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-150424-4','ZDB-TGCONSTRCT-150424-4','ZDB-EFG-181217-2','coding sequence of');
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;

drop table tmp_mrel;








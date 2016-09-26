--liquibase formatted sql
--changeset prita:14514

delete from construct_component where cc_component like 'Cdi.';
update construct_component set cc_component_zdb_id='ZDB-EFG-160817-1' where cc_component='Tox';
update construct_component set cc_component ='DipTox' where cc_component='Tox';


update tmprel set mrelid=get_id('MREL');
update tmprel set cmrelid=get_id('CMREL');
insert into zdb_active_Data (zactvd_zdb_id) select mrelid from tmprel;

insert into zdb_active_Data (zactvd_zdb_id) select cmrelid from tmprel;
insert into marker_relationship (mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select mrelid,mrkrzdb,tgtzdb,reltype from tmprel;

insert into construct_marker_relationship (conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select cmrelid,mrkrzdb,tgtzdb,reltype from tmprel;

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select mrelid,pubzdb from tmprel;


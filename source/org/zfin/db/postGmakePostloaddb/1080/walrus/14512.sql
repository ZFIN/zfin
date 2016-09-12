--liquibase formatted sql
--changeset prita:14512

update construct_component set cc_component_zdb_id='ZDB-REGION-160817-1' where cc_component='TagRFP' and cc_order=9;
update construct_component set cc_component ='FMA' where cc_component='TagRFP' and cc_order=9;
update construct_component set cc_component_zdb_id='ZDB-EFG-110425-1' where cc_component='F' and cc_order=10;
update construct_component set cc_component ='TagRFP' where cc_component='F' and cc_order=10;
update construct_component set cc_component_type='coding sequence of' where cc_component='TagRFP' and cc_order=10 and cc_component_type like '%text%';

update construct_component set cc_component_zdb_id='ZDB-REGION-160817-1' where cc_component='TagRFP' and cc_order=8;
update construct_component set cc_component ='FMA' where cc_component='TagRFP' and cc_order=8;
update construct_component set cc_component_zdb_id='ZDB-EFG-110425-1' where cc_component='F' and cc_order=9;
update construct_component set cc_component ='TagRFP' where cc_component='F' and cc_order=9;
update construct_component set cc_component_type='coding sequence of' where cc_component='TagRFP' and cc_order=9 and cc_component_type like '%text%';
update tmprel set mrelid=get_id('MREL');
update tmprel set cmrelid=get_id('CMREL');
insert into zdb_active_Data (zactvd_zdb_id) select mrelid from tmprel;

insert into zdb_active_Data (zactvd_zdb_id) select cmrelid from tmprel;
insert into marker_relationship (mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select mrelid,mrkrzdb,tgtzdb,reltype from tmprel;

insert into construct_marker_relationship (conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select cmrelid,mrkrzdb,tgtzdb,reltype from tmprel;

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select mrelid,pubzdb from tmprel;

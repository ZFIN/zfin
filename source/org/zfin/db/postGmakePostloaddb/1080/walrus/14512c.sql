--liquibase formatted sql
--changeset prita:14512c

delete * from tmprel;
update construct set construct_name='Tg(5xUAS:FMAVenus-2A-3xNLS-EGFP)' where construct_zdb_id='ZDB-TGCONSTRCT-151204-8';

update marker set mrkr_name='Tg(5xUAS:FMAVenus-2A-3xNLS-EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-151204-8';
update marker set mrkr_abbrev='Tg(5xUAS:FMAVenus-2A-3xNLS-EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-151204-8';
update construct_component set cc_component= 'FMAVenus' where cc_component='VenusF' and cc_construct_zdb_id='ZDB-TGCONSTRCT-151204-8';

update tmprel set mrelid=get_id('MREL');
update tmprel set cmrelid=get_id('CMREL');
insert into zdb_active_Data (zactvd_zdb_id) select mrelid from tmprel;

insert into zdb_active_Data (zactvd_zdb_id) select cmrelid from tmprel;
insert into marker_relationship (mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select mrelid,mrkrzdb,tgtzdb,reltype from tmprel;
insert into construct_marker_relationship (conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select cmrelid,mrkrzdb,tgtzdb,reltype from tmprel;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select mrelid,pubzdb from tmprel;

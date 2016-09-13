--liquibase formatted sql
--changeset prita:14513

delete * from tmprel;
delete from construct_component where cc_component like 'Cte.';
delete from construct_component where cc_component like 'Cte';
delete from construct_component where cc_component like '.' and cc_construct_zdb_id='ZDB-ETCONSTRCT-150421-1';
update construct_component set cc_component_zdb_id='ZDB-EFG-080528-1' where cc_component='TetX';
update construct_component set cc_component ='tetxlc' where cc_component='TetX';


update tmprel set mrelid=get_id('MREL');
update tmprel set cmrelid=get_id('CMREL');
insert into zdb_active_Data (zactvd_zdb_id) select mrelid from tmprel;

insert into zdb_active_Data (zactvd_zdb_id) select cmrelid from tmprel;
insert into marker_relationship (mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select mrelid,mrkrzdb,tgtzdb,reltype from tmprel;

insert into construct_marker_relationship (conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select cmrelid,mrkrzdb,tgtzdb,reltype from tmprel;

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select mrelid,pubzdb from tmprel;

update construct set construct_name='Et(hsp70l:LOXP-RFP-LOXP-GFP-tetxlc)' where construct_zdb_id='ZDB-ETCONSTRCT-150421-1';
update marker set mrkr_name='Et(hsp70l:LOXP-RFP-LOXP-GFP-tetxlc)' where mrkr_zdb_id='ZDB-ETCONSTRCT-150421-1';
update marker set mrkr_abbrev='Et(hsp70l:LOXP-RFP-LOXP-GFP-tetxlc)' where mrkr_zdb_id='ZDB-ETCONSTRCT-150421-1';

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
update construct_component set cc_component_zdb_id='ZDB-REGION-160817-1' where cc_component='TagRFP' and cc_order=12;
update construct_component set cc_component ='FMA' where cc_component='TagRFP' and cc_order=12;

update construct_component set cc_component_zdb_id='ZDB-EFG-110425-1' where cc_component='F' and cc_order=13;

update construct_component set cc_component ='TagRFP' where cc_component='F' and cc_order=13;

update construct_component set cc_component_type='coding sequence of' where cc_component='TagRFP' and cc_order=13 and cc_component_type like '%text%';

update construct_component set cc_component_zdb_id='ZDB-REGION-160817-1' where cc_component='Venus' and cc_order=12;
update construct_component set cc_component ='FMA' where cc_component='Venus' and cc_order=12;

update construct_component set cc_component_zdb_id='ZDB-EFG-080131-4' where cc_component='F' and cc_order=13;

update construct_component set cc_component ='Venus' where cc_component='F' and cc_order=13;

update construct_component set cc_component_type='coding sequence of' where cc_component='Venus' and cc_order=13 and cc_component_type like '%text%';
update construct_component set cc_component='Venus' where cc_order=13 and cc_construct_zdb_id='ZDB-TGCONSTRCT-151204-9';

update construct_component set cc_component_zdb_id='ZDB-EFG-080131-4' where cc_order=13 and cc_construct_zdb_id='ZDB-TGCONSTRCT-151204-9';
delete from marker_history where mhist_dalias_zdb_id in (select dalias_zdb_id from data_alias where dalias_alias='Tg(ca8-ADV.E1b:Cr.ChR2-FMATagRFP,RGECO)');
delete from data_alias where dalias_alias='Tg(ca8-ADV.E1b:Cr.ChR2-FMATagRFP,RGECO)';
update construct set construct_name='Tg(5xUAS:FMAVenus-2A-3xNLS-EGFP)' where construct_zdb_id='ZDB-TGCONSTRCT-151204-8';

update marker set mrkr_name='Tg(5xUAS:FMAVenus-2A-3xNLS-EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-151204-8';
update marker set mrkr_abbrev='Tg(5xUAS:FMAVenus-2A-3xNLS-EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-151204-8';
update construct_component set cc_component= 'FMAVenus' where cc_component='VenusF' and cc_construct_zdb_id='ZDB-TGCONSTRCT-151204-8';

delete from construct_component where cc_component like 'Cte.';
delete from construct_component where cc_component like 'Cte';
delete from construct_component where cc_component like '.' and cc_construct_zdb_id='ZDB-ETCONSTRCT-150421-1';
update construct_component set cc_component_zdb_id='ZDB-EFG-080528-1' where cc_component='TetX';
update construct_component set cc_component ='tetxlc' where cc_component='TetX';
update construct set construct_name='Et(hsp70l:LOXP-RFP-LOXP-GFP-tetxlc)' where construct_zdb_id='ZDB-ETCONSTRCT-150421-1';
update marker set mrkr_name='Et(hsp70l:LOXP-RFP-LOXP-GFP-tetxlc)' where mrkr_zdb_id='ZDB-ETCONSTRCT-150421-1';
update marker set mrkr_abbrev='Et(hsp70l:LOXP-RFP-LOXP-GFP-tetxlc)' where mrkr_zdb_id='ZDB-ETCONSTRCT-150421-1';
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

--liquibase formatted sql
--changeset pm:DLOAD-595
--adding 1 after SCP and changing NRSE to REX2

 update construct set construct_name='Et(REX2-SCP1-Ocu.HBB2:Cre-2A-Cerulean)' where construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
 update marker set mrkr_name='Et(REX2-SCP1-Ocu.HBB2:Cre-2A-Cerulean)' where mrkr_zdb_id='ZDB-ETCONSTRCT-151102-1';
 update marker set mrkr_abbrev='Et(REX2-SCP1-Ocu.HBB2:Cre-2A-Cerulean)' where mrkr_zdb_id='ZDB-ETCONSTRCT-151102-1';
update construct_component set cc_component_zdb_id='ZDB-EREGION-181127-1' where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1' and cc_component='NRSE';
 update construct_component set cc_component='REX2' where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1' and cc_component='NRSE';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EREGION-181127-1' where mrel_mrkr_1_zdb_id='ZDB-ETCONSTRCT-151102-1' and mrel_mrkr_2_zdb_id='ZDB-EREGION-121107-1';
 update construct_marker_relationship set conmrkrrel_mrkr_zdb_id ='ZDB-EREGION-181127-1' where conmrkrrel_construct_zdb_id ='ZDB-ETCONSTRCT-151102-1' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-121107-1';


update construct_component set cc_order=20 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=19;
update construct_component set cc_order=19 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=18;
update construct_component set cc_order=18 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=17;
update construct_component set cc_order=17 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=16;
update construct_component set cc_order=16 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=15;
update construct_component set cc_order=15 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=14;
update construct_component set cc_order=14 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=13;
update construct_component set cc_order=13 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=12;
update construct_component set cc_order=12 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=11;
update construct_component set cc_order=11 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=10;
update construct_component set cc_order=10 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=9;
update construct_component set cc_order=9 where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_order=8;


insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-ETCONSTRCT-151102-2','text component','promoter component','1',1,8);
update construct_component set cc_component_zdb_id='ZDB-EREGION-181127-1' where cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-2' and cc_component='NRSE';

update construct set construct_name='Et(ATTP-REX2-SCP1-Ocu.HBB2:Cre-2A-Cerulean-ATTP)' where construct_zdb_id='ZDB-ETCONSTRCT-151102-2';
 update marker set mrkr_name='Et(ATTP-REX2-SCP1-Ocu.HBB2:Cre-2A-Cerulean-ATTP)' where mrkr_zdb_id='ZDB-ETCONSTRCT-151102-2';
 update marker set mrkr_abbrev='Et(ATTP-REX2-SCP1-Ocu.HBB2:Cre-2A-Cerulean-ATTP)' where mrkr_zdb_id='ZDB-ETCONSTRCT-151102-2';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EREGION-181127-1' where mrel_mrkr_1_zdb_id='ZDB-ETCONSTRCT-151102-2' and mrel_mrkr_2_zdb_id='ZDB-EREGION-121107-1';
 update construct_marker_relationship set conmrkrrel_mrkr_zdb_id ='ZDB-EREGION-181127-1' where conmrkrrel_construct_zdb_id ='ZDB-ETCONSTRCT-151102-2' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-121107-1';









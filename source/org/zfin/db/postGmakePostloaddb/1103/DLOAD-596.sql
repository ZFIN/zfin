--liquibase formatted sql
--changeset pm:DLOAD-596

update construct_component set cc_order=16 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-1' and cc_order=13;
update construct_component set cc_order=15 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-1' and cc_order=12;
update construct_component set cc_order=14 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-1' and cc_order=11;
update construct_component set cc_order=13 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-1' and cc_order=10;
update construct_component set cc_order=12 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-1' and cc_order=9;
update construct_component set cc_order=11 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-1' and cc_order=8;
update construct_component set cc_order=10 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-1' and cc_order=7;
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-ETCONSTRCT-180514-1','controlled vocab component','promoter component','ZDB-CV-150506-25','Ocu.',1,8);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-ETCONSTRCT-180514-1','text component','promoter component','-',1,7);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-ETCONSTRCT-180514-1','text component','promoter component','Hbb2',1,9);
 update construct_component set cc_component_zdb_id='ZDB-EFG-110824-3' where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-1' and cc_component='CFP';
 update construct_component set cc_component='Cerulean' where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-1' and cc_component='CFP';
 update construct set construct_name='Et(hoxa2b-SCP1-Ocu.Hbb2:Cre-2A-Cerulean)' where construct_zdb_id='ZDB-ETCONSTRCT-180514-1';
 update marker set mrkr_name='Et(hoxa2b-SCP1-Ocu.Hbb2:Cre-2A-Cerulean)' where mrkr_zdb_id='ZDB-ETCONSTRCT-180514-1';
 update marker set mrkr_abbrev='Et(hoxa2b-SCP1-Ocu.Hbb2:Cre-2A-Cerulean)' where mrkr_zdb_id='ZDB-ETCONSTRCT-180514-1';
 update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-110824-3' where mrel_mrkr_1_zdb_id='ZDB-ETCONSTRCT-180514-1' and mrel_mrkr_2_zdb_id='ZDB-EFG-080201-1';
 update construct_marker_relationship set conmrkrrel_mrkr_zdb_id ='ZDB-EFG-110824-3' where conmrkrrel_construct_zdb_id ='ZDB-ETCONSTRCT-180514-1' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-080201-1';

update construct_component set cc_order=16 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180518-1' and cc_order=13;
update construct_component set cc_order=15 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180518-1' and cc_order=12;
update construct_component set cc_order=14 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180518-1' and cc_order=11;
update construct_component set cc_order=13 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180518-1' and cc_order=10;
update construct_component set cc_order=12 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180518-1' and cc_order=9;
update construct_component set cc_order=11 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180518-1' and cc_order=8;
update construct_component set cc_order=10 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180518-1' and cc_order=7;
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-ETCONSTRCT-180518-1','controlled vocab component','promoter component','ZDB-CV-150506-25','Ocu.',1,8);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-ETCONSTRCT-180518-1','text component','promoter component','-',1,7);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-ETCONSTRCT-180518-1','text component','promoter component','Hbb2',1,9);
  update construct_component set cc_component_zdb_id='ZDB-EREGION-181127-1' where cc_construct_zdb_id='ZDB-ETCONSTRCT-180518-1' and cc_component='NRSE';
 update construct_component set cc_component='REX2' where cc_construct_zdb_id='ZDB-ETCONSTRCT-180518-1' and cc_component='NRSE';
 update construct set construct_name='Et(REX2-SCP1-Ocu.Hbb2:Cre-2A-Cerulean)' where construct_zdb_id='ZDB-ETCONSTRCT-180518-1';
 update marker set mrkr_name='Et(REX2-SCP1-Ocu.Hbb2:Cre-2A-Cerulean)' where mrkr_zdb_id='ZDB-ETCONSTRCT-180518-1';
 update marker set mrkr_abbrev='Et(REX2-SCP1-Ocu.Hbb2:Cre-2A-Cerulean)' where mrkr_zdb_id='ZDB-ETCONSTRCT-180518-1';
 update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EREGION-181127-1' where mrel_mrkr_1_zdb_id='ZDB-ETCONSTRCT-180518-1' and mrel_mrkr_2_zdb_id='ZDB-EREGION-121107-1';
 update construct_marker_relationship set conmrkrrel_mrkr_zdb_id ='ZDB-EREGION-181127-1' where conmrkrrel_construct_zdb_id ='ZDB-ETCONSTRCT-180518-1' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-121107-1';


update construct_component set cc_order=12 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-2' and cc_order=9;
update construct_component set cc_order=11 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-2' and cc_order=8;
update construct_component set cc_order=10 where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-2' and cc_order=7;
 update construct_component set cc_component_zdb_id='ZDB-EREGION-181127-1' where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-2' and cc_component='NRSE';
 update construct_component set cc_component='REX2' where cc_construct_zdb_id='ZDB-ETCONSTRCT-180514-2' and cc_component='NRSE';
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-ETCONSTRCT-180514-2','controlled vocab component','promoter component','ZDB-CV-150506-25','Ocu.',1,8);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-ETCONSTRCT-180514-2','text component','promoter component','-',1,7);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-ETCONSTRCT-180514-2','text component','promoter component','Hbb2',1,9);
 update construct set construct_name='Et(REX2-SCP1-Ocu.Hbb2:Cre)' where construct_zdb_id='ZDB-ETCONSTRCT-180514-2';
 update marker set mrkr_name='Et(REX2-SCP1-Ocu.Hbb2:Cre)' where mrkr_zdb_id='ZDB-ETCONSTRCT-180514-2';
 update marker set mrkr_abbrev='Et(REX2-SCP1-Ocu.Hbb2:Cre)' where mrkr_zdb_id='ZDB-ETCONSTRCT-180514-2';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EREGION-181127-1' where mrel_mrkr_1_zdb_id='ZDB-ETCONSTRCT-180514-2' and mrel_mrkr_2_zdb_id='ZDB-EREGION-121107-1';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id ='ZDB-EREGION-181127-1' where conmrkrrel_construct_zdb_id ='ZDB-ETCONSTRCT-180514-2' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-121107-1';

delete from zdb_active_Data where zactvd_zdb_id='ZDB-ETCONSTRCT-180514-3';





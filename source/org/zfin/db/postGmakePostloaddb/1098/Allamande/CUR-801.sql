--liquibase formatted sql
--changeset pm:CUR-801


update construct set construct_name='Et(NRSE-SCP1-Ocu.HBB2:Cre-2A-Cerulean))' where construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
update marker set mrkr_name='Et(NRSE-SCP1-Ocu.HBB2:Cre-2A-Cerulean)' where mrkr_zdb_id='ZDB-ETCONSTRCT-151102-1';
update marker set mrkr_abbrev='Et(NRSE-SCP1-Ocu.HBB2:Cre-2A-Cerulean)' where mrkr_zdb_id='ZDB-ETCONSTRCT-151102-1';

update construct_Component set cc_order=16 where cc_order=15 and cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
update construct_Component set cc_order=15 where cc_order=14 and cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
update construct_Component set cc_order=14 where cc_order=13 and cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
update construct_Component set cc_order=13 where cc_order=12 and cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
update construct_Component set cc_order=12 where cc_order=11 and cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
update construct_Component set cc_order=11 where cc_order=10 and cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
update construct_Component set cc_order=10 where cc_order=9 and cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
update construct_Component set cc_order=9 where cc_order=8 and cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
update construct_Component set cc_order=8 where cc_order=7 and cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1';
update construct_Component set cc_order=7 where cc_order=6 and cc_construct_zdb_id='ZDB-ETCONSTRCT-151102-1';

insert into construct_component  (cc_order,cc_construct_Zdb_id,cc_component, cc_component_type ,cc_component_category, cc_cassette_number) values (6,'ZDB-ETCONSTRCT-151102-1','1','text component', 'promoter component', 1);








--liquibase formatted sql
--changeset pm:CUR-737

update construct_component set cc_order=17 where cc_order=16 and cc_construct_zdb_id='ZDB-TGCONSTRCT-151204-8';
update construct_component set cc_order=16 where cc_order=15 and cc_construct_zdb_id='ZDB-TGCONSTRCT-151204-8';
update construct_component set   cc_component='CFP' where cc_construct_zdb_id='ZDB-TGCONSTRCT-151204-8' and cc_component='ECFP';
update construct_component set cc_component_zdb_id='ZDB-EFG-080201-1' where cc_component='CFP' and cc_construct_zdb_id='ZDB-TGCONSTRCT-151204-8';


insert into construct_component (cc_construct_Zdb_id,cc_component_type,cc_component_category,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-151204-8','text component','coding component','mse',1,15);

update construct set construct_name='Tg(5xUAS:FMAVenus-2A-3xNLS-HA-mseCFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-151204-8';

update marker set mrkr_name='Tg(5xUAS:FMAVenus-2A-3xNLS-HA-mseCFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-151204-8';


update marker set mrkr_abbrev='Tg(5xUAS:FMAVenus-2A-3xNLS-HA-mseCFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-151204-8';




update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EFG-080201-1' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-151204-8' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-111115-1';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-080201-1' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-151204-8' and  mrel_mrkr_2_zdb_id='ZDB-EFG-111115-1';



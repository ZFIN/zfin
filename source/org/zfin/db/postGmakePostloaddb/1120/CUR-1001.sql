--liquibase formatted sql
--changeset pm:CUR-1001



update construct set construct_name='Tg(-0.2unc45b:TFP)' where construct_zdb_id='ZDB-TGCONSTRCT-160613-1';
update marker set mrkr_name='Tg(-0.2unc45b:TFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-160613-1';
update marker set mrkr_abbrev='Tg(-0.2unc45b:TFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-160613-1';


insert into construct_component(cc_construct_zdb_id, cc_order, cc_component, cc_component_type,cc_component_category) values ('ZDB-TGCONSTRCT-160613-1',3,'-0.2','text component','promoter component');
update construct_component set cc_order=4  where cc_component='unc45b' and cc_construct_zdb_id='ZDB-TGCONSTRCT-160613-1';
update construct_component set cc_order=5  where cc_component=':' and cc_construct_zdb_id='ZDB-TGCONSTRCT-160613-1';
update construct_component set cc_order=6  where cc_component='TFP' and cc_construct_zdb_id='ZDB-TGCONSTRCT-160613-1';
update construct_component set cc_order=7  where cc_component=')' and cc_construct_zdb_id='ZDB-TGCONSTRCT-160613-1';













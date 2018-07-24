--liquibase formatted sql
--changeset prita:ZFIN-5987

update construct set construct_name='Tg(-0.8myl7:NLS-DsRedx)' where construct_zdb_id='ZDB-TGCONSTRCT-110222-3';
update marker set mrkr_name='Tg(-0.8myl7:NLS-DsRedx)' where mrkr_zdb_id='ZDB-TGCONSTRCT-110222-3';
update marker set mrkr_abbrev='Tg(-0.8myl7:NLS-DsRedx)' where mrkr_zdb_id='ZDB-TGCONSTRCT-110222-3';



delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-110222-3' and cc_order=9

update construct_component set cc_order=11 where cc_construct_zdb_id='ZDB-TGCONSTRCT-110222-3' and cc_order=10;
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_component_zdb_id,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-110222-3','coding sequence of','coding component','NLS','ZDB-EREGION-110816-1',1,9);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-110222-3','coding sequence of','coding component','DsRedx','ZDB-EFG-081029-1',1,10);

update construct_marker_relationship set conmrkrrel_relationship_type='coding sequence of' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-110222-3' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-110816-1');
update construct_marker_relationship set conmrkrrel_relationship_type='coding sequence of' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-110222-3' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-110816-1');
update marker_relationship set mrel_type='coding sequence of' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-110222-3' and mrel_mrkr_2_zdb_id='';
update marker_relationship set mrel_type='coding sequence of' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-110222-3' and mrel_mrkr_2_zdb_id='';






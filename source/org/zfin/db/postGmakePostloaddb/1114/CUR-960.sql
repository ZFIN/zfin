--liquibase formatted sql
--changeset pm:CUR-960



update construct set construct_name=replace(construct_name,'cxxc1:RFP','opn1lw2:DsRedx') where construct_name like '%cxxc1:RFP%';
update marker set mrkr_name=replace(mrkr_name,'cxxc1:RFP','opn1lw2:DsRedx') where mrkr_name like '%cxxc1:RFP%';
update marker set mrkr_abbrev=replace(mrkr_abbrev,'cxxc1:RFP','opn1lw2:DsRedx') where mrkr_name like '%cxxc1:RFP%';

update construct set construct_name=replace(construct_name,'cxxc1:GFP','opn1lw2:GFP') where construct_name like '%cxxc1:GFP%';
update marker set mrkr_name=replace(mrkr_name,'cxxc1:GFP','opn1lw2:GFP') where mrkr_name like '%cxxc1:GFP%';
update marker set mrkr_abbrev=replace(mrkr_abbrev,'cxxc1:GFP','opn1lw2:GFP') where mrkr_name like '%cxxc1:GFP%';


update construct_component set cc_component='opn1lw2' where cc_component='cxxc1' and cc_construct_zdb_id like 'ZDB-TGCONSTRCT-110519-%';
update construct_component set cc_component='DsRedx' where cc_component='RFP' and cc_construct_zdb_id like 'ZDB-TGCONSTRCT-110519-%';

update construct_component set cc_component_zdb_id='ZDB-GENE-040718-141'  where cc_component='opn1lw2' and cc_construct_zdb_id like 'ZDB-TGCONSTRCT-110519-%';
update construct_component set cc_component_zdb_id='ZDB-EFG-081029-1'  where cc_component='DsRedx' and cc_construct_zdb_id like 'ZDB-TGCONSTRCT-110519-%';



update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-GENE-040718-141' where mrel_mrkr_2_zdb_id='ZDB-GENE-030728-4' and mrel_mrkr_1_zdb_id like 'ZDB-TGCONSTRCT-110519-%';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-GENE-040718-141' where conmrkrrel_construct_zdb_id like 'ZDB-TGCONSTRCT-110519-%' and conmrkrrel_mrkr_zdb_id='ZDB-GENE-030728-4';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-081029-1' where mrel_mrkr_1_zdb_id like 'ZDB-TGCONSTRCT-110519-%' and mrel_mrkr_2_zdb_id ='ZDB-EFG-070117-3';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EFG-081029-1' where conmrkrrel_construct_zdb_id like 'ZDB-TGCONSTRCT-110519-%' and conmrkrrel_mrkr_zdb_id ='ZDB-EFG-070117-3';
















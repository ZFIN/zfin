--liquibase formatted sql
--changeset pm:CUR-919

update construct set construct_name = 'Tg(rr.col2a1a.2:EGFP)' where construct_zdb_id = 'ZDB-TGCONSTRCT-111205-2';

update marker set mrkr_name = 'Tg(rr.col2a1a.2:EGFP)' where mrkr_zdb_id = 'ZDB-TGCONSTRCT-111205-2';     

update marker set mrkr_abbrev = 'Tg(rr.col2a1a.2:EGFP)' where mrkr_zdb_id = 'ZDB-TGCONSTRCT-111205-2';


update construct_component set cc_component_zdb_id='ZDB-RR-190807-1' where cc_construct_zdb_id='ZDB-TGCONSTRCT-111205-2' and cc_component_zdb_id='ZDB-GENE-980526-192';



update marker_relationship
set mrel_mrkr_2_zdb_id='ZDB-RR-190807-1'
where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-111205-2'
and mrel_mrkr_2_zdb_id='ZDB-GENE-980526-192';


update construct_marker_relationship
set conmrkrrel_mrkr_zdb_id='ZDB-RR-190807-1'
where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-111205-2'
and conmrkrrel_mrkr_zdb_id='ZDB-GENE-980526-192';

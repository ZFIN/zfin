--liquibase formatted sql
--changeset pm:CUR-864

update construct_component set cc_component_zdb_id='ZDB-EREGION-181218-1' where cc_component='egr2b' and cc_construct_zdb_id='ZDB-TGCONSTRCT-101103-3';
update construct_component set cc_component='ELTC' where cc_component_zdb_id='ZDB-EREGION-181218-1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-101103-3';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EREGION-181218-1' where  conmrkrrel_construct_zdb_id = 'ZDB-TGCONSTRCT-101103-3' and conmrkrrel_mrkr_zdb_id='ZDB-GENE-980526-283';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EREGION-181218-1' where  mrel_mrkr_1_zdb_id = 'ZDB-TGCONSTRCT-101103-3' and mrel_mrkr_2_zdb_id='ZDB-GENE-980526-283';

update construct_component set cc_component_zdb_id='ZDB-EREGION-181218-2' where cc_component='ubc' and cc_construct_zdb_id='ZDB-TGCONSTRCT-160824-4';
update construct_component set cc_component='UBCI' where cc_component_zdb_id='ZDB-EREGION-181218-2' and cc_construct_zdb_id='ZDB-TGCONSTRCT-160824-4';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EREGION-181218-2' where  conmrkrrel_construct_zdb_id = 'ZDB-TGCONSTRCT-160824-4' and conmrkrrel_mrkr_zdb_id='ZDB-GENE-061110-88';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EREGION-181218-2' where  mrel_mrkr_1_zdb_id = 'ZDB-TGCONSTRCT-160824-4' and mrel_mrkr_2_zdb_id='ZDB-GENE-061110-88';









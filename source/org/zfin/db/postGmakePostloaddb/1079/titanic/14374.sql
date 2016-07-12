begin work;
update construct_component set cc_component_zdb_id='ZDB-GENE-030613-1' where cc_construct_zdb_id='ZDB-TGCONSTRCT-150828-5' and cc_component_zdb_id='ZDB-GENE-000210-20';
update construct_component set cc_component='dusp6' where cc_construct_zdb_id='ZDB-TGCONSTRCT-150828-5' and cc_component='cat';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-GENE-030613-1' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-150828-5' and conmrkrrel_relationship_type='coding sequence of';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-GENE-030613-1' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-150828-5' and mrel_type='coding sequence of';

--rollback work;
commit work;

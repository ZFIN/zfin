--liquibase formatted sql
--changeset pm:CUR-911

update construct set construct_name = 'Tg(ca8-E1B:Hso.Arch3-TagRFPT,GCaMP5G)' where construct_zdb_id = 'ZDB-TGCONSTRCT-151204-5';

update marker set mrkr_name = 'Tg(ca8-E1B:Hso.Arch3-TagRFPT,GCaMP5G)' where mrkr_zdb_id = 'ZDB-TGCONSTRCT-151204-5';     

update marker set mrkr_abbrev = 'Tg(ca8-E1B:Hso.Arch3-TagRFPT,GCaMP5G)' where mrkr_zdb_id = 'ZDB-TGCONSTRCT-151204-5';

delete from construct_component where cc_component_zdb_id = 'ZDB-EREGION-160817-1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-151204-5';

update construct_component set cc_component_zdb_id='ZDB-EFG-160519-3' where cc_construct_zdb_id='ZDB-TGCONSTRCT-151204-5' and cc_component_zdb_id='ZDB-EFG-110425-1';


delete from marker_relationship
where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-151204-5'
and mrel_mrkr_2_zdb_id='ZDB-EREGION-160817-1';

update marker_relationship
set mrel_mrkr_2_zdb_id='ZDB-EFG-160519-3'
where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-151204-5'
and mrel_mrkr_2_zdb_id='ZDB-EFG-110425-1';


delete from construct_marker_relationship
where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-151204-5'
and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-160817-1';

update construct_marker_relationship
set conmrkrrel_mrkr_zdb_id='ZDB-EFG-160519-3'
where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-151204-5'
and conmrkrrel_mrkr_zdb_id='ZDB-EFG-110425-1';

--liquibase formatted sql
--changeset pm:DLOAD-597


 update construct_component set cc_component_zdb_id='ZDB-EFG-160519-3' where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_component_zdb_id='ZDB-EFG-110425-1';
 update construct_component set cc_component='TagRFPT' where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_component='TagRFP';

update construct_component set cc_component_zdb_id='ZDB-EFG-160519-3' where cc_construct_zdb_id='ZDB-TGCONSTRCT-160830-1' and cc_component_zdb_id='ZDB-EFG-110425-1';
 update construct_component set cc_component='TagRFPT' where cc_construct_zdb_id='ZDB-TGCONSTRCT-160830-1' and cc_component='TagRFP';




 update construct set construct_name='Tg(UAS:NTR-TagRFPT-UTR)' where construct_zdb_id='ZDB-TGCONSTRCT-160830-1';
 update marker set mrkr_name='Tg(UAS:NTR-TagRFPT-UTR)' where mrkr_zdb_id='ZDB-TGCONSTRCT-160830-1';
 update marker set mrkr_abbrev='Tg(UAS:NTR-TagRFPT-UTR)' where mrkr_zdb_id='ZDB-TGCONSTRCT-160830-1';

 update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-160519-3' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-160830-1' and mrel_mrkr_2_zdb_id='ZDB-EFG-110425-1';
 update construct_marker_relationship set conmrkrrel_mrkr_zdb_id ='ZDB-EFG-160519-3' where conmrkrrel_construct_zdb_id ='ZDB-TGCONSTRCT-160830-1' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-110425-1';

 update construct set construct_name='Tg(UAS-ADV.E1b:NTR-TagRFPT)' where construct_zdb_id='ZDB-TGCONSTRCT-141113-4';
 update marker set mrkr_name='Tg(UAS-ADV.E1b:NTR-TagRFPT)' where mrkr_zdb_id='ZDB-TGCONSTRCT-141113-4';
 update marker set mrkr_abbrev='Tg(UAS-ADV.E1b:NTR-TagRFPT)' where mrkr_zdb_id='ZDB-TGCONSTRCT-141113-4';

 update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-160519-3' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-141113-4' and mrel_mrkr_2_zdb_id='ZDB-EFG-110425-1';
 update construct_marker_relationship set conmrkrrel_mrkr_zdb_id ='ZDB-EFG-160519-3' where conmrkrrel_construct_zdb_id ='ZDB-TGCONSTRCT-141113-4' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-110425-1';






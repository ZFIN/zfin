--liquibase formatted sql
--changeset pm:ZFIN-6121


update construct_component set cc_component_zdb_id='ZDB-EFG-160519-3' where cc_component='TagRFP' and cc_construct_zdb_id='ZDB-TGCONSTRCT-170125-1' ;
update construct_component set cc_component='TagRFPT' where cc_component='TagRFP' and cc_construct_zdb_id='ZDB-TGCONSTRCT-170125-1';

update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-160519-3' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-170125-1' and mrel_mrkr_2_zdb_id='ZDB-EFG-110425-1';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EFG-160519-3' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-170125-1' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-110425-1';

update construct set construct_name='Tg(tph2:epNTR-TagRFPT)' where construct_zdb_id='ZDB-TGCONSTRCT-170125-1';
update marker set mrkr_name='Tg(tph2:epNTR-TagRFPT)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170125-1';
update marker set mrkr_abbrev='Tg(tph2:epNTR-TagRFPT)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170125-1';





--liquibase formatted sql
--changeset pm:CUR-757

delete from zdb_active_data where zactvd_zdb_id='ZDB-MREL-180129-6';
delete form zdb_active_Data where zactvd_zdb_id='ZDB-CMREL-180129-4';

update construct_Component set cc_component_zdb_id='ZDB-CV-150506-13' where cc_construct_Zdb_id='ZDB-TGCONSTRCT-180129-2' and cc_order=5;
update construct_Component set cc_component='NRAS_Q61K' where cc_construct_Zdb_id='ZDB-TGCONSTRCT-180129-2' and cc_order=6;

update construct set construct_name='Tg(fabp10a:Hsa.NRAS_Q61K)' where construct_zdb_id='ZDB-TGCONSTRCT-180129-2';
update marker set mrkr_name='Tg(fabp10a:Hsa.NRAS_Q61K)' where mrkr_zdb_id='ZDB-TGCONSTRCT-180129-2';
update marker set mrkr_abbrev='Tg(fabp10a:Hsa.NRAS_Q61K)' where mrkr_zdb_id='ZDB-TGCONSTRCT-180129-2';





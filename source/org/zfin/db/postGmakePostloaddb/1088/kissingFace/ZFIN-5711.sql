--liquibase formatted sql
--changeset pm:ZFIN-5711

update construct_component set cc_component_zdb_id='' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-7' and cc_component='MLS';
update construct_component set cc_component_zdb_id='' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-5' and cc_component='MLS';
update construct_component set cc_component_zdb_id='' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-6' and cc_component='MLS';
update construct_component set cc_component_zdb_id='' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-3' and cc_component='MLS';
update construct_component set cc_component_zdb_id='' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-4' and cc_component='MLS';
update construct_component set cc_component_zdb_id='' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-19' and cc_component='MLS';
update construct_component set cc_component_zdb_id='' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-20' and cc_component='MLS';


update construct_component set cc_component='Hsa.HRAS' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-7' and cc_component='MLS';
update construct_component set cc_component='Hsa.HRAS' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-5' and cc_component='MLS';
update construct_component set cc_component='Hsa.HRAS' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-6' and cc_component='MLS';
update construct_component set cc_component='Hsa.HRAS' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-3' and cc_component='MLS';
update construct_component set cc_component='Hsa.HRAS' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-4' and cc_component='MLS';
update construct_component set cc_component='Hsa.HRAS' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-19' and cc_component='MLS';
update construct_component set cc_component='Hsa.HRAS' where cc_construct_zdb_id='ZDB-TGCONSTRCT-170322-20' and cc_component='MLS';



update construct set construct_name='Tg(kdrl:Eco.BirA-Hsa.HRAS-mCherry)' where construct_Zdb_id='ZDB-TGCONSTRCT-170322-7';
update construct set construct_name='Tg(myl7:Eco.BirA-Hsa.HRAS-mCherry)' where construct_Zdb_id='ZDB-TGCONSTRCT-170322-5';
update construct set construct_name='Tg(myl7:NLS-Eco.BirA-Hsa.HRAS-mCherry)' where construct_Zdb_id='ZDB-TGCONSTRCT-170322-6';
update construct set construct_name='Tg(sox10:Eco.BirA-Hsa.HRAS-mCherry)' where construct_Zdb_id='ZDB-TGCONSTRCT-170322-3';
update construct set construct_name='Tg(zic2a:NLS-Eco.BirA-Hsa.HRAS-mCherry)' where construct_Zdb_id='ZDB-TGCONSTRCT-170322-4';
update construct set construct_name='Tg(actb2:Eco.BirA-Hsa.HRAS-mCherry)' where construct_Zdb_id='ZDB-TGCONSTRCT-170322-19';
update construct set construct_name='Tg(actb2:NLS-Eco.BirA-Hsa.HRAS-mCherry)' where construct_Zdb_id='ZDB-TGCONSTRCT-170322-20';

update marker set mrkr_name='Tg(kdrl:Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-7';
update marker set mrkr_name='Tg(myl7:Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-5';
update marker set mrkr_name='Tg(myl7:NLS-Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-6';
update marker set mrkr_name='Tg(sox10:Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-3';
update marker set mrkr_name='Tg(zic2a:NLS-Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-4';
update marker set mrkr_name='Tg(actb2:Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-19';
update marker set mrkr_name='Tg(actb2:NLS-Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-20';


update marker set mrkr_abbrev='Tg(kdrl:Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-7';
update marker set mrkr_abbrev='Tg(myl7:Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-5';
update marker set mrkr_abbrev='Tg(myl7:NLS-Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-6';
update marker set mrkr_abbrev='Tg(sox10:Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-3';
update marker set mrkr_abbrev='Tg(zic2a:NLS-Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-4';
update marker set mrkr_abbrev='Tg(actb2:Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-19';
update marker set mrkr_abbrev='Tg(actb2:NLS-Eco.BirA-Hsa.HRAS-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170322-20';


delete from zdb_active_Data where zactvd_zdb_id=(select mrel_zdb_id from marker_relationship where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-170322-7' and mrel_mrkr_2_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select mrel_zdb_id from marker_relationship where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-170322-5' and mrel_mrkr_2_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select mrel_zdb_id from marker_relationship where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-170322-6' and mrel_mrkr_2_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select mrel_zdb_id from marker_relationship where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-170322-3' and mrel_mrkr_2_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select mrel_zdb_id from marker_relationship where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-170322-4' and mrel_mrkr_2_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select mrel_zdb_id from marker_relationship where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-170322-19' and mrel_mrkr_2_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select mrel_zdb_id from marker_relationship where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-170322-20' and mrel_mrkr_2_zdb_id='ZDB-EREGION-170208-1');

delete from zdb_active_Data where zactvd_zdb_id=(select conmrkrrel_zdb_id from construct_marker_relationship where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-170322-7' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select conmrkrrel_zdb_id from construct_marker_relationship where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-170322-5' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select conmrkrrel_zdb_id from construct_marker_relationship where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-170322-6' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select conmrkrrel_zdb_id from construct_marker_relationship where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-170322-3' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select conmrkrrel_zdb_id from construct_marker_relationship where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-170322-4' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select conmrkrrel_zdb_id from construct_marker_relationship where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-170322-19' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-170208-1');
delete from zdb_active_Data where zactvd_zdb_id=(select conmrkrrel_zdb_id from construct_marker_relationship where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-170322-20' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-170208-1');
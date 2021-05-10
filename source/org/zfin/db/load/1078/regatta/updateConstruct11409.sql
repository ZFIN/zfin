--liquibase formatted sql
--changeset pm:constructNameUpdatesRegatta

update construct_component set cc_component=',' where cc_component=')' and cc_construct_zdb_id like 'ZDB-TGCONSTRCT-140814-%';
update construct_component set cc_component_type='controlled vocab component' where cc_construct_Zdb_id like 'ZDB-TGCONSTRCT-140814-%' and cc_order=13;
update construct_component set cc_component_category='cassette delimiter' where cc_construct_Zdb_id like 'ZDB-TGCONSTRCT-140814-%' and cc_order=13;
update construct_component set cc_component_zdb_id='ZDB-CV-150506-11' where cc_construct_Zdb_id like 'ZDB-TGCONSTRCT-140814-%' and cc_order=13;

insert into construct_component (cc_construct_zdb_id, cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) select * from cc_temp;

update construct set construct_name='Tg1(UAS:MYC-vangl2,NLS-RFP,myl7:EGFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-140814-1';
update construct set construct_name='Tg2(UAS:MYC-vangl2,NLS-RFP,myl7:EGFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-140814-2';
update construct set construct_name='Tg(UAS:MYC-vangl2-Hsa.PLCD1,NLS-RFP,myl7:EGFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-140814-3';
update construct set construct_name='Tg1(UAS:vangl2-MYC,NLS-RFP,myl7:EGFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-140814-4';
update construct set construct_name='Tg2(UAS:vangl2-MYC,NLS-RFP,myl7:EGFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-140814-5';
update construct set construct_name='Tg3(UAS:vangl2-MYC,NLS-RFP,myl7:EGFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-140814-6';
update construct set construct_name='Tg(UAS:MYC-vangl2-Mmu.Lyn,NLS-RFP,myl7:EGFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-140814-7';
update construct set construct_name='Tg(UAS:MYC-vangl2-Dme.Vang,NLS-RFP,myl7:EGFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-140814-8';
update construct set construct_name=	'Tg(UAS:MYC-vangl2-Rno.P2rx2,NLS-RFP,myl7:EGFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-140814-10';
update construct set construct_name=	'Tg4(UAS:MYC-vangl2,NLS-RFP,myl7:EGFP)' where construct_Zdb_id='ZDB-TGCONSTRCT-150227-1';

update marker set mrkr_name='Tg1(UAS:MYC-vangl2,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-1';
update marker set mrkr_name='Tg2(UAS:MYC-vangl2,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-2';
update marker set mrkr_name='Tg(UAS:MYC-vangl2-Hsa.PLCD1,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-3';
update marker set mrkr_name='Tg1(UAS:vangl2-MYC,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-4';
update marker set mrkr_name='Tg2(UAS:vangl2-MYC,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-5';
update marker set mrkr_name='Tg3(UAS:vangl2-MYC,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-6';
update marker set mrkr_name='Tg(UAS:MYC-vangl2-Mmu.Lyn,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-7';
update marker set mrkr_name='Tg(UAS:MYC-vangl2-Dme.Vang,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-8';
update marker set mrkr_name=	'Tg(UAS:MYC-vangl2-Rno.P2rx2,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-10';
update marker set mrkr_name=	'Tg4(UAS:MYC-vangl2,NLS-RFP,myl7:EGFP)' where mrkr_Zdb_id='ZDB-TGCONSTRCT-150227-1';

update marker set mrkr_abbrev='Tg1(UAS:MYC-vangl2,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-1';
update marker set mrkr_abbrev='Tg2(UAS:MYC-vangl2,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-2';
update marker set mrkr_abbrev='Tg(UAS:MYC-vangl2-Hsa.PLCD1,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-3';
update marker set mrkr_abbrev='Tg1(UAS:vangl2-MYC,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-4';
update marker set mrkr_abbrev='Tg2(UAS:vangl2-MYC,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-5';
update marker set mrkr_abbrev='Tg3(UAS:vangl2-MYC,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-6';
update marker set mrkr_abbrev='Tg(UAS:MYC-vangl2-Mmu.Lyn,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-7';
update marker set mrkr_abbrev='Tg(UAS:MYC-vangl2-Dme.Vang,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-8';
update marker set mrkr_abbrev=	'Tg(UAS:MYC-vangl2-Rno.P2rx2,NLS-RFP,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140814-10';
update marker set mrkr_abbrev=	'Tg4(UAS:MYC-vangl2,NLS-RFP,myl7:EGFP)' where mrkr_Zdb_id='ZDB-TGCONSTRCT-150227-1';

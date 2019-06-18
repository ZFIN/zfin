--liquibase formatted sql
--changeset xshao:ZFIN-6281

update construct set construct_name = 'Tg(nccr.6500-Mmu.Fos:GFP)' where construct_zdb_id = 'ZDB-TGCONSTRCT-190404-9';

update marker set mrkr_name = 'Tg(nccr.6500-Mmu.Fos:GFP)' where mrkr_zdb_id = 'ZDB-TGCONSTRCT-190404-9';     

update marker set mrkr_abbrev = 'Tg(nccr.6500-Mmu.Fos:GFP)' where mrkr_zdb_id = 'ZDB-TGCONSTRCT-190404-9';

delete from construct_component where cc_pk_id = 51909;

update construct_component set cc_order = 9 where cc_pk_id = 51910;


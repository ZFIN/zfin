--liquibase formatted sql
--changeset pm:CUR-802


update construct set construct_name='Tg(myl7:Hso.Arch_D95N-GCaMP5G)' where construct_zdb_id='ZDB-TGCONSTRCT-150323-1';
update marker set mrkr_name='Tg(myl7:Hso.Arch_D95N-GCaMP5G)' where mrkr_zdb_id='ZDB-TGCONSTRCT-150323-1';
update marker set mrkr_abbrev='Tg(myl7:Hso.Arch_D95N-GCaMP5G)' where mrkr_zdb_id='ZDB-TGCONSTRCT-150323-1';

update construct_Component set cc_order=11 where cc_component=')' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150323-1';








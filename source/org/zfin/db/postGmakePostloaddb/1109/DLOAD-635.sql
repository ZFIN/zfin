--liquibase formatted sql
--changeset pm:DLOAD-635

--deleting problem GO evidence (with field) annotations so that the next load can add them properly


delete from zdb_active_data where zactvd_zdb_id in (select mrkrgoev_zdb_id from marker_go_term_evidence, inference_group_member where infgrmem_inferred_from like '%, ZFIN' and mrkrgoev_zdb_id=infgrmem_mrkrgoev_zdb_id);
delete from zdb_active_data where zactvd_zdb_id in (select mrkrgoev_zdb_id from marker_go_term_evidence, inference_group_member where infgrmem_inferred_from like '%,ZFIN' and mrkrgoev_zdb_id=infgrmem_mrkrgoev_zdb_id);
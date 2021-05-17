--liquibase formatted sql
--changeset sierra:delete-zfin-GO-annotations.sql

delete from marker_go_term_evidence
  where mrkrgoev_annotation_organization in (1,6)
  and mrkrgoev_evidence_code != 'IEA';

delete from zdb_active_data where zactvd_zdb_id not in (select mrkrgoev_zdb_id
 from marker_go_term_evidence where mrkrgoev_zdb_id = zactvd_zdb_id)
and zactvd_zdb_id like 'ZDB-MRKRGOEV%';

 

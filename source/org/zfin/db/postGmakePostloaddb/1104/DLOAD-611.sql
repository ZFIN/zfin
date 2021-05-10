--liquibase formatted sql
--changeset pm:DLOAD-611


 update  marker_go_term_annotation_extension set mgtae_identifier_term_zdb_id=(select term_zdb_id from term where mgtae_term_text= term_ont_id)
where mgtae_identifier_term_zdb_id is null and mgtae_term_text like 'ZFA%';


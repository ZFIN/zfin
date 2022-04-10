--liquibase formatted sql
--changeset cmpich:ZFIN-7825

--alter table marker_go_term_evidence drop constraint mrkrgoev_protein_dblink_id_fk_odc;
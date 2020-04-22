--liquibase formatted sql
--changeset pm:DLOAD-672

insert into eco_go_mapping (egm_term_zdb_id,egm_go_evidence_code) values ('ZDB-TERM-170419-208', 'ISS');

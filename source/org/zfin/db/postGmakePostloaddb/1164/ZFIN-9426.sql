--liquibase formatted sql
--changeset cmpich:ZFIN-9426

insert into eco_go_mapping (egm_term_zdb_id,egm_go_evidence_code) values ('ZDB-TERM-170419-718', 'IDA');
insert into eco_go_mapping (egm_term_zdb_id,egm_go_evidence_code) values ('ZDB-TERM-170419-535', 'IMP');

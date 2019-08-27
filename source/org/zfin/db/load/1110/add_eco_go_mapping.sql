--liquibase formatted sql
--changeset sierra:eco_go_mapping.sql

create table eco_go_mapping (egm_pk_id serial8 not null primary key,
             egm_term_zdb_id text,
             egm_go_evidence_code text);

create unique index egm_alternate_key_index
  on eco_go_mapping (egm_term_zdb_id, egm_go_evidence_code);

alter table eco_go_mapping
  add constraint egm_eco_term_zdb_id_fk foreign key (egm_term_zdb_id)
  references term on delete cascade;


--liquibase formatted sql
--changeset pm:add_relation_to_qualifier_go.sql

alter table marker_go_term_evidence
add column mrkrgoev_relation_term_zdb_id text;

alter table marker_go_term_evidence
  add constraint mrkrgoev_relation_term_zdb_id_fk_odc
  foreign key (mrkrgoev_relation_term_zdb_id)
  references term(term_zdb_id)
 ;





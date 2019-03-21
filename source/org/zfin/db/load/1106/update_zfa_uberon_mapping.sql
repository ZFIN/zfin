--liquibase formatted sql
--changeset sierra:update_zfa_uberon_mapping.sql

delete from zfa_uberon_mapping;

insert into zfa_uberon_mapping(zum_uberon_id, zum_zfa_term_zdb_id,zum_zfa_id)
  select u_uberon_id, term_zdb_id, u_zfa_id from tmp_uberon_map, term
 where term_ont_id = u_zfa_id;


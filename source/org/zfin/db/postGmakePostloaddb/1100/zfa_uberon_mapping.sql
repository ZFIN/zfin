--liquibase formatted sql
--changeset kschaper:zfa_uberon_mapping

create table zfa_uberon_mapping (
  zum_zfa_id            varchar(55),
  zum_zfa_term_zdb_id   varchar(55),
  zum_uberon_id         varchar(55)
)



--liquibase formatted sql
--changeset kschaper:zum_update_for_v2_0

insert into zfa_uberon_mapping (zum_zfa_id, zum_uberon_id)
values
  ('ZFA:0009176','UBERON:0016887'),
  ('ZFA:0000556','UBERON:0002104'),
  ('ZFA:0000435','UBERON:0002104'),
  ('ZFA:0001678','UBERON:0002104'),
  ('ZFA:0000137','UBERON:0002104'),
  ('ZFA:0005830','UBERON:0002193'),
  ('ZFA:0009250','UBERON:0002193')
;

update zfa_uberon_mapping
set zum_zfa_term_zdb_id = term_zdb_id
from term
where term_ont_id = zum_zfa_id;


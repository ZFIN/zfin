--liquibase formatted sql
--changeset sierra:add_zyg_ont_id.sql

alter table zygocity
  add column zyg_geno_ont_id text;

update zygocity
  set zyg_geno_ont_id = 'GENO:0000136'
 where zyg_name = 'homozygous';

update zygocity
  set zyg_geno_ont_id = 'GENO:0000135'
 where zyg_name = 'heterozygous';

update zygocity
  set zyg_geno_ont_id = 'GENO:000013'
 where zyg_name = 'complex';

update zygocity
  set zyg_geno_ont_id = 'GENO:0000137'
 where zyg_name = 'unknown';

update zygocity
  set zyg_geno_ont_id = 'GENO:000013'
 where zyg_name = 'wild type';

--liquibase formatted sql
--changeset sierra:fix_zyg_ont_id.sql

update zygocity
  set zyg_geno_ont_id = 'GENO:0000137'
 where zyg_geno_ont_id = 'GENO:000013';

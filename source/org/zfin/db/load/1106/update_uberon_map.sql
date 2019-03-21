--liquibase formatted sql
--changeset sierra:update_uberon_map.sql

update zfa_uberon_mapping
  set zum_uberon_id = 'UBERON:0005409'
 where zum_uberon_id = 'UBERON:0001007';


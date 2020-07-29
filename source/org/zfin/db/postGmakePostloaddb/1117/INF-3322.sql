--liquibase formatted sql
--changeset sierra:INF-3322.sql

update genotype
  set geno_complexity_order = geno_complexity_order
 where geno_complexity_order != update_geno_sort_order(geno_zdb_id);
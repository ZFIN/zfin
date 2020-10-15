--liquibase formatted sql
--changeset sierra:ZFIN-6826.sql

update fish
 set fish_name = fish_name;

update marker
  set mrkr_abbrev = mrkr_abbrev 
where mrkr_abbrev in ('tenm2','faap100','mhc2bl', 'slc4a5a','vit','olfcq18','msi1a','zp3e','wnt11','wnt11r');

update genotype
 set geno_display_name = get_genotype_display(geno_zdb_id)
 where geno_display_name = '';


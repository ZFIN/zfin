--liquibase formatted sql
--changeset xshao:CUR-759

update genotype
 set geno_display_name = 'uwm12Tg'
where geno_zdb_id = 'ZDB-GENO-150430-1';

update fish
  set fish_name = 'uwm12Tg'
where fish_zdb_id = 'ZDB-FISH-150901-10562';

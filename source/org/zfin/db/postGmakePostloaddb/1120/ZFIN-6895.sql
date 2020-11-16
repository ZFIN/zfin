--liquibase formatted sql
--changeset sierra:ZFIN-6895.sql

update fish
  set fish_name = get_fish_name(fish_zdb_id, fish_genotype_zdb_id)
 where exists (Select 'x' from genotype, genotype_background 
		where geno_Zdb_id = genoback_geno_zdb_id
		and fish_genotype_zdb_id = geno_zdb_id)
and fish_name not like '%(%)';



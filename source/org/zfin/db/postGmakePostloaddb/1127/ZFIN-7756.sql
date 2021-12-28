--liquibase formatted sql
--changeset ryan:ZFIN-7756
--basically a re-run of 6895
--added extra check in where clause to make sure the only difference between the fish_name and get_fish_name is the addition of background

update fish
set fish_name = get_fish_name(fish_zdb_id, fish_genotype_zdb_id)
WHERE
    EXISTS ( SELECT 'x' FROM genotype, genotype_background WHERE geno_Zdb_id = genoback_geno_zdb_id AND fish_genotype_zdb_id = geno_zdb_id )
  AND regexp_replace( get_fish_name ( fish_zdb_id, fish_genotype_zdb_id ), ' ?\(.*\)$', '') =  regexp_replace( fish_name, ' ?\(.*\)$', '');


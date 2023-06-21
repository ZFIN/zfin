--liquibase formatted sql
--changeset cmpich:ZFIN-8684.sql

-- rename genotype according to the naming rule in the DB
update genotype set geno_display_name = get_genotype_display('ZDB-GENO-170308-12') where geno_zdb_id = 'ZDB-GENO-170308-12';

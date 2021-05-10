--liquibase formatted sql
--changeset xshao:ZFIN-6392

update genotype set geno_display_name = 'Df(Chr6:mir141,mir200c,mir429b)hza52/hza52' where geno_zdb_id = 'ZDB-GENO-180823-5';

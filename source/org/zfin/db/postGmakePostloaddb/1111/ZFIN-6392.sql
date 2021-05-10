--liquibase formatted sql
--changeset xshao:ZFIN-6392

update genotype set geno_display_name = get_genotype_display(geno_zdb_id) where geno_display_name is null or geno_display_name = '';

update genotype set geno_display_name = 'Df(Chr23:mir200a,mir200b,mir429)hza53/hza53' where geno_zdb_id = 'ZDB-GENO-180823-6';

update genotype set geno_display_name = 'Df(Chr6:mir141,mir200c,mir429b)hza52/hza52' where geno_zdb_id = 'ZDB-GENO-180823-5';


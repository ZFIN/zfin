--liquibase formatted sql
--changeset xshao:ZFIN-5720

update genotype set geno_display_name = 'Tüpfel long fin' where geno_zdb_id = 'ZDB-GENO-990623-2';

update data_alias set dalias_alias = 'Tüebingen long fin' where dalias_zdb_id = 'ZDB-DALIAS-070518-18';


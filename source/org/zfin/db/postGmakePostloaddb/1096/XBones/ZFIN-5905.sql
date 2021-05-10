--liquibase formatted sql
--changeset xshao:ZFIN-5905

update genotype set geno_display_name = "T(Chr09:dlx1a,dlx2a,eng1a,hoxd4a)b566/b566" 
 where geno_zdb_id = "ZDB-GENO-991201-132";

update genotype set geno_display_name = "T(Chr01:dlx6a,eng1b)r1/r1"
 where geno_zdb_id = "ZDB-GENO-991111-48";



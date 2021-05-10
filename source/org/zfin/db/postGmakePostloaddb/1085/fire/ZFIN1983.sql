--liquibase formatted sql
--changeset xiang:ZFIN1983

update genotype set geno_display_name = 'ptch1<sup>tj222/tj222</sup>; ptch2<sup>hu1602/hu1602</sup>; zf132Tg'
where geno_zdb_id = 'ZDB-GENO-120203-7';

update genotype set geno_display_name = 'ptch1<sup>tj222/+</sup>; ptch2<sup>hu1602/+</sup>'
where geno_zdb_id = 'ZDB-GENO-080728-5';

update genotype set geno_display_name = 'ptch1<sup>tj222/tj222</sup>; ptch2<sup>hu1602/hu1602</sup>'
where geno_zdb_id = 'ZDB-GENO-080728-6';

update genotype set geno_display_name = 'ptch1<sup>tj222/+</sup>; ptch2<sup>hu1602/hu1602</sup>'
where geno_zdb_id = 'ZDB-GENO-080728-7';

update genotype set geno_display_name = 'plcg1<sup>t26480/</sup>; ptch1<sup>tj222/</sup>; ptch2<sup>hu1602/+</sup>'
where geno_zdb_id = 'ZDB-GENO-130603-1';


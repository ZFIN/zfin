--liquibase formatted sql
--changeset ryan:ZFIN-7891

-- fix names
update genotype set geno_display_name = 'ihb19Tg' where geno_zdb_id = 'ZDB-GENO-131226-1';
update genotype set geno_display_name = 'slc24a5<sup>b1/+</sup>' where geno_zdb_id = 'ZDB-GENO-070209-25';
update genotype set geno_display_name = 'slc24a5<sup>b1/+</sup>' where geno_zdb_id = 'ZDB-GENO-090205-2';
update genotype set geno_display_name = 'slc24a5<sup>b1/b1</sup>' where geno_zdb_id = 'ZDB-GENO-100708-1';
update genotype set geno_display_name = 'nhsa<sup>fh299</sup>' where geno_zdb_id = 'ZDB-GENO-091112-30';
update genotype set geno_display_name = 'slc24a5<sup>b1/b1</sup>' where geno_zdb_id = 'ZDB-GENO-071214-1';
update genotype set geno_display_name = 'slc24a5<sup>b1/b1</sup>' where geno_zdb_id = 'ZDB-GENO-100824-4';
update genotype set geno_display_name = 'slc24a5<sup>b1</sup>' where geno_zdb_id = 'ZDB-GENO-120210-60';
update genotype set geno_display_name = 'rbm15<sup>cq96/cq96</sup>' where geno_zdb_id = 'ZDB-GENO-211230-1';
update genotype set geno_display_name = 'sart3<sup>sm471/sm471</sup>' where geno_zdb_id = 'ZDB-GENO-140130-7';
update genotype set geno_display_name = 'slc24a5<sup>b1/b1</sup>' where geno_zdb_id = 'ZDB-GENO-210415-9';
update genotype set geno_display_name = 'w250Tg/+' where geno_zdb_id = 'ZDB-GENO-210429-23';
update genotype set geno_display_name = 'slc24a5<sup>b1/b1</sup>' where geno_zdb_id = 'ZDB-GENO-970801-17';

-- add constraint
update genotype set geno_display_name = '_' where geno_display_name = '';
alter table genotype add CONSTRAINT no_blank_genotype_names check (geno_display_name <> '');



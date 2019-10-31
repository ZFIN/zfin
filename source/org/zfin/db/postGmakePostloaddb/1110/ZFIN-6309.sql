--liquibase formatted sql
--changeset xshao:ZFIN-6309

update fish set fish_name = 'alas1<sup>sm350/sm350</sup>' where fish_zdb_id = 'ZDB-FISH-150901-25941';
update genotype set geno_display_name = 'alas1<sup>sm350/sm350</sup>' where geno_zdb_id = 'ZDB-GENO-140124-30';

update fish set fish_name = 'myadml2l<sup>dmh4/+</sup>' where fish_zdb_id = 'ZDB-FISH-180503-12';
update genotype set geno_display_name = 'myadml2l<sup>dmh4/+</sup>' where geno_zdb_id = 'ZDB-GENO-180503-12';


update fish set fish_name = 'cct5<sup>tf212</sup>' where fish_zdb_id = 'ZDB-FISH-150901-22477';
update genotype set geno_display_name = 'cct5<sup>tf212</sup>' where geno_zdb_id = 'ZDB-GENO-980410-99';




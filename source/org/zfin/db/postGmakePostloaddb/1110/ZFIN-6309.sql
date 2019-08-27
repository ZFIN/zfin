--liquibase formatted sql
--changeset xshao:ZFIN-6309

update fish set fish_name = 'alas1<sup>sm350/sm350</sup>' where fish_zdb_id = 'ZDB-FISH-150901-25941';

update genotype set geno_display_name = 'alas1<sup>sm350/sm350</sup>' where geno_zdb_id = 'ZDB-GENO-140124-30';



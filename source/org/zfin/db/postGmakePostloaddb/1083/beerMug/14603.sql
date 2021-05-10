--liquibase formatted sql
--changeset xiang:14603.sql

update genotype set geno_display_name = 'slc45a2<sup>b4/b4</sup>; roy<sup>a9/a9</sup>; mitfa<sup>w2/w2</sup>' where geno_zdb_id = 'ZDB-GENO-160927-1';



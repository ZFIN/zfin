--liquibase formatted sql
--changeset cmpich:ZFIN-9555.sql

update genotype set geno_is_extinct = false where geno_zdb_id = 'ZDB-GENO-250214-13';
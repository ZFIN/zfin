--liquibase formatted sql
--changeset rtaylor:ZFIN-10147.sql

-- ZDB-GENE-041229-3
select convert_gene_to_ncrna('ZDB-GENE-041229-3');
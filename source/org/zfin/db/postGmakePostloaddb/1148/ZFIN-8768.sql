--liquibase formatted sql
--changeset rtaylor:ZFIN-8768.sql

select convert_gene_to_ncrna('ZDB-GENE-030616-59');
select convert_gene_to_ncrna('ZDB-GENE-060526-243');

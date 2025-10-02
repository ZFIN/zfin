--liquibase formatted sql
--changeset rtaylor:ZFIN-9951

select convert_gene_to_ncrna('ZDB-GENE-120709-33');


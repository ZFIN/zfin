--liquibase formatted sql
--changeset rtaylor:ZFIN-8789.sql

select convert_gene_to_ncrna('ZDB-GENE-070912-717');

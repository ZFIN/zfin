--liquibase formatted sql
--changeset rtaylor:ZFIN-8809.sql

select convert_gene_to_type('ZDB-LINCRNAG-050208-65','GENE');

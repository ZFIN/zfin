--liquibase formatted sql
--changeset cmpich:ZFIN-8746.sql

select convert_gene_to_ncrna('ZDB-GENE-030131-3332');
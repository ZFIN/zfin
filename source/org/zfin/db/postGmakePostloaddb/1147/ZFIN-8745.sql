--liquibase formatted sql
--changeset cmpich:ZFIN-8745.sql

select convert_gene_to_ncrna('ZDB-GENE-090313-169');
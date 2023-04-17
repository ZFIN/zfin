--liquibase formatted sql
--changeset cmpich:ZFIN-8504.sql

alter table UI.CHEBI_PHENOTYPE_DISPLAY add COLUMN cpd_exp_condition_chebi_search text;
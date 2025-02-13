--liquibase formatted sql
--changeset cmpich:ZFIN-9190.sql

Alter table ui.chebi_phenotype_display add column cpd_has_chebi_in_phenotype boolean default false;
--liquibase formatted sql
--changeset cmpich:ZFIN-9190.sql

Alter table ui.chebi_phenotype_display add column cpd_eqe_phenotype boolean default false;
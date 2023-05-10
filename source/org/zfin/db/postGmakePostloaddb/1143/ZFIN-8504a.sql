--liquibase formatted sql
--changeset cmpich:ZFIN-8504.sql

alter table UI.CHEBI_PHENOTYPE_DISPLAY
    add COLUMN cpd_is_single_misfortune boolean;
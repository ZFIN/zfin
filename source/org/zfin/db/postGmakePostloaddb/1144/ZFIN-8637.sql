--liquibase formatted sql
--changeset cmpich:ZFIN-8637.sql


alter table UI.CHEBI_PHENOTYPE_DISPLAY
    add COLUMN
        cpd_has_images boolean;

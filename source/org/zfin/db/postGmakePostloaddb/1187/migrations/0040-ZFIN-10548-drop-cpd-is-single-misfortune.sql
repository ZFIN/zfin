--liquibase formatted sql
--changeset rtaylor:ZFIN-10548-drop-cpd-is-single-misfortune

-- Orphan column from the ZFIN-8504 single-misfortune filter (release 1143), which never reached main.
-- Not mapped on ChebiPhenotypeDisplay and always NULL; the multi-chebi-condition flag covers the same ground.
-- Databases built from main never had it, hence "if exists".
alter table ui.chebi_phenotype_display drop column if exists cpd_is_single_misfortune;

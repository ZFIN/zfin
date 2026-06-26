--liquibase formatted sql
--changeset rtaylor:ZFIN-10350-drop-phenotype-mart-bkup-tables

-- The incremental refreshPhenotypeMart.sql no longer writes the *_bkup backup
-- tables (the old rebuild-and-swap did, as a pre-swap snapshot). They are
-- unused and reconstructable from the live tables; drop them.
DROP TABLE IF EXISTS phenotype_observation_generated_bkup;
DROP TABLE IF EXISTS phenotype_source_generated_bkup;
DROP TABLE IF EXISTS phenotype_generated_curated_mapping_bkup;

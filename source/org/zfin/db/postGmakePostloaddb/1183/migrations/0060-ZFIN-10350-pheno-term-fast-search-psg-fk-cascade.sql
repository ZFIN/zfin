--liquibase formatted sql
--changeset rtaylor:ZFIN-10350-pheno-term-fast-search-psg-fk-cascade

-- Convert the live pheno_term_fast_search -> phenotype_observation_generated
-- foreign key to ON DELETE CASCADE.

ALTER TABLE pheno_term_fast_search DROP CONSTRAINT IF EXISTS pheno_term_fast_search_psg_fk;
ALTER TABLE pheno_term_fast_search ADD CONSTRAINT pheno_term_fast_search_psg_fk
    FOREIGN KEY (ptfs_psg_id) REFERENCES phenotype_observation_generated (psg_id)
    ON DELETE CASCADE;

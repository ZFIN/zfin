--liquibase formatted sql
--changeset rtaylor:ZFIN-10350-pheno-term-fast-search-psg-fk-cascade

-- Convert the live pheno_term_fast_search -> phenotype_observation_generated
-- foreign key to ON DELETE CASCADE.
--
-- phenotype_observation_generated is now maintained by an incremental apply
-- (regen_phenotype_mart) that DELETEs gone psg rows in place. pheno_term_fast_search
-- is still rebuilt wholesale, later in the same regen run, and carries a hard FK
-- on ptfs_psg_id. Between the mart apply and that rebuild the prior run's
-- fast-search rows still reference psg rows the apply wants to delete, so the
-- non-cascade FK blocks the delete:
--
--   ERROR: update or delete on table "phenotype_observation_generated" violates
--   foreign key constraint "pheno_term_fast_search_psg_fk" on table
--   "pheno_term_fast_search"
--
-- pheno_term_regen.sql now re-adds this FK with ON DELETE CASCADE on every
-- rebuild, but that runs after the failing apply step, so the constraint
-- already on the table must be converted here, once, at deploy. The orphaned
-- search rows a cascade drops are rebuilt by the next pheno_term_regen run, so
-- cascading is safe.

ALTER TABLE pheno_term_fast_search DROP CONSTRAINT IF EXISTS pheno_term_fast_search_psg_fk;
ALTER TABLE pheno_term_fast_search ADD CONSTRAINT pheno_term_fast_search_psg_fk
    FOREIGN KEY (ptfs_psg_id) REFERENCES phenotype_observation_generated (psg_id)
    ON DELETE CASCADE;

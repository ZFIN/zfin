--liquibase formatted sql
--changeset cmpich:add-ancestor-term-ids

-- Add text[] columns for pre-computed ancestor term IDs to eliminate
-- all_term_contains joins at query time (deadlock prevention).

ALTER TABLE ui.chebi_phenotype_display ADD COLUMN cpd_ancestor_term_ids text[];
ALTER TABLE ui.term_phenotype_display ADD COLUMN tpd_ancestor_term_ids text[];
ALTER TABLE ui.zebrafish_models_display ADD COLUMN zmd_ancestor_term_ids text[];
ALTER TABLE ui.zebrafish_models_chebi_association ADD COLUMN omca_ancestor_term_ids text[];
ALTER TABLE ui.omim_phenotype_display ADD COLUMN opd_ancestor_term_ids text[];

CREATE INDEX cpd_ancestor_term_ids_gin ON ui.chebi_phenotype_display USING GIN (cpd_ancestor_term_ids);
CREATE INDEX tpd_ancestor_term_ids_gin ON ui.term_phenotype_display USING GIN (tpd_ancestor_term_ids);
CREATE INDEX zmd_ancestor_term_ids_gin ON ui.zebrafish_models_display USING GIN (zmd_ancestor_term_ids);
CREATE INDEX omca_ancestor_term_ids_gin ON ui.zebrafish_models_chebi_association USING GIN (omca_ancestor_term_ids);
CREATE INDEX opd_ancestor_term_ids_gin ON ui.omim_phenotype_display USING GIN (opd_ancestor_term_ids);

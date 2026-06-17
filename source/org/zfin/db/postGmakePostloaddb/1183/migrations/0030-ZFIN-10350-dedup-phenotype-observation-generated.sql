--liquibase formatted sql
--changeset rtaylor:ZFIN-10350-dedup-phenotype-observation-generated

-- One-time cleanup of legacy duplicate phenotype_observation_generated rows
-- left by the pre-dedup populate (the three pog inserts in populateTables.sql
-- now SELECT DISTINCT). Runs at deploy, before any job, so the incremental
-- refreshPhenotypeMart.sql starts from a unique-natural-key live table -- no
-- manual deduped rebuild needed at cutover.
--
-- Keeps the lowest psg_id per identity group; duplicate rows are identical on
-- every column, so the choice is arbitrary. psg is built with DISTINCT and the
-- pgcm inserts use DISTINCT, so neither needs dedup.
--
-- pheno_term_fast_search.ptfs_psg_id has a live foreign key
-- (pheno_term_fast_search_psg_fk -> phenotype_observation_generated.psg_id,
-- added by pheno_term_regen.sql), so the duplicates cannot simply be deleted.
-- We clear the referencing fast-search rows for the duplicate psg_ids first.
-- The deploy runs a one-time pheno_term_regen afterward (see deployment
-- instructions) to fully rebuild pheno_term_fast_search, so dropping these rows
-- here is safe. genotype_figure_fast_search has only an index on gffs_psg_id,
-- not a foreign key, so it needs no pre-delete handling.

-- Map each duplicate psg_id to the surviving psg_id for its identity group.
CREATE TEMP TABLE pog_dedup_map AS
  SELECT psg_id,
         min(psg_id) OVER (
           PARTITION BY psg_pg_id, psg_mrkr_zdb_id, psg_mrkr_relation,
                        psg_e1a_zdb_id, psg_e1_relation_name, psg_e1b_zdb_id,
                        psg_e2a_zdb_id, psg_e2_relation_name, psg_e2b_zdb_id,
                        psg_tag, psg_quality_zdb_id, psg_pre_eap_phenotype) AS keep_id
    FROM phenotype_observation_generated;
DELETE FROM pog_dedup_map WHERE psg_id = keep_id;

-- Remove referencing fast-search rows for the duplicates (rebuilt by the
-- one-time pheno_term_regen run during deployment).
DELETE FROM pheno_term_fast_search
 WHERE ptfs_psg_id IN (SELECT psg_id FROM pog_dedup_map);

-- The duplicates are now unreferenced and can be removed.
DELETE FROM phenotype_observation_generated
 WHERE psg_id IN (SELECT psg_id FROM pog_dedup_map);

DROP TABLE pog_dedup_map;

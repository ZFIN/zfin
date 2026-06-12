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
-- pgcm inserts use DISTINCT, so neither needs dedup. Nothing has a DB foreign
-- key to phenotype_observation_generated.psg_id, so deleting duplicate rows is
-- unconstrained; genotype_figure_fast_search / pheno_term_fast_search are
-- rebuilt by their own regen jobs afterward.
DELETE FROM phenotype_observation_generated
 WHERE psg_id IN (
   SELECT psg_id FROM (
     SELECT psg_id,
            row_number() OVER (
              PARTITION BY psg_pg_id, psg_mrkr_zdb_id, psg_mrkr_relation,
                           psg_e1a_zdb_id, psg_e1_relation_name, psg_e1b_zdb_id,
                           psg_e2a_zdb_id, psg_e2_relation_name, psg_e2b_zdb_id,
                           psg_tag, psg_quality_zdb_id, psg_pre_eap_phenotype
              ORDER BY psg_id) AS rn
       FROM phenotype_observation_generated ) d
    WHERE rn > 1 );

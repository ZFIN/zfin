--liquibase formatted sql
--changeset rtaylor:ZFIN-10350-phenotype-mart-natural-key-unique-indexes

-- Enforce the natural-key uniqueness the incremental refreshPhenotypeMart.sql
-- relies on, so duplicates can never silently recur (a regressed populate that
-- dropped its DISTINCT would now fail loudly at insert time instead). Must run
-- after the pog dedup changeset above.
--
-- NULLS NOT DISTINCT (PG 15+) is required because the pog identity columns are
-- nullable (e1b/e2a/e2b/quality/marker) -- a default unique index treats NULLs
-- as distinct and would let null-bearing "duplicates" through. coalesce-style
-- null handling in the job already treats those as equal; this matches it.

CREATE UNIQUE INDEX phenotype_source_generated_natkey_uq
    ON phenotype_source_generated
       (pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id)
    NULLS NOT DISTINCT;

CREATE UNIQUE INDEX phenotype_observation_generated_natkey_uq
    ON phenotype_observation_generated
       (psg_pg_id, psg_mrkr_zdb_id, psg_mrkr_relation,
        psg_e1a_zdb_id, psg_e1_relation_name, psg_e1b_zdb_id,
        psg_e2a_zdb_id, psg_e2_relation_name, psg_e2b_zdb_id,
        psg_tag, psg_quality_zdb_id, psg_pre_eap_phenotype)
    NULLS NOT DISTINCT;

CREATE UNIQUE INDEX phenotype_generated_curated_mapping_natkey_uq
    ON phenotype_generated_curated_mapping
       (pgcm_pg_id, pgcm_source_id, pgcm_id_type)
    NULLS NOT DISTINCT;

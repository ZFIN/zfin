-- Match key for a phenotype_observation_generated row: the resolved psg_pg_id
-- plus the 11 identity columns, null-normalised with a delimiter that cannot
-- occur in a zdb_id. Used by regen_phenotype_mart() to match desired (temp,
-- resolved onto live pg_id) against live rows; centralising it guarantees both
-- sides compute the key identically. Excludes the denormalised name columns so
-- a term/marker rename is an in-place update, not a new identity.
-- SQL + IMMUTABLE so the planner inlines it (no per-row call overhead).
CREATE OR REPLACE FUNCTION phenotype_mart_pog_key(
    p_psg_pg_id          bigint,
    p_mrkr_zdb_id        text,
    p_mrkr_relation      text,
    p_e1a_zdb_id         text,
    p_e1_relation_name   text,
    p_e1b_zdb_id         text,
    p_e2a_zdb_id         text,
    p_e2_relation_name   text,
    p_e2b_zdb_id         text,
    p_tag                text,
    p_quality_zdb_id     text,
    p_pre_eap_phenotype  boolean
) RETURNS text AS $$
    SELECT concat_ws('|',
        p_psg_pg_id::text,
        coalesce(p_mrkr_zdb_id, ''),
        coalesce(p_mrkr_relation, ''),
        coalesce(p_e1a_zdb_id, ''),
        coalesce(p_e1_relation_name, ''),
        coalesce(p_e1b_zdb_id, ''),
        coalesce(p_e2a_zdb_id, ''),
        coalesce(p_e2_relation_name, ''),
        coalesce(p_e2b_zdb_id, ''),
        coalesce(p_tag, ''),
        coalesce(p_quality_zdb_id, ''),
        p_pre_eap_phenotype::text);
$$ LANGUAGE sql IMMUTABLE;

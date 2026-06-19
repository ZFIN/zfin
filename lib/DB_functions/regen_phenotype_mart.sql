-- Incrementally apply the freshly-recomputed *_temp tables onto the live
-- phenotype mart tables, matching on NATURAL keys so existing rows keep their
-- bigint pg_id / psg_id across runs: insert new rows, update changed
-- denormalised name columns in place, delete gone rows. No table swap and no
-- AccessExclusiveLock -- only row locks on the rows that change, which don't
-- block the read-only application (MVCC). Called once per run by
-- refreshPhenotypeMart.sql; runs in that statement's single transaction.
--
--   psg  -- 4-col natural key; no non-key columns => insert / delete only.
--   pog  -- match on (resolved psg_pg_id + 11 identity cols) via
--           phenotype_mart_pog_key(); the 7 denormalised name columns are the
--           UPDATE payload.
--   pgcm -- (psg natural key + source + type); insert / delete only.
--
-- The *_temp surrogate ids differ from live's, so temp rows are resolved onto
-- the live pg_id via the psg natural key before pog/pgcm are matched. That
-- resolution is done once, into the _psg_resolved (temp_pg_id -> live_pg_id)
-- mapping the orchestrator builds after the psg insert; pog and pgcm then look
-- up live_pg_id by a single join on it instead of repeating the natural-key
-- join. Assumes the live tables hold unique natural keys (enforced by the
-- *_natkey_uq indexes and the populate's DISTINCT; legacy dups are removed by a
-- deploy migration).
--
-- The per-table work is split into helper functions below for readability;
-- regen_phenotype_mart() at the bottom is the orchestrator and owns the single
-- transaction, the FK-safe ordering (parent insert -> child delete/update/insert
-- -> parent delete) and the summary NOTICE. The helpers must only ever be called
-- from that orchestrator, in that order, within one transaction -- they are not
-- independently safe to run (psg insert must precede the _psg_resolved build,
-- which the pog/pgcm helpers consume; psg delete must follow the child deletes).
-- The _psg_resolved / _pog_* / _pgcm_* working tables are ON COMMIT DROP, so
-- they live for the orchestrator's transaction.

-- PSG insert: brand-new natural keys (pg_id from the live default sequence).
-- Must run first so pog/pgcm can resolve their temp rows onto live pg_ids.
CREATE OR REPLACE FUNCTION regen_phenotype_mart_apply_psg_insert() RETURNS int AS $$
DECLARE n int;
BEGIN
    INSERT INTO phenotype_source_generated (pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id)
    SELECT t.pg_genox_zdb_id, t.pg_fig_zdb_id, t.pg_start_stg_zdb_id, t.pg_end_stg_zdb_id
      FROM phenotype_source_generated_temp t
     WHERE NOT EXISTS (
       SELECT 1 FROM phenotype_source_generated l
        WHERE coalesce(l.pg_genox_zdb_id,'')    = coalesce(t.pg_genox_zdb_id,'')
          AND coalesce(l.pg_fig_zdb_id,'')      = coalesce(t.pg_fig_zdb_id,'')
          AND coalesce(l.pg_start_stg_zdb_id,'') = coalesce(t.pg_start_stg_zdb_id,'')
          AND coalesce(l.pg_end_stg_zdb_id,'')   = coalesce(t.pg_end_stg_zdb_id,''));
    GET DIAGNOSTICS n = ROW_COUNT;
    RETURN n;
END;
$$ LANGUAGE plpgsql;

-- PSG delete: gone natural keys. Must run last, after pog/pgcm children for
-- those psg rows have been deleted (FK phenotype_warehouse_foreign_key).
CREATE OR REPLACE FUNCTION regen_phenotype_mart_apply_psg_delete() RETURNS int AS $$
DECLARE n int;
BEGIN
    DELETE FROM phenotype_source_generated l
     WHERE NOT EXISTS (
       SELECT 1 FROM phenotype_source_generated_temp t
        WHERE coalesce(t.pg_genox_zdb_id,'')    = coalesce(l.pg_genox_zdb_id,'')
          AND coalesce(t.pg_fig_zdb_id,'')      = coalesce(l.pg_fig_zdb_id,'')
          AND coalesce(t.pg_start_stg_zdb_id,'') = coalesce(l.pg_start_stg_zdb_id,'')
          AND coalesce(t.pg_end_stg_zdb_id,'')   = coalesce(l.pg_end_stg_zdb_id,''));
    GET DIAGNOSTICS n = ROW_COUNT;
    RETURN n;
END;
$$ LANGUAGE plpgsql;

-- POG apply: resolve temp rows onto live psg ids (via _psg_resolved, keyed for
-- hashable matching), then delete-gone / update-changed-names / insert-new.
-- Match key excludes the 7 denormalised name columns, so a term/marker rename is
-- an in-place update. Consumes the orchestrator's _psg_resolved mapping.
CREATE OR REPLACE FUNCTION regen_phenotype_mart_apply_pog(OUT ins int, OUT upd int, OUT del int) AS $$
BEGIN
    CREATE TEMP TABLE _pog_desired ON COMMIT DROP AS
    SELECT r.live_pg_id AS psg_pg_id,
           t.psg_mrkr_zdb_id, t.psg_mrkr_abbrev, t.psg_mrkr_relation,
           t.psg_e1a_zdb_id, t.psg_e1a_name, t.psg_e1_relation_name,
           t.psg_e1b_zdb_id, t.psg_e1b_name,
           t.psg_e2a_zdb_id, t.psg_e2a_name, t.psg_e2_relation_name,
           t.psg_e2b_zdb_id, t.psg_e2b_name,
           t.psg_tag, t.psg_quality_zdb_id, t.psg_quality_name,
           t.psg_short_name, t.psg_pre_eap_phenotype,
           phenotype_mart_pog_key(r.live_pg_id, t.psg_mrkr_zdb_id, t.psg_mrkr_relation,
                 t.psg_e1a_zdb_id, t.psg_e1_relation_name, t.psg_e1b_zdb_id,
                 t.psg_e2a_zdb_id, t.psg_e2_relation_name, t.psg_e2b_zdb_id,
                 t.psg_tag, t.psg_quality_zdb_id, t.psg_pre_eap_phenotype) AS k
      FROM phenotype_observation_generated_temp t
      JOIN _psg_resolved r ON r.temp_pg_id = t.psg_pg_id;
    CREATE INDEX ON _pog_desired (k);

    CREATE TEMP TABLE _pog_live ON COMMIT DROP AS
    SELECT psg_id,
           phenotype_mart_pog_key(psg_pg_id, psg_mrkr_zdb_id, psg_mrkr_relation,
                 psg_e1a_zdb_id, psg_e1_relation_name, psg_e1b_zdb_id,
                 psg_e2a_zdb_id, psg_e2_relation_name, psg_e2b_zdb_id,
                 psg_tag, psg_quality_zdb_id, psg_pre_eap_phenotype) AS k
      FROM phenotype_observation_generated;
    CREATE INDEX ON _pog_live (k);

    DELETE FROM phenotype_observation_generated l
     USING _pog_live lk
     WHERE l.psg_id = lk.psg_id
       AND NOT EXISTS (SELECT 1 FROM _pog_desired d WHERE d.k = lk.k);
    GET DIAGNOSTICS del = ROW_COUNT;

    UPDATE phenotype_observation_generated l
       SET psg_mrkr_abbrev  = d.psg_mrkr_abbrev,
           psg_e1a_name     = d.psg_e1a_name,
           psg_e1b_name     = d.psg_e1b_name,
           psg_e2a_name     = d.psg_e2a_name,
           psg_e2b_name     = d.psg_e2b_name,
           psg_quality_name = d.psg_quality_name,
           psg_short_name   = d.psg_short_name
      FROM _pog_live lk
      JOIN _pog_desired d ON d.k = lk.k
     WHERE l.psg_id = lk.psg_id
       AND ( l.psg_mrkr_abbrev  IS DISTINCT FROM d.psg_mrkr_abbrev
          OR l.psg_e1a_name     IS DISTINCT FROM d.psg_e1a_name
          OR l.psg_e1b_name     IS DISTINCT FROM d.psg_e1b_name
          OR l.psg_e2a_name     IS DISTINCT FROM d.psg_e2a_name
          OR l.psg_e2b_name     IS DISTINCT FROM d.psg_e2b_name
          OR l.psg_quality_name IS DISTINCT FROM d.psg_quality_name
          OR l.psg_short_name   IS DISTINCT FROM d.psg_short_name );
    GET DIAGNOSTICS upd = ROW_COUNT;

    INSERT INTO phenotype_observation_generated
       (psg_pg_id, psg_mrkr_zdb_id, psg_mrkr_abbrev, psg_mrkr_relation,
        psg_e1a_zdb_id, psg_e1a_name, psg_e1_relation_name, psg_e1b_zdb_id, psg_e1b_name,
        psg_e2a_zdb_id, psg_e2a_name, psg_e2_relation_name, psg_e2b_zdb_id, psg_e2b_name,
        psg_tag, psg_quality_zdb_id, psg_quality_name, psg_short_name, psg_pre_eap_phenotype)
    SELECT d.psg_pg_id, d.psg_mrkr_zdb_id, d.psg_mrkr_abbrev, d.psg_mrkr_relation,
           d.psg_e1a_zdb_id, d.psg_e1a_name, d.psg_e1_relation_name, d.psg_e1b_zdb_id, d.psg_e1b_name,
           d.psg_e2a_zdb_id, d.psg_e2a_name, d.psg_e2_relation_name, d.psg_e2b_zdb_id, d.psg_e2b_name,
           d.psg_tag, d.psg_quality_zdb_id, d.psg_quality_name, d.psg_short_name, d.psg_pre_eap_phenotype
      FROM _pog_desired d
     WHERE NOT EXISTS (SELECT 1 FROM _pog_live lk WHERE lk.k = d.k);
    GET DIAGNOSTICS ins = ROW_COUNT;
END;
$$ LANGUAGE plpgsql;

-- PGCM apply: resolve onto live psg ids (via _psg_resolved), then delete-gone /
-- insert-new. Every column is part of the key, so there is no update payload.
-- Consumes the orchestrator's _psg_resolved mapping.
CREATE OR REPLACE FUNCTION regen_phenotype_mart_apply_pgcm(OUT ins int, OUT del int) AS $$
BEGIN
    CREATE TEMP TABLE _pgcm_desired ON COMMIT DROP AS
    SELECT r.live_pg_id AS pgcm_pg_id, t.pgcm_source_id, t.pgcm_id_type,
           concat_ws('|', r.live_pg_id::text, coalesce(t.pgcm_source_id,''), coalesce(t.pgcm_id_type,'')) AS k
      FROM phenotype_generated_curated_mapping_temp t
      JOIN _psg_resolved r ON r.temp_pg_id = t.pgcm_pg_id;
    CREATE INDEX ON _pgcm_desired (k);

    CREATE TEMP TABLE _pgcm_live ON COMMIT DROP AS
    SELECT ctid AS row_ctid,
           concat_ws('|', pgcm_pg_id::text, coalesce(pgcm_source_id,''), coalesce(pgcm_id_type,'')) AS k
      FROM phenotype_generated_curated_mapping;
    CREATE INDEX ON _pgcm_live (k);

    DELETE FROM phenotype_generated_curated_mapping l
     USING _pgcm_live lk
     WHERE l.ctid = lk.row_ctid
       AND NOT EXISTS (SELECT 1 FROM _pgcm_desired d WHERE d.k = lk.k);
    GET DIAGNOSTICS del = ROW_COUNT;

    INSERT INTO phenotype_generated_curated_mapping (pgcm_pg_id, pgcm_source_id, pgcm_id_type)
    SELECT d.pgcm_pg_id, d.pgcm_source_id, d.pgcm_id_type
      FROM _pgcm_desired d
     WHERE NOT EXISTS (SELECT 1 FROM _pgcm_live lk WHERE lk.k = d.k);
    GET DIAGNOSTICS ins = ROW_COUNT;
END;
$$ LANGUAGE plpgsql;

-- Orchestrator: owns the transaction, the FK-safe ordering, and the summary
-- NOTICE (which regenPhenotypeMart.sh tees to the Jenkins console).
CREATE OR REPLACE FUNCTION regen_phenotype_mart() RETURNS void AS $$
DECLARE
    psg_ins int; psg_del int;
    pog_ins int; pog_upd int; pog_del int;
    pgcm_ins int; pgcm_del int;
BEGIN
    UPDATE zdb_flag SET (zflag_is_on, zflag_last_modified) = ('t', now())
     WHERE zflag_name = 'regen_phenotypemart';

    -- Parent inserts first, then children, then parent deletes last.
    psg_ins := regen_phenotype_mart_apply_psg_insert();

    -- Resolve every temp psg id to its live pg_id, once. Built after the insert
    -- so live holds all temp natural keys -> the join is total (no pog/pgcm rows
    -- dropped) and 1:1 (both sides have unique natural keys). pog/pgcm consume it.
    CREATE TEMP TABLE _psg_resolved ON COMMIT DROP AS
    SELECT tpsg.pg_id AS temp_pg_id, lpsg.pg_id AS live_pg_id
      FROM phenotype_source_generated_temp tpsg
      JOIN phenotype_source_generated lpsg
        ON coalesce(lpsg.pg_genox_zdb_id,'')    = coalesce(tpsg.pg_genox_zdb_id,'')
       AND coalesce(lpsg.pg_fig_zdb_id,'')      = coalesce(tpsg.pg_fig_zdb_id,'')
       AND coalesce(lpsg.pg_start_stg_zdb_id,'') = coalesce(tpsg.pg_start_stg_zdb_id,'')
       AND coalesce(lpsg.pg_end_stg_zdb_id,'')   = coalesce(tpsg.pg_end_stg_zdb_id,'');
    CREATE INDEX ON _psg_resolved (temp_pg_id);

    SELECT ins, upd, del INTO pog_ins, pog_upd, pog_del FROM regen_phenotype_mart_apply_pog();
    SELECT ins, del INTO pgcm_ins, pgcm_del FROM regen_phenotype_mart_apply_pgcm();
    psg_del := regen_phenotype_mart_apply_psg_delete();

    UPDATE zdb_flag SET (zflag_is_on, zflag_last_modified) = ('f', now())
     WHERE zflag_name = 'regen_phenotypemart';

    UPDATE warehouse_run_tracking SET wrt_last_loaded_date = now()
     WHERE wrt_mart_name = 'phenotype mart';

    RAISE NOTICE 'phenotype mart applied: psg +% -%, pog +% ~% -%, pgcm +% -%',
        psg_ins, psg_del, pog_ins, pog_upd, pog_del, pgcm_ins, pgcm_del;
END;
$$ LANGUAGE plpgsql;

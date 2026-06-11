-- Incrementally apply the freshly-recomputed *_temp tables onto the live
-- phenotype mart tables: insert new rows, update changed denormalized columns,
-- delete gone rows -- matching on NATURAL KEYS so existing rows keep their
-- (bigint) pg_id / psg_id across runs.
--
-- There is NO table swap and NO AccessExclusiveLock: the only locks taken are
-- row locks on the rows that actually change (the nightly delta is small), and
-- those do not block the read-only application (MVCC). Everything runs in one
-- transaction so readers see either the whole old state or the whole new state.
-- runPhenotypeMart.sh does not wrap this file in begin/commit (the transaction
-- boundary below is load-bearing).
--
-- Matching:
--   psg  -- natural key (pg_genox/fig/start/end). No non-key columns, so
--           insert-new / delete-gone only.
--   pog  -- (psg natural key + the 11 identity columns); the 7 denormalized
--           name columns (psg_*_name, psg_mrkr_abbrev, psg_short_name) are
--           UPDATEd in place when matched, so a term/marker rename stays an
--           in-place update rather than a new identity.
--   pgcm -- (psg natural key + source_id + id_type). Insert-new / delete-gone.
-- The *_temp surrogate ids differ from live's, so temp rows are resolved onto
-- the live pg_id via the psg natural key before pog/pgcm are matched.
--
-- ASSUMES the live tables already hold unique natural keys (psg built with
-- DISTINCT; pog deduped -- see populateTables.sql). Cut over from the old
-- rebuild-and-swap only after one clean deduped rebuild.

BEGIN;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('t',now())
 where zflag_name = 'regen_phenotypemart';

-- =========================================================================
-- PSG -- insert brand-new natural keys (pg_id assigned by the live default
-- sequence). Gone psg are deleted LAST, after their pog/pgcm children.
-- =========================================================================
INSERT INTO phenotype_source_generated (pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id)
SELECT t.pg_genox_zdb_id, t.pg_fig_zdb_id, t.pg_start_stg_zdb_id, t.pg_end_stg_zdb_id
  FROM phenotype_source_generated_temp t
 WHERE NOT EXISTS (
   SELECT 1 FROM phenotype_source_generated l
    WHERE coalesce(l.pg_genox_zdb_id,'')    = coalesce(t.pg_genox_zdb_id,'')
      AND coalesce(l.pg_fig_zdb_id,'')      = coalesce(t.pg_fig_zdb_id,'')
      AND coalesce(l.pg_start_stg_zdb_id,'') = coalesce(t.pg_start_stg_zdb_id,'')
      AND coalesce(l.pg_end_stg_zdb_id,'')   = coalesce(t.pg_end_stg_zdb_id,''));

-- =========================================================================
-- POG -- resolve temp rows onto live psg ids, then delete / update / insert.
-- A single text match key over the resolved psg_pg_id + the 11 identity
-- columns keeps the joins hashable and null-safe (coalesce, not IS DISTINCT).
-- =========================================================================
CREATE TEMP TABLE _pog_desired ON COMMIT DROP AS
SELECT lpsg.pg_id AS psg_pg_id,
       t.psg_mrkr_zdb_id, t.psg_mrkr_abbrev, t.psg_mrkr_relation,
       t.psg_e1a_zdb_id, t.psg_e1a_name, t.psg_e1_relation_name,
       t.psg_e1b_zdb_id, t.psg_e1b_name,
       t.psg_e2a_zdb_id, t.psg_e2a_name, t.psg_e2_relation_name,
       t.psg_e2b_zdb_id, t.psg_e2b_name,
       t.psg_tag, t.psg_quality_zdb_id, t.psg_quality_name,
       t.psg_short_name, t.psg_pre_eap_phenotype,
       concat_ws('|', lpsg.pg_id::text, coalesce(t.psg_mrkr_zdb_id,''),
                 coalesce(t.psg_mrkr_relation,''), coalesce(t.psg_e1a_zdb_id,''),
                 coalesce(t.psg_e1_relation_name,''), coalesce(t.psg_e1b_zdb_id,''),
                 coalesce(t.psg_e2a_zdb_id,''), coalesce(t.psg_e2_relation_name,''),
                 coalesce(t.psg_e2b_zdb_id,''), coalesce(t.psg_tag,''),
                 coalesce(t.psg_quality_zdb_id,''), t.psg_pre_eap_phenotype::text) AS k
  FROM phenotype_observation_generated_temp t
  JOIN phenotype_source_generated_temp tpsg ON tpsg.pg_id = t.psg_pg_id
  JOIN phenotype_source_generated lpsg
    ON coalesce(lpsg.pg_genox_zdb_id,'')    = coalesce(tpsg.pg_genox_zdb_id,'')
   AND coalesce(lpsg.pg_fig_zdb_id,'')      = coalesce(tpsg.pg_fig_zdb_id,'')
   AND coalesce(lpsg.pg_start_stg_zdb_id,'') = coalesce(tpsg.pg_start_stg_zdb_id,'')
   AND coalesce(lpsg.pg_end_stg_zdb_id,'')   = coalesce(tpsg.pg_end_stg_zdb_id,'');
CREATE INDEX ON _pog_desired (k);

CREATE TEMP TABLE _pog_live ON COMMIT DROP AS
SELECT psg_id,
       concat_ws('|', psg_pg_id::text, coalesce(psg_mrkr_zdb_id,''),
                 coalesce(psg_mrkr_relation,''), coalesce(psg_e1a_zdb_id,''),
                 coalesce(psg_e1_relation_name,''), coalesce(psg_e1b_zdb_id,''),
                 coalesce(psg_e2a_zdb_id,''), coalesce(psg_e2_relation_name,''),
                 coalesce(psg_e2b_zdb_id,''), coalesce(psg_tag,''),
                 coalesce(psg_quality_zdb_id,''), psg_pre_eap_phenotype::text) AS k
  FROM phenotype_observation_generated;
CREATE INDEX ON _pog_live (k);

-- delete gone pog (live key not in desired)
DELETE FROM phenotype_observation_generated l
 USING _pog_live lk
 WHERE l.psg_id = lk.psg_id
   AND NOT EXISTS (SELECT 1 FROM _pog_desired d WHERE d.k = lk.k);

-- update changed denormalized columns on matched rows (psg_id preserved)
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

-- insert new pog (desired key not in live); psg_id from the live default sequence
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

-- =========================================================================
-- PGCM -- resolve onto live psg ids, then delete-gone / insert-new (no
-- non-key columns). pgcm has no surrogate id; identify live rows by ctid.
-- =========================================================================
CREATE TEMP TABLE _pgcm_desired ON COMMIT DROP AS
SELECT lpsg.pg_id AS pgcm_pg_id, t.pgcm_source_id, t.pgcm_id_type,
       concat_ws('|', lpsg.pg_id::text, coalesce(t.pgcm_source_id,''), coalesce(t.pgcm_id_type,'')) AS k
  FROM phenotype_generated_curated_mapping_temp t
  JOIN phenotype_source_generated_temp tpsg ON tpsg.pg_id = t.pgcm_pg_id
  JOIN phenotype_source_generated lpsg
    ON coalesce(lpsg.pg_genox_zdb_id,'')    = coalesce(tpsg.pg_genox_zdb_id,'')
   AND coalesce(lpsg.pg_fig_zdb_id,'')      = coalesce(tpsg.pg_fig_zdb_id,'')
   AND coalesce(lpsg.pg_start_stg_zdb_id,'') = coalesce(tpsg.pg_start_stg_zdb_id,'')
   AND coalesce(lpsg.pg_end_stg_zdb_id,'')   = coalesce(tpsg.pg_end_stg_zdb_id,'');
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

INSERT INTO phenotype_generated_curated_mapping (pgcm_pg_id, pgcm_source_id, pgcm_id_type)
SELECT d.pgcm_pg_id, d.pgcm_source_id, d.pgcm_id_type
  FROM _pgcm_desired d
 WHERE NOT EXISTS (SELECT 1 FROM _pgcm_live lk WHERE lk.k = d.k);

-- =========================================================================
-- PSG deletes -- now that gone pog/pgcm children are gone, remove gone psg
-- (natural key no longer in temp).
-- =========================================================================
DELETE FROM phenotype_source_generated l
 WHERE NOT EXISTS (
   SELECT 1 FROM phenotype_source_generated_temp t
    WHERE coalesce(t.pg_genox_zdb_id,'')    = coalesce(l.pg_genox_zdb_id,'')
      AND coalesce(t.pg_fig_zdb_id,'')      = coalesce(l.pg_fig_zdb_id,'')
      AND coalesce(t.pg_start_stg_zdb_id,'') = coalesce(l.pg_start_stg_zdb_id,'')
      AND coalesce(t.pg_end_stg_zdb_id,'')   = coalesce(l.pg_end_stg_zdb_id,''));

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('f',now())
 where zflag_name = 'regen_phenotypemart';

update warehouse_run_tracking
 set wrt_last_loaded_date = now()
 where wrt_mart_name = 'phenotype mart';

COMMIT;

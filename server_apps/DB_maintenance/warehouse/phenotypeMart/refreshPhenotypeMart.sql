-- Refresh phenotype mart tables by swapping in data from *_temp tables.
--
-- Run as TWO separate transactions to keep the AccessExclusiveLock window on
-- the live tables tiny AND bounded. runPhenotypeMart.sh deliberately does NOT
-- wrap this file in begin/commit -- the transaction boundaries below are load
-- bearing.
--
--   TRANSACTION 1 (build) -- no conflicting lock on the live tables.
--     Back up current data to *_bkup (AccessShare only) and build the
--     fully-populated / indexed / FK'd *_new replacement tables that the
--     application never reads. Committing here releases the SHARE ROW
--     EXCLUSIVE locks the FK-adds hold on the referenced parent tables
--     (fish_experiment, figure, stage, marker, term) and makes *_new durable.
--
--   TRANSACTION 2 (swap) -- the only AccessExclusiveLock window on the live
--     tables. Metadata-only renames (live -> *_old_<ts>, *_new -> live),
--     guarded by a short lock_timeout and a bounded retry loop. Because this
--     transaction touches ONLY the live phenotype_* tables (no *_bkup copy, no
--     FK-adds), it holds nothing during the back-off sleeps -- production reads
--     are fully unblocked between attempts, so the swap lands in the next gap
--     between long-running readers instead of starving (which is what made the
--     old single-transaction rebuild-under-lock hang ~7 min and roll back).
--
-- Old tables are renamed with a timestamp suffix and left for later cleanup by
-- regen_cleanup_renamed_tables().

-- =========================================================================
-- TRANSACTION 1 -- build the replacement tables (slow; no lock on live).
-- =========================================================================
BEGIN;

DO $$
BEGIN

-- Flag the (slow) rebuild as in progress; cleared once the *_new tables are
-- built, just before the swap.
update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('t',now())
 where zflag_name = 'regen_phenotypemart';

-- Back up current data into *_bkup tables (reads live = AccessShareLock only).
truncate phenotype_generated_curated_mapping_bkup;
truncate phenotype_source_generated_bkup;
truncate phenotype_observation_generated_bkup;

insert into phenotype_source_generated_bkup (pg_id,
       	     			 pg_genox_zdb_id,
				 pg_fig_zdb_id,
				 pg_start_stg_zdb_id,
				 pg_end_stg_zdb_id)
select pg_id, pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id
  from phenotype_source_generated;

insert into phenotype_observation_generated_bkup (psg_id,
       	     				   psg_pg_id,
       	     				   psg_mrkr_zdb_id,
					   psg_mrkr_abbrev,
					   psg_mrkr_relation,
					   psg_e1a_zdb_id,
					   psg_e1a_name,
					   psg_e1_relation_name,
					   psg_e1b_zdb_id,
					   psg_e1b_name,
					   psg_e2a_zdb_id,
					   psg_e2a_name,
					   psg_e2_relation_name,
					   psg_e2b_zdb_id,
					   psg_e2b_name,
					   psg_tag,
					   psg_quality_zdb_id,
					   psg_quality_name,
					   psg_short_name,
					   psg_pre_eap_phenotype)
select psg_id, psg_pg_id,psg_mrkr_zdb_id, psg_mrkr_abbrev,psg_mrkr_relation,psg_e1a_zdb_id,
	psg_e1a_name,psg_e1_relation_name, psg_e1b_zdb_id, psg_e1b_name,
	psg_e2a_zdb_id,psg_e2a_name, psg_e2_relation_name, psg_e2b_zdb_id,
	psg_e2b_name,psg_tag,psg_quality_zdb_id, psg_quality_name, psg_short_name, psg_pre_eap_phenotype
  from phenotype_observation_generated;

insert into phenotype_generated_curated_mapping_bkup (pgcm_pg_id, pgcm_source_id, pgcm_id_type)
 select pgcm_pg_id, pgcm_source_id, pgcm_id_type from phenotype_generated_curated_mapping;

-- Drop any *_new tables left behind by a previously failed/interrupted run.
-- Drop the child (FK) table before its parent.
DROP TABLE IF EXISTS phenotype_observation_generated_new;
DROP TABLE IF EXISTS phenotype_source_generated_new;
DROP TABLE IF EXISTS phenotype_generated_curated_mapping_new;

-- -------------------------------------------------------------------------
-- Build phenotype_source_generated_new (parent table)
-- -------------------------------------------------------------------------
CREATE TABLE phenotype_source_generated_new (LIKE phenotype_source_generated_temp INCLUDING ALL);
ALTER TABLE phenotype_source_generated_new ADD CONSTRAINT phenotype_source_generated_new_pkey PRIMARY KEY (pg_id);
-- Detach the sequence so cleanup's DROP of the renamed _old_ table can't
-- cascade-drop it. OWNED BY NONE avoids PG's role-ownership-must-match
-- check that re-linking OWNED BY <col> would impose.
IF pg_get_serial_sequence('phenotype_source_generated_new', 'pg_id') IS NOT NULL THEN
    EXECUTE 'ALTER SEQUENCE ' || pg_get_serial_sequence('phenotype_source_generated_new', 'pg_id')
        || ' OWNED BY NONE';
END IF;
CREATE INDEX phenotype_source_generated_new_genox ON phenotype_source_generated_new (pg_genox_zdb_id);
CREATE INDEX phenotype_source_generated_new_fig ON phenotype_source_generated_new (pg_fig_zdb_id);
CREATE INDEX phenotype_source_generated_new_start ON phenotype_source_generated_new (pg_start_stg_zdb_id);
CREATE INDEX phenotype_source_generated_new_end ON phenotype_source_generated_new (pg_end_stg_zdb_id);
ALTER TABLE phenotype_source_generated_new ADD CONSTRAINT phenotype_source_generated_new_fk1 FOREIGN KEY (pg_genox_zdb_id) REFERENCES fish_experiment (genox_zdb_id);
ALTER TABLE phenotype_source_generated_new ADD CONSTRAINT phenotype_source_generated_new_fk2 FOREIGN KEY (pg_fig_zdb_id) REFERENCES figure (fig_zdb_id);
ALTER TABLE phenotype_source_generated_new ADD CONSTRAINT phenotype_source_generated_new_fk3 FOREIGN KEY (pg_start_stg_zdb_id) REFERENCES stage (stg_zdb_id);
ALTER TABLE phenotype_source_generated_new ADD CONSTRAINT phenotype_source_generated_new_fk4 FOREIGN KEY (pg_end_stg_zdb_id) REFERENCES stage (stg_zdb_id);

insert into phenotype_source_generated_new (pg_id,
       	     			 pg_genox_zdb_id,
				 pg_fig_zdb_id,
				 pg_start_stg_zdb_id,
				 pg_end_stg_zdb_id)
select pg_id, pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id
  from phenotype_source_generated_temp;

-- -------------------------------------------------------------------------
-- Build phenotype_observation_generated_new (child table). Its FK points at
-- phenotype_source_generated_new so that, after the swap, it references the
-- new live parent table (FKs track by OID, surviving the rename).
-- -------------------------------------------------------------------------
CREATE TABLE phenotype_observation_generated_new (LIKE phenotype_observation_generated_temp INCLUDING ALL);
ALTER TABLE phenotype_observation_generated_new ADD CONSTRAINT phenotype_observation_generated_new_pkey PRIMARY KEY (psg_id);
-- Detach the sequence (see note above).
IF pg_get_serial_sequence('phenotype_observation_generated_new', 'psg_id') IS NOT NULL THEN
    EXECUTE 'ALTER SEQUENCE ' || pg_get_serial_sequence('phenotype_observation_generated_new', 'psg_id')
        || ' OWNED BY NONE';
END IF;
ALTER TABLE phenotype_observation_generated_new ADD CONSTRAINT pog_new_wh_fk FOREIGN KEY (psg_pg_id) REFERENCES phenotype_source_generated_new (pg_id);
ALTER TABLE phenotype_observation_generated_new ADD CONSTRAINT pog_new_mrkr_fk FOREIGN KEY (psg_mrkr_zdb_id) REFERENCES marker (mrkr_zdb_id);
ALTER TABLE phenotype_observation_generated_new ADD CONSTRAINT pog_new_e1a_fk FOREIGN KEY (psg_e1a_zdb_id) REFERENCES term (term_zdb_id);
ALTER TABLE phenotype_observation_generated_new ADD CONSTRAINT pog_new_e1b_fk FOREIGN KEY (psg_e1b_zdb_id) REFERENCES term (term_zdb_id);
ALTER TABLE phenotype_observation_generated_new ADD CONSTRAINT pog_new_e2a_fk FOREIGN KEY (psg_e2a_zdb_id) REFERENCES term (term_zdb_id);
ALTER TABLE phenotype_observation_generated_new ADD CONSTRAINT pog_new_e2b_fk FOREIGN KEY (psg_e2b_zdb_id) REFERENCES term (term_zdb_id);
ALTER TABLE phenotype_observation_generated_new ADD CONSTRAINT pog_new_q_fk FOREIGN KEY (psg_quality_zdb_id) REFERENCES term (term_zdb_id);

insert into phenotype_observation_generated_new (psg_id,
       	     				   psg_pg_id,
       	     				   psg_mrkr_zdb_id,
					   psg_mrkr_abbrev,
					   psg_mrkr_relation,
					   psg_e1a_zdb_id,
					   psg_e1a_name,
					   psg_e1_relation_name,
					   psg_e1b_zdb_id,
					   psg_e1b_name,
					   psg_e2a_zdb_id,
					   psg_e2a_name,
					   psg_e2_relation_name,
					   psg_e2b_zdb_id,
					   psg_e2b_name,
					   psg_tag,
					   psg_quality_zdb_id,
					   psg_quality_name,
					   psg_short_name,
					   psg_pre_eap_phenotype)
select psg_id, psg_pg_id,psg_mrkr_zdb_id, psg_mrkr_abbrev,psg_mrkr_relation,psg_e1a_zdb_id,
	psg_e1a_name,psg_e1_relation_name, psg_e1b_zdb_id, psg_e1b_name,
	psg_e2a_zdb_id,psg_e2a_name, psg_e2_relation_name, psg_e2b_zdb_id,
	psg_e2b_name,psg_tag,psg_quality_zdb_id, psg_quality_name, psg_short_name, psg_pre_eap_phenotype
  from phenotype_observation_generated_temp;

-- -------------------------------------------------------------------------
-- Build phenotype_generated_curated_mapping_new
-- -------------------------------------------------------------------------
CREATE TABLE phenotype_generated_curated_mapping_new (LIKE phenotype_generated_curated_mapping_temp INCLUDING ALL);

insert into phenotype_generated_curated_mapping_new (pgcm_pg_id, pgcm_source_id, pgcm_id_type)
 select pgcm_pg_id, pgcm_source_id, pgcm_id_type from phenotype_generated_curated_mapping_temp;

-- *_new tables are built and durable; clear the rebuild-in-progress flag.
update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('f',now())
 where zflag_name = 'regen_phenotypemart';

END $$;

COMMIT;

-- =========================================================================
-- TRANSACTION 2 -- fast metadata swap, lock_timeout + bounded retry.
-- The ONLY AccessExclusiveLock window on the live tables. Holds nothing on
-- the live or parent tables during back-off sleeps.
-- =========================================================================
BEGIN;

DO $$
DECLARE
    psg_old_name text;
    pog_old_name text;
    pgcm_old_name text;
    ts text := to_char(now(), 'YYMMDDHH24MI') || '_' || substring(md5(random()::text), 1, 4);
    swap_attempts constant int := 5;   -- max tries before giving up
    swap_wait     constant text := '10s';   -- per-attempt lock_timeout
    swap_backoff  constant int  := 20;   -- seconds to wait between attempts
    swap_attempt int := 0;
BEGIN
    LOOP
        swap_attempt := swap_attempt + 1;
        BEGIN
            -- Bound how long any single rename will wait for the exclusive
            -- lock; if a live reader is mid-query we fail fast rather than
            -- block (and head-of-line-block) the whole site. Transaction-local,
            -- re-applied each attempt (a sub-transaction rollback reverts it).
            PERFORM set_config('lock_timeout', swap_wait, true);

            -- 1. Rename the current live tables OUT to *_old_<ts>, freeing the
            --    canonical table / index / constraint names. Left for
            --    regen_cleanup_renamed_tables().

            -- phenotype_generated_curated_mapping (no FKs pointing to it)
            IF EXISTS (SELECT 1 FROM information_schema.tables
                       WHERE table_name = 'phenotype_generated_curated_mapping' AND table_schema = 'public') THEN
                pgcm_old_name := 'phenotype_generated_curated_mapping_old_' || ts;
                RAISE NOTICE 'Renaming phenotype_generated_curated_mapping to %', pgcm_old_name;
                EXECUTE 'ALTER TABLE phenotype_generated_curated_mapping RENAME TO ' || pgcm_old_name;
            END IF;

            -- phenotype_observation_generated (child of phenotype_source_generated)
            IF EXISTS (SELECT 1 FROM information_schema.tables
                       WHERE table_name = 'phenotype_observation_generated' AND table_schema = 'public') THEN
                pog_old_name := 'phenotype_observation_generated_old_' || ts;
                RAISE NOTICE 'Renaming phenotype_observation_generated to %', pog_old_name;
                EXECUTE 'ALTER TABLE phenotype_observation_generated RENAME TO ' || pog_old_name;

                EXECUTE 'ALTER INDEX IF EXISTS phenotype_observation_generated_pkey RENAME TO ' || pog_old_name || '_pkey';
                BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT phenotype_warehouse_foreign_key TO ' || pog_old_name || '_wh_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
                BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT marker_foreign_key TO ' || pog_old_name || '_mrkr_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
                BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT e1a_foreign_key TO ' || pog_old_name || '_e1a_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
                BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT e1b_foreign_key TO ' || pog_old_name || '_e1b_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
                BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT e2a_foreign_key TO ' || pog_old_name || '_e2a_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
                BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT e2b_foreign_key TO ' || pog_old_name || '_e2b_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
                BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT quality_foreign_key TO ' || pog_old_name || '_q_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
            END IF;

            -- phenotype_source_generated (parent)
            IF EXISTS (SELECT 1 FROM information_schema.tables
                       WHERE table_name = 'phenotype_source_generated' AND table_schema = 'public') THEN
                psg_old_name := 'phenotype_source_generated_old_' || ts;
                RAISE NOTICE 'Renaming phenotype_source_generated to %', psg_old_name;
                EXECUTE 'ALTER TABLE phenotype_source_generated RENAME TO ' || psg_old_name;

                EXECUTE 'ALTER INDEX IF EXISTS phenotype_source_generated_pkey RENAME TO ' || psg_old_name || '_pkey';
                EXECUTE 'ALTER INDEX IF EXISTS phenotype_source_generated_genox RENAME TO ' || psg_old_name || '_genox';
                EXECUTE 'ALTER INDEX IF EXISTS phenotype_source_generated_fig RENAME TO ' || psg_old_name || '_fig';
                EXECUTE 'ALTER INDEX IF EXISTS phenotype_source_generated_start RENAME TO ' || psg_old_name || '_start';
                EXECUTE 'ALTER INDEX IF EXISTS phenotype_source_generated_end RENAME TO ' || psg_old_name || '_end';
                BEGIN EXECUTE 'ALTER TABLE ' || psg_old_name || ' RENAME CONSTRAINT constraint_fk1 TO ' || psg_old_name || '_fk1'; EXCEPTION WHEN undefined_object THEN NULL; END;
                BEGIN EXECUTE 'ALTER TABLE ' || psg_old_name || ' RENAME CONSTRAINT constraint_fk2 TO ' || psg_old_name || '_fk2'; EXCEPTION WHEN undefined_object THEN NULL; END;
                BEGIN EXECUTE 'ALTER TABLE ' || psg_old_name || ' RENAME CONSTRAINT constraint_fk3 TO ' || psg_old_name || '_fk3'; EXCEPTION WHEN undefined_object THEN NULL; END;
                BEGIN EXECUTE 'ALTER TABLE ' || psg_old_name || ' RENAME CONSTRAINT constraint_fk4 TO ' || psg_old_name || '_fk4'; EXCEPTION WHEN undefined_object THEN NULL; END;
            END IF;

            -- 2. Rename the *_new tables IN to the canonical live names,
            --    restoring the canonical index/constraint names so the next
            --    run's rename-out matches.
            ALTER TABLE phenotype_source_generated_new RENAME TO phenotype_source_generated;
            ALTER INDEX phenotype_source_generated_new_pkey RENAME TO phenotype_source_generated_pkey;
            ALTER INDEX phenotype_source_generated_new_genox RENAME TO phenotype_source_generated_genox;
            ALTER INDEX phenotype_source_generated_new_fig RENAME TO phenotype_source_generated_fig;
            ALTER INDEX phenotype_source_generated_new_start RENAME TO phenotype_source_generated_start;
            ALTER INDEX phenotype_source_generated_new_end RENAME TO phenotype_source_generated_end;
            ALTER TABLE phenotype_source_generated RENAME CONSTRAINT phenotype_source_generated_new_fk1 TO constraint_fk1;
            ALTER TABLE phenotype_source_generated RENAME CONSTRAINT phenotype_source_generated_new_fk2 TO constraint_fk2;
            ALTER TABLE phenotype_source_generated RENAME CONSTRAINT phenotype_source_generated_new_fk3 TO constraint_fk3;
            ALTER TABLE phenotype_source_generated RENAME CONSTRAINT phenotype_source_generated_new_fk4 TO constraint_fk4;

            ALTER TABLE phenotype_observation_generated_new RENAME TO phenotype_observation_generated;
            ALTER INDEX phenotype_observation_generated_new_pkey RENAME TO phenotype_observation_generated_pkey;
            ALTER TABLE phenotype_observation_generated RENAME CONSTRAINT pog_new_wh_fk TO phenotype_warehouse_foreign_key;
            ALTER TABLE phenotype_observation_generated RENAME CONSTRAINT pog_new_mrkr_fk TO marker_foreign_key;
            ALTER TABLE phenotype_observation_generated RENAME CONSTRAINT pog_new_e1a_fk TO e1a_foreign_key;
            ALTER TABLE phenotype_observation_generated RENAME CONSTRAINT pog_new_e1b_fk TO e1b_foreign_key;
            ALTER TABLE phenotype_observation_generated RENAME CONSTRAINT pog_new_e2a_fk TO e2a_foreign_key;
            ALTER TABLE phenotype_observation_generated RENAME CONSTRAINT pog_new_e2b_fk TO e2b_foreign_key;
            ALTER TABLE phenotype_observation_generated RENAME CONSTRAINT pog_new_q_fk TO quality_foreign_key;

            ALTER TABLE phenotype_generated_curated_mapping_new RENAME TO phenotype_generated_curated_mapping;

            EXIT;  -- swap succeeded

        EXCEPTION
            WHEN lock_not_available OR deadlock_detected THEN
                -- The sub-transaction rolled back: every rename in this attempt
                -- is undone and the exclusive locks released, so the back-off
                -- sleep below holds nothing on the live tables.
                IF swap_attempt >= swap_attempts THEN
                    RAISE EXCEPTION 'phenotype mart swap could not acquire the AccessExclusiveLock on the live tables after % attempts (lock_timeout %); aborting. The live tables are unchanged; the *_new tables remain for a manual swap or the next run.', swap_attempts, swap_wait;
                END IF;
                RAISE NOTICE 'phenotype mart swap: attempt %/% timed out waiting for the exclusive lock; backing off %s before retry', swap_attempt, swap_attempts, swap_backoff;
                PERFORM pg_sleep(swap_backoff);
        END;
    END LOOP;

    update warehouse_run_tracking
     set wrt_last_loaded_date = now()
     where wrt_mart_name = 'phenotype mart';

END $$;

COMMIT;

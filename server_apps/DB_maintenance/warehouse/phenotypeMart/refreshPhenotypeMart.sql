-- Refresh phenotype mart tables by swapping in data from *_temp tables.
--
-- Two-phase design to minimise the AccessExclusiveLock window on the live
-- tables. ALL the slow work — backing up current data and building the
-- fully-populated / indexed / FK'd replacement tables — happens first under
-- *_new staging names that the application never reads, so production queries
-- against the live phenotype_* tables are NOT blocked while it runs. Only the
-- final metadata-only renames (live -> *_old_<ts>, *_new -> live) need the
-- exclusive lock, so that window is just the renames + commit instead of the
-- whole rebuild-and-copy (which previously held the lock for minutes and was
-- starved out by concurrent readers).
--
-- Old tables are renamed with a timestamp suffix and left for later cleanup
-- by regen_cleanup_renamed_tables().

DO $$
DECLARE
    psg_old_name text;
    pog_old_name text;
    pgcm_old_name text;
    ts text := to_char(now(), 'YYMMDDHH24MI') || '_' || substring(md5(random()::text), 1, 4);
BEGIN

-- =========================================================================
-- PHASE A -- slow work, NO conflicting lock on the live tables.
-- Production reads of the live phenotype_* tables continue uninterrupted:
-- the *_bkup copy only takes AccessShareLock on the live tables, and the
-- *_new tables built below are invisible to the application.
-- =========================================================================

-- Back up current data into *_bkup tables.
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


update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('t',now())
 where zflag_name = 'regen_phenotypemart';

-- =========================================================================
-- PHASE B -- fast metadata swap. This is the ONLY part that takes an
-- AccessExclusiveLock on the live tables; every statement below is a
-- metadata-only rename, so the lock is held just until the commit that
-- immediately follows.
-- =========================================================================

-- 1. Rename the current live tables OUT to *_old_<ts>, freeing the canonical
--    table / index / constraint names. Left for regen_cleanup_renamed_tables().

-- phenotype_generated_curated_mapping (no FKs pointing to it, simplest)
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

    -- Free the canonical index/constraint names for the incoming table.
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

-- Drop any *_new tables left behind by a previously failed run (a failed run
-- rolls back atomically, so these only appear if someone left them manually).
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

-- 2. Rename the *_new tables IN to the canonical live names, restoring the
--    canonical index/constraint names so the next run's rename-out matches.

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

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('f',now())
 where zflag_name = 'regen_phenotypemart' ;

update warehouse_run_tracking
 set wrt_last_loaded_date = now()
 where wrt_mart_name = 'phenotype mart';

END $$;

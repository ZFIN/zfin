-- Refresh phenotype mart tables by swapping in data from *_temp tables.
-- Uses rename-instead-of-truncate to avoid deadlocks with the indexer.
-- Old tables are renamed with a timestamp suffix for later cleanup.

DO $$
DECLARE
    psg_old_name text;
    pog_old_name text;
    pgcm_old_name text;
    ts text := to_char(now(), 'YYMMDDHH24MI') || '_' || substring(md5(random()::text), 1, 4);
BEGIN

-- Back up current data into *_bkup tables

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

-- -------------------------------------------------------------------------
-- Swap: rename old tables out with timestamp suffix to avoid deadlocks.
-- The exclusive lock window is just these fast DDL renames, not a full
-- truncate cascade which would block on FK checks.
-- Old tables are left for later cleanup by regen_cleanup_renamed_tables().
-- -------------------------------------------------------------------------

-- 1. phenotype_generated_curated_mapping (no FKs pointing to it, simplest)
IF EXISTS (SELECT 1 FROM information_schema.tables
           WHERE table_name = 'phenotype_generated_curated_mapping' AND table_schema = 'public') THEN
    pgcm_old_name := 'phenotype_generated_curated_mapping_old_' || ts;
    RAISE NOTICE 'Renaming phenotype_generated_curated_mapping to %', pgcm_old_name;
    EXECUTE 'ALTER TABLE phenotype_generated_curated_mapping RENAME TO ' || pgcm_old_name;
END IF;

-- 2. phenotype_observation_generated (has FK to phenotype_source_generated, rename first)
IF EXISTS (SELECT 1 FROM information_schema.tables
           WHERE table_name = 'phenotype_observation_generated' AND table_schema = 'public') THEN
    pog_old_name := 'phenotype_observation_generated_old_' || ts;
    RAISE NOTICE 'Renaming phenotype_observation_generated to %', pog_old_name;
    EXECUTE 'ALTER TABLE phenotype_observation_generated RENAME TO ' || pog_old_name;

    -- Rename indexes to avoid collisions
    EXECUTE 'ALTER INDEX IF EXISTS phenotype_observation_generated_pkey RENAME TO ' || pog_old_name || '_pkey';
    -- Rename constraints
    BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT phenotype_warehouse_foreign_key TO ' || pog_old_name || '_wh_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
    BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT marker_foreign_key TO ' || pog_old_name || '_mrkr_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
    BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT e1a_foreign_key TO ' || pog_old_name || '_e1a_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
    BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT e1b_foreign_key TO ' || pog_old_name || '_e1b_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
    BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT e2a_foreign_key TO ' || pog_old_name || '_e2a_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
    BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT e2b_foreign_key TO ' || pog_old_name || '_e2b_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
    BEGIN EXECUTE 'ALTER TABLE ' || pog_old_name || ' RENAME CONSTRAINT quality_foreign_key TO ' || pog_old_name || '_q_fk'; EXCEPTION WHEN undefined_object THEN NULL; END;
END IF;

-- 3. phenotype_source_generated (parent table, rename after child is gone)
IF EXISTS (SELECT 1 FROM information_schema.tables
           WHERE table_name = 'phenotype_source_generated' AND table_schema = 'public') THEN
    psg_old_name := 'phenotype_source_generated_old_' || ts;
    RAISE NOTICE 'Renaming phenotype_source_generated to %', psg_old_name;
    EXECUTE 'ALTER TABLE phenotype_source_generated RENAME TO ' || psg_old_name;

    -- Rename indexes
    EXECUTE 'ALTER INDEX IF EXISTS phenotype_source_generated_pkey RENAME TO ' || psg_old_name || '_pkey';
    EXECUTE 'ALTER INDEX IF EXISTS phenotype_source_generated_genox RENAME TO ' || psg_old_name || '_genox';
    EXECUTE 'ALTER INDEX IF EXISTS phenotype_source_generated_fig RENAME TO ' || psg_old_name || '_fig';
    EXECUTE 'ALTER INDEX IF EXISTS phenotype_source_generated_start RENAME TO ' || psg_old_name || '_start';
    EXECUTE 'ALTER INDEX IF EXISTS phenotype_source_generated_end RENAME TO ' || psg_old_name || '_end';
    -- Rename constraints
    BEGIN EXECUTE 'ALTER TABLE ' || psg_old_name || ' RENAME CONSTRAINT constraint_fk1 TO ' || psg_old_name || '_fk1'; EXCEPTION WHEN undefined_object THEN NULL; END;
    BEGIN EXECUTE 'ALTER TABLE ' || psg_old_name || ' RENAME CONSTRAINT constraint_fk2 TO ' || psg_old_name || '_fk2'; EXCEPTION WHEN undefined_object THEN NULL; END;
    BEGIN EXECUTE 'ALTER TABLE ' || psg_old_name || ' RENAME CONSTRAINT constraint_fk3 TO ' || psg_old_name || '_fk3'; EXCEPTION WHEN undefined_object THEN NULL; END;
    BEGIN EXECUTE 'ALTER TABLE ' || psg_old_name || ' RENAME CONSTRAINT constraint_fk4 TO ' || psg_old_name || '_fk4'; EXCEPTION WHEN undefined_object THEN NULL; END;
END IF;

-- -------------------------------------------------------------------------
-- Recreate empty live tables (with correct structure) and populate from temp
-- -------------------------------------------------------------------------

-- Recreate phenotype_source_generated
CREATE TABLE phenotype_source_generated (LIKE phenotype_source_generated_temp INCLUDING ALL);
ALTER TABLE phenotype_source_generated ADD PRIMARY KEY (pg_id);
-- Reassign sequence ownership to the new live column so cleanup's DROP of
-- the renamed _old_ table doesn't take the sequence with it.
IF pg_get_serial_sequence('phenotype_source_generated', 'pg_id') IS NOT NULL THEN
    EXECUTE 'ALTER SEQUENCE ' || pg_get_serial_sequence('phenotype_source_generated', 'pg_id')
        || ' OWNED BY phenotype_source_generated.pg_id';
END IF;
CREATE INDEX phenotype_source_generated_genox ON phenotype_source_generated (pg_genox_zdb_id);
CREATE INDEX phenotype_source_generated_fig ON phenotype_source_generated (pg_fig_zdb_id);
CREATE INDEX phenotype_source_generated_start ON phenotype_source_generated (pg_start_stg_zdb_id);
CREATE INDEX phenotype_source_generated_end ON phenotype_source_generated (pg_end_stg_zdb_id);
ALTER TABLE phenotype_source_generated ADD CONSTRAINT constraint_fk1 FOREIGN KEY (pg_genox_zdb_id) REFERENCES fish_experiment (genox_zdb_id);
ALTER TABLE phenotype_source_generated ADD CONSTRAINT constraint_fk2 FOREIGN KEY (pg_fig_zdb_id) REFERENCES figure (fig_zdb_id);
ALTER TABLE phenotype_source_generated ADD CONSTRAINT constraint_fk3 FOREIGN KEY (pg_start_stg_zdb_id) REFERENCES stage (stg_zdb_id);
ALTER TABLE phenotype_source_generated ADD CONSTRAINT constraint_fk4 FOREIGN KEY (pg_end_stg_zdb_id) REFERENCES stage (stg_zdb_id);

insert into phenotype_source_generated (pg_id,
       	     			 pg_genox_zdb_id,
				 pg_fig_zdb_id,
				 pg_start_stg_zdb_id,
				 pg_end_stg_zdb_id)
select pg_id, pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id
  from phenotype_source_generated_temp;

-- Recreate phenotype_observation_generated
CREATE TABLE phenotype_observation_generated (LIKE phenotype_observation_generated_temp INCLUDING ALL);
ALTER TABLE phenotype_observation_generated ADD PRIMARY KEY (psg_id);
-- Reassign sequence ownership to the new live column (see note above).
IF pg_get_serial_sequence('phenotype_observation_generated', 'psg_id') IS NOT NULL THEN
    EXECUTE 'ALTER SEQUENCE ' || pg_get_serial_sequence('phenotype_observation_generated', 'psg_id')
        || ' OWNED BY phenotype_observation_generated.psg_id';
END IF;
ALTER TABLE phenotype_observation_generated ADD CONSTRAINT phenotype_warehouse_foreign_key FOREIGN KEY (psg_pg_id) REFERENCES phenotype_source_generated (pg_id);
ALTER TABLE phenotype_observation_generated ADD CONSTRAINT marker_foreign_key FOREIGN KEY (psg_mrkr_zdb_id) REFERENCES marker (mrkr_zdb_id);
ALTER TABLE phenotype_observation_generated ADD CONSTRAINT e1a_foreign_key FOREIGN KEY (psg_e1a_zdb_id) REFERENCES term (term_zdb_id);
ALTER TABLE phenotype_observation_generated ADD CONSTRAINT e1b_foreign_key FOREIGN KEY (psg_e1b_zdb_id) REFERENCES term (term_zdb_id);
ALTER TABLE phenotype_observation_generated ADD CONSTRAINT e2a_foreign_key FOREIGN KEY (psg_e2a_zdb_id) REFERENCES term (term_zdb_id);
ALTER TABLE phenotype_observation_generated ADD CONSTRAINT e2b_foreign_key FOREIGN KEY (psg_e2b_zdb_id) REFERENCES term (term_zdb_id);
ALTER TABLE phenotype_observation_generated ADD CONSTRAINT quality_foreign_key FOREIGN KEY (psg_quality_zdb_id) REFERENCES term (term_zdb_id);

insert into phenotype_observation_generated (psg_id,
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

-- Recreate phenotype_generated_curated_mapping
CREATE TABLE phenotype_generated_curated_mapping (LIKE phenotype_generated_curated_mapping_temp INCLUDING ALL);

insert into phenotype_generated_curated_mapping (pgcm_pg_id, pgcm_source_id, pgcm_id_type)
 select pgcm_pg_id, pgcm_source_id, pgcm_id_type from phenotype_generated_curated_mapping_temp;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('f',now())
 where zflag_name = 'regen_phenotypemart' ;

update warehouse_run_tracking
 set wrt_last_loaded_date = now()
 where wrt_mart_name = 'phenotype mart';

END $$;

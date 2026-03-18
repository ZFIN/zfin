


-- ---------------------------------------------------------------------
-- REGEN_TERM
-- ---------------------------------------------------------------------

create or replace function regen_term()
  returns int as $log$

  -- populates term fast search tables:
  --  all_term_contains: each and every ancestor and descendant
  --
  -- This table stores every term with every ancestor that has a contains
  -- relationship and the shortest distance between each pair.
  -- Contains relationships: is_a, part_of, positively/negatively regulates, occurs in.

    declare old_table_name text;

    begin
      raise notice 'regen_term: creating all_term_contains_new table';
      drop table if exists all_term_contains_new;

      create table all_term_contains_new
        (
	  alltermcon_container_zdb_id		text,
	  alltermcon_contained_zdb_id		text,
	  alltermcon_min_contain_distance	int not null
        );

      -- populate the transitive closure
      raise notice 'regen_term: populating transitive closure';
      perform populate_all_term_contains();

      -- add self-records before the swap so the table is complete when it goes live
      raise notice 'regen_term: adding self-records';
      insert into all_term_contains_new
              select term_zdb_id, term_zdb_id, 0
              from term;

      -- build indexes and constraints on _new table BEFORE the swap
      -- so the live table is never unindexed and queries are fast immediately
      raise notice 'regen_term: building indexes';
      create index atc_container_idx_new
          on all_term_contains_new (alltermcon_container_zdb_id);

      create index atc_contained_idx_new
          on all_term_contains_new (alltermcon_contained_zdb_id);

      raise notice 'regen_term: adding constraints';
      alter table all_term_contains_new
          add constraint atc_pk_new primary key (alltermcon_container_zdb_id, alltermcon_contained_zdb_id);

      alter table all_term_contains_new add constraint atc_container_fk_new
          foreign key (alltermcon_container_zdb_id) references term on delete cascade;

      alter table all_term_contains_new add constraint atc_contained_fk_new
          foreign key (alltermcon_contained_zdb_id) references term on delete cascade;

    -- -------------------------------------------------------------------------
    -- Swap: rename old out, rename new in.
    -- The exclusive lock window is just these two fast DDL renames.
    -- Old table is renamed with a timestamp, truncated, and left for later cleanup.
    -- -------------------------------------------------------------------------

      IF EXISTS (SELECT 1 FROM information_schema.tables
                 WHERE table_name = 'all_term_contains' AND table_schema = 'public') THEN

          old_table_name := 'all_term_contains_old_' || to_char(now(), 'YYMMDDHH24MI');
          old_table_name := old_table_name || '_' || substring(md5(random()::text), 1, 4);

          raise notice 'regen_term: swapping old table to %', old_table_name;
          EXECUTE 'ALTER TABLE all_term_contains RENAME TO ' || old_table_name;
          EXECUTE 'TRUNCATE ' || old_table_name;

          EXECUTE 'ALTER INDEX IF EXISTS all_term_contains_primary_key_index RENAME TO '
              || old_table_name || '_pk_idx';
          EXECUTE 'ALTER INDEX IF EXISTS alltermcon_container_zdb_id_index RENAME TO '
              || old_table_name || '_container_idx';
          EXECUTE 'ALTER INDEX IF EXISTS alltermcon_contained_zdb_id_index RENAME TO '
              || old_table_name || '_contained_idx';

          -- rename old constraints so they don't collide with the new table's canonical names
          BEGIN
              EXECUTE 'ALTER TABLE ' || old_table_name || ' RENAME CONSTRAINT all_term_contains_primary_key_index TO '
                  || old_table_name || '_pk';
          EXCEPTION WHEN undefined_object THEN NULL;
          END;
          BEGIN
              EXECUTE 'ALTER TABLE ' || old_table_name || ' RENAME CONSTRAINT alltermcon_container_zdb_id_foreign_key TO '
                  || old_table_name || '_container_fk';
          EXCEPTION WHEN undefined_object THEN NULL;
          END;
          BEGIN
              EXECUTE 'ALTER TABLE ' || old_table_name || ' RENAME CONSTRAINT alltermcon_contained_zdb_id_foreign_key TO '
                  || old_table_name || '_contained_fk';
          EXCEPTION WHEN undefined_object THEN NULL;
          END;

      END IF;

      raise notice 'regen_term: promoting new table to all_term_contains';
      alter table all_term_contains_new rename to all_term_contains;

      -- rename indexes and constraints to canonical names
      -- Note: for a PK, renaming the index also renames the constraint (they share a name),
      -- so we only rename the index and let the constraint follow.
      raise notice 'regen_term: renaming indexes and constraints to canonical names';
      alter index atc_pk_new rename to all_term_contains_primary_key_index;
      alter index atc_container_idx_new rename to alltermcon_container_zdb_id_index;
      alter index atc_contained_idx_new rename to alltermcon_contained_zdb_id_index;
      alter table all_term_contains rename constraint atc_container_fk_new to alltermcon_container_zdb_id_foreign_key;
      alter table all_term_contains rename constraint atc_contained_fk_new to alltermcon_contained_zdb_id_foreign_key;

      raise notice 'regen_term: done';
  return 0;

end;

$log$ LANGUAGE plpgsql;

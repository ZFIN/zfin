
-- ---------------------------------------------------------------------
-- UPDATE_ALL_TERM_CONTAINS_BATCH
-- ---------------------------------------------------------------------
-- Batch updates the transitive closure after an ontology load.
--
-- Expects two temp tables to exist (created by handleRelationships.sql):
--   tmp_zfin_rels: new relationships (termrel_term_1_zdb_id, termrel_term_2_zdb_id, termrel_type)
--   Removed relationships already deleted from term_relationship before this is called
--
-- Also expects tmp_term (from loadTerms.sql) for new term self-records.
--
-- This function is called from handleRelationships.sql after the
-- new/removed relationship processing is complete.

create or replace function update_all_term_contains_batch()
  returns int as $$
declare
  v_total int := 0;
  v_count int;
  rec record;
begin
  -- Part 1: Add self-records for any new terms
  insert into all_term_contains (alltermcon_container_zdb_id,
                                  alltermcon_contained_zdb_id,
                                  alltermcon_min_contain_distance)
  select term_zdb_id, term_zdb_id, 0
  from term
  where not exists (
    select 1 from all_term_contains
    where alltermcon_container_zdb_id = term_zdb_id
      and alltermcon_contained_zdb_id = term_zdb_id
  );
  get diagnostics v_count = row_count;
  v_total := v_total + v_count;
  raise notice 'Inserted % new term self-records', v_count;

  -- Part 2: Process new relationships (bulk)
  -- All new closure paths through all new edges at once
  insert into all_term_contains (alltermcon_container_zdb_id,
                                  alltermcon_contained_zdb_id,
                                  alltermcon_min_contain_distance)
  select a.alltermcon_container_zdb_id,
         d.alltermcon_contained_zdb_id,
         a.alltermcon_min_contain_distance + 1 + d.alltermcon_min_contain_distance
  from tmp_zfin_rels nr
  join all_term_contains a on a.alltermcon_contained_zdb_id = nr.termrel_term_1_zdb_id
  join all_term_contains d on d.alltermcon_container_zdb_id = nr.termrel_term_2_zdb_id
  where nr.termrel_type in ('is_a', 'part_of', 'part of',
                             'positively regulates', 'negatively regulates',
                             'regulates', 'occurs in')
  on conflict (alltermcon_container_zdb_id, alltermcon_contained_zdb_id)
  do update set alltermcon_min_contain_distance =
    least(all_term_contains.alltermcon_min_contain_distance, excluded.alltermcon_min_contain_distance);

  get diagnostics v_count = row_count;
  v_total := v_total + v_count;
  raise notice 'Processed % closure rows for new relationships', v_count;

  return v_total;
end;
$$ language plpgsql;

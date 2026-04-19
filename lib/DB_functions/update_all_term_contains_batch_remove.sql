
-- ---------------------------------------------------------------------
-- UPDATE_ALL_TERM_CONTAINS_BATCH_REMOVE
-- ---------------------------------------------------------------------
-- Batch updates the transitive closure after relationships are removed
-- during an ontology load.
--
-- Expects temp table tmp_removed_rels to exist with columns:
--   termrel_term_1_zdb_id (parent), termrel_term_2_zdb_id (child), termrel_type
--
-- Must be called AFTER the relationships are deleted from term_relationship.
-- Iterates over each removed relationship and delegates to
-- update_all_term_contains_for_removed_relationship().

create or replace function update_all_term_contains_batch_remove()
  returns int as $$
declare
  v_deleted int := 0;
  v_total_deleted int := 0;
  v_count int := 0;
  rec record;
begin
  for rec in
    select termrel_term_1_zdb_id, termrel_term_2_zdb_id, termrel_type
    from tmp_removed_rels
  loop
    select update_all_term_contains_for_removed_relationship(
      rec.termrel_term_1_zdb_id,
      rec.termrel_term_2_zdb_id,
      rec.termrel_type
    ) into v_deleted;
    v_total_deleted := v_total_deleted + v_deleted;
    v_count := v_count + 1;
  end loop;

  raise notice 'Processed % removed relationships, deleted/recomputed % closure rows',
    v_count, v_total_deleted;

  return v_total_deleted;
end;
$$ language plpgsql;

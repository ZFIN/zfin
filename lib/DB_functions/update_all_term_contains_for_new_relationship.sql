
-- ---------------------------------------------------------------------
-- UPDATE_ALL_TERM_CONTAINS_FOR_NEW_RELATIONSHIP
-- ---------------------------------------------------------------------
-- Updates the transitive closure when a new term_relationship edge is added.
--
-- When edge P -> C is added (P is parent/container, C is child/contained):
-- Every ancestor of P (including P) can now reach every descendant of C (including C)
-- through this new edge. The new distance = dist(X,P) + 1 + dist(C,Y).
--
-- Only processes closure-relevant relationship types.

create or replace function update_all_term_contains_for_new_relationship(
    p_parent_zdb_id text,
    p_child_zdb_id text,
    p_rel_type text
)
  returns int as $$
declare
  v_inserted int;
begin
  -- Only process closure-relevant relationship types
  if p_rel_type not in ('is_a', 'part_of', 'part of',
                         'positively regulates', 'negatively regulates',
                         'regulates', 'occurs in') then
    return 0;
  end if;

  -- Insert all new transitive paths through the new edge.
  -- ancestors_of_P (including P via self-record) x descendants_of_C (including C via self-record)
  insert into all_term_contains (alltermcon_container_zdb_id,
                                  alltermcon_contained_zdb_id,
                                  alltermcon_min_contain_distance)
  select a.alltermcon_container_zdb_id,
         d.alltermcon_contained_zdb_id,
         a.alltermcon_min_contain_distance + 1 + d.alltermcon_min_contain_distance
  from all_term_contains a,
       all_term_contains d
  where a.alltermcon_contained_zdb_id = p_parent_zdb_id
    and d.alltermcon_container_zdb_id = p_child_zdb_id
  on conflict (alltermcon_container_zdb_id, alltermcon_contained_zdb_id)
  do update set alltermcon_min_contain_distance =
    least(all_term_contains.alltermcon_min_contain_distance, excluded.alltermcon_min_contain_distance);

  get diagnostics v_inserted = row_count;
  return v_inserted;
end;
$$ language plpgsql;

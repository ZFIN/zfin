
-- ---------------------------------------------------------------------
-- UPDATE_ALL_TERM_CONTAINS_FOR_REMOVED_RELATIONSHIP
-- ---------------------------------------------------------------------
-- Updates the transitive closure when a term_relationship edge is removed.
--
-- When edge P -> C is removed (P is parent/container, C is child/contained):
-- 1. Identify all closure rows that COULD have used this edge:
--    (ancestor_of_P, descendant_of_C) pairs
-- 2. Delete those closure rows
-- 3. Recompute closure for the affected descendants by walking remaining edges
--
-- This must be called AFTER the edge has been removed from term_relationship.
-- Only processes closure-relevant relationship types.

create or replace function update_all_term_contains_for_removed_relationship(
    p_parent_zdb_id text,
    p_child_zdb_id text,
    p_rel_type text
)
  returns int as $$
declare
  v_affected int;
  v_count int;
  v_changed int;
  v_iterations int := 0;
  v_closure_types text[] := array['is_a', 'part_of', 'part of',
                                   'positively regulates', 'negatively regulates',
                                   'regulates', 'occurs in'];
begin
  -- Only process closure-relevant relationship types
  if p_rel_type not in ('is_a', 'part_of', 'part of',
                         'positively regulates', 'negatively regulates',
                         'regulates', 'occurs in') then
    return 0;
  end if;

  -- Step 1: Collect all descendants of C (including C itself) from closure
  drop table if exists _tmp_affected_descendants;
  create temp table _tmp_affected_descendants as
  select alltermcon_contained_zdb_id as term_id
  from all_term_contains
  where alltermcon_container_zdb_id = p_child_zdb_id;

  -- Step 2: Collect all ancestors of P (including P itself) from closure
  drop table if exists _tmp_affected_ancestors;
  create temp table _tmp_affected_ancestors as
  select alltermcon_container_zdb_id as term_id
  from all_term_contains
  where alltermcon_contained_zdb_id = p_parent_zdb_id;

  -- Step 3: Delete closure rows for (ancestor_of_P, descendant_of_C) pairs
  -- but NOT self-records (distance 0)
  delete from all_term_contains
  where alltermcon_container_zdb_id in (select term_id from _tmp_affected_ancestors)
    and alltermcon_contained_zdb_id in (select term_id from _tmp_affected_descendants)
    and alltermcon_container_zdb_id != alltermcon_contained_zdb_id;

  get diagnostics v_affected = row_count;

  -- Step 4: Recompute closure for affected pairs from remaining edges
  -- Use iterative BFS similar to populate_all_term_contains but scoped
  -- to the affected subgraph
  drop table if exists _tmp_recompute;
  create temp table _tmp_recompute (
    container_zdb_id text,
    contained_zdb_id text,
    min_distance int
  );

  -- Seed: direct edges from remaining term_relationship that connect affected terms
  insert into _tmp_recompute
  select termrel_term_1_zdb_id, termrel_term_2_zdb_id, 1
  from term_relationship
  where termrel_type = any(v_closure_types)
    and termrel_term_2_zdb_id in (select term_id from _tmp_affected_descendants);

  -- Expand iteratively through the full closure (Bellman-Ford-like relaxation).
  -- Each iteration prepends one ancestor hop via existing closure. Both new
  -- pairs AND strictly-shorter paths to existing pairs count as progress;
  -- earlier versions skipped shorter paths once any path for a pair existed,
  -- recording whichever distance happened to be found first.
  while v_iterations < 20 loop
    v_iterations := v_iterations + 1;
    v_changed := 0;

    drop table if exists _tmp_candidates;
    -- Candidates come from two sources:
    --   1. atc (left) ⨝ _tmp_recompute (right): walk one hop UP through the
    --      *remaining* closure to extend an ancestor chain.
    --   2. _tmp_recompute (left) ⨝ _tmp_recompute (right): chain two seed
    --      edges within the affected subgraph. Necessary because closure rows
    --      among affected_ancestors × affected_descendants were just deleted,
    --      so atc no longer carries them — we must rebuild via direct edges.
    create temp table _tmp_candidates as
    select container_zdb_id, contained_zdb_id, min(min_distance) as min_distance
    from (
      select atc.alltermcon_container_zdb_id as container_zdb_id,
             r.contained_zdb_id              as contained_zdb_id,
             atc.alltermcon_min_contain_distance + r.min_distance as min_distance
      from all_term_contains atc
      join _tmp_recompute r on r.container_zdb_id = atc.alltermcon_contained_zdb_id
      where atc.alltermcon_container_zdb_id != atc.alltermcon_contained_zdb_id
      union all
      select l.container_zdb_id  as container_zdb_id,
             r.contained_zdb_id  as contained_zdb_id,
             l.min_distance + r.min_distance as min_distance
      from _tmp_recompute l
      join _tmp_recompute r on r.container_zdb_id = l.contained_zdb_id
      where l.container_zdb_id <> r.contained_zdb_id
    ) all_paths
    group by container_zdb_id, contained_zdb_id;

    update _tmp_recompute t
       set min_distance = c.min_distance
      from _tmp_candidates c
     where t.container_zdb_id = c.container_zdb_id
       and t.contained_zdb_id = c.contained_zdb_id
       and t.min_distance > c.min_distance;
    get diagnostics v_count = row_count;
    v_changed := v_changed + v_count;

    insert into _tmp_recompute (container_zdb_id, contained_zdb_id, min_distance)
    select c.container_zdb_id, c.contained_zdb_id, c.min_distance
      from _tmp_candidates c
     where not exists (
       select 1 from _tmp_recompute t
       where t.container_zdb_id = c.container_zdb_id
         and t.contained_zdb_id = c.contained_zdb_id
     );
    get diagnostics v_count = row_count;
    v_changed := v_changed + v_count;

    exit when v_changed = 0;
  end loop;
  drop table if exists _tmp_candidates;

  -- Insert recomputed closure rows back, keeping minimum distance
  insert into all_term_contains (alltermcon_container_zdb_id,
                                  alltermcon_contained_zdb_id,
                                  alltermcon_min_contain_distance)
  select container_zdb_id, contained_zdb_id, min(min_distance)
  from _tmp_recompute
  group by container_zdb_id, contained_zdb_id
  on conflict (alltermcon_container_zdb_id, alltermcon_contained_zdb_id)
  do update set alltermcon_min_contain_distance =
    least(all_term_contains.alltermcon_min_contain_distance, excluded.alltermcon_min_contain_distance);

  -- Cleanup
  drop table if exists _tmp_affected_descendants;
  drop table if exists _tmp_affected_ancestors;
  drop table if exists _tmp_recompute;

  return v_affected;
end;
$$ language plpgsql;

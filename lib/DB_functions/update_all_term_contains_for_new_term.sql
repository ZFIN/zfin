
-- ---------------------------------------------------------------------
-- UPDATE_ALL_TERM_CONTAINS_FOR_NEW_TERM
-- ---------------------------------------------------------------------
-- Adds the self-record for a newly created term.
-- Called from loadTerms.sql after inserting new terms.

create or replace function update_all_term_contains_for_new_term(p_term_zdb_id text)
  returns void as $$
begin
  insert into all_term_contains (alltermcon_container_zdb_id,
                                  alltermcon_contained_zdb_id,
                                  alltermcon_min_contain_distance)
  values (p_term_zdb_id, p_term_zdb_id, 0)
  on conflict (alltermcon_container_zdb_id, alltermcon_contained_zdb_id) do nothing;
end;
$$ language plpgsql;

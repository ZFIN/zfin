


-- ---------------------------------------------------------------------
-- REGEN_TERM
-- ---------------------------------------------------------------------

create or replace function regen_term()
  returns int as $log$

  -- Refreshes the all_term_contains materialized view.
  --
  -- This view stores every term with every ancestor that has a contains
  -- relationship and the shortest distance between each pair.
  -- Contains relationships: is_a, part_of, positively/negatively regulates, occurs in.
  --
  -- Uses CONCURRENTLY so readers are never blocked during refresh.

    begin
      raise notice 'regen_term: refreshing all_term_contains materialized view';
      refresh materialized view concurrently all_term_contains;
      raise notice 'regen_term: done';
      return 0;
    end;

$log$ LANGUAGE plpgsql;

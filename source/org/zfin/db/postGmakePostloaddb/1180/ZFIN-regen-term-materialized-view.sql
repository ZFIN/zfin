--liquibase formatted sql
--changeset rtaylor:ZFIN-10185b

-- Convert all_term_contains from a table to a materialized view.
-- This eliminates the AccessExclusiveLock deadlock during regen_term()
-- because REFRESH MATERIALIZED VIEW CONCURRENTLY does not block readers.

-- Step 1: Drop the existing table (cascades FK constraints)
drop table if exists all_term_contains cascade;

-- Step 2: Create the materialized view with the same recursive logic
-- that was previously in populate_all_term_contains() + regen_term()
create materialized view all_term_contains as
with recursive tc as (
    -- base case: direct relationships
    select termrel_term_1_zdb_id as alltermcon_container_zdb_id,
           termrel_term_2_zdb_id as alltermcon_contained_zdb_id,
           1 as dist
    from term_relationship
    where termrel_type in ('is_a', 'part_of', 'part of',
                           'positively regulates', 'negatively regulates',
                           'regulates', 'occurs in')
    union
    -- recursive step: walk up ancestors
    select tr.termrel_term_1_zdb_id,
           tc.alltermcon_contained_zdb_id,
           tc.dist + 1
    from term_relationship tr
    join tc on tc.alltermcon_container_zdb_id = tr.termrel_term_2_zdb_id
    where tr.termrel_type in ('is_a', 'part_of', 'part of',
                              'positively regulates', 'negatively regulates',
                              'regulates', 'occurs in')
)
select alltermcon_container_zdb_id,
       alltermcon_contained_zdb_id,
       min(dist) as alltermcon_min_contain_distance
from (
    -- transitive closure with minimum distance
    select alltermcon_container_zdb_id,
           alltermcon_contained_zdb_id,
           dist
    from tc
    union all
    -- self-records (distance 0)
    select term_zdb_id, term_zdb_id, 0
    from term
) combined
group by alltermcon_container_zdb_id, alltermcon_contained_zdb_id
with data;

-- Step 3: Create the unique index required for REFRESH CONCURRENTLY
create unique index all_term_contains_primary_key_index
    on all_term_contains (alltermcon_container_zdb_id, alltermcon_contained_zdb_id);

create index alltermcon_container_zdb_id_index
    on all_term_contains (alltermcon_container_zdb_id);

create index alltermcon_contained_zdb_id_index
    on all_term_contains (alltermcon_contained_zdb_id);

-- Step 4: Clean up old renamed tables left from previous regen_term runs
do $$
declare
    tbl text;
begin
    for tbl in select tablename from pg_tables
               where tablename like 'all_term_contains_old_%'
                 and schemaname = 'public'
    loop
        execute 'drop table if exists ' || tbl || ' cascade';
    end loop;
end $$;

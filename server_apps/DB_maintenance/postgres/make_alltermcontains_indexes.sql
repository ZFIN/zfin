-- Indexes and constraints for all_term_contains.
-- As of regen_term() update, these are now built inside the function before
-- the table swap. This script uses IF NOT EXISTS so it's safe to run as a
-- no-op after regen_term(), or standalone if needed.

create unique index if not exists all_term_contains_primary_key_index
    on all_term_contains (alltermcon_container_zdb_id,
                                 alltermcon_contained_zdb_id);

create index if not exists alltermcon_container_zdb_id_index
     on all_term_contains (alltermcon_container_zdb_id);

create index if not exists alltermcon_contained_zdb_id_index
     on all_term_contains (alltermcon_contained_zdb_id);

-- Add primary key constraint if not already present.
-- Note: regen_term() names the PK constraint 'all_term_contains_primary_key_index'
-- (PK constraint and backing index share a name in PostgreSQL).
do $$
begin
    if not exists (
        select 1 from pg_constraint
        where conname = 'all_term_contains_primary_key_index'
          and conrelid = 'all_term_contains'::regclass
    ) then
        alter table all_term_contains
            add constraint all_term_contains_primary_key_index primary key
            using index all_term_contains_primary_key_index;
    end if;
end $$;

-- Add foreign keys if not already present
do $$
begin
    if not exists (
        select 1 from pg_constraint
        where conname = 'alltermcon_container_zdb_id_foreign_key'
          and conrelid = 'all_term_contains'::regclass
    ) then
        alter table all_term_contains add constraint alltermcon_container_zdb_id_foreign_key
            foreign key (alltermcon_container_zdb_id)
            references term on delete cascade;
    end if;

    if not exists (
        select 1 from pg_constraint
        where conname = 'alltermcon_contained_zdb_id_foreign_key'
          and conrelid = 'all_term_contains'::regclass
    ) then
        alter table all_term_contains add constraint alltermcon_contained_zdb_id_foreign_key
            foreign key (alltermcon_contained_zdb_id)
            references term on delete cascade;
    end if;
end $$;

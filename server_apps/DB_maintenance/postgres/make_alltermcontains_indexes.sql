drop index if exists all_term_contains_primary_key_index;

create unique index concurrently all_term_contains_primary_key_index
    on all_term_contains (alltermcon_container_zdb_id,
                                 alltermcon_contained_zdb_id);

                                          
create index concurrently alltermcon_container_zdb_id_index
     on all_term_contains (alltermcon_container_zdb_id);


create index concurrently alltermcon_contained_zdb_id_index
     on all_term_contains (alltermcon_contained_zdb_id);


alter table all_term_contains
    add constraint all_term_contains_primary_key primary key using index all_term_contains_primary_key_index;

      -- foreign keys                                                                                                                                  

alter table all_term_contains add constraint alltermcon_container_zdb_id_foreign_key
   foreign key (alltermcon_container_zdb_id)
   references term
   on delete cascade;

alter table all_term_contains add constraint alltermcon_contained_zdb_id_foreign_key
   foreign key (alltermcon_contained_zdb_id)
   references term
   on delete cascade;

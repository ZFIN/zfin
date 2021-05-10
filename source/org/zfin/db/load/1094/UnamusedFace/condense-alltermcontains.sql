--liquibase formatted sql
--changeset sierra:condense-alltermcontains

create table all_term_contains_new 
  (
    alltermcon_container_zdb_id varchar(50),
    alltermcon_contained_zdb_id varchar(50),
    alltermcon_min_contain_distance integer not null 
  ) 
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3 
  extent size 819200 next size 819200 lock mode row;


create unique index all_term_contains_primary_key_indexn 
    on all_term_contains_new (alltermcon_container_zdb_id,
    alltermcon_contained_zdb_id) using btree  in idxdbs2;
create index alltermcon_contained_zdb_id_indexn on 
    all_term_contains_new (alltermcon_contained_zdb_id) 
    using btree  in idxdbs2;
create index alltermcon_container_zdb_id_indexn on 
    all_term_contains_new (alltermcon_container_zdb_id) 
    using btree  in idxdbs2;
alter table all_term_contains_new add constraint primary 
    key (alltermcon_container_zdb_id,alltermcon_contained_zdb_id) 
    constraint all_term_contains_primary_keyn  ;


alter table all_term_contains_new add constraint (foreign 
    key (alltermcon_container_zdb_id) references term 
     on delete cascade constraint alltermcon_container_zdb_id_foreign_keyn);
    
alter table all_term_contains_new add constraint (foreign 
    key (alltermcon_contained_zdb_id) references term 
     on delete cascade constraint alltermcon_contained_zdb_id_foreign_keyn);
    
insert into all_term_contains_new
 select * from all_term_contains;

rename table all_term_contains to all_term_contains_old;

rename table all_term_contains_new to all_term_contains;

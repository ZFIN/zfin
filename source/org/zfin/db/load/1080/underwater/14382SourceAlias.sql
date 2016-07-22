--liquibase formatted sql
--changeset xiang_sierra:source_alias

create table source_alias 
  (
    salias_zdb_id varchar(50) not null constraint salias_zdb_id_not_null,
    salias_source_zdb_id varchar(50) not null constraint salias_source_zdb_id_not_null,
    salias_alias varchar(255) not null constraint salias_alias_not_null,
    salias_alias_lower varchar(255) not null constraint salias_alias_lower_not_null
  )
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048;

create index salias_source_zdb_id_index
  on source_alias (salias_source_zdb_id) 
  using btree in idxdbs1;

create unique index source_alias_primary_key_index 
    on source_alias (salias_zdb_id) 
    using btree in idxdbs2;

create unique index source_alias_alternate_key_index 
    on source_alias (salias_alias,salias_source_zdb_id)
    using btree in idxdbs3;

alter table source_alias add constraint primary key 
    (salias_zdb_id) constraint source_alias_primary_key;

alter table source_alias add constraint (foreign key 
    (salias_source_zdb_id) references zdb_active_source 
     on delete cascade constraint salias_source_zdb_id_foreign_key_odc);
    
alter table source_alias add constraint (foreign key 
    (salias_zdb_id) references zdb_active_source 
    on delete cascade constraint salias_zdb_id_foreign_key);
    
create trigger source_alias_insert_trigger insert
    on source_alias referencing new as new_source_alias
    for each row
        (
        execute function scrub_char(new_source_alias.salias_alias 
    ) into source_alias.salias_alias,
        execute function lower(new_source_alias.salias_alias 
    ) into source_alias.salias_alias_lower);

create trigger salias_alias_update_trigger update of 
    salias_alias on source_alias referencing new as new_source_alias
    for each row
        (
        execute function scrub_char(new_source_alias.salias_alias 
    ) into source_alias.salias_alias,
        execute function lower(new_source_alias.salias_alias 
    ) into source_alias.salias_alias_lower);

insert into zdb_object_type
 (zobjtype_name, zobjtype_home_table, zobjtype_home_zdb_id_column, zobjtype_is_data, zobjtype_is_source)
 values("SALIAS", "source_alias", "salias_zdb_id", "f", "t");

begin work ;

drop table all_map_names ;

create table all_map_names 
  (
    allmapnm_name varchar(255) not null ,
    allmapnm_zdb_id varchar(50) not null ,
    allmapnm_significance integer not null ,
    allmapnm_precedence varchar(80) not null ,
    allmapnm_name_lower varchar(255) not null ,
    allmapnm_serial_id serial not null ,
    
    check (allmapnm_name_lower = LOWER(allmapnm_name ) )
  ) 
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
  extent size 8192 next size 8192 lock mode page;

create unique index all_map_names_primary_key_index 
    on all_map_names (allmapnm_serial_id) using btree 
     in idxdbs1 ;
create unique index allmapnm_alternate_key_index on 
    all_map_names (allmapnm_name,allmapnm_zdb_id) using 
    btree  in idxdbs1 ;
create index allmapnm_name_lower_index on 
    all_map_names (allmapnm_name_lower) using btree  in idxdbs3 
    ;
create index allmapnm_precedence_index on 
    all_map_names (allmapnm_precedence) using btree  in idxdbs3 
    ;
create index allmapnm_zdb_id_index on all_map_names 
    (allmapnm_zdb_id) using btree  in idxdbs3 ;
alter table all_map_names add constraint unique (allmapnm_name,
    allmapnm_zdb_id) constraint all_map_names_alternate_key 
     ;
alter table all_map_names add constraint primary key 
    (allmapnm_serial_id) constraint all_map_names_primary_key 
     ;
alter table all_map_names add constraint (foreign 
    key (allmapnm_zdb_id) references zdb_active_data 
     on delete cascade constraint allmapnm_zdb_id_foreign_key);
    
alter table all_map_names add constraint (foreign 
    key (allmapnm_precedence) references name_precedence 
     constraint allmapnm_precedence_foreign_key);

delete from name_precedence 
where nmprec_significance = 6;

delete from name_precedence 
where nmprec_significance > 100;

set constraints all deferred ;

update name_precedence
  set nmprec_significance = '7'
  where nmprec_significance = '5' ;

update name_precedence
  set nmprec_significance = '6'
  where nmprec_significance = '4' ;

update name_precedence
  set nmprec_significance = '5'
  where nmprec_significance = '3' ;

update name_precedence
  set nmprec_significance = '4'
  where nmprec_significance = '2' ;

update name_precedence
  set nmprec_significance = '3'
  where nmprec_significance = '1' ;


insert into name_precedence
values ("Genetic feature name", 1, "Name of a Feature.");

insert into name_precedence 
values ("Genetic feature abbreviation", 102, "Abbreviation of a Feature.");
 
insert into name_precedence 
values ("Genetic feature alias", 2, "Previous name of a Feature.");

insert into name_precedence 
values ("Genotype alias", 202, "Previous name of a Genotype.");

--select count(*), nmprec_significance
--  from name_precedence 
--  group by nmprec_significance
--  having count(*) > 1; 

--select * from name_precedence
--  where nmprec_significance in ('6', '7');

set constraints all immediate ;

commit work ;

execute function regen_names();

update statistics high ;
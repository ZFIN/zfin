begin work ;

create table term_definition (termdef_zdb_id varchar(50) 
				not null constraint termdef_zdb_id_not_null,
			      termdef_term_zdb_id varchar(50) not null
				constraint termdef_term_zdb_id_not_null,
			      termdef_definition lvarchar)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 1028 next size 1028
lock mode page; 

create unique index termdef_primary_key_index
  on term_definition (termdef_zdb_id)
  using btree in idxdbs4 ;

create unique index termdef_term_id_unique_index
  on term_definition (termdef_term_zdb_id)
  using btree in idxdbs4 ;

alter table term_definition
  add constraint primary key (termdef_zdb_id)
  constraint termdef_primary_key ;

alter table term_definition
 add constraint (foreign key (termdef_zdb_id)
 references zdb_active_data on delete cascade
  constraint termdef_term_zdb_active_data_foreign_key_odc) ;

alter table term_definition
 add constraint (foreign key (termdef_term_zdb_id)
 references term on delete cascade
  constraint termdef_term_zdb_id_foreign_key_odc) ;

alter table term
  add (term_is_root boolean default 't' not null constraint
        term_is_root_not_null) ;

create table term_relationship (
	termrel_zdb_id varchar(50) not null constraint 
		termrel_zdb_id_not_null,
	termrel_term_1_zdb_id varchar(50) not null constraint
		termrel_term_1_zdb_id_not_null,
	termrel_term_2_zdb_id varchar(50) not null constraint
		termrel_term_2_zdb_id_not_null,
	termrel_type varchar(40) not null constraint
		termrel_type_not_null )
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048 
lock mode page;

create table term_relationship_type (termreltype_name varchar(40) not null constraint
		termreltype_name_not_null)
in tbldbs2 extent size 8 next size 8 ;

create unique index term_relationship_type_primary_key_index
  on term_relationship_type (termreltype_name)
  using btree in idxdbs4 ;

alter table term_relationship_type
  add constraint primary key (termreltype_name)
  constraint term_relationship_type_primary_key ;


create unique index term_relationship_primary_key_index
    on term_relationship (termrel_zdb_id)
   using btree in idxdbs4 ;

create index tmrel_term_1_foreign_key_to_term_index
  on term_relationship (termrel_term_1_zdb_id)
  using btree in idxdbs4 ;

create index tmrel_term_2_foreign_key_to_term_index
  on term_relationship (termrel_term_2_zdb_id)
  using btree in idxdbs4 ;

create index tmrel_type_foreign_key_to_term_index
  on term_relationship (termrel_type)
  using btree in idxdbs4 ;

create unique index term_relationship_alternate_key_index
    on term_relationship (termrel_term_1_zdb_id, 
				termrel_term_2_zdb_id,
				termrel_type)
   using btree in idxdbs4 ;

alter table term_relationship
  add constraint primary key (termrel_zdb_id)
  constraint term_relationship_primary_key_constraint ;

alter table term_relationship
  add constraint (foreign key (termrel_term_1_zdb_id)
  references term on delete cascade
  constraint termrel_term_1_zdb_id_foreign_key_to_term_odc) ;

alter table term_relationship
  add constraint (foreign key (termrel_term_2_zdb_id)
  references term on delete cascade
  constraint termrel_term_2_zdb_id_foreign_key_to_term_odc) ;

alter table term_relationship
  add constraint (foreign key (termrel_type)
  references term_relationship_type
  constraint termrel_term_1_zdb_id_foreign_key_to_term) ;

alter table term_relationship
  add constraint (foreign key (termrel_zdb_id)
  references zdb_active_data on delete cascade 
  constraint termrel_zactvd_foreign_key_odc) ;

insert into term_relationship_type
  values ('is_a');

insert into term_relationship_type
  values ('part_of');

insert into term_relationship_type
  values ('develops_from');

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('TERMREL', '8/7/2006','1','','term_relationship', 
	  'termrel_zdb_id', 't','f', '2') ;


insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('TERMDEF', '8/7/2006','1','','term_definition', 
	  'termdef_zdb_id', 't','f', '2') ;

alter table term
  add (term_definition lvarchar) ;

alter table term
  add (term_comment lvarchar) ;

create index term_name_index on term 
    (term_name) using btree  in idxdbs4 ;

alter table term
  drop term_definition;

update statistics high for table term ;

commit work ;
--rollback work ;
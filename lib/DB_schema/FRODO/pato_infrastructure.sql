begin work ;

create table apato_infrastructure (api_zdb_id varchar(50) not null constraint
						api_zdb_id_not_null,
					api_entity_zdb_id varchar(50) 
						not null constraint
						api_entity_zdb_id_not_null,
					api_quality_zdb_id varchar(50)
						not null constraint
						api_quality_zdb_id_not_null,
					api_context varchar(35)
						not null constraint
						api_context_not_null,
					api_tag varchar(35)
						not null constraint
						api_tag_not_null,
					api_pub_zdb_id varchar(50)
						not null constraint
						api_pub_zdb_id_not_null,
					api_curator_zdb_id varchar(50)
						not null constraint
						api_curator_zdb_id_not_null,
					api_date datetime year to day
						default current year to day
						not null constraint
						api_date_not_null)
in tbldbs3 extent size 32 next size 32 lock mode page ;

create unique index api_zdb_id_primary_key_index
  on apato_infrastructure (api_zdb_id)
  using btree in idxdbs1 ;

create index api_entity_zdb_id_fk_index
  on apato_infrastructure (api_entity_zdb_id)
  using btree in idxdbs1 ;

create index api_context_zdb_id_fk_index
  on apato_infrastructure (api_context)
  using btree in idxdbs1 ;

create index api_tag_zdb_id_fk_index
  on apato_infrastructure (api_tag)
  using btree in idxdbs1 ;

create index api_context_fk_index
  on apato_infrastructure (api_quality_zdb_id)
  using btree in idxdbs1 ;

create index api_curator_zdb_id_fk_index
  on apato_infrastructure (api_curator_zdb_id)
  using btree in idxdbs1 ;

create index api_pub_zdb_id_fk_index
  on apato_infrastructure (api_pub_zdb_id)
  using btree in idxdbs1 ;

alter table apato_infrastructure
  add constraint primary key (api_zdb_id)
  constraint apato_infrastructure_primary_key ;

alter table apato_infrastructure
  add constraint (foreign key (api_zdb_id)  
  references zdb_active_data on delete cascade constraint
  apato_zdb_id_active_data_fk_odc) ;

alter table apato_infrastructure
  add constraint (foreign key (api_entity_zdb_id)  
  references zdb_active_data on delete cascade constraint
  apato_entity_zdb_id_fk) ;

alter table apato_infrastructure
  add constraint (foreign key (api_context)  
  references apato_context constraint
  apato_context_fk) ;

alter table apato_infrastructure
  add constraint (foreign key (api_tag)
  references apato_tag constraint
  apato_tag_fk) ;

alter table apato_infrastructure
  add constraint (foreign key (api_quality_zdb_id)
  references term constraint
  apato_quality_zdb_id_fk) ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('API', '11/15/2005','1','','apato_infrastructure', 
	  'api_zdb_id', 't','f', '2') ;

rollback work ;

--commit work ;
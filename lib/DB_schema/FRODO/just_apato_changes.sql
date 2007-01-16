begin work ;

alter table atomic_phenotype
  add (apato_entity_a_zdb_id varchar(50)) ;

alter table atomic_phenotype
  add (apato_entity_b_zdb_id varchar(50)) ;

update atomic_phenotype
  set apato_entity_a_zdb_id = apato_entity_zdb_id ;

alter table atomic_phenotype
  modify (apato_entity_a_zdb_id varchar(50) not null constraint
		apato_entity_a_zdb_id_not_null) ;

alter table atomic_phenotype
  drop apato_entity_zdb_id ;

create index apato_entity_a_fk_index
  on atomic_phenotype (apato_entity_a_zdb_id)
  using btree in idxdbs3 ;

create index apato_entity_b_fk_index
  on atomic_phenotype (apato_entity_b_zdb_id)
  using btree in idxdbs3 ;

alter table atomic_phenotype
  add constraint (foreign key (apato_entity_a_zdb_id)  
  references zdb_active_data on delete cascade constraint
  apato_entity_a_zdb_id_active_data_fk_odc);

alter table atomic_phenotype
  add constraint (foreign key (apato_entity_b_zdb_id)  
  references zdb_active_data on delete cascade constraint
  apato_entity_b_zdb_id_active_data_fk_odc);

alter table apato_infrastructure
  add (api_entity_a_zdb_id varchar(50)) ;

alter table apato_infrastructure
  add (api_entity_b_zdb_id varchar(50)) ;

update apato_infrastructure
  set api_entity_a_zdb_id = api_entity_zdb_id ;

alter table apato_infrastructure
  drop api_entity_zdb_id ;

alter table apato_infrastructure
  modify (api_entity_a_zdb_id varchar(50) not null constraint
		api_entity_a_zdb_id_not_null) ;

create index api_entity_a_fk_index
  on apato_infrastructure (api_entity_a_zdb_id)
  using btree in idxdbs3 ;

create index api_entity_b_fk_index
  on apato_infrastructure (api_entity_b_zdb_id)
  using btree in idxdbs3 ;

alter table apato_infrastructure
  add constraint (foreign key (api_entity_a_zdb_id)  
  references zdb_active_data on delete cascade constraint
  api_entity_a_zdb_id_active_data_fk_odc);

alter table apato_infrastructure
  add constraint (foreign key (api_entity_b_zdb_id)  
  references zdb_active_data on delete cascade constraint
  api_entity_b_zdb_id_active_data_fk_odc);

alter table apato_infrastructure
  drop api_context ;

update atomic_phenotype
  set apato_entity_zdb_id = (select apatoegm_entity_zdb_id
				from apato_entity_group_member
				where apato_apatoeg_zdb_id =
					apatoegm_apatoeg_zdb_id);

alter table atomic_phenotype
  drop apato_apatoeg_zdb_id ;

alter table atomic_phenotype 
  modify (apato_entity_zdb_id varchar(50) not null constraint
		apato_entity_zdb_id_not_null );

create index apato_entity_foreign_key_index
  on atomic_phenotype (apato_entity_zdb_id) 
  using btree in idxdbs3 ;

alter table atomic_phenotype
  add constraint (foreign key (apato_entity_zdb_id)
  references zdb_active_data constraint
  apato_entity_zdb_id_zdb_active_data_foreign_key) ;

delete from apato_entity_group_member ;

delete from apato_entity_group ;

delete from zdb_active_data where zactvd_zdb_id like 'ZDB-APATOEG-%';

drop table apato_entity_group_member ;

drop table apato_entity_group ;

select first 1 * from atomic_phenotype ;

alter table atomic_phenotype
  drop apato_context ;

drop table apato_context ;

alter table atomic_phenotype
  add (apato_entity_a_zdb_id varchar(50)) ;

alter table atomic_phenotype
  add (apato_entity_b_zdb_id varchar(50)) ;

update atomic_phenotype
  set apato_entity_a_zdb_id = apato_entity_zdb_id ;

alter table atomic_phenotype
  modify (apato_entity_a_zdb_id varchar(50) not null constraint
		apato_entity_a_zdb_id_not_null) ;

alter table atomic_phenotype
  drop apato_entity_zdb_id ;

create index apato_entity_a_fk_index
  on atomic_phenotype (apato_entity_a_zdb_id)
  using btree in idxdbs3 ;

create index apato_entity_b_fk_index
  on atomic_phenotype (apato_entity_b_zdb_id)
  using btree in idxdbs3 ;

alter table atomic_phenotype
  add constraint (foreign key (apato_entity_a_zdb_id)  
  references zdb_active_data on delete cascade constraint
  apato_entity_a_zdb_id_active_data_fk_odc);

alter table atomic_phenotype
  add constraint (foreign key (apato_entity_b_zdb_id)  
  references zdb_active_data on delete cascade constraint
  apato_entity_b_zdb_id_active_data_fk_odc);

alter table apato_infrastructure
  add (api_entity_a_zdb_id varchar(50)) ;

alter table apato_infrastructure
  add (api_entity_b_zdb_id varchar(50)) ;

update apato_infrastructure
  set api_entity_a_zdb_id = api_entity_zdb_id ;

alter table apato_infrastructure
  drop api_entity_zdb_id ;

alter table apato_infrastructure
  modify (api_entity_a_zdb_id varchar(50) not null constraint
		api_entity_a_zdb_id_not_null) ;

create index api_entity_a_fk_index
  on apato_infrastructure (api_entity_a_zdb_id)
  using btree in idxdbs3 ;

create index api_entity_b_fk_index
  on apato_infrastructure (api_entity_b_zdb_id)
  using btree in idxdbs3 ;

alter table apato_infrastructure
  add constraint (foreign key (api_entity_a_zdb_id)  
  references zdb_active_data on delete cascade constraint
  api_entity_a_zdb_id_active_data_fk_odc);

alter table apato_infrastructure
  add constraint (foreign key (api_entity_b_zdb_id)  
  references zdb_active_data on delete cascade constraint
  api_entity_b_zdb_id_active_data_fk_odc);

alter table apato_infrastructure
  drop api_context ;

update statistics high for table atomic_phenotype ;
update statistics high for table apato_infrastructure;


update statistics high for table atomic_phenotype ;
update statistics high for table apato_infrastructure;

commit work ;

--rollback work ;
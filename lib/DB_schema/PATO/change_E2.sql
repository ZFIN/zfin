begin work ;

drop trigger atomic_phenotype_insert_trigger ;
drop trigger atomic_phenotype_update_trigger ;
drop trigger apatoinf_insert_trigger ;
drop trigger apatoinf_update_trigger ;

alter table apato_infrastructure
  add (api_related_entity_a_zdb_id varchar(50));

alter table apato_infrastructure
  add (api_related_entity_b_zdb_id varchar(50));

create table apato_genox_stage (
	apatogs_zdb_id varchar(50) not null
		constraint apatogs_zdb_id_not_null,
	apatogs_source_zdb_id varchar(50) not null
   		constraint apatogs_source_zdb_id_not_null,
	apatogs_genox_zdb_id varchar(50) not null
		constraint apatogs_genox_zdb_id_not_null,
	apatogs_start_stg_zdb_id varchar(50) not null
		constraint apatogs_start_stg_zdb_id_not_null,
	apatogs_end_stg_zdb_id varchar(50) not null
		constraint apatogs_end_stg_zdb_id_not_null
	)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 512 next size 512 lock mode page ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('APATOGS', '03/12/2007','1','','apato_genox_stage', 
	  'apatogs_zdb_id', 't','f', '2') ;

alter table atomic_phenotype 
  add (apato_apatogs_zdb_id varchar(50));

alter table atomic_phenotype
  add (apato_related_entity_a_zdb_id varchar(50));

alter table atomic_phenotype 
  add (apato_related_entity_b_zdb_id varchar(50));

create index apato_related_entity_a_zdb_id_index
  on atomic_phenotype (apato_related_entity_a_zdb_id) 
  using btree in idxdbs3 ;

create index apato_related_entity_b_zdb_id_index
  on atomic_phenotype (apato_related_entity_b_zdb_id) 
  using btree in idxdbs3 ;

set constraints all deferred ;

insert into apato_genox_stage (
				apatogs_source_zdb_id,
				apatogs_genox_zdb_id,
				apatogs_start_stg_zdb_id,
				apatogs_end_stg_zdb_id)
	select distinct apato_pub_zdb_id,
			apato_genox_zdb_id,
			apato_start_stg_zdb_id,
			apato_end_stg_zdb_id
           from atomic_phenotype ;
	  
update apato_genox_stage 
  set apatogs_zdb_id = get_id('APATOGS');


insert into zdb_active_data
  select apatogs_zdb_id from apato_genox_stage ;



update atomic_phenotype
  set apato_apatogs_zdb_id = (select apatogs_zdb_id
				from apato_genox_stage
				where apatogs_source_zdb_id =
					apato_pub_zdb_id
				and apatogs_genox_zdb_id=
					apato_genox_zdb_id
				and apatogs_start_stg_zdb_id = 
					apato_start_stg_zdb_id
				and apatogs_end_stg_zdb_id = 
					apato_end_stg_zdb_id)
 where apato_apatogs_zdb_id is null ;

alter table atomic_phenotype
  modify (apato_apatogs_zdb_id varchar(50) not null constraint
		apato_apatogs_zdb_id_not_null);

create index apato_apatogs_zdb_id_index
  on atomic_phenotype (apato_apatogs_zdb_id) 
  using btree in idxdbs3 ;

alter table atomic_phenotype
  drop apato_genox_zdb_id ;

alter table atomic_phenotype
  drop apato_start_stg_zdb_id;

alter table atomic_phenotype
  drop apato_end_stg_zdb_id ;

alter table atomic_phenotype
  drop apato_pub_zdb_id ;


set constraints all immediate ;

alter table apato_genox_stage
  modify (apatogs_zdb_id varchar(50) 
	not null constraint apatogs_zdb_id_not_null);


create unique index apato_genox_stage_primary_key_index
 on apato_genox_stage (apatogs_zdb_id) 
  using btree in idxdbs3 ;

alter table apato_genox_stage
  add constraint primary key (apatogs_zdb_id)
  constraint apato_genox_stage_primary_key ;

alter table atomic_phenotype
  add constraint (foreign key (apato_apatogs_zdb_id)
  references apato_genox_stage on delete cascade
  constraint apato_apatogs_zdb_id_foreign_key_odc);

alter table atomic_phenotype
  add constraint (foreign key (apato_related_entity_a_zdb_id)
  references zdb_active_data
  constraint apato_related_entity_a_zdb_id_foreign_key_odc);

alter table atomic_phenotype
  add constraint (foreign key (apato_related_entity_b_zdb_id)
  references zdb_active_data
  constraint apato_related_entity_b_zdb_id_foreign_key_odc);

create unique index apato_genox_stage_alternate_key_index
  on apato_genox_stage (apatogs_source_zdb_id,
			apatogs_genox_zdb_id,
			apatogs_start_stg_zdb_id,
			apatogs_end_stg_zdb_id)
  using btree in idxdbs3 ;

alter table apato_genox_stage
  add constraint unique (apatogs_source_zdb_id,
			apatogs_genox_zdb_id,
			apatogs_start_stg_zdb_id,
			apatogs_end_stg_zdb_id)
 constraint apato_genox_stage_alternate_key ;

create unique index apato_alternate_key_index
  on atomic_phenotype (apato_apatogs_zdb_id,
			apato_entity_a_zdb_id,
			apato_entity_b_zdb_id,
			apato_quality_zdb_id,
			apato_related_entity_a_zdb_id,
			apato_related_entity_b_zdb_id,
			apato_tag)
  using btree in idxdbs3;

alter table atomic_phenotype
  add constraint unique (apato_apatogs_zdb_id,
			apato_entity_a_zdb_id,
			apato_entity_b_zdb_id,
			apato_quality_zdb_id,
			apato_related_entity_a_zdb_id,
			apato_related_entity_b_zdb_id,
			apato_tag)
  constraint atomic_phenotype_alternate_key_constraint;

create trigger atomic_phenotype_insert_trigger insert on 
    atomic_phenotype referencing new as new_apato
    for each row
        (
	execute procedure p_check_pato_entities (
			new_apato.apato_zdb_id,
			new_apato.apato_entity_a_zdb_id,
			new_apato.apato_entity_b_zdb_id),
	execute procedure p_check_pato_entities (
			new_apato.apato_zdb_id,
			new_apato.apato_related_entity_a_zdb_id,
			new_apato.apato_related_entity_b_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_quality_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_entity_a_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_entity_b_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_related_entity_a_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_related_entity_b_zdb_id),
	execute function scrub_char(new_apato.apato_entity_a_zdb_id)
		into apato_entity_a_zdb_id
         );

create trigger atomic_phenotype_update_trigger update of 
    apato_apatogs_zdb_id, 
	apato_entity_a_zdb_id,
	apato_entity_b_zdb_id, 
	apato_related_entity_a_zdb_id, 
	apato_related_entity_b_zdb_id,
	apato_quality_zdb_id
    on atomic_phenotype
    referencing new as new_apato
    for each row
        (
	execute procedure p_check_pato_entities (
			new_apato.apato_zdb_id,
			new_apato.apato_entity_a_zdb_id,
			new_apato.apato_entity_b_zdb_id),
	execute procedure p_check_pato_entities (
			new_apato.apato_zdb_id,
			new_apato.apato_related_entity_a_zdb_id,
			new_apato.apato_related_entity_b_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_quality_zdb_id),	
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_entity_a_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_entity_b_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_related_entity_a_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_related_entity_b_zdb_id),
	execute function scrub_char(new_apato.apato_entity_a_zdb_id)
		into apato_entity_a_zdb_id
     ) ;

create trigger apato_genox_stage_insert_trigger insert on 
    apato_genox_stage referencing new as new_apato
    for each row
        (
	execute procedure p_stg_hours_consistent(
			new_apato.apatogs_start_stg_zdb_id,
			new_apato.apatogs_end_stg_zdb_id),
	execute procedure p_insert_into_record_attribution_datazdbids(
		new_apato.apatogs_zdb_id, new_apato.apatogs_source_zdb_id)
	);

create trigger apato_genox_stage_update_trigger update of 
    apatogs_start_stg_zdb_id, apatogs_end_stg_zdb_id, apatogs_source_zdb_id
    on apato_genox_stage
    referencing new as new_apato
    for each row
        (
	execute procedure p_stg_hours_consistent(
			new_apato.apatogs_start_stg_zdb_id,
			new_apato.apatogs_end_stg_zdb_id),
	execute procedure p_insert_into_record_attribution_datazdbids(
		new_apato.apatogs_zdb_id, new_apato.apatogs_source_zdb_id)
	);


create trigger apatoinf_insert_trigger insert on 
    apato_infrastructure referencing new as new_apatoinf
    for each row
        (
	execute procedure p_check_pato_entities (
			new_apatoinf.api_zdb_id,
			new_apatoinf.api_entity_a_zdb_id,
			new_apatoinf.api_entity_b_zdb_id),
	execute procedure p_check_pato_entities (
			new_apatoinf.api_zdb_id,
			new_apatoinf.api_related_entity_a_zdb_id,
			new_apatoinf.api_related_entity_b_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_quality_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_entity_a_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_entity_b_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(
		new_apatoinf.api_related_entity_a_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(
		new_apatoinf.api_related_entity_b_zdb_id)
         );

create trigger apatoinf_update_trigger update of 
    api_entity_a_zdb_id,
    api_entity_b_zdb_id, 
    api_quality_zdb_id,
    api_related_entity_a_zdb_id,
    api_related_entity_b_zdb_id
    on apato_infrastructure
    referencing new as new_apatoinf
    for each row
        ( execute procedure p_check_pato_entities (
			new_apatoinf.api_zdb_id,
			new_apatoinf.api_entity_a_zdb_id,
			new_apatoinf.api_entity_b_zdb_id),
execute procedure p_check_pato_entities (
			new_apatoinf.api_zdb_id,
			new_apatoinf.api_related_entity_a_zdb_id,
			new_apatoinf.api_related_entity_b_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_quality_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_entity_a_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_entity_b_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_related_entity_a_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_related_entity_b_zdb_id)
     ) ;

update statistics high for table atomic_phenotype ;
update statistics high for table apato_genox_stage ;
update statistics high for table apato_infrastructure ;

commit work ;
--rollback work ;


begin work ;

create temp table tmp_EUtrans (geno_id varchar(50),
				start_Stg_id varchar(50),
				end_Stg_id varchar(50),
				entity_id varchar(50),
				quality_id varchar(50),
				tag varchar(60))
with no log ;

load from EUtransTabbed
 insert into tmp_EUTrans ;

create index entity_index
 on tmp_EUtrans (entity_id)
  using btree in idxdbs3;

create index geno_index
 on tmp_EUtrans (geno_id)
  using btree in idxdbs3;

create index sstg_index
 on tmp_EUtrans (start_stg_id)
  using btree in idxdbs3;

create index estg_index
 on tmp_EUtrans (end_stg_id)
  using btree in idxdbs3;

create index quality_index
 on tmp_EUtrans (quality_id)
  using btree in idxdbs3;

update statistics high for table tmp_eutrans;

update tmp_EUTrans
  set start_stg_id = (Select stg_zdb_id
			from stage
			where start_stg_id = stg_abbrev);

update tmp_EUTrans
  set end_stg_id = (Select stg_zdb_id
			from stage
			where end_stg_id = stg_abbrev);
update tmp_EUTrans
  set entity_id = (select goterm_Zdb_id
			from go_term
			where "GO:"||goterm_go_id = entity_id)
  where entity_id like 'GO:%'
  and exists (select 'x' 
		from go_term
		where "GO:"||goterm_go_id = entity_id);


update tmp_EUTrans
  set entity_id = (select zrepld_new_zdb_id
			from zdb_replaced_data
			where zrepld_old_zdb_id = entity_id)
 where entity_id like 'ZDB-ANAT-%'
 and not exists (Select 'x'
			from anatomy_item
			where anatitem_zdb_id = entity_id)
 and exists (select 'x'
		from zdb_replaced_data
		where entity_id = zrepld_old_Zdb_id);

update tmp_EUTrans
  set quality_id = (select term_Zdb_id
			from term
			where term_ont_id = quality_id);

update tmp_eutrans
  set geno_id = replace(geno_id, 'FISH', 'GENO');


delete from zdb_active_data
  where exists (select 'x'
		from genotype_experiment, tmp_eutrans, atomic_phenotype
		where genox_geno_zdb_id = geno_id
		and genox_zdb_id = apato_genox_zdb_id
		and zactvd_zdb_id = apato_zdb_id)
  and zactvd_zdb_id like 'ZDB-APATO-%';


set constraints all deferred ;

set triggers for atomic_phenotype disabled ;

insert into atomic_phenotype (apato_zdb_id,
				apato_genox_zdb_id,
				apato_entity_a_zdb_id,
				apato_quality_zdb_id,
				apato_start_stg_zdb_id,
				apato_end_stg_zdb_id,
				apato_tag,
				apato_pub_zdb_id)
 select get_id('APATO'),
	genox_zdb_id,
	entity_id,
	quality_id,
	start_stg_id,
	end_stg_id,
	tag,
	'ZDB-PUB-060606-1'
   from genotype_experiment, experiment, tmp_eutrans
  where geno_id =genox_geno_zdb_id
  and exp_zdb_id = genox_exp_zdb_id
  and exp_name = '_Standard' ;

update atomic_phenotype
  set apato_entity_b_zdb_id = (Select anatitem_zdb_id 
				from anatomy_item 
				where anatitem_name = 'whole organism')
  where exists (select 'x'
			from go_term
			where goterm_zdb_id = apato_Entity_a_zdb_id
			and goterm_ontology = 'Cellular Component')
  and apato_entity_a_zdb_id like 'ZDB-GOTERM-%'
  and apato_entity_b_zdb_id is null;

insert into zdb_active_data
  select apato_zdb_id
    from atomic_phenotype
   where not exists (select 'x'
			from zdb_active_Data
			where zactvd_zdb_id = apato_zdb_id);

insert into record_Attribution (recattrib_data_zdb_id, recattrib_source_zdb_id,
	recattrib_source_type)
  select apato_zdb_id,'ZDB-PUB-060606-1', 'standard'
   from atomic_phenotype
   where not exists (Select 'x'
			from record_Attribution b
			where b.recattrib_data_zdb_id = apato_zdb_id
			and b.recattrib_source_zdb_id = 'ZDB-PUB-060606-1'
			and b.recattrib_source_type = 'standard')
   and exists (select 'x'
		from tmp_eutrans, genotype_experiment
		where geno_id = genox_geno_zdb_id
		and genox_zdb_id = apato_genox_zdb_id);

select distinct apato_quality_zdb_id
  from atomic_phenotype
  where apato_quality_zdb_id not in (Select term_zdb_id from term); 
  

select distinct apato_quality_zdb_id
  from atomic_phenotype
  where exists (Select 'x'
		from term
		where apato_quality_zdb_id = term_Zdb_id
		and term_zdb_id = term_ont_id);

set constraints all immediate ;

set triggers for atomic_phenotype enabled ;

select distinct term_name
  from term
  where exists (Select 'x'
		from atomic_phenotype
		where term_zdb_id = apato_quality_zdb_id)
  and term_ont_id = term_zdb_id ;

delete from term
  where term_ont_id = term_zdb_id ;

commit work ;
--rollback work ;

begin work ;

delete from figure_term_fish_Search;

select distinct geno_handle, phenox_genox_zdb_id, phenox_fig_zdb_id, alltermcon_container_zdb_id as term
  from phenotype_experiment, phenotype_statement, genotype_Experiment, genotype, all_term_contains
  where phenox_pk_id = phenos_phenox_pk_id
  and genox_geno_zdb_id = geno_Zdb_id
  and genox_zdb_id = phenox_genox_zdb_id
  and alltermcon_contained_zdb_id = phenos_entity_1_superterm_zdb_id
  and phenos_entity_1_superterm_zdb_id is not null
 union
select distinct geno_handle, phenox_genox_zdb_id, phenox_fig_zdb_id, alltermcon_container_zdb_id as term
  from phenotype_experiment, phenotype_statement, genotype_Experiment, genotype, all_term_contains
  where phenox_pk_id = phenos_phenox_pk_id
  and genox_geno_zdb_id = geno_Zdb_id
  and genox_zdb_id = phenox_genox_zdb_id
and alltermcon_contained_zdb_id = phenos_entity_1_subterm_zdb_id
  and phenos_entity_1_subterm_zdb_id is not null
 union
select distinct geno_handle, phenox_genox_zdb_id, phenox_fig_zdb_id, alltermcon_container_zdb_id as term
  from phenotype_experiment, phenotype_statement, genotype_Experiment, genotype, all_term_contains
  where phenox_pk_id = phenos_phenox_pk_id
  and genox_geno_zdb_id = geno_Zdb_id
  and genox_zdb_id = phenox_genox_zdb_id
and alltermcon_contained_zdb_id = phenos_entity_2_superterm_zdb_id
  and phenos_entity_2_superterm_zdb_id is not null
 union
select distinct geno_handle, phenox_genox_zdb_id, phenox_fig_zdb_id,alltermcon_container_zdb_id  as term
  from phenotype_experiment, phenotype_statement, genotype_Experiment, genotype, all_term_contains
  where phenox_pk_id = phenos_phenox_pk_id
  and genox_geno_zdb_id = geno_Zdb_id
  and genox_zdb_id = phenox_genox_zdb_id
and alltermcon_contained_zdb_id = phenos_entity_2_subterm_zdb_id
  and phenos_entity_2_subterm_zdb_id is not null
into temp tmp_phenox;

create index geno_name_index
  on tmp_phenox(geno_handle)
 using btree in idxdbs1;

create index genox_index
  on tmp_phenox(phenox_genox_zdb_id)
 using btree in idxdbs3;



create index fig_index
  on tmp_phenox(phenox_fig_zdb_id)
 using btree in idxdbs2;
update statistics high for table tmp_phenox;

insert into figure_term_fish_search (ftfs_fas_id, ftfs_geno_handle, ftfs_fig_zdb_id, ftfs_genox_zdb_id)
 select distinct  fas_pk_id, fas_geno_handle, phenox_fig_Zdb_id, phenox_genox_zdb_id
    from fish_annotation_Search, phenotype_Experiment, functional_annotation
    where fas_geno_handle = fa_geno_handle
    and phenox_genox_zdb_id = fa_genox_zdb_id;


update statistics high for table figure_term_fish_Search;


--set explain on avoid_execute;
update figure_term_fish_search
  set ftfs_term_group = replace(replace(replace(substr(multiset (select distinct item term 
      		      						   from tmp_phenox, functional_annotation
								   where ftfs_geno_handle = fa_geno_handle
								   and fa_genox_zdb_id = phenox_genox_zdb_id
								   and ftfs_fig_zdb_id = phenox_fig_zdb_id
							  )::lvarchar(3000),11),""),"'}",""),"'","");

update figure_term_fish_search set ftfs_term_group = replace(ftfs_term_group,","," ");


update figure_term_fish_search
 set ftfs_term_group = lower(ftfs_term_group);

commit work ;

--rollback work ;


update statistics high for table functional_annotation;
update statistics high for table feature_group;

set pdqpriority 80;

update functional_annotation
  set fa_gene_group = (Select distinct afg_group_name 
      				   	   from affected_gene_group
      				   	   where afg_genox_Zdb_id = fa_genox_zdb_id)
  where fa_genox_zdb_id is not null
;

select distinct afg_group_name, afg_geno_zdb_id, afg_group_order
  from affected_gene_group, functional_annotation
 where afg_genox_Zdb_id is null
       and fa_genox_zdb_id is null
 and afg_geno_zdb_id = fa_geno_zdb_id
 into temp tmp_afg_to_update;

create index tmp_geno_id
  on tmp_afg_to_update(afg_geno_Zdb_id)
  using btree in idxdbs2;



update functional_annotation
  set fa_gene_group = (Select afg_group_name 
      				   	   from tmp_afg_To_update
      				   	   where afg_geno_Zdb_id = fa_geno_zdb_id
					   )
  where fa_genox_zdb_id is null
;


update functional_annotation
  set fa_construct_group = (Select distinct cg_group_name from construct_group 
      		       	 	 where cg_genox_zdb_id = fa_genox_zdb_id 
				 and fa_genox_zdb_id is not null)
 where fa_genox_zdb_id is not null
 and fa_construct_group is null;

update functional_annotation
  set fa_construct_group = (Select distinct cg_group_name from construct_group 
      		       	 	 where cg_geno_zdb_id = fa_geno_zdb_id 
				 and fa_genox_zdb_id is null)
  where fa_genox_zdb_id is null
  and fa_construct_group is null;


update functional_annotation
  set fa_feature_group = (Select distinct fg_group_name from feature_group 
      		       	 	 where fg_geno_zdb_id = fa_geno_zdb_id 
				 and fa_genox_zdb_id is null)
  where fa_genox_zdb_id is null
  and fa_feature_group is null;

update functional_annotation
  set fa_feature_group = (Select distinct fg_group_name from feature_group 
      		       	 	 where fg_genox_zdb_id = fa_genox_zdb_id 
				 and fa_genox_zdb_id is not null)
 where fa_genox_zdb_id is not null
 and fa_feature_group is null;


update functional_annotation
  set fa_affector_type_group = (Select distinct fg_type_group from feature_group 
      		       	 	 where fg_geno_zdb_id = fa_geno_zdb_id 
				 and fa_genox_zdb_id is null)
  where fa_genox_zdb_id is null
  and fa_feature_group is not null
and fa_affector_type_group is null;

update functional_annotation
  set fa_affector_type_group = (Select distinct fg_type_group from feature_group 
      		       	 	 where fg_genox_zdb_id = fa_genox_zdb_id 
				 and fa_genox_zdb_id is not null)
 where fa_genox_zdb_id is not null
 and fa_feature_group is not null
 and fa_affector_type_group is null;


update functional_annotation
  set fa_feature_order = (Select distinct fg_group_order from feature_group 
      		       	 	 where fg_geno_zdb_id = fa_geno_zdb_id 
				 and fa_genox_zdb_id is null)
  where fa_genox_zdb_id is null
  and fa_feature_order is null;

update functional_annotation
  set fa_feature_order = (Select distinct fg_group_order from feature_group 
      		       	 	 where fg_genox_zdb_id = fa_genox_zdb_id 
				 and fa_genox_zdb_id is not null)
 where fa_genox_zdb_id is not null
 and fa_feature_order is null;

update functional_annotation
  set fa_gene_order = (Select afg_group_order from tmp_afg_to_update
      		       	 	 where afg_geno_zdb_id = fa_geno_zdb_id 
				 )
  where fa_genox_zdb_id is null
  and fa_gene_order is null;

update functional_annotation
  set fa_gene_order = (Select distinct afg_group_order from affected_gene_group 
      		       	 	 where afg_genox_zdb_id = fa_genox_zdb_id 
				 and fa_genox_zdb_id is not null)
 where fa_genox_zdb_id is not null
 and fa_gene_order is null;



update statistics high for table phenotype_figure_group;

update statistics high for table environment_group;

update functional_annotation
 set fa_environment_group_is_standard_or_control = 't'
 where fa_genox_zdb_id in (select genox_zdb_id from genotype_experiment
       		      	 where genox_is_std_or_generic_control = 't');

update functional_annotation
  set fa_environment_group  = (select eg_group_name from environment_Group where eg_genox_zdb_id = fa_genox_zdb_id);

update functional_annotation
  set fa_pheno_term_group = (select tg_group_name from term_Group where tg_genox_group = fa_genox_zdb_id);


delete from functional_annotation
  where (fa_environment_group like '%chemical%'
  or fa_environment_group like '%pH%'
  or fa_environment_group like '%physical%'
  or fa_environment_group like '%physiological%'
  or fa_environment_group like '%salinity%'
  or fa_environment_group like '%temperature'
  or fa_environment_group like '%salinity'
  or fa_environment_group like '%temperature%')
  and fa_environment_group_is_standard_or_control = 'f';

update statistics high for table functional_annotation;
update statistics high for table feature_group;
update statistics high for table morpholino_group;
update statistics high for table affected_gene_group;
update statistics high for table environment_group;
update statistics high for table term_group;

update functional_annotation
   set fa_affector_type_group = 'zzzzzzzzzzzzzzzzzzzzzz'
   where fa_affector_type_group is null;

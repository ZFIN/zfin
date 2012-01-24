begin work ;
!echo "begin geneGroup.sql";
--set pdqpriority high;

--insert into genox_group (gg_genotype_Zdb_id)
--  select geno_zdb_id from genotype
-- where exists (Select 'x' from genotype_experiment, phenotype_Experiment
--       	      	      where genox_zdb_id = phenox_genox_zdb_id
--		      and geno_Zdb_id = genox_geno_zdb_id);

--update genox_group 
--  set gg_group_name = replace(replace(replace(substr(multiset (select distinct item genox_zdb_id
--							      	      from genotype_Experiment,phenotype_experiment
--							 	      where phenox_genox_zdb_id = genox_zdb_id
--							 	      and genox_geno_Zdb_id = gg_genotype_Zdb_id
--				)::lvarchar(380),11),""),"'}",""),"'","");

--insert into genox_group_member (ggm_group_id, ggm_member_id)
--  select gg_group_pk_id, genox_zdb_id
--    from genotype_experiment, genox_group
--    where genox_geno_zdb_id = gg_genotype_Zdb_id;


insert into affected_gene_group (afg_genox_zdb_id, afg_geno_zdb_id)
  select genox_zdb_id, genox_geno_zdb_id 
  from genotype_experiment;

insert into affected_gene_group (afg_geno_zdb_id)
  select distinct geno_zdb_id from genotype;

--insert into affected_gene_group (afg_geno_zdb_id, afg_genox_Zdb_id)
--  select distinct genox_geno_zdb_id,genox_Zdb_id from genotype_experiment
--    where not exists (Select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
--    and not exists (select 'x' from phenotype_experiment where phenox_genox_Zdb_id = genox_Zdb_id);

--insert into affected_gene_group (afg_geno_zdb_id, afg_genox_Zdb_id)
--  select distinct genox_geno_zdb_id,genox_Zdb_id from genotype_experiment
--    where exists (Select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
--    and not exists (select 'x' from phenotype_experiment where phenox_genox_Zdb_id = genox_Zdb_id);

select distinct b.mrkr_abbrev||"|"||b.mrkr_name as name, afg_genox_zdb_id as genox_id
							       	from genotype_experiment,marker a,marker b,
							       marker_relationship,
							  experiment_Condition, genotype, condition_Data_type, affected_gene_group
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and cdt_zdb_id = expcond_cdt_zdb_id
							  and geno_zdb_id = genox_geno_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and expcond_mrkr_Zdb_id = a.mrkr_zdb_id
							  and expcond_mrkr_zdb_id is not null
							  and mrel_mrkr_1_zdb_id = a.mrkr_Zdb_id
							  and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
							  and b.mrkr_Type like 'GENE%'
							  union
							  select distinct
							  mrkr_abbrev||"|"||mrkr_name as name, afg_genox_zdb_id as genox_id
							       from marker, feature_marker_relationship,
							  genotype_Feature, genotype_experiment, affected_gene_group
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and fmrel_type in ('is allele of','markers missing','markers moved')
							  and mrkr_type like 'GENE%'

into temp tmp_ordered_markers;

create index tmp_genox_idx on tmp_ordered_markers (genox_id)
  using btree in idxdbs2;

update tmp_ordered_markers
  set name = replace(name,"'","$");

update statistics high for table tmp_ordered_markers;

update affected_gene_group 
  set afg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where genox_id = afg_genox_zdb_id
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","");

update affected_gene_group
  set afg_group_name = replace(afg_Group_name,"$","'");

drop table tmp_ordered_markers;


select distinct 
						  	   b.mrkr_Abbrev_order as name, afg_genox_zdb_id as genox_id
							       	from genotype_experiment,marker a,marker b,
							       marker_relationship,
							  experiment_Condition, genotype, condition_Data_type, affected_gene_group
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and cdt_zdb_id = expcond_cdt_zdb_id
							  and geno_zdb_id = genox_geno_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and expcond_mrkr_Zdb_id = a.mrkr_zdb_id
							  and expcond_mrkr_zdb_id is not null
							  and mrel_mrkr_1_zdb_id = a.mrkr_Zdb_id
							  and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
							  and b.mrkr_type like 'GENE%'
							  union
							  select distinct
							   mrkr_abbrev_order as name, afg_Genox_zdb_id as genox_id
							       from marker, feature_marker_relationship,
							  genotype_Feature, genotype_experiment, affected_gene_group
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and fmrel_type in ('is allele of','markers missing','markers moved')
							  and mrkr_type like 'GENE%'
into temp tmp_ordered_markers;

create index tmp_genox_idx on tmp_ordered_markers (genox_id)
  using btree in idxdbs2;

update tmp_ordered_markers
  set name = replace(name,"'","$");

update statistics high for table tmp_ordered_markers;

update affected_gene_group 
  set afg_group_order = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where genox_id = afg_genox_zdb_id
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","");

update affected_gene_group
  set afg_group_name = replace(afg_Group_name,"$","'");


update statistics high for table tmp_ordered_markers;



drop table tmp_ordered_markers;

select distinct 
						  	   mrkr_abbrev||"|"||mrkr_name as name, afg_geno_zdb_id as geno_id
							  from marker, 
							       feature_marker_relationship,
							       genotype_Feature, affected_Gene_group
							  where  genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and fmrel_type in ('is allele of','markers missing','markers moved')
							  and genofeat_geno_Zdb_id = afg_Geno_Zdb_id
							  and mrkr_type like 'GENE%'
							  and afg_genox_zdb_id is null
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update tmp_ordered_markers
  set name = replace(name,"'","$");

update statistics high for table tmp_ordered_markers;

update affected_gene_group 
  set afg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = afg_geno_zdb_id							  
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where afg_group_name is null
 and afg_genox_zdb_id is null;

update affected_gene_group
  set afg_group_name = replace(afg_Group_name,"$","'");





drop table tmp_ordered_markers;

select distinct 
						  	   mrkr_abbrev_order as name, afg_geno_zdb_id as geno_id
							  from marker, 
							       feature_marker_relationship,
							       genotype_Feature, affected_gene_group
							  where  genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and fmrel_type in ('is allele of','markers missing','markers moved')
							  and genofeat_geno_Zdb_id = afg_Geno_Zdb_id
							  and mrkr_type like 'GENE%'
							  and afg_genox_zdb_id is null

into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;


update tmp_ordered_markers
  set name = replace(name,"'","$");

update affected_gene_group 
  set afg_group_order = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = afg_geno_zdb_id
							  and afg_genox_zdb_id is null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where afg_group_order is null
 and afg_genox_zdb_id is null;


update affected_gene_group
  set afg_group_name = replace(afg_Group_name,"$","'");


drop table tmp_ordered_markers;


insert into affected_gene_group_member (afgm_group_id, afgm_member_name, afgm_member_id)
  select afg_group_pk_id, b.mrkr_abbrev, b.mrkr_zdb_id
   from genotype_experiment,marker a,marker b,affected_gene_group,
							       marker_relationship,
							  experiment_Condition, genotype, condition_Data_type
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and cdt_zdb_id = expcond_cdt_zdb_id
							  and afg_genox_zdb_id = genox_zdb_id
							  and geno_zdb_id = genox_geno_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and expcond_mrkr_Zdb_id = a.mrkr_zdb_id
							  and expcond_mrkr_zdb_id is not null
							  and mrel_mrkr_1_zdb_id = a.mrkr_Zdb_id
							  and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
							  and b.mrkr_type like 'GENE%';

 insert into affected_gene_group_member (afgm_group_id, afgm_member_name, afgm_member_id)
   select afg_group_pk_id, mrkr_abbrev, mrkr_zdb_id					
   	  from marker, feature_marker_relationship,affected_gene_group,
							  genotype_Feature, genotype_experiment
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and afg_genox_zdb_id = genox_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and mrkr_type like 'GENE%' ;

insert into affected_gene_group_member (afgm_group_id, afgm_member_name, afgm_member_id)
   select afg_group_pk_id, mrkr_abbrev, mrkr_zdb_id					
   	  from marker, feature_marker_relationship,affected_gene_group,
							  genotype_Feature
							  where afg_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and mrkr_type like 'GENE%' 
							  and afg_genox_zdb_id is null;
							  --and not exists (Select 'x' from genotype_Experiment
							   --   	  where genox_Geno_zdb_id = afg_geno_Zdb_id);




!echo "max octet length for gene_group_name";

select max(octet_length(afg_group_name))
 from affected_gene_group ;

commit work;
--rollback work;
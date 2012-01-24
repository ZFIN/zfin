begin work ;
!echo "begin construct group";
--set pdqpriority high;


--ZDB-GENO-110722-21


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


insert into construct_group (cg_genox_zdb_id, cg_geno_zdb_id)
  select distinct genox_zdb_id, genox_geno_zdb_id from genotype_experiment
    where exists (Select 'x' from phenotype_Experiment
    	  	 	 where phenox_genox_zdb_id = genox_Zdb_id)
    and exists (Select 'x' from genotype_Feature, feature
    	       	      where genofeat_geno_zdb_id = genox_geno_zdb_id
		      and feature_zdb_id = genofeat_feature_zdb_id
		      and feature_type like 'TRANSGENIC%');

insert into construct_group (cg_geno_zdb_id)
  select distinct geno_zdb_id from genotype
    where not exists (Select 'x' from genotype_Experiment where geno_zdb_id = genox_geno_zdb_id)
   and exists (Select 'x' from genotype_Feature, feature
    	       	      where feature_zdb_id = genofeat_feature_zdb_id
		      and feature_type like 'TRANSGENIC%'
		      and geno_zdb_id = genofeat_geno_zdb_id);

insert into construct_group (cg_geno_zdb_id, cg_genox_Zdb_id)
  select distinct genox_geno_zdb_id,genox_Zdb_id from genotype_experiment
    where not exists (Select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
    and not exists (select 'x' from phenotype_experiment where phenox_genox_Zdb_id = genox_Zdb_id)
   and exists (Select 'x' from genotype_Feature, feature
    	       	      where genofeat_geno_zdb_id = genox_geno_zdb_id
		      and feature_zdb_id = genofeat_feature_zdb_id
		      and feature_type like 'TRANSGENIC%');


insert into construct_group (cg_geno_zdb_id, cg_genox_Zdb_id)
  select distinct genox_geno_zdb_id,genox_Zdb_id from genotype_experiment
    where exists (Select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
    and not exists (select 'x' from phenotype_experiment where phenox_genox_Zdb_id = genox_Zdb_id)
   and exists (Select 'x' from genotype_Feature, feature
    	       	      where genofeat_geno_zdb_id = genox_geno_zdb_id
		      and feature_zdb_id = genofeat_feature_zdb_id
		      and feature_type like 'TRANSGENIC%');


select distinct
							   mrkr_abbrev||"|"||mrkr_name as name, cg_genox_zdb_id as genox_id
							       from marker, feature_marker_relationship,
							  genotype_Feature, genotype_experiment, construct_group
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = cg_genox_zdb_id
							  
							  and mrkr_type like '%CONSTRCT%'
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (genox_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update construct_group 
  set cg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where genox_id = cg_genox_zdb_id
							  and cg_genox_zdb_id is not null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where cg_group_name is null
 and cg_genox_zdb_id is not null;

drop table tmp_ordered_markers;

select distinct
							   mrkr_abbrev_order as name, cg_genox_zdb_id as genox_id
							       from marker, feature_marker_relationship,
							  genotype_Feature, genotype_experiment, construct_group
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = cg_genox_zdb_id
							  
							  and mrkr_type like '%CONSTRCT%'
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (genox_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update construct_group 
  set cg_group_order = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where genox_id = cg_genox_zdb_id
							  and cg_genox_zdb_id is not null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where cg_group_order is null
 and cg_genox_zdb_id is not null;

drop table tmp_ordered_markers;


select distinct 
						  	   mrkr_abbrev||"|"||mrkr_name as name, cg_geno_zdb_id as geno_id
							  from marker, 
							       feature_marker_relationship,
							       genotype_Feature, construct_group
							  where  genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							 and cg_genox_zdb_id is null
							  and genofeat_geno_Zdb_id = cg_Geno_Zdb_id
							  and mrkr_type like '%CONSTRCT%'
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update construct_group 
  set cg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = cg_geno_zdb_id
							  and cg_genox_zdb_id is null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where cg_group_name is null
 and cg_genox_zdb_id is null;

drop table tmp_ordered_markers;

select distinct 
						  	   mrkr_abbrev_order as name, cg_geno_zdb_id as geno_id 
							  from marker, 
							       feature_marker_relationship,
							       genotype_Feature, construct_group
							  where  genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and cg_genox_zdb_id is null
							  and genofeat_geno_Zdb_id = cg_Geno_Zdb_id
							  and mrkr_type like '%CONSTRCT%'
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update construct_group 
  set cg_group_order = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = cg_geno_zdb_id
							  and cg_genox_zdb_id is null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where cg_group_order is null
 and cg_genox_zdb_id is null;


 insert into construct_group_member (cgm_group_id, cgm_member_name, cgm_member_id, cgm_abbrev_order)
   select cg_group_pk_id, mrkr_abbrev, mrkr_zdb_id, mrkr_Abbrev_order					
   	  from marker, feature_marker_relationship,construct_group,
							  genotype_Feature, genotype_experiment
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and cg_genox_zdb_id = genox_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = cg_genox_zdb_id
							  and mrkr_type like '%CONSTRCT%' ;

insert into construct_group_member (cgm_group_id, cgm_member_name, cgm_member_id, cgm_abbrev_order)
   select cg_group_pk_id, mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order					
   	  from marker, feature_marker_relationship,construct_group,
							  genotype_Feature
							  where cg_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and mrkr_type like '%CONSTRCT%' 
							  and cg_genox_zdb_id is null
							  and not exists (Select 'x' from genotype_Experiment
							      	  where genox_Geno_zdb_id = cg_geno_Zdb_id);



!echo "max octet length for construct_group_name";

select max(octet_length(cg_group_name))
 from construct_group ;

commit work;
--rollback work;

--update statistics high;

!echo "begin aliases.sql";

set pdqpriority 80;

select distinct dalias_alias, fg_Geno_zdb_id, fg_group_name as group_name, fg_group_pk_id as group_id
 from data_alias, feature_group,               
	feature_group_member 
 where dalias_data_zdb_id = fgm_member_id
 and fg_group_pk_id = fgm_group_id
 order by dalias_alias
into temp tmp_alias;



delete from tmp_alias where dalias_alias is null;

create index dalias on tmp_alias(dalias_alias)
  using btree in idxdbs3;

create index dalias3 on tmp_alias(group_id)
  using btree in idxdbs3;

update statistics high for table tmp_alias;

--drop table tmp_tg;

select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_alias
							  where fg_group_pk_id = group_id
							  order by dalias_alias
							 )::lvarchar(4000),11),""),"'}",""),"'","") 
							 as tg_name,fg_group_name as ttg_group_name, fg_geno_Zdb_id
  from feature_group
into temp tmp_tg;

delete from tmp_tg where tg_name is null;

--select first 6 * from tmp_tg;

create index tmp_fish on tmp_tg(fg_geno_zdb_id) 
  using btree in idxdbs1;

update statistics high for table tmp_tg;

update functional_annotation
  set fa_feature_alias = (select distinct tg_name 
      		       	 	 from tmp_tg 
				 where fg_geno_Zdb_id = fa_geno_Zdb_id) 
  where fa_feature_group is not null;

drop table tmp_tg;
drop table tmp_alias;

select distinct dalias_alias, cg_Geno_zdb_id, cg_group_name as group_name, cg_group_pk_id as group_id
 from data_alias, construct_group,               
	construct_group_member 
 where dalias_data_zdb_id = cgm_member_id
 and cg_group_pk_id = cgm_group_id
 order by dalias_alias
into temp tmp_alias;

insert into tmp_alias (dalias_alias, cg_geno_zdb_id, group_name, group_id)
  select distinct allnmend_name_end_lower, cg_geno_zdb_id, cg_group_name, cg_group_pk_id
    from all_name_ends, all_map_names, construct_group, construct_Group_member
    where allnmend_allmapnm_serial_id = allmapnm_serial_id
   and cg_group_pk_id = cgm_group_id
   and allmapnm_zdb_id = cgm_member_id;

delete from tmp_alias where dalias_alias is null;

create index dalias on tmp_alias(dalias_alias)
  using btree in idxdbs3;

create index dalias3 on tmp_alias(group_id)
  using btree in idxdbs3;

update statistics high for table tmp_alias;

--drop table tmp_tg;

select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_alias
							  where cg_group_pk_id = group_id
							  order by dalias_alias
							 )::lvarchar(4000),11),""),"'}",""),"'","") 
							 as tg_name,cg_group_name as ttg_group_name, cg_geno_Zdb_id
  from construct_group
into temp tmp_tg;

delete from tmp_tg where tg_name is null;

--select first 6 * from tmp_tg;

create index tmp_fish on tmp_tg(cg_geno_zdb_id) 
  using btree in idxdbs1;

update statistics high for table tmp_tg;

update functional_annotation
  set fa_construct_alias = (select distinct tg_name 
      		       	 	 from tmp_tg 
				 where cg_geno_Zdb_id = fa_geno_Zdb_id) 
  where fa_construct_group is not null;

select max(octet_length(fa_construct_alias))
  from functional_annotation;

drop table tmp_tg;
drop table tmp_alias;


select distinct dalias_alias, afg_group_name as group_name, afg_genox_zdb_id as genox, afg_geno_zdb_id as geno
from data_alias, affected_gene_group, 
	affected_gene_group_member
where dalias_data_zdb_id = afgm_member_id
and afg_group_pk_id = afgm_group_id
order by dalias_alias
into temp tmp_alias;

select distinct mrkr_zdb_id, genox_geno_zdb_id, genox_zdb_id
							  	 from marker, 
								      feature_marker_relationship,
								      marker_relationship,	
								      genotype_Feature,
								      genotype_experiment
								  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrel_mrkr_1_zdb_id
							  and mrel_mrkr_2_zdb_id = mrkr_zdb_id
							  and mrkr_type in ( 'GENE','GENEP','REGION','EFG')
							  and mrel_type in 
							      ('contains engineered region','coding sequence of','promoter of')
into temp tmp_mrkrs;


insert into tmp_mrkrs (mrkr_zdb_id, genox_geno_zdb_id, genox_zdb_id)
select distinct mrkr_zdb_id,genofeat_geno_zdb_id,''
							  	 from marker, 
								      feature_marker_relationship,
								      marker_relationship,	
								      genotype_Feature
								  where genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrel_mrkr_1_zdb_id
							  and mrel_mrkr_2_zdb_id = mrkr_zdb_id
							  and mrkr_type in( 'GENE','GENEP','REGION','EFG')
							  and mrel_type in 
							      ('contains engineered region','coding sequence of','promoter of');


update tmp_mrkrs
  set genox_zdb_id = null
 where genox_zdb_id = '';



insert into tmp_Alias (dalias_alias, group_name, genox, geno)
  select distinct mrkr_abbrev||"|"||mrkr_name, mrkr_abbrev||"|"||mrkr_name, genox_zdb_id, genox_geno_zdb_id
							  	 from marker, 
								      feature_marker_relationship,
								      marker_relationship,	
								      genotype_Feature,
								      genotype_experiment
								  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrel_mrkr_1_zdb_id
							  and mrel_mrkr_2_zdb_id = mrkr_zdb_id
							  and mrkr_type in ( 'GENE','GENEP','REGION','EFG')
							  and mrel_type in 
							      ('contains engineered region','coding sequence of','promoter of');


insert into tmp_Alias (dalias_alias, group_name, geno)
   select distinct mrkr_abbrev||"|"||mrkr_name, mrkr_abbrev||"|"||mrkr_name, genofeat_geno_zdb_id
							  	 from marker, 
								      feature_marker_relationship,
								      marker_relationship,	
								      genotype_Feature
								  where genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrel_mrkr_1_zdb_id
							  and mrel_mrkr_2_zdb_id = mrkr_zdb_id
							  and mrkr_type in ( 'GENE','GENEP','REGION','EFG')
							  and mrel_type in 
							      ('contains engineered region','coding sequence of','promoter of')
;

create index mrkr on tmp_mrkrs(mrkr_zdb_id)
  using btree in idxdbs1;



insert into tmp_Alias (Dalias_alias, group_name, genox, geno)
select distinct dalias_alias, mrkr_Zdb_id, genox_Zdb_id, genox_geno_Zdb_id
   from data_Alias, tmp_mrkrs
 where mrkr_zdb_id = dalias_data_zdb_id
;


delete from tmp_alias where dalias_alias is null;

create index dalias on tmp_alias(dalias_alias)
  using btree in idxdbs3;

create index dalias1 on tmp_alias(geno)
  using btree in idxdbs3;

create index dalias3 on tmp_alias(genox)
  using btree in idxdbs3;

update statistics high for table tmp_alias;

--drop table tmp_tg;

select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_alias
							  where genox = afg_Genox_zdb_id
							  order by dalias_alias
							 )::lvarchar(4000),11),""),"'}",""),"'","") as tg_name,
							 afg_group_name as ttg_group_name, afg_genox_zdb_id as fish
  from affected_gene_group
where afg_genox_zdb_id is not null
into temp tmp_tg;

create index tmp_fish on tmp_tg(fish) 
  using btree in idxdbs1;

update statistics high for table tmp_tg;
update statistics high for table affected_gene_group;

insert into tmp_tg (tg_name, ttg_Group_name, fish)
select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_alias
							  where geno = afg_Geno_zdb_id
							  and genox is null
							  order by dalias_alias
							 )::lvarchar(4000),11),""),"'}",""),"'","") as tg_name,
							 afg_group_name as ttg_group_name, afg_geno_zdb_id as fish
  from affected_gene_group
  where afg_genox_Zdb_id is null
 and not exists (Select 'x' from tmp_tg where fish = afg_geno_zdb_id);


delete from tmp_tg where tg_name is null;
--select first 6 * from tmp_tg;


update functional_annotation
  set fa_gene_alias = (select distinct tg_name from tmp_tg where fish = fa_genox_zdb_id
      		     and fish like 'ZDB-GENOX-%' )
 where fa_gene_group is not null
 and fa_genox_zdb_id is not null;

--'ZDB-GENO-110722-21'
update functional_annotation
  set fa_gene_alias = (select distinct tg_name from tmp_tg where fish = fa_geno_Zdb_id
      		    and fish like 'ZDB-GENO-%')
 where fa_gene_group is not null
 and fa_genox_zdb_id is null
and fa_gene_alias is null;

update functional_annotation
  set fa_all = (select distinct tg_name from tmp_tg where fish = fa_geno_Zdb_id
      		    and fish like 'ZDB-GENO-%')
 where fa_gene_group is null
 and fa_genox_zdb_id is null
and fa_gene_alias is null
 and exists (Select 'x' from tmp_tg where fish = fa_geno_Zdb_id)
 and fa_geno_Zdb_id is not null;

update functional_annotation
  set fa_gene_alt_alias = (select distinct tg_name from tmp_tg where fish = fa_geno_Zdb_id
      		    and fish like 'ZDB-GENO-%')
 where fa_gene_group is null
 and fa_genox_zdb_id is null
and fa_gene_alias is null
 and exists (Select 'x' from tmp_tg where fish = fa_geno_Zdb_id)
 and fa_geno_Zdb_id is not null;

--select * from functional_annotation 
--where fa_geno_Zdb_id = 'ZDB-GENO-110722-21';

drop table tmp_tg;
drop table tmp_alias;

select distinct dalias_alias, morphg_group_name as group_name
from data_alias, morpholino_group, 
	morpholino_group_member
where dalias_data_zdb_id = morphgm_member_id
and morphg_group_pk_id = morphgm_group_id
into temp tmp_alias;


delete from tmp_alias where dalias_alias is null;

create index dalias on tmp_alias(dalias_alias)
  using btree in idxdbs3;

create index dalias_name on tmp_alias(group_name)
  using btree in idxdbs3;

update statistics high for table tmp_alias;


select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_alias
							  where morphg_group_name = group_name
							  order by dalias_alias
							 )::lvarchar(380),11),""),"'}",""),"'","") 
							 as tg_name,morphg_group_name as ttg_group_name
  from morpholino_group
into temp tmp_tg;

delete from tmp_tg where tg_name is null;

--select first 6 * from tmp_tg;

update functional_annotation
  set fa_morph_alias = (select distinct tg_name 
      		       	       from tmp_tg 
			       where ttg_group_name = fa_morpholino_group)
  where fa_morpholino_group is not null;

drop table tmp_tg;
drop table tmp_alias;

select distinct dalias_alias, geno_zdb_id as group_name
from data_alias, genotype
where dalias_data_zdb_id =geno_zdb_id
into temp tmp_alias;


delete from tmp_alias where dalias_alias is null;

create index dalias on tmp_alias(dalias_alias)
  using btree in idxdbs3;

create index dalias3 on tmp_alias(group_name)
  using btree in idxdbs2;

update statistics high for table tmp_alias;


select replace(replace(replace(substr(multiset ( select distinct item dalias_alias from tmp_alias
							  where fa_geno_Zdb_id = group_name
							  order by dalias_alias
							 )::lvarchar(380),11),""),"'}",""),"'","") 
							 as tg_name,fa_geno_Zdb_id as ttg_group_name
  from functional_Annotation
into temp tmp_tg;

delete from tmp_tg where tg_name is null;
--select first 6 * from tmp_tg;

update functional_annotation
  set fa_geno_alias = (select distinct tg_name 
      		      	      from tmp_tg 
			      where ttg_group_name = fa_geno_Zdb_id
			      )
  where fa_geno_Zdb_id is not null;

drop table tmp_tg;
drop table tmp_alias;

--update functional_annotation
--  set fa_geno_alias = (replace(replace(replace(substr(multiset (select distinct item-
--						  	  dalias_alias from data_alias
--							  where dalias_data_zdb_id = fa_geno_Zdb_id
--							  and fa_geno_Zdb_id is not null
--							  )::lvarchar(10000),11),""),"'}",""),"'",""));


update functional_annotation
  set fa_all = "sierra"
 where (fa_all is null
        or fa_all = '');



update functional_Annotation
  set fa_all = fa_all||","||fa_feature_group
 where fa_feature_group is not null
 ;

update functional_Annotation
  set fa_all = fa_all||","||fa_morpholino_group
 where fa_morpholino_group is not null;



update functional_Annotation
  set fa_all = fa_all||","||fa_gene_group
 where fa_gene_group is not null;



update functional_Annotation
  set fa_all = fa_all||","||fa_construct_group
 where fa_construct_group is not null;


update functional_annotation
  set fa_all = fa_all||","||fa_gene_alias
  where fa_gene_alias is not null;


update functional_annotation
  set fa_all = fa_all||","||fa_morph_alias
  where fa_morph_alias is not null;

select count(*) from functional_annotation 
where fa_all ='sierra';


update functional_annotation
  set fa_all = fa_all||","||fa_feature_alias
  where fa_feature_alias is not null;


update functional_annotation
  set fa_all = fa_all||","||fa_geno_alias
  where fa_geno_alias is not null;

update functional_annotation
  set fa_all = fa_all||","||fa_pheno_term_group
  where fa_pheno_term_group is not null;

!echo "functional_annotation with fa_all null";
select count(*) from functional_annotation
 where fa_all is null;

update functional_Annotation
  set fa_all = replace(fa_all,'sierra,','');

update functional_Annotation
  set fa_all = replace(fa_all,'sierra','');

--create index fa_all_bts_index
--  on functional_Annotation (fa_all bts_lvarchar_ops) USING BTS IN smartbs1;

--


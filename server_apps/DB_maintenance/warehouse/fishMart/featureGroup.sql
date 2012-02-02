
!echo "feature_group";


insert into feature_group (fg_geno_Zdb_id)
  select geno_zdb_id from genotype
 where not exists (Select 'x' from genotype_experiment where genox_geno_zdb_id = geno_Zdb_id);

insert into feature_group (fg_Geno_zdb_id, fg_genox_zdb_id)
  select distinct genox_geno_zdb_id, genox_zdb_id
    from genotype_Experiment;


create temp table tmp_ordered_markers (name lvarchar(1000), geno_id varchar(50))
 with no log;

insert into tmp_ordered_markers (name, geno_id)
select distinct feature.feature_name||"|"||feature.feature_abbrev||"|"||fp_prefix as name, 
							   genofeat_geno_Zdb_id as geno_id
							  from feature, genotype_feature, feature_group, feature_prefix
  	 	   	   				  where feature_zdb_id = genofeat_feature_zdb_id
							  
			   				  and genofeat_geno_zdb_id = fg_geno_Zdb_id
							  and fp_pk_id = feature_lab_prefix_id;

insert into tmp_ordered_markers (name, geno_id)
  select distinct feature.feature_name||"|"||feature.feature_abbrev, 
							   genofeat_geno_Zdb_id
							  from feature, genotype_feature, feature_group
  	 	   	   				  where feature_zdb_id = genofeat_feature_zdb_id
			   				  and genofeat_geno_zdb_id = fg_geno_Zdb_id
							  and feature_lab_prefix_id is null
;

select * from tmp_ordered_markers
  where geno_id = 'ZDB-GENO-120130-345';

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update feature_group 
  set fg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = fg_geno_zdb_id
							  and fg_geno_zdb_id is not null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where fg_group_name is null
 and fg_geno_zdb_id is not null;

drop table tmp_ordered_markers;


select distinct feature.feature_Abbrev_order as name, genofeat_geno_zdb_id as geno_id from feature, genotype_feature, feature_Group
  	 	   	   				  where feature_zdb_id = genofeat_feature_zdb_id
			   				  and genofeat_geno_zdb_id = fg_geno_Zdb_id
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update feature_group 
  set fg_group_order = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = fg_geno_zdb_id
							  and fg_geno_zdb_id is not null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where fg_group_order is null
 and fg_geno_zdb_id is not null;

drop table tmp_ordered_markers;


update feature_group (fg_type_group)
  set fg_type_group = replace(replace(replace(substr(multiset (select distinct 
						  	  item feature.feature_type from feature, genotype_feature
  	 	   	   				  where feature_zdb_id = genofeat_feature_zdb_id
			   				  and genofeat_geno_zdb_id = fg_geno_Zdb_id
							  )::lvarchar(380),11),""),"'}",""),"'","")
 ;



insert into feature_group_member (fgm_group_id, fgm_member_name, fgm_member_id, fgm_genotype_id, fgm_significance, fgm_feature_type)
  select fg_group_pk_id, feature_name, feature_zdb_id, genofeat_geno_zdb_id, ftrtype_significance,  feature_type
    from feature_group, genotype_feature, feature, feature_type
    where fg_Geno_zdb_id = genofeat_geno_Zdb_id
    and ftrtype_name = feature_type
    and genofeat_feature_zdb_id = feature_zdb_id;

!echo "max octet length for feature_group_name";

select max(octet_length(fg_group_name))
 from feature_group ;

update feature_group_member
 set fgm_significance = 0 
where fgm_significance is null;

select * from feature_group
 where fg_geno_zdb_id = 'ZDB-GENO-120130-345';



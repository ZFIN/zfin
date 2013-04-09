
!echo "start Burgess Lin";

set pdqpriority 50;


insert into fish_annotation_search_temp (fas_all,
					fas_genotype_Group,
					fas_geno_name, 
       	    			   	fas_line_handle,
       	    			   	fas_feature_group, 
					fas_gene_group,
					fas_fish_parts_count, 
					fas_affector_type_group,
					fas_feature_order,
					fas_gene_order,
					fas_gene_count)
select feature_name||"|"||feature_abbrev||","||a.mrkr_name||"|"||a.mrkr_abbrev,
       'ZDB-GENO-F1 Pool',
       '', 
       		feature_zdb_id,
       		feature_name||"|"||feature_abbrev, 
		a.mrkr_name||"|"||a.mrkr_abbrev,
		"1", 
		feature_type,
		feature_Abbrev_order, 
		a.mrkr_abbrev_order, 
		"1"
  from feature, feature_marker_relationship c, marker a
  where feature_zdb_id = c.fmrel_ftr_zdb_id
  and a.mrkr_zdb_id = c.fmrel_mrkr_zdb_id
  and c.fmrel_type = 'is allele of'
and not exists (Select 'x' from genotype_feature
 where genofeat_feature_zdb_id = feature_zdb_id)
and (feature_abbrev like 'sa%' or feature_abbrev like 'hu%')
;



select distinct dalias_alias, feature_zdb_id as f_id
 from data_alias, feature
 where dalias_data_zdb_id = feature_zdb_id
 and not exists (Select 'x' from genotype_feature where genofeat_feature_zdb_id = feature_zdb_id)
 and (feature_abbrev like 'sa%' or feature_abbrev like 'hu%')
 order by dalias_alias
into temp tmp_aliasBs;


delete from tmp_aliasBs where dalias_alias is null;

create index daliasBs on tmp_aliasBs(dalias_alias)
  using btree in idxdbs3;

create index dalias3Bs on tmp_aliasBs(f_id)
  using btree in idxdbs3;

update statistics high for table tmp_aliasBs;

--drop table tmp_tg;
select feature_zdb_id fe_id
 from feature
 where not exists (Select 'x' from genotype_feature
       	   	  	  where genofeat_feature_zdb_id = feature_zdb_id)
 and (feature_abbrev like 'sa%' or feature_abbrev like 'hu%')
into temp temp_features1;

create index f_id_index
  on temp_features1 (fe_id)
 using btree in idxdbs2;

select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_aliasBs
							  where feature_zdb_id = f_id
							  and fe_id = f_id
							  order by dalias_alias
							 )::lvarchar(4000),11),""),"'}",""),"'","") 
							 as tg_name, feature_name||"|"||feature_abbrev as namer,feature_Zdb_id as f_id
  from feature, temp_features1
 where feature_zdb_id = fe_id
and (feature_abbrev like 'sa%' or feature_abbrev like 'hu%')
into temp tmp_tg1s;

delete from tmp_tg1s where tg_name is null;

--select first 6 * from tmp_tg;

create index tmp_fishBs on tmp_tg1s(f_id) 
  using btree in idxdbs1;

update statistics high for table tmp_tg1s;
update statistics high for table fish_annotation_Search_temp;

update fish_annotation_search_temp
  set fas_all = fas_all||","||(select distinct tg_name 
      		       	 	 from tmp_tg1s
				 where fas_line_handle = f_id)
  where exists (Select 'x' from tmp_tg1s
      	     	     where fas_line_handle = f_id);

drop table tmp_aliasBs;

drop table tmp_tg1s;

select distinct dalias_alias, fmrel_ftr_zdb_id as f_id
 from data_alias, feature_marker_relationship,feature
 where dalias_data_zdb_id = fmrel_mrkr_zdb_id
 and fmrel_ftr_zdb_id = feature_zdb_id
and (feature_abbrev like 'sa%' or feature_abbrev like 'hu%')
 and not exists (Select 'x' from genotype_feature where genofeat_feature_zdb_id = feature_zdb_id)
 order by dalias_alias
into temp tmp_aliasBs;

delete from tmp_aliasBs where dalias_alias is null;


create index daliasBs on tmp_aliasBs(dalias_alias)
  using btree in idxdbs3;

create index dalias3Bs on tmp_aliasBs(f_id)
  using btree in idxdbs3;


--drop table tmp_tg;
drop table temp_features1;


select feature_zdb_id fe_id
 from feature
 where not exists (Select 'x' from genotype_feature
       	   	  	  where genofeat_feature_zdb_id = feature_zdb_id)
 and (feature_abbrev like 'sa%' or feature_abbrev like 'hu%')
into temp temp_features1;

create index fe1_id_index on temp_features1 (fe_id)
using btree in idxdbs3;

select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_aliasBs
							  where f_id = feature_zdb_id
							  and fe_id = f_id
							  order by dalias_alias
							 )::lvarchar(4000),11),""),"'}",""),"'","") 
							 as tg_name, feature_name||"|"||feature_abbrev as namer,feature_Zdb_id as f_id
  from feature, temp_features1
 where feature_zdb_id = fe_id
and (feature_abbrev like 'sa%' or feature_abbrev like 'hu%')
into temp tmp_tg2s;

delete from tmp_tg2s where tg_name is null;

--select first 6 * from tmp_tg;

create index tmp_fishBs on tmp_tg2s(f_id) 
  using btree in idxdbs1;


update statistics high for table tmp_tg2s;
update statistics high for table fish_annotation_Search_temp;

update fish_annotation_search_temp
  set fas_all = fas_all||","||(select distinct tg_name 
      		       	 	 from tmp_tg2s 
				 where fas_line_handle = f_id)
  where (fas_feature_group like 'sa%' or  fas_feature_group like 'hu%')
 and fas_line_handle like 'ZDB-ALT%'
 and fas_line_handle in (select f_id from tmp_tg2s)
;

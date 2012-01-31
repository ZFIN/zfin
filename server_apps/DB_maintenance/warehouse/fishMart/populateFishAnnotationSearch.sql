
delete from fish_annotation_search;

update phenotype_figure_group
 set pfigg_geno_handle = (Select distinct fa_geno_handle
     		       	       from functional_annotation
			       where fa_genox_zdb_id = pfigg_genox_zdb_id);

update term_group
 set tg_geno_handle = (Select distinct fa_geno_handle
     		       	       from functional_annotation
			       where fa_genox_zdb_id = tg_genox_group);

insert into fish_annotation_search (fas_geno_name, 
       	    			   	fas_geno_handle,
       	    			   	fas_feature_group, 
					fas_gene_group, 
					fas_morpholino_group, 
					fas_construct_group, 
					fas_fish_parts_count, 
					fas_affector_type_group,
					fas_feature_order,
					fas_gene_order,
					fas_gene_count)
select distinct fa_geno_name, 
       		fa_geno_handle,
       		fa_feature_group, 
		fa_gene_group, 
		fa_morpholino_group, 
		fa_construct_group, 
		fa_fish_parts_count, 
		fa_affector_type_group,
		fa_feature_order, 
		fa_gene_order, 
		fa_gene_count
  from functional_annotation;



update fish_annotation_search
  set fas_all = "sierra"
 where (fas_all is null
        or fas_all = '');


update fish_annotation_search
  set fas_all = fas_all||","||fas_feature_group
 where fas_feature_group is not null
 ;


update fish_annotation_search
  set fas_all = fas_all||","||fas_morpholino_group
 where fas_morpholino_group is not null;



update fish_annotation_search
  set fas_all = fas_all||","||fas_gene_group
 where fas_gene_group is not null;



update fish_annotation_search
  set fas_all = fas_all||","||fas_construct_group
 where fas_construct_group is not null;



update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_gene_alias from functional_annotation where fa_geno_handle = fas_geno_handle and fa_gene_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_gene_alias is not null);

update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_gene_alt_alias from functional_annotation where fa_geno_handle = fas_geno_handle and fa_gene_alt_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_gene_alt_alias is not null);

update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_feature_alias from functional_annotation where fa_geno_handle = fas_geno_handle and fa_feature_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_feature_alias is not null);

update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_morph_alias from functional_annotation where fa_geno_name = fas_geno_name and fa_morph_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_morph_alias is not null);

update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_geno_alias from functional_annotation where fa_geno_name = fas_geno_name and fa_geno_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_geno_alias is not null);

update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_construct_alias from functional_annotation where fa_geno_name = fas_geno_name and fa_construct_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_construct_alias is not null);

update fish_annotation_search
  set fas_all = replace(fas_all,'sierra,','');

update fish_annotation_search
  set fas_all = replace(fas_all,'sierra','');

select count(*) from fish_annotation_search
 where fas_all is null;

!echo "should be zero";

select count(*) as counter, fas_geno_handle as geno_handle
 from fish_annotation_search
 group by fas_geno_handle
 having count(*) > 1
into temp tmp_dups2;


select first 6 *
from fish_annotation_search, tmp_dups2
 where fas_geno_handle = geno_handle
 order by fas_geno_handle;

delete from fish_Annotation_search
 where fas_geno_handle in (Select geno_handle from tmp_dups2);

update statistics high for table fish_annotation_search;
update statistics high for table phenotype_figure_group;
update statistics high for table phenotype_figure_group_member;
update statistics high for table term_group;
update statistics high for table term_group_member;


--set explain on avoid_execute ;

update fish_annotation_Search
  set fas_pheno_term_group = replace(replace(replace(substr(multiset (select distinct item tgm_member_id
      		      						   from term_group,
								   term_group_member
								   where tgm_group_id = tg_group_pk_id
								   and tg_geno_handle = fas_geno_handle
							  )::lvarchar(1000),11),""),"'}",""),"'","");

--update fish_annotation_search
--  set fas_all = fas_all||","||fas_pheno_term_group
--  where fas_pheno_term_group is not null
-- and fas_all is not null;

update fish_annotation_Search
  set fas_pheno_figure_group = replace(replace(replace(substr(multiset (select distinct item pfiggm_member_id
      		      						   from phenotype_figure_group,
								   phenotype_figure_group_member
								   where pfiggm_group_id = pfigg_group_pk_id
								   and pfigg_geno_handle = fas_geno_handle
							  )::lvarchar(1000),11),""),"'}",""),"'","");

set explain on avoid_execute;
update fish_annotation_Search
  set fas_xpat_figure_group = replace(replace(replace(substr(multiset (select distinct item xfiggm_member_id
      		      						   from xpat_figure_group,
								   xpat_figure_group_member
								   where xfiggm_group_id = xfigg_group_pk_id
								   and xfigg_geno_handle = fas_geno_handle
							  )::lvarchar(4000),11),""),"'}",""),"'","")
;
set explain off;

update fish_annotation_search
  set fas_pheno_figure_count = (Select count(distinct pfiggm_member_id)
      			       	       from phenotype_figure_group, phenotype_Figure_group_member
				       where pfiggm_group_id = pfigg_group_pk_id
				       and pfigg_geno_handle = fas_geno_handle);


--update fish_annotation_search
--  set fas_xpat_figure_count = (Select count(distinct xfiggm_member_id)
--      			       	       from xpat_figure_group, xpat_Figure_group_member
--				       where xfiggm_group_id = xfigg_group_pk_id
--				       and xfigg_geno_handle = fas_geno_handle);

update statistics high for table fish_Annotation_search;

select distinct fa_genox_zdb_id, fa_geno_handle
from functional_annotation
where fa_genox_zdb_id is not null
 order by fa_genox_zdb_id
into temp tmp_genox2;

create index fa_geno_handle_tmp_index
 on tmp_genox2 (fa_geno_handle)
  using btree in idxdbs3;

update fish_annotation_search
  set fas_genox_group = replace(replace(replace(substr(multiset (select distinct item fa_genox_zdb_id
      		      						   from tmp_genox2
								   where fa_geno_handle = fas_geno_handle
								   order by fa_genox_zdb_id
							  )::lvarchar(1000),11),""),"'}",""),"'","");

update fish_annotation_Search
  set fas_genotype_group = replace(replace(replace(substr(multiset (select item geno_Zdb_id
      		      						   from genotype
								   where geno_handle = fas_geno_handle
								   
							  )::lvarchar(1000),11),""),"'}",""),"'","");

update fish_annotation_Search
  set fas_genotype_group = replace(replace(replace(substr(multiset (select distinct item genox_geno_Zdb_id
      		      						   from functional_annotation, genotype_Experiment
								   where fa_geno_handle = fas_geno_handle
								   and fa_genox_zdb_id = genox_zdb_id
								   
							  )::lvarchar(1000),11),""),"'}",""),"'","")

 where fas_genotype_group is null;

insert into genotype_group (gg_geno_name)
 select distinct geno_display_name from genotype;

update genotype_group
  set gg_group_name = replace(replace(replace(substr(multiset (select item geno_Zdb_id
      		      						   from genotype
								   where geno_handle = gg_geno_handle 
							  )::lvarchar(1000),11),""),"'}",""),"'","");

insert into genotype_group_member (ggm_group_id, ggm_member_name, ggm_member_id)
  select gg_group_pk_id, geno_Display_name, geno_Zdb_id
   from genotype_Group, genotype
   where geno_handle = gg_geno_handle;

select max(octet_length(fa_geno_handle))
  from functional_annotation;

!echo "max length for fas_all";

select max(octet_length(fas_all))
 from fish_annotation_search;

!echo "max length for fa_all" ;

select max(octet_length(fa_all))
 from functional_annotation;

update fish_annotation_Search
  set fas_all_with_spaces = fas_all;

-- because JDBC won't escape a :, replace with a $
update fish_annotation_search 
 set fas_all = replace(fas_all,':','$');

update fish_annotation_search 
 set fas_all = replace(fas_all,'|',' ');

update fish_annotation_search 
  set fas_all = replace(fas_all,',',' ');


update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'\',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'+',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'-',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,':',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'(',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,')',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'[',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,']',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'?',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'~',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'.',' ');

update fish_annotation_search 
 set fas_all_with_spaces = replace(fas_all_with_spaces,'|',' ');

update fish_annotation_search 
  set fas_all_with_spaces = replace(fas_all_with_spaces,',',' ');

update fish_annotation_Search
  set fas_all = fas_all||" "||fas_all_with_spaces
  where fas_all is not null
and fas_all_with_spaces is not null;

update fish_annotation_search 
  set fas_pheno_term_group = replace(fas_pheno_term_group,","," ");

update fish_annotation_search
  set fas_feature_order = 'zzzzzzzzzzzzzzz'
 where faS_feature_order is null;

update fish_annotation_search
  set fas_gene_order = 'zzzzzzzzzzzzzzz'
 where faS_gene_order is null;


update fish_annotation_search
  set fas_affector_group = fas_feature_group
 where fas_feature_group is not null;

update fish_annotation_search
  set fas_affector_group = fas_morpholino_group
 where fas_morpholino_group is not null
 and fas_feature_group is null
 and fas_affector_group is null;

update fish_annotation_search
  set fas_affector_group = fas_affector_Group||","||fas_morpholino_group
 where fas_morpholino_group is not null
 and fas_feature_group is not null;


update fish_annotation_search
  set fas_affector_order = fas_feature_order
 where fas_feature_order is not null;

update fish_annotation_search
  set fas_affector_order = fas_morpholino_group
 where fas_morpholino_group is not null
 and fas_feature_order is null
 and fas_affector_order is null;

update fish_annotation_search
  set fas_affector_order = fas_affector_order||", zzzzzzzzzzzz, "||fas_morpholino_group
 where fas_morpholino_group is not null
 and fas_feature_order is not null;

update fish_annotation_search
  set fas_affector_order = 'zzzzzzzzzzzzz'
 where faS_affector_order is null;

update fish_annotation_Search
 set fas_affector_Type_group = fas_affector_Type_group||", morpholino"
 where fas_morpholino_group is not null
and fas_affector_type_group is not null;

update fish_annotation_Search
 set fas_affector_Type_group = "morpholino"
 where fas_morpholino_group is not null
and fas_feature_group is null;

update fish_annotation_search
  set fas_all = lower(fas_all)
 where fas_all is not null;

update fish_annotation_search
  set fas_pheno_term_group = lower(fas_pheno_term_group);

update fish_annotation_search
  set fas_affector_type_group = lower(fas_affector_type_group);


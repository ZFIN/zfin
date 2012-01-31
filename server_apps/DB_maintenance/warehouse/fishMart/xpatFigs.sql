
!echo "XPAT FIGURE";

update statistics high for table functional_annotation;

set pdqpriority 80;

insert into xpat_figure_group (xfigg_genox_zdb_id, xfigg_geno_handle)
  select distinct xpatex_genox_zdb_id, geno_handle from expression_Experiment, experiment, genotype, genotype_Experiment
  where exp_name != '_Standard'
  and exp_name != '_Generic-control'
  and genox_zdb_id = xpatex_genox_zdb_id
  and exp_zdb_id = genox_exp_zdb_id
  and genox_geno_zdb_id = geno_zdb_id
 and geno_is_wildtype = 't';

update statistics high for table xpat_figure_group;

select distinct xpatfig_fig_Zdb_id, xfigg_genox_zdb_id
  from expression_pattern_Figure, expression_Result, expression_experiment, xpat_Figure_group
  where xpatres_zdb_id = xpatfig_xpatres_zdb_id
  and xpatres_xpatex_zdb_id = xpatex_zdb_id
  and xpatex_genox_zdb_id = xfigg_genox_zdb_id
into temp tmp_xpat;

create index xfig_genox_index
  on tmp_xpat (xfigg_genox_zdb_id)
  using btree in idxdbs1;

select count(*) as counter, xfigg_genox_zdb_id
  from tmp_xpat
  group by xfigg_genox_Zdb_id
   into temp tmp_count;

select count (distinct xpatfig_fig_zdb_id)
  from expression_pattern_figure, expression_Result, expression_experiment
  where xpatex_zdb_id = xpatres_xpatex_zdb_id
  and xpatres_zdb_id = xpatfig_xpatres_zdb_id
 and xpatex_genox_Zdb_id = 'ZDB-GENOX-041102-1429';

select * 
  from tmp_count
 where counter=29425;


select * from experiment, genotype_Experiment, genotype
 where genox_exp_zdb_id = exp_zdb_id
 and geno_zdb_id = genox_geno_zdb_id
and genox_zdb_id = 'ZDB-GENOX-041102-1429';

--set explain on avoid_Execute;
update xpat_figure_group
  set xfigg_group_name = replace(replace(replace(substr(multiset  (select item xpatfig_Fig_Zdb_id from tmp_xpat
      		       	 					 	 	  where tmp_xpat.xfigg_genox_zdb_id = xpat_figure_group.xfigg_genox_zdb_id
										  )::lvarchar,11),""),"'}",""),"'","");

--set explain off;
insert into xpat_figure_group_member (xfiggm_group_id, xfiggm_member_name, xfiggm_member_id)
  select distinct xfigg_group_pk_id, fig_label, xpatfig_fig_zdb_id
    from xpat_figure_group, expression_experiment, expression_result, expression_pattern_figure, figure
    where xpatex_genox_zdb_id = xfigg_genox_zdb_id
    and xpatex_zdb_id = xpatres_xpatex_zdb_id
    and xpatres_zdb_id = xpatfig_xpatres_zdb_id
    and fig_zdb_id = xpatfig_fig_zdb_id;

update functional_annotation
  set fa_xpat_figure_group = (select xfigg_group_name 
      			      	      from xpat_figure_group 
				      where fa_genox_zdb_id = xfigg_genox_zdb_id);


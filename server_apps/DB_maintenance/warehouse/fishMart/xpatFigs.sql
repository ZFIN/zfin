
!echo "XPAT FIGURE";

--update statistics high for table functional_annotation;


insert into xpat_figure_group (xfigg_genox_zdb_id)
  select distinct xpatex_genox_zdb_id
  from expression_pattern_Figure, expression_Result, expression_experiment
  where xpatres_zdb_id = xpatfig_xpatres_zdb_id
  and xpatex_gene_zdb_id is not null
  and xpatex_zdb_id = xpatres_Xpatex_zdb_id
 and not exists (Select 'x' from genotype,genotype_Experiment where geno_is_wildtype = 't'
     	 		and genox_geno_zdb_id = geno_zdb_id
			and genox_Zdb_id = xpatex_genox_zdb_id
			and genox_is_std_or_generic_control = 't');

select count(*) from xpat_figure_group 
where exists (Select 'x' from genotype_experiment where genox_zdb_id = xfigg_genox_zdb_id and genox_is_std_or_Generic_control ='t');



--update statistics high for table xpat_figure_group;

select distinct xpatfig_fig_Zdb_id, xpatex_genox_zdb_id
  from expression_pattern_Figure, expression_Result, expression_experiment
  where xpatres_zdb_id = xpatfig_xpatres_zdb_id
  and xpatex_gene_zdb_id is not null
  and xpatres_xpatex_zdb_id = xpatex_zdb_id
 and not exists (Select 'x' from genotype,genotype_Experiment 
     	 		where geno_is_wildtype = 't'
     	 		and genox_geno_zdb_id = geno_zdb_id
			and genox_Zdb_id = xpatex_genox_zdb_id
			and genox_is_std_or_generic_control = 't')
into temp tmp_xpat;

create index xfig_genox_index
  on tmp_xpat (xpatex_genox_zdb_id)
  using btree in idxdbs1;

--set explain on avoid_Execute;
update xpat_figure_group
  set xfigg_group_name = replace(replace(replace(substr(multiset  (select item xpatfig_Fig_Zdb_id from tmp_xpat
      		       	 					 	 	  where tmp_xpat.xpatex_genox_zdb_id = xpat_figure_group.xfigg_genox_zdb_id
										  )::lvarchar,11),""),"'}",""),"'","");

--set explain off;
insert into xpat_figure_group_member (xfiggm_group_id, xfiggm_member_name, xfiggm_member_id)
  select distinct xfigg_group_pk_id, fig_label, xpatfig_fig_zdb_id
    from xpat_figure_group, expression_experiment, expression_result, expression_pattern_figure, figure
    where xpatex_genox_zdb_id = xfigg_genox_zdb_id
    and xpatex_gene_zdb_id is not null
    and xpatex_zdb_id = xpatres_xpatex_zdb_id
    and xpatres_zdb_id = xpatfig_xpatres_zdb_id
    and fig_zdb_id = xpatfig_fig_zdb_id
    and not exists (Select 'x' from genotype,genotype_Experiment where geno_is_wildtype = 't'
     	 		and genox_geno_zdb_id = geno_zdb_id
			and genox_Zdb_id = xpatex_genox_zdb_id
			and genox_is_std_or_generic_control = 't');

update functional_annotation
  set fa_xpat_figure_group = (select xfigg_group_name 
      			      	      from xpat_figure_group 
				      where fa_genox_zdb_id = xfigg_genox_zdb_id);

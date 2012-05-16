
!echo "start populate figure term xpat fish search";
delete from figure_term_xpat_fish_search_temp;

select distinct geno_handle, xpatex_genox_zdb_id, xpatfig_fig_zdb_id, alltermcon_container_zdb_id as term
  from expression_experiment, expression_Result, all_term_contains, genotype, genotype_experiment, expression_pattern_figure
  where xpatex_zdb_id = xpatres_xpatex_zdb_id
  and xpatres_zdb_id = xpatfig_xpatres_zdb_id
  and genox_geno_Zdb_id = geno_Zdb_id
  and genox_zdb_id = xpatex_genox_Zdb_id
  and genox_geno_zdb_id = geno_Zdb_id
  and genox_zdb_id = phenox_genox_zdb_id
  and alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
  and xpatres_superterm_zdb_id is not null
 union
select distinct geno_handle, xpatex_genox_zdb_id, xpatfig_fig_zdb_id, alltermcon_container_zdb_id as term
  from expression_experiment, expression_Result, all_term_contains, genotype, genotype_Experiment, expression_pattern_figure
  where xpatex_zdb_id = xpatres_xpatex_zdb_id
  and genox_geno_zdb_id = geno_Zdb_id
  and genox_zdb_id = phenox_genox_zdb_id
  and alltermcon_contained_zdb_id = xpatres_subterm_zdb_id
  and xpatres_subterm_zdb_id is not null
  and xpatres_zdb_id = xpatfig_xpatres_zdb_id
into temp tmp_xpat;

create index geno_name_index
  on tmp_xpat(geno_handle)
 using btree in idxdbs1;

create index genox_index
  on tmp_xpat(xpatex_genox_zdb_id)
 using btree in idxdbs3;

create index fig_index
  on tmp_xpat(xpatfig_fig_zdb_id)
 using btree in idxdbs2;

update statistics high for table tmp_phenox;

insert into figure_term_xpat_fish_search_temp (ftxfs_fas_id, ftxfs_geno_handle, ftxfs_fig_zdb_id, ftxfs_genox_zdb_id)
 select distinct  fas_pk_id, fas_geno_handle, xpat_fig_Zdb_id, xpat_genox_zdb_id
    from fish_annotation_search_temp, expression_Experiment, functional_annotation
    where fas_geno_handle = fa_geno_handle
    and xpatex_genox_zdb_id = fa_genox_zdb_id;


update statistics high for table figure_term_xpat_fish_search_temp;


--set explain on avoid_execute;
update figure_term_xpat_fish_search_temp
  set ftxfs_term_group = replace(replace(replace(substr(multiset (select distinct item term 
      		      						   from tmp_xpat, functional_annotation
								   where ftxfs_geno_handle = fa_geno_handle
								   and fa_genox_zdb_id = xpatex_genox_zdb_id
								   and ftxfs_fig_zdb_id = xpatfig_fig_zdb_id
							  )::lvarchar(3000),11),""),"'}",""),"'","");

update figure_term_xpat_fish_search_temp set ftxfs_term_group = replace(ftxfs_term_group,","," ");


update figure_term_xpat_fish_search_temp
 set ftxfs_term_group = lower(ftxfs_term_group);

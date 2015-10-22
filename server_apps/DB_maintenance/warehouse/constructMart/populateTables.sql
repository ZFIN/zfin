create temp table tmp_ordered_markers (name varchar(100), construct_id varchar(50))
with no log;

insert into tmp_ordered_markers (name, construct_id)
  select mrkr_abbrev||"|"||mrkr_name, mrkr_Zdb_id
     from marker
     where mrkr_type in ('PTCONSTRCT','ETCONSTRCT','TGCONSTRCT','GTCONSTRCT');

insert into tmp_ordered_markers (name, construct_id)
  select a.mrkr_abbrev||"|"||a.mrkr_name, b.mrkr_zdb_id
    from marker a, marker b, marker_relationship
    where a.mrkr_zdb_id = mrel_mrkr_2_zdb_id
    and b.mrkr_Zdb_id = mrel_mrkr_1_zdb_id
    and mrel_type in ('promoter of','coding sequence of','contains engineered region'); 

insert into tmp_ordered_markers (name, construct_id)
  select dalias_alias, mrkr_zdb_id
    from marker, data_alias
    where mrkr_Zdb_id = dalias_data_zdb_id
    and mrkr_type in ('PTCONSTRCT','ETCONSTRCT','TGCONSTRCT','GTCONSTRCT');

insert into tmp_ordered_markers (name, construct_id)
   select dalias_alias, b.mrkr_zdb_id
   from data_alias, marker a, marker b, marker_relationship
     where a.mrkr_zdb_id = mrel_mrkr_2_zdb_id
    and b.mrkr_Zdb_id = mrel_mrkr_1_zdb_id
    and b.mrkr_type in ('PTCONSTRCT','ETCONSTRCT','TGCONSTRCT','GTCONSTRCT')
    and a.mrkr_zdb_id = dalias_data_zdb_id;

--insert into tmp_ordered_markers (name, construct_id)
--  select feature_name||"|"||feature_abbrev, fmrel_mrkr_zdb_id
--    from feature, feature_marker_relationship
--    where feature_zdb_id = fmrel_ftr_zdb_id
--    and fmrel_type in ('contains innocuous sequence feature','contains phenotypic sequence feature')
--    and fmrel_mrkr_zdb_id like '%CONSTRCT%';

--insert into tmp_ordered_markers (name, construct_id)
--  select dalias_Alias, fmrel_mrkr_zdb_id
--    from feature, feature_marker_relationship, data_alias
--    where feature_zdb_id = fmrel_ftr_zdb_id
--    and fmrel_ftr_Zdb_id = dalias_data_zdb_id	
--    and fmrel_type in ('contains innocuous sequence feature','contains phenotypic sequence feature')
--    and fmrel_mrkr_zdb_id like '%CONSTRCT%';

insert into tmp_ordered_markers (name, construct_id)
  select allnmend_name_end_lower, mrkr_zdb_id
    from all_name_ends, all_map_names, marker
    where mrkr_zdb_id = allmapnm_zdb_id
    and allnmend_allmapnm_serial_id = allmapnm_serial_id
    and mrkr_type in ('PTCONSTRCT','ETCONSTRCT','TGCONSTRCT','GTCONSTRCT')
    and allmapnm_precedence not in ('Accession number','Ortholog','Sequence similarity');

insert into construct_search_temp (cons_construct_zdb_id, cons_abbrev_order, cons_name, cons_abbrev)
  select mrkr_Zdb_id, mrkr_abbrev_order, mrkr_name, mrkr_abbrev from marker where mrkr_type in ('PTCONSTRCT','ETCONSTRCT','TGCONSTRCT','GTCONSTRCT');

update construct_search_temp
  set cons_type = 'Gene Trap Construct'
 where cons_construct_zdb_id like 'ZDB-GT%';

update construct_search_temp
  set cons_type = 'Promoter Trap Construct'
 where cons_construct_zdb_id like 'ZDB-PT%';

update construct_search_temp
  set cons_type = 'Enhancer Trap Construct'
 where cons_construct_zdb_id like 'ZDB-ET%';

update construct_search_temp
  set cons_type = 'Transgenic Construct'
 where cons_construct_zdb_id like 'ZDB-TG%';

create index tmp_om_construct_id_index 
  on tmp_ordered_markers (construct_id)
  using btree in idxdbs2;

update statistics high for table construct_search_temp;

select count(*), construct_id
  from tmp_ordered_markers
  group by construct_id
 having count(*) > 400; 

update construct_search_temp
  set cons_all_names = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where cons_construct_zdb_id = construct_id
							  order by 1
							  )::lvarchar(4000),11),""),"'}",""),"'","")
;

delete from tmp_ordered_markers;

--do we need gene names that are "is allele of relations to TG features?"
--select distinct a.mrkr_name||"|"||a.mrkr_abbrev, b.mrkr_Zdb_id
-- from feature_marker_relationship 1, marker a, marker b, feature, feature_marker_relationship 2
-- where 1.fmrel_mrkr_zdb_id = a.mrkr_Zdb_id
   -- and 2.fmrel_ftr_zdb_id = 1.fmrel_ftr_zdb_id
  -- and 2.fmrel_type like 'contains%'
  -- and 2.fmrel_mrkr_zdb_id = b.mrkr_zdb_id
  -- and 2.fmrel_mrkr_Zdb_id like '%CONSTRCT%'
--   and 1.fmrel_ftr_Zdb_id = feature_zdb_id
--   and 1.fmrel_type like 'is allele of'
--   and feature_type like 'TRANSGENIC%';

insert into construct_gene_feature_result_view_temp (cgfrv_cs_id, cgfrv_feature_zdb_id, cgfrv_feature_name, cgfrv_feature_abbrev, cgfrv_feature_order)
  select distinct cons_pk_id, feature_zdb_id, feature_name, feature_abbrev, feature_abbrev_order
    from feature, feature_marker_relationship, construct_search_temp
    where feature_zdb_id = fmrel_ftr_zdb_id
    and cons_construct_zdb_id = fmrel_mrkr_Zdb_id
    and feature_type = 'TRANSGENIC_INSERTION'
    ;

select * from construct_gene_feature_result_view_temp
 where cgfrv_feature_abbrev = 'ct47aGt';

--insert into construct_gene_feature_result_view_temp (cgfrv_cs_id, cgfrv_feature_zdb_id, cgfrv_feature_name, cgfrv_feature_abbrev, cgfrv_gene_zdb_id, cgfrv_gene_abbrev, cgfrv_relationship_type)
--  select cons_pk_id, feature_zdb_id, feature_name, feature_abbrev, mrkr_zdb_id, mrkr_abbrev,  b.fmrel_type
--    from feature, feature_marker_relationship a, construct_search_temp, feature_marker_relationship b, marker
--    where feature_zdb_id = b.fmrel_ftr_zdb_id
--    and b.fmrel_type = 'is allele of'
--    and b.fmrel_mrkr_zdb_id = mrkr_zdb_id
--    and a.fmrel_ftr_zdb_id = feature_zdb_id
--    and b.fmrel_mrkr_zdb_id != a.fmrel_mrkr_zdb_id
--    and a.fmrel_type like 'contains%'
--    and a.fmrel_mrkr_zdb_id like '%CONSTRCT%'
--    and b.fmrel_mrkr_zdb_id not like '%CONSTRCT%'
--    and cons_construct_zdb_id = a.fmrel_mrkr_zdb_id;

update construct_gene_feature_result_view_temp
  set cgfrv_allele_gene_zdb_id = (Select mrkr_Zdb_id
      			  		       	       from marker, feature, feature_marker_relationship
						       where feature_zdb_id = cgfrv_feature_zdb_id
						       and fmrel_Ftr_zdb_id = feature_zdb_id
						       and fmrel_mrkr_zdb_id = mrkr_Zdb_id
						       and fmrel_type = 'is allele of');

update construct_gene_feature_result_view_temp
  set cgfrv_allele_gene_order = (select mrkr_abbrev_order from marker where mrkr_zdb_id = cgfrv_allele_gene_zdb_id);

update construct_gene_feature_result_view_temp
  set cgfrv_allele_gene_abbrev = (Select mrkr_abbrev
      			  		       	       from marker, feature, feature_marker_relationship
						       where feature_zdb_id = cgfrv_feature_zdb_id
						       and fmrel_Ftr_zdb_id = feature_zdb_id
						       and fmrel_mrkr_zdb_id = mrkr_Zdb_id
						       and fmrel_type = 'is allele of');

select * from construct_gene_feature_result_view_temp
 where cgfrv_feature_abbrev = 'ct47aGt';


update construct_gene_feature_result_view_temp
  set cgfrv_lab_of_origin = (Select distinct ids_source_zdb_id from int_data_source
      			    	    where ids_data_zdb_id = cgfrv_feature_zdb_id);


update construct_gene_feature_result_view_temp
  set cgfrv_lab_name = (select name from lab
      		       	       where zdb_id = cgfrv_lab_of_origin);

update construct_gene_feature_result_view_temp
  set cgfrv_available = (select distinct idsup_supplier_zdb_id from int_data_supplier
  	       	       where idsup_data_zdb_id = cgfrv_feature_zdb_id
		       and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53');



update construct_gene_feature_result_view_temp
  set cgfrv_allele_gene_all_names = replace(replace(replace(substr(multiset (select distinct 
						  	  item allnmend_name_end_lower
							  from  all_map_names, all_name_ends
							  where cgfrv_allele_gene_zdb_id = allmapnm_zdb_id
							  and allnmend_allmapnm_serial_id = allmapnm_serial_id
							      and allmapnm_precedence not in ('Accession number','Ortholog','Sequence similarity')
							  )::lvarchar,11),""),"'}",""),"'","");


update construct_gene_feature_result_view_temp
 set cgfrv_allele_gene_all_names = replace (cgfrv_allele_gene_all_names, ",", " ");

insert into construct_component_search_temp (ccs_cons_id, ccs_gene_zdb_id, ccs_gene_abbrev, ccs_gene_abbrev_order, ccs_relationship_type)
  select distinct cons_pk_id, mrkr_zdb_id, mrkr_abbrev, mrkr_abbrev_order, mrel_type
    from construct_search_temp, marker_relationship, marker
    where cons_construct_zdb_id  = mrel_mrkr_1_zdb_id
    and mrel_mrkr_2_zdb_id = mrkr_zdb_id;

update construct_component_search_temp
  set ccs_promoter_all_names = replace(replace(replace(substr(multiset (select distinct 
						  	  item allnmend_name_end_lower
							  from  all_map_names, all_name_ends
							  where  ccs_gene_zdb_id = allmapnm_zdb_id
							  and ccs_relationship_type = 'promoter of'
							  and allnmend_allmapnm_serial_id = allmapnm_serial_id
   and allmapnm_precedence not in ('Accession number','Ortholog','Sequence similarity')
							  order by 1
							  )::lvarchar,11),""),"'}",""),"'","");

update construct_component_search_temp
 set ccs_promoter_all_names = replace (ccs_promoter_all_names, ",", " ");



update construct_component_search_temp
  set ccs_coding_all_names = replace(replace(replace(substr(multiset (select distinct 
						  	  item allnmend_name_end_lower
							  from  all_map_names, all_name_ends
							  where ccs_gene_zdb_id = allmapnm_zdb_id
							  and ccs_relationship_type = 'coding sequence of'
							  and allnmend_allmapnm_serial_id = allmapnm_serial_id
							      and allmapnm_precedence not in ('Accession number','Ortholog','Sequence similarity')
							  order by 1
							  )::lvarchar,11),""),"'}",""),"'","");

update construct_component_Search_temp
 set ccs_coding_all_names = replace (ccs_coding_all_names, ",", " ");

update construct_component_search_Temp
  set ccs_engineered_region_all_names = replace(replace(replace(substr(multiset (select distinct 
						  	  item allnmend_name_end_lower
							  from  all_map_names, all_name_ends
							  where ccs_gene_zdb_id = allmapnm_zdb_id
							  and ccs_relationship_type = 'contains engineered region'
							  and allnmend_allmapnm_serial_id = allmapnm_serial_id
							      and allmapnm_precedence not in ('Accession number','Ortholog','Sequence similarity')
							  order by 1
							  )::lvarchar,11),""),"'}",""),"'","");

update construct_component_search_temp
 set ccs_engineered_region_all_names = replace (ccs_engineered_region_all_names, ",", " ");

-- because JDBC won't escape a :, replace with a $
update construct_search_temp 
 set cons_all_names = replace(cons_all_names,':','$');

update construct_search_temp 
 set cons_all_names = replace(cons_all_names,'|',' ');

update construct_search_temp 
  set cons_all_names = replace(cons_all_names,',',' ');

update construct_search_temp
  set cons_all_with_spaces = cons_all_names;

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,'\',' ');

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,'+',' ');

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,'-',' ');

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,':',' ');

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,'(',' ');

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,')',' ');

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,'[',' ');

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,']',' ');

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,'?',' ');

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,'~',' ');

update construct_search_temp
set cons_all_with_spaces = replace(cons_all_with_spaces,'.',' ');

update construct_search_temp 
 set cons_all_with_spaces = replace(cons_all_with_spaces,'|',' ');

update construct_search_temp 
  set cons_all_with_spaces = replace(cons_all_with_spaces,',',' ');


update construct_search_temp
  set cons_all_names = cons_all_names||" "||cons_all_with_spaces;


insert into figure_term_construct_search_temp (ftcs_cs_id, ftcs_fig_zdb_id, ftcs_genox_zdb_id, ftcs_geno_handle, ftcs_geno_name)
  select distinct cons_pk_id, xpatfig_fig_zdb_id, genox_zdb_id, geno_handle, geno_display_name
     from construct_search_temp, feature_marker_relationship, fish_experiment,
     	  expression_Experiment, expression_Result, expression_pattern_figure, genotype_feature, marker, genotype, fish
     where cons_construct_zdb_id = fmrel_mrkr_zdb_id
     and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
     and genofeat_geno_zdb_id = fish_genotype_Zdb_id
     and genox_fish_zdb_id = fish_Zdb_id
     and fish_genotype_Zdb_id = geno_zdb_id
     and genox_zdb_id = xpatex_genox_Zdb_id
     and genox_is_std_or_generic_control = 't'
     and xpatex_zdb_id =xpatres_xpatex_zdb_id
     and xpatres_zdb_id = xpatfig_xpatres_zdb_id
     and xpatex_gene_Zdb_id = mrkr_Zdb_id
     and mrkr_type = 'EFG';

create temp table tmp_anat (fig_id varchar(50), anat_id varchar(50), genox_id varchar(50))
with no log;

insert into tmp_anat (fig_id, anat_id, genox_id)
    select distinct xpatfig_fig_zdb_id, alltermcon_container_zdb_id, ftcs_genox_zdb_id
     from all_Term_contains, expression_result, expression_pattern_figure, expression_experiment,figure_term_construct_search_temp, term, marker, fish_experiment
     where alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
     and xpatex_zdb_id = xpatres_xpatex_zdb_id
     and xpatres_zdb_id = xpatfig_xpatres_zdb_id
     and ftcs_genox_zdb_id = xpatex_genox_zdb_id
     and genox_zdb_id = xpatex_genox_Zdb_id
     and genox_is_std_or_generic_control = 't'
     and ftcs_fig_zdb_id = xpatfig_fig_zdb_id
     and xpatres_expression_found = 't'
     and mrkr_type = 'EFG'
     and mrkr_Zdb_id = xpatex_gene_zdb_id
and term_zdb_id = alltermcon_container_zdb_id;


insert into tmp_anat (fig_id, anat_id, genox_id)
    select distinct xpatfig_fig_zdb_id, alltermcon_container_zdb_id, ftcs_genox_zdb_id
     from all_Term_contains, expression_result, expression_pattern_figure, expression_experiment,figure_term_construct_search_temp, term, marker, fish_Experiment
     where alltermcon_contained_zdb_id = xpatres_subterm_zdb_id
     and xpatex_zdb_id = xpatres_xpatex_zdb_id
     and xpatres_zdb_id = xpatfig_xpatres_zdb_id
     and ftcs_genox_zdb_id = xpatex_genox_zdb_id
     and ftcs_fig_zdb_id = xpatfig_fig_zdb_id
     and xpatres_expression_found = 't'
     and genox_zdb_id = xpatex_genox_Zdb_id
     and genox_is_std_or_generic_control = 't'
     and mrkr_type = 'EFG'
     and mrkr_Zdb_id = xpatex_gene_zdb_id
     and term_zdb_id = alltermcon_container_zdb_id;

--and term_zdb_id = alltermcon_container_zdb_id;

create index genox_id_index on tmp_anat (genox_id)
 using btree in idxdbs2;

create index fig_id_index on tmp_anat (fig_id) 
 using btree in idxdbs1;

update figure_term_construct_search_temp 
  set ftcs_term_group = replace(replace(replace(substr(multiset (select distinct 
						  	  item anat_id from tmp_anat
							  where ftcs_fig_zdb_id = fig_id
							  and ftcs_genox_zdb_id = genox_id
							  order by 1
							  )::lvarchar(4000),11),""),"'}",""),"'","")
;

update figure_term_construct_search_temp
  set ftcs_term_group = replace(ftcs_term_group,","," ");

update figure_term_construct_search_temp
  set ftcs_term_group = lower(ftcs_term_group);


update construct 
  set construct_name = get_construct_name(construct_zdb_id);

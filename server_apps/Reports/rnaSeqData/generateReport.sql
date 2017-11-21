begin work;


create temp table tmp_report (start_stage_zdb_id varchar(50),
       	    	  	      start_stage_zfs_id varchar(50),
			      start_stage_name_long varchar(100),
			      gene_zdb_id varchar(50),
			      gene_symbol varchar(255),
			      structure_name varchar(255),
			      structure_zdb_id varchar(50),
			      structure_ont_id varchaR(50),
			      assay varchar(255))
with no log;


create temp table tmp_report_pre (start_stage_zdb_id varchar(50),
			      gene_zdb_id varchar(50),
			      structure_zdb_id varchar(50),
			      assay varchar(255))
with no log;

create temp table tmp_report_distinct (start_stage_zdb_id varchar(50),
			      gene_zdb_id varchar(50),
			      structure_zdb_id varchar(50),
			      assay varchar(255))
with no log;





insert into tmp_report_pre (start_stage_zdb_id, gene_zdb_id, structure_zdb_id, assay)
select  efs_start_stg_zdb_id, xpatex_gene_zdb_id, xpatres_superterm_zdb_id, xpatex_assay_name
  from expression_experiment2, expression_figure_stage, expression_result2,  clean_expression_fast_search
  where xpatex_zdb_id = efs_xpatex_zdb_id
  and efs_pk_id = xpatres_efs_id
  and xpatres_expression_found = 't'
  and cefs_genox_zdb_id = xpatex_genox_zdb_id
  and cefs_mrkr_zdb_id = xpatex_gene_zdb_id
 and xpatres_superterm_zdb_id is not null;

insert into tmp_report_pre (start_stage_zdb_id, gene_zdb_id, structure_zdb_id, assay)
select   efs_start_stg_zdb_id, xpatex_gene_zdb_id, xpatres_subterm_zdb_id, xpatex_assay_name
  from expression_experiment2, expression_figure_stage, expression_result2, clean_expression_fast_search
  where xpatex_zdb_id = efs_xpatex_zdb_id
  and efs_pk_id = xpatres_efs_id
  and xpatres_expression_found = 't'
  and cefs_genox_zdb_id = xpatex_genox_zdb_id
  and cefs_mrkr_zdb_id = xpatex_gene_zdb_id
  and xpatres_subterm_zdb_id is not null
;


insert into tmp_report_distinct (start_stage_zdb_id, gene_zdb_id, structure_zdb_id, assay)
select distinct start_stage_zdb_id, gene_zdb_id, structure_zdb_id, assay
  from tmp_report_pre;

create index structure_index on tmp_report_distinct (structure_zdb_id)
using btree in idxdbs1;

update statistics high; 

--set explain on avoid_execute;

insert into tmp_report (start_stage_zdb_id, gene_zdb_id, structure_zdb_id, assay)
select distinct start_stage_zdb_id, gene_zdb_id, alltermcon_container_zdb_id, assay
 from tmp_report_distinct, all_term_contains
where structure_zdb_id = alltermcon_contained_zdb_id;

update tmp_report 
 set start_stage_zfs_id = (select stg_obo_id
     			  	  from stage where stg_zdb_id = start_stage_zdb_id);


update tmp_report 
 set start_stage_name_long = (select stg_name_long
     			  	  from stage where stg_zdb_id = start_stage_zdb_id);

update tmp_report
 set gene_symbol = (select mrkr_abbrev
     		   	   from marker where mrkr_zdb_id = gene_zdb_id);

update tmp_report
 set structure_name = (select term_name
     		   	   from term where term_zdb_id = structure_zdb_id);

update tmp_report
 set structure_ont_id = (select term_ont_id
     		   	   from term where term_zdb_id = structure_zdb_id);

unload to report.unl
 select * from tmp_report
  order by start_stage_zdb_id, gene_symbol, structure_name;

unload to counts.unl
select count(distinct structure_zdb_id), start_stage_zdb_id, start_stage_name_long
  from tmp_report
group by start_stage_zdb_id, start_stage_name_long;

--commit work;

rollback work;

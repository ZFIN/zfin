begin work;


create temp table tmp_report (start_stage_zdb_id varchar(50),
			      end_stage_zdb_id varchar(50),
			      end_stage_hours int,
			      start_stage_hours int,
       	    	  	      start_stage_zfs_id varchar(50),
			      start_stage_name_long varchar(100),
			      is_direct boolean default 'f',
			      gene_zdb_id varchar(50),
			      gene_symbol varchar(255),
			      structure_name varchar(255),
			      structure_zdb_id varchar(50),
			      structure_ont_id varchaR(50),
			      assay varchar(255))
with no log;


create temp table tmp_report_pre (is_direct boolean default 'f',
					start_stage_zdb_id varchar(50),
				end_stage_zdb_id varchar(50),
			      start_stage_hours int,
			      end_stage_hours int,
			      gene_zdb_id varchar(50),
			      structure_zdb_id varchar(50),
			      assay varchar(255))
with no log;

create temp table tmp_report_distinct (is_direct boolean default 'f',
					start_stage_zdb_id varchar(50),
					end_stage_zdb_id varchar(50),
			         start_stage_hours int,
				 end_stage_hours int,
			 	gene_zdb_id varchar(50),
			      structure_zdb_id varchar(50),
			      assay varchar(255))
with no log;





insert into tmp_report_pre (is_direct, start_stage_zdb_id, end_stage_zdb_id,gene_zdb_id, structure_zdb_id, assay)
select  't',efs_start_stg_zdb_id, efs_end_stg_zdb_id, xpatex_gene_zdb_id, xpatres_superterm_zdb_id, xpatex_assay_name
  from expression_experiment2, expression_figure_stage, expression_result2,  clean_expression_fast_search, marker
  where xpatex_zdb_id = efs_xpatex_zdb_id
  and efs_pk_id = xpatres_efs_id
  and xpatres_expression_found = 't'
  and cefs_genox_zdb_id = xpatex_genox_zdb_id
  and cefs_mrkr_zdb_id = xpatex_gene_zdb_id
 and xpatres_superterm_zdb_id is not null
and mrkr_zdb_id = xpatex_gene_zdb_id
and mrkr_name not like 'WITHDRAWN%';

insert into tmp_report_pre (is_direct, start_stage_zdb_id, end_stage_zdb_id, gene_zdb_id, structure_zdb_id, assay)
select  't', efs_start_stg_zdb_id, efs_end_stg_zdb_id, xpatex_gene_zdb_id, xpatres_subterm_zdb_id, xpatex_assay_name
  from expression_experiment2, expression_figure_stage, expression_result2, clean_expression_fast_search, marker
  where xpatex_zdb_id = efs_xpatex_zdb_id
  and efs_pk_id = xpatres_efs_id
  and xpatres_expression_found = 't'
  and cefs_genox_zdb_id = xpatex_genox_zdb_id
  and cefs_mrkr_zdb_id = xpatex_gene_zdb_id
  and xpatres_subterm_zdb_id is not null
and mrkr_zdb_id = xpatex_gene_zdb_id
and mrkr_name not like 'WITHDRAWN%';


insert into tmp_report_distinct (is_direct, start_stage_zdb_id, end_Stage_zdb_id,gene_zdb_id, structure_zdb_id, assay)
select distinct is_direct, start_stage_zdb_id, end_stage_zdb_id, gene_zdb_id, structure_zdb_id, assay
  from tmp_report_pre;

create index structure_index on tmp_report_distinct (structure_zdb_id)
using btree in idxdbs1;

update statistics high; 

--set explain on avoid_execute;

insert into tmp_report (is_direct,start_stage_zdb_id, end_stage_zdb_id, gene_zdb_id, structure_zdb_id, assay)
select distinct is_direct, start_stage_zdb_id, end_stage_zdb_id, gene_zdb_id, alltermcon_container_zdb_id, assay
 from tmp_report_distinct, all_term_contains,term
where structure_zdb_id = alltermcon_contained_zdb_id
and alltermcon_contained_zdb_id = term_zdb_id
and term_ont_id like 'ZFA:%'
;

select count(*) from tmp_report 
where is_direct = 't';

select count(*) from tmp_report
 where is_direct = 'f';

update tmp_report
  set end_stage_hours = (Select stg_hours_end from stage where stg_zdb_id = end_stage_zdb_id);

update tmp_report
  set start_stage_hours = (Select stg_hours_start from stage where stg_zdb_id = start_stage_zdb_id);

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

select stg_zdb_id, stg_obo_id, stg_name_long, stg_start_hours as start_stage_hours, stg_end_hours as end_stage_hours, gene_zdb_id, gene_symbol,
	structure_name, structure_zdb_id, structure_ont_id, assay
   from tmp_report, stage 
   where start_stage_hours >= stg_hours_start
   and end_stage_hours <= stg_hours_end
into temp tmp_all_stages_terms;

select a.stg_Zdb_id, a.stg_obo_id, a.stg_name_long, gene_zdb_id, gene_symbol,
	structure_name, structure_Zdb_id, structure_ont_id, assay, b.stg_hours_start, 
	b.stg_hours_end
  from tmp_all_stages_terms, stage a, term_stage, stage b, stage c
  where start_stage_hours >= a.stg_hours_start
    and end_stage_hours <=c.stg_hours_end
   and ts_term_zdb_id = structure_zdb_id
  and ts_start_stg_zdb_id = a.stg_zdb_id
  and ts_end_Stg_zdb_id = c.stg_zdb_id
  and b.stg_zdb_id = tmp_all_stage_terms.stg_zdb_id
into temp tmp_report_limited;




--commit work;

rollback work;

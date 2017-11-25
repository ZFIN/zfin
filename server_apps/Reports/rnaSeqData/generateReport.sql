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
 from tmp_report_distinct, all_term_contains,term, stage d, stage a, stage b, stage c, term_stage
where structure_zdb_id = alltermcon_contained_zdb_id
and alltermcon_contained_zdb_id = term_zdb_id
and term_ont_id like 'ZFA:%'
and start_stage_zdb_id = a.stg_zdb_id
and end_Stage_zdb_id = b.stg_zdb_id
and ts_term_zdb_id = term_zdb_id
and ts_start_stg_zdb_id = c.stg_zdb_id
and ts_end_stg_zdb_id = d.stg_zdb_id
and a.stg_hours_start >= c.stg_hours_start
and b.stg_hours_end <= d.stg_hours_end
and exists (Select 'x' from term_stage where ts_term_zdb_id = term_zdb_id);

select distinct is_direct, start_stage_zdb_id, a.stg_hours_start, end_stage_zdb_id,b.stg_hours_end,
 gene_zdb_id, alltermcon_container_zdb_id, assay, c.stg_hours_start as term_start, d.stg_hours_end as term_end
 from tmp_report_distinct, all_term_contains,term, stage d, stage a, stage b, stage c, term_stage
where structure_zdb_id = alltermcon_contained_zdb_id
and alltermcon_contained_zdb_id = term_zdb_id
and term_ont_id like 'ZFA:%'
and start_stage_zdb_id = a.stg_zdb_id
and end_Stage_zdb_id = b.stg_zdb_id
and ts_term_zdb_id = term_zdb_id
and ts_start_stg_zdb_id = c.stg_zdb_id
and ts_end_stg_zdb_id = d.stg_zdb_id
and gene_zdb_id = 'ZDB-GENE-000112-47'
and end_stage_zdb_id = 'ZDB-STAGE-010723-12';

select * from tmp_report
 where gene_zdb_id = 'ZDB-GENE-000112-47'
and end_stage_zdb_id ='ZDB-STAGE-010723-12';

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

select stg_zdb_id, stg_obo_id, stg_name_long, stg_hours_start as start_stage_hours, stg_hours_end as end_stage_hours, gene_zdb_id, gene_symbol,
	structure_name, structure_zdb_id, structure_ont_id, assay
   from tmp_report
--, stage 
--   where start_stage_hours >= stg_hours_start
--   and end_stage_hours <= stg_hours_end
into temp tmp_all_stages_terms;

select stg_Zdb_id as stg_zdb_id, stg_obo_id as stg_obo_id, stg_name_long as stg_name_long,
          gene_zdb_id, gene_symbol, start_stage_hours, structure_name, structure_zdb_id, structure_ont_id, assay
   from tmp_all_stages_terms
into temp tmp_report_limited;

--select a.stg_Zdb_id as stg_zdb_id, a.stg_obo_id as stg_obo_id, a.stg_name_long as stg_name_long,
--          gene_zdb_id, gene_symbol,
--	structure_name, structure_Zdb_id, structure_ont_id, assay, b.stg_hours_start as stage_start_hours, 
--	b.stg_hours_end
--  from tmp_all_stages_terms, stage a, term_stage, stage b, stage c
--  where start_stage_hours >= a.stg_hours_start
--    and end_stage_hours <=c.stg_hours_end
--   and ts_term_zdb_id = structure_zdb_id
--  and ts_start_stg_zdb_id = a.stg_zdb_id
--  and ts_end_Stg_zdb_id = c.stg_zdb_id
--  and b.stg_zdb_id = tmp_all_stages_terms.stg_zdb_id
--into temp tmp_report_limited;

unload to report.txt
select distinct stg_zdb_id, stg_obo_id, stg_name_long, gene_zdb_id, gene_symbol, structure_name, structure_zdb_id,
	structure_ont_id, assay
  from tmp_report_limited
  order by stg_zdb_id, gene_zdb_id, structure_name;

unload to report_by_gene.txt
select distinct gene_symbol, structure_name, stg_name_long, gene_zdb_id, stg_obo_id, stg_zdb_id,  structure_zdb_id,
        structure_ont_id, assay, start_stage_hours
  from tmp_report_limited
 order by gene_zdb_id, structure_name, start_stage_hours, stg_zdb_id;

unload to report_for_ppardb.txt
select distinct gene_symbol, structure_name, stg_name_long, gene_zdb_id, stg_obo_id, stg_zdb_id,  structure_zdb_id,
        structure_ont_id, assay, start_stage_hours
  from tmp_report_limited
  where gene_symbol = 'ppardb'
 order by gene_zdb_id, start_stage_hours, structure_name, stg_zdb_id;


--commit work;

rollback work;

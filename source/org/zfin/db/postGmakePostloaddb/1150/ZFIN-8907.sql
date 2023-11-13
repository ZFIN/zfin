--liquibase formatted sql
--changeset cmpich:ZFIN-8907.sql


-- list multiple expression_result2 records that point to a single expression_figure_stage for the same
-- key (group by
create view dupes as
select xpatres_efs_id, xpatres_expression_found, xpatres_superterm_zdb_id, xpatres_subterm_zdb_id, count(*) as ct
from expression_result2
group by xpatres_efs_id, xpatres_expression_found, xpatres_superterm_zdb_id, xpatres_subterm_zdb_id
having count(*) > 1
order by 4 desc;

select *
from dupes,
     expression_figure_stage,
     expression_experiment2
where efs_pk_id = xpatres_efs_id
  and efs_xpatex_zdb_id = xpatex_zdb_id
order by xpatex_source_zdb_id;

select x2.xpatres_efs_id, x2.xpatres_pk_id
from dupes as d,
     expression_figure_stage as efs,
     expression_experiment2,
     expression_result2 as x2
where efs.efs_pk_id = d.xpatres_efs_id
  and efs.efs_xpatex_zdb_id = xpatex_zdb_id
  AND x2.xpatres_efs_id = d.xpatres_efs_id
  AND nvl(d.xpatres_subterm_zdb_id, '') = nvl(x2.xpatres_subterm_zdb_id, '')
  AND d.xpatres_superterm_zdb_id = x2.xpatres_superterm_zdb_id
  AND d.xpatres_expression_found = x2.xpatres_expression_found
order by x2.xpatres_efs_id;

-- put all duplicate expression_result2 records (referred to in view dupe) into a new view
create view dupe_xpat as
select x2.*
from expression_result2 as x2
where exists(select 1
             from dupes as d
             where x2.xpatres_efs_id = d.xpatres_efs_id
               AND x2.xpatres_expression_found = d.xpatres_expression_found
               and x2.xpatres_superterm_zdb_id = d.xpatres_superterm_zdb_id
               and nvl(x2.xpatres_subterm_zdb_id, '') = nvl(d.xpatres_subterm_zdb_id, ''));

-- create another view that holds only the expression_result2 records with a single (max value)  xpatres_pk_id
create view dupe_xpat_xpatres as
select xpatres_efs_id, xpatres_expression_found, xpatres_superterm_zdb_id,xpatres_subterm_zdb_id, max(xpatres_pk_id) as xpatres_pk_id
from dupe_xpat
group by xpatres_efs_id, xpatres_expression_found, xpatres_superterm_zdb_id, xpatres_subterm_zdb_id;

-- remove all records except the ones with the max xpatres_pk_id
delete
from expression_result2
where xpatres_pk_id in (select x.xpatres_pk_id
                        from dupe_xpat as x, dupe_xpat_xpatres as dx
                        where x.xpatres_efs_id = dx.xpatres_efs_id
                          AND x.xpatres_pk_id = dx.xpatres_pk_id);


drop view dupe_xpat_xpatres;
drop view dupe_xpat;
drop view dupes;


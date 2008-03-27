-----------------------------------------------------------
-- Get statistics after each Thisse FR load. 
-- We want the same statistics for FR# and EU# clone loads. 
-- Since they are exclusive, we add the different pub ids 
-- into the SQL and with the date clause we are fine. 
--
-- statistics:
--   # of named genes that got expression data in this FR load
--   # of genes that have general xpat in this load, and other Thisse 
--     xpats with specific xpat
--   genes have more than 1 Thisse expression experiments
--   genes have more than 2 Thisse expression experiments
-----------------------------------------------------------

!echo 'number of named genes that got expression data in this FR load'

select count(distinct mrkr_zdb_id)
 from expression_experiment, marker
where xpatex_direct_submission_date = TODAY
  and (xpatex_source_zdb_id = "ZDB-PUB-040907-1"
       or xpatex_source_zdb_id = "ZDB-PUB-051025-1"
       or xpatex_source_zdb_id="ZDB-PUB-080227-22" )
  and xpatex_gene_zdb_id = mrkr_zdb_id
  and mrkr_name <> mrkr_abbrev;


!echo 'number of genes that have general xpat in this load, and other Thisse xpats with specific xpat'
select count(p1.xpatex_gene_zdb_id)
  from expression_experiment p1 join expression_experiment p2
       on p1.xpatex_gene_zdb_id = p2.xpatex_gene_zdb_id
 where p1.xpatex_direct_submission_date = TODAY
   and (p1.xpatex_source_zdb_id = "ZDB-PUB-040907-1"
        or p1.xpatex_source_zdb_id = "ZDB-PUB-051025-1"
        or p1.xpatex_source_zdb_id = "ZDB-PUB-080227-22")
   and p2.xpatex_source_zdb_id in ("ZDB-PUB-040907-1","ZDB-PUB-010810-1","ZDB-PUB-051025-1","ZDB-PUB-080227-22")
   and exists       -- an experiment in this send with general xpat
	      (select xpatres_zdb_id
                 from expression_result
                where xpatres_xpatex_zdb_id = p1.xpatex_zdb_id
                  and xpatres_start_stg_zdb_id = "ZDB-STAGE-010723-4"  -- Zygote:1-cell
  		  and xpatres_end_stg_zdb_id = "ZDB-STAGE-010723-26"  -- Hatching:Pec-fin
   		  and xpatres_comments like "not spatially restricted%"
	       )
   and not exists    -- another experiment that has specific xpat
	      (select xpatres_zdb_id
                 from expression_result
                where xpatres_xpatex_zdb_id = p2.xpatex_zdb_id
                  and xpatres_start_stg_zdb_id = "ZDB-STAGE-010723-4"  -- Zygote:1-cell
  		  and xpatres_end_stg_zdb_id = "ZDB-STAGE-010723-26"  -- Hatching:Pec-fin
   		  and xpatres_comments like "not spatially restricted%"
	       );


!echo 'genes have more than 1 Thisse expression experiments'
select xpatex_gene_zdb_id
  from expression_experiment
 where xpatex_source_zdb_id in ("ZDB-PUB-040907-1", "ZDB-PUB-010810-1", "ZDB-PUB-051025-1","ZDB-PUB-080227-22")
group by xpatex_gene_zdb_id
having count(xpatex_zdb_id) > 1;

!echo 'genes have more than 2 Thisse expression experiments'			
select xpatex_gene_zdb_id
  from expression_experiment
 where xpatex_source_zdb_id in ("ZDB-PUB-040907-1", "ZDB-PUB-010810-1", "ZDB-PUB-051025-1","ZDB-PUB-080227-22")
group by xpatex_gene_zdb_id
having count(xpatex_zdb_id) > 2;

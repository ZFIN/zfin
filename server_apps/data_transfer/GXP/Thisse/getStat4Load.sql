!echo 'count named gene got expression data by this FR load'

select count(distinct mrkr_zdb_id)
 from expression_pattern, marker
where xpat_direct_submission_date = TODAY
  and xpat_source_zdb_id = "ZDB-PUB-040907-1"
  and xpat_gene_zdb_id = mrkr_zdb_id
  and mrkr_name <> mrkr_abbrev;


!echo 'genes that have one image xpat in this load, and other thisse xpats with specificity'
select count(p1.xpat_gene_zdb_id)
  from expression_pattern p1, expression_pattern p2
 where p1.xpat_direct_submission_date = TODAY
   and p1.xpat_zdb_id in (
			select xpatfimg_xpat_zdb_id  
  	  	  	  from expression_pattern_image
			group by xpatfimg_xpat_zdb_id
 			having count(xpatfimg_fimg_zdb_id) = 1
			)
   and p1.xpat_source_zdb_id = "ZDB-PUB-040907-1"
   and p2.xpat_zdb_id in (
 			select xpatfimg_xpat_zdb_id  
  	 		  from expression_pattern_image
	  	  group by xpatfimg_xpat_zdb_id
 			having count(xpatfimg_fimg_zdb_id) > 1
		)
   and p1.xpat_source_zdb_id in ("ZDB-PUB-040907-1", "ZDB-PUB-010810-1")
   and p1.xpat_gene_zdb_id = p2.xpat_gene_zdb_id;


!echo 'genes have more than 1 thisse xpats'
select xpat_gene_zdb_id
  from expression_pattern
 where xpat_source_zdb_id in ("ZDB-PUB-040907-1", "ZDB-PUB-010810-1")
group by xpat_gene_zdb_id
having count(xpat_zdb_id) > 1;

!echo 'genes have more than 2 thisse expression patterns'			
select xpat_gene_zdb_id
  from expression_pattern
 where xpat_source_zdb_id in ("ZDB-PUB-040907-1", "ZDB-PUB-010810-1")
group by xpat_gene_zdb_id
having count(xpat_zdb_id) > 2;

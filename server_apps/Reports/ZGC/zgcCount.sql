!echo 'total MGC clones in ZFIN'

select count(*) mgc_clones
  from marker
 where mrkr_abbrev[1,4] = "MGC:";

!echo '-----------------------------------------------------------'
!echo 'total ZGC genes (genes with MGC clone(s))'

select count(distinct gene.mrkr_zdb_id) zgc_genes
  from marker_relationship, marker gene, marker clone
 where mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
   and gene.mrkr_type = "GENE"	
   and clone.mrkr_abbrev[1,4] = "MGC:";


!echo '-----------------------------------------------------------------'
!echo 'ZGC genes with expression patterns'

select count(distinct xpat_gene_zdb_id) genes
  from expression_pattern
 where exists 
	     (select *
            from marker_relationship, marker clone
	 	   where mrel_mrkr_1_zdb_id = xpat_gene_zdb_id 
		     and mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
		     and clone.mrkr_abbrev[1,4] = "MGC:");

!echo 'ZGC genes with expression patterns images'

select count(distinct xpat_gene_zdb_id) zgc_genes, 
       count(xpatfimg_fimg_zdb_id) images
  from expression_pattern, expression_pattern_image
 where xpat_zdb_id = xpatfimg_xpat_zdb_id
   and exists 
	     (select *
            from marker_relationship, marker clone
	 	   where mrel_mrkr_1_zdb_id = xpat_gene_zdb_id 
		     and mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
		     and clone.mrkr_abbrev[1,4] = "MGC:");

!echo 'ZGC genes with expression patterns but no images'

select count(gene.mrkr_zdb_id) zgc_genes
  from marker gene
 where not exists 
		 (select * 
            from expression_pattern_image, expression_pattern
           where gene.mrkr_zdb_id = xpat_gene_zdb_id
		     and xpat_zdb_id = xpatfimg_xpat_zdb_id)
   and exists 
	     (select * 
            from expression_pattern
           where gene.mrkr_zdb_id = xpat_gene_zdb_id)
   and exists 
	     (select *
            from marker_relationship, marker clone
	 	   where mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id 
		     and mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
		     and clone.mrkr_abbrev[1,4] = "MGC:");

!echo '----------------------------------------------------------------------'
!echo 'ZGC genes with informative name'

select count(gene.mrkr_zdb_id) zgc_genes
  from marker gene
 where gene.mrkr_type = "GENE"
   and exists 
	     (select *
            from marker_relationship, marker clone
	 	   where mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id 
		     and mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
		     and clone.mrkr_abbrev[1,4] = "MGC:")
   and gene.mrkr_abbrev not like "%:%";


!echo 'ZGC genes came to ZFIN with informative name'

select count(gene.mrkr_zdb_id) genes
  from marker gene
 where gene.mrkr_type = "GENE"
   and exists 
	     (select *
            from marker_relationship, marker clone
	 	   where mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id 
		     and mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
		     and clone.mrkr_abbrev[1,4] = "MGC:")
   and not exists 
		 (select *
 			from data_alias
           where dalias_data_zdb_id = gene.mrkr_zdb_id
             and dalias_alias like "zgc:%") 
   and gene.mrkr_abbrev not like "%:%";


!echo 'informative name genes that have "zgc:" genes merged/renamed into them'

select count(distinct gene.mrkr_zdb_id) genes
  from marker gene, data_alias
 where gene.mrkr_type = 'GENE'
   and gene.mrkr_abbrev not like '%:%'
   and dalias_data_zdb_id = gene.mrkr_zdb_id
   and dalias_alias like 'zgc:%';

!echo '"zgc:" genes that are merged/renamed into informative name genes'

select count(distinct dalias_alias) zgc_alias
  from marker gene, data_alias
 where gene.mrkr_type = 'GENE'
   and gene.mrkr_abbrev not like '%:%'
   and dalias_data_zdb_id = gene.mrkr_zdb_id
   and dalias_alias like 'zgc:%';


!echo 'ZGC genes having "zgc:" name'
select count(distinct gene.mrkr_zdb_id) genes
  from marker_relationship, marker gene, marker clone
 where mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
   and gene.mrkr_type = "GENE"	
   and gene.mrkr_abbrev[1,4] = "zgc:"
   and clone.mrkr_abbrev[1,4] = "MGC:";

-- this query should report the same as above, otherwise it suggests that 
-- some "zgc:" genes are not MGC clone attached.

--select count(gene.mrkr_zdb_id) zgc_genes
--  from marker gene
-- where gene.mrkr_type = "GENE"	
--   and gene.mrkr_abbrev[1,4] = "zgc:";


!echo '----------------------------------------------------------------------'
!echo 'ESTs associated with ZGC genes'

select count(distinct b.mrel_mrkr_2_zdb_id) ests_w_zgc
  from marker clone, marker gene, marker_relationship a, marker_relationship b, marker est
 where a.mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and a.mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
   and b.mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and b.mrel_mrkr_2_zdb_id = est.mrkr_zdb_id
   and gene.mrkr_type = 'GENE'	
   and est.mrkr_type = "EST"
   and clone.mrkr_abbrev[1,4] = "MGC:";

!echo 'ZGC genes associated with ESTs'

select count(distinct gene.mrkr_zdb_id) genes
  from marker clone, marker gene, marker_relationship a, marker_relationship b, marker est
 where a.mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and a.mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
   and b.mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and b.mrel_mrkr_2_zdb_id = est.mrkr_zdb_id
   and gene.mrkr_type = 'GENE'	
   and est.mrkr_type = "EST"
   and clone.mrkr_abbrev[1,4] = "MGC:";


!echo '---------------------------------------------------------------------------'
!echo 'total genes with expression patterns'

select count(distinct xpat_gene_zdb_id) genes
  from expression_pattern;

!echo 'total genes with expression patterns images '
select count(distinct xpat_gene_zdb_id) genes,
       count(xpatfimg_fimg_zdb_id) images
  from expression_pattern, expression_pattern_image
 where xpat_zdb_id  =  xpatfimg_xpat_zdb_id;


!echo '----------------------------------------------------------------------------'
!echo 'informative name genes have expression pattern with images'
--note: some informative name genes have ':' in name
--pseudogenes have name and abbrev different, though they are both si:

select count(distinct xpat_gene_zdb_id) genes
  from expression_pattern, marker
 where xpat_gene_zdb_id = mrkr_zdb_id
   and mrkr_abbrev not like "%:%"
   and exists 
		  (select *
             from expression_pattern_image
            where xpat_zdb_id = xpatfimg_xpat_zdb_id);

!echo '-----------------------------------------------------------------------------'
!echo 'informative name ZGC genes have expression pattern with images'

select count(distinct xpat_gene_zdb_id) genes
  from expression_pattern, marker gene
 where exists 
	     (select *
          from marker_relationship, marker clone
	 	 where mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
           and mrel_mrkr_1_zdb_id = xpat_gene_zdb_id 
		   and clone.mrkr_abbrev[1,4] = "MGC:" ) 
   and xpat_gene_zdb_id = gene.mrkr_zdb_id
   and gene.mrkr_abbrev not like "%:%"
   and exists 
		  (select *
             from expression_pattern_image
            where xpat_zdb_id = xpatfimg_xpat_zdb_id); 


!echo '----------------------------------------------------------------------------'
!echo 'Thisse FR expression patterns (and image numbers)'

select count(distinct xpat_zdb_id) xpats, count(xpatfimg_fimg_zdb_id) images
  from expression_pattern, expression_pattern_image
 where  xpat_source_zdb_id = "ZDB-PUB-040907-1"
  and   xpat_zdb_id  =  xpatfimg_xpat_zdb_id;


!echo '----------------------------------------------------------------------------'
!echo 'ZGC genes have added expression patterns from Thisse FR (and image numbers)'
select count(distinct xpat_gene_zdb_id) zgc_genes, count(xpatfimg_fimg_zdb_id) images
  from expression_pattern, expression_pattern_image
 where  exists 
	     (select *
            from marker_relationship, marker clone
	 	   where clone.mrkr_abbrev[1,4] = "MGC:"
   		     and mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
             and mrel_mrkr_1_zdb_id = xpat_gene_zdb_id )
  and   xpat_zdb_id  =  xpatfimg_xpat_zdb_id
  and   xpat_source_zdb_id = "ZDB-PUB-040907-1";

!echo '---------------------------------------------------------------------------'
!echo 'FR xpats explicitly from MGC clones'

select count(distinct xpat_zdb_id) xpats,
       count(xpatfimg_fimg_zdb_id) images
from expression_pattern, expression_pattern_image, marker clone
 where  xpat_source_zdb_id = "ZDB-PUB-040907-1"
  and   xpat_zdb_id  =  xpatfimg_xpat_zdb_id
  and   xpat_probe_zdb_id = clone.mrkr_zdb_id
  and   clone.mrkr_abbrev[1,4] = "MGC:";

!echo '----------------------------------------------------------------------------'
!echo 'non zgc:, non im: temporary name genes that have expression pattern with images'

select count(distinct xpat_gene_zdb_id) genes
  from expression_pattern, marker
 where xpat_gene_zdb_id = mrkr_zdb_id
   and mrkr_abbrev like "%:%"
   and mrkr_abbrev not like "im:%"
   and mrkr_abbrev not like "zgc:%"
   and exists 
		  (select *
             from expression_pattern_image
            where xpat_zdb_id = xpatfimg_xpat_zdb_id);

!echo '----------------------------------------------------------------------------'
!echo 'ensembl genes have expression pattern with images'
select count(distinct xpat_gene_zdb_id) genes
  from expression_pattern, marker
 where xpat_gene_zdb_id = mrkr_zdb_id
   and (mrkr_abbrev like "si:%"
      or exists ( select *
	                from data_alias
                   where dalias_data_zdb_id = mrkr_zdb_id
                     and dalias_alias like "si:%") 
	   )
    and exists 
		  (select *
             from expression_pattern_image
            where xpat_zdb_id = xpatfimg_xpat_zdb_id);



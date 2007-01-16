!echo 'total MGC clones in ZFIN'

select count(*) mgc_clones
  from marker
 where mrkr_abbrev[1,4] = "MGC:";

-- save zgc genes for furthur reference
create temp table tmp_zgcCount_zgc_gene (
	tzg_gene_zdb_id 	varchar(50) not null primary key
)with no log;

insert into tmp_zgcCount_zgc_gene (tzg_gene_zdb_id)
     select distinct gene.mrkr_zdb_id
       from marker_relationship, marker gene, marker clone
      where mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
        and mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
        and gene.mrkr_type = "GENE"
        and clone.mrkr_abbrev[1,4] = "MGC:";

!echo '----------------------------------------------------------------'
!echo 'total ZGC genes (genes with MGC clone(s))'

select count(tzg_gene_zdb_id) zgc_genes
  from tmp_zgcCount_zgc_gene;


!echo '-----------------------------------------------------------------'
!echo 'ZGC genes with expression experiment data'

select count(tzg_gene_zdb_id)  zgc_genes
  from tmp_zgcCount_zgc_gene
 where exists
             (select 't'
                from expression_experiment
	       where tzg_gene_zdb_id = xpatex_gene_zdb_id);

-- 'ZGC genes with expression patterns images'

select tzg_gene_zdb_id zgc_genes, img_zdb_id images
  from tmp_zgcCount_zgc_gene join expression_experiment
       on tzg_gene_zdb_id = xpatex_gene_zdb_id
       join expression_result
       on xpatex_zdb_id = xpatres_xpatex_zdb_id
       join expression_pattern_figure
       on xpatres_zdb_id = xpatfig_xpatres_zdb_id
       join image
       on xpatfig_fig_zdb_id = img_fig_zdb_id
into temp tmp_zgcCount_zgc_xpat with no log;

!echo 'ZGC genes with expression patterns images'
select count(distinct zgc_genes) from tmp_zgcCount_zgc_xpat;
select count(distinct images) from tmp_zgcCount_zgc_xpat;



!echo '----------------------------------------------------------------------'
!echo 'ZGC genes with informative name'

select count(tzg_gene_zdb_id) zgc_genes
  from tmp_zgcCount_zgc_gene join marker
       on tzg_gene_zdb_id = mrkr_zdb_id
 where mrkr_abbrev not like "%:%";


!echo 'ZGC genes came to ZFIN with informative name'

select count(tzg_gene_zdb_id) genes
  from tmp_zgcCount_zgc_gene join marker
       on tzg_gene_zdb_id = mrkr_zdb_id
 where not exists
		 (select 't'
 		    from data_alias
           	   where dalias_data_zdb_id = tzg_gene_zdb_id
             	     and dalias_alias[1,4] = "zgc:")
   and mrkr_abbrev not like "%:%";


!echo 'informative name genes that have "zgc:" genes merged/renamed into them'

select count(distinct gene.mrkr_zdb_id) genes
  from marker gene, data_alias
 where gene.mrkr_type = 'GENE'
   and gene.mrkr_abbrev not like '%:%'
   and dalias_data_zdb_id = gene.mrkr_zdb_id
   and dalias_alias[1,4] = 'zgc:';

!echo '"zgc:" genes that are merged/renamed into informative name genes'

select count(distinct dalias_alias) zgc_alias
  from marker gene, data_alias
 where gene.mrkr_type = 'GENE'
   and gene.mrkr_abbrev not like '%:%'
   and dalias_data_zdb_id = gene.mrkr_zdb_id
   and dalias_alias[1,4] = 'zgc:';


!echo 'ZGC genes having "zgc:" name'
select count(tzg_gene_zdb_id) genes
  from tmp_zgcCount_zgc_gene join marker
       on tzg_gene_zdb_id = mrkr_zdb_id
 where mrkr_abbrev[1,4] = "zgc:";


!echo '----------------------------------------------------------------------'
!echo 'ESTs associated with ZGC genes'

select count(distinct est.mrkr_zdb_id) ests_w_zgc
  from tmp_zgcCount_zgc_gene, marker_relationship, marker est
 where mrel_mrkr_1_zdb_id = tzg_gene_zdb_id
   and mrel_mrkr_2_zdb_id = est.mrkr_zdb_id
   and est.mrkr_type = "EST";

!echo 'ZGC genes associated with ESTs'

select count(distinct tzg_gene_zdb_id) genes
  from tmp_zgcCount_zgc_gene, marker_relationship, marker est
 where mrel_mrkr_1_zdb_id = tzg_gene_zdb_id
   and mrel_mrkr_2_zdb_id = est.mrkr_zdb_id
   and est.mrkr_type = "EST";



!echo '---------------------------------------------------------------------------'
!echo 'total genes with expression experiment record'

select count(distinct xpatex_gene_zdb_id) genes
  from expression_experiment;

-- 'total genes with expression patterns images '
select xpatex_gene_zdb_id as genes,
       img_zdb_id as images
  from expression_experiment
       join expression_result
       on xpatex_zdb_id = xpatres_xpatex_zdb_id
       join expression_pattern_figure
       on xpatres_zdb_id = xpatfig_xpatres_zdb_id
       join image
       on xpatfig_fig_zdb_id = img_fig_zdb_id
into temp tmp_zgcCount_gene_xpat with no log;

!echo 'total genes with expression patterns images '
select count(distinct genes) from tmp_zgcCount_gene_xpat;
select count(distinct images) from tmp_zgcCount_gene_xpat;

-- save genes with xpat images
create temp table tmp_zgcCount_real_gene_img (
	tzgi_gene_zdb_id	varchar(50) not null primary key
) with no log;

insert into tmp_zgcCount_real_gene_img
      select distinct xpatex_gene_zdb_id
        from marker join expression_experiment
	     on xpatex_gene_zdb_id = mrkr_zdb_id
       	     join expression_result
             on xpatex_zdb_id = xpatres_xpatex_zdb_id
             join expression_pattern_figure
             on xpatres_zdb_id = xpatfig_xpatres_zdb_id
             join image
             on xpatfig_fig_zdb_id = img_fig_zdb_id
       where mrkr_abbrev not like "%:%";

!echo '----------------------------------------------------------------------------'
!echo 'informative name genes have expression experiment data with images'
--note: some informative name genes have ':' in name
--pseudogenes have name and abbrev different, though they are both si:

select count(tzgi_gene_zdb_id) genes
  from tmp_zgcCount_real_gene_img;

!echo '-----------------------------------------------------------------------------'
!echo 'informative name ZGC genes have expression experiment data  with images'

select count(distinct tzgi_gene_zdb_id) genes
  from tmp_zgcCount_real_gene_img
 where exists
          (select 't'
             from tmp_zgcCount_zgc_gene
            where tzgi_gene_zdb_id = tzg_gene_zdb_id);


!echo '----------------------------------------------------------------------------'
!echo 'Thisse FR expression experiment count '

select count(xpatex_zdb_id) xpats
  from expression_experiment
 where xpatex_source_zdb_id in ("ZDB-PUB-040907-1","ZDB-PUB-051025-1");

!echo 'Thisse FR expression experiemnt image numbers'

select count(img_zdb_id) images
  from figure join image
       on fig_zdb_id = img_fig_zdb_id
 where fig_source_zdb_id in ("ZDB-PUB-040907-1","ZDB-PUB-051025-1");


!echo '----------------------------------------------------------------------------'
-- 'ZGC genes have added expression patterns from Thisse FR (and image numbers)'

select  tzg_gene_zdb_id as zgc_genes,
        img_zdb_id as images
  from tmp_zgcCount_zgc_gene join expression_experiment
       on tzg_gene_zdb_id = xpatex_gene_zdb_id
       join expression_result
       on xpatex_zdb_id = xpatres_xpatex_zdb_id
       join expression_pattern_figure
       on xpatres_zdb_id = xpatfig_xpatres_zdb_id
       join image
       on xpatfig_fig_zdb_id = img_fig_zdb_id
 where xpatex_source_zdb_id in ("ZDB-PUB-040907-1","ZDB-PUB-051025-1")
 into temp tmp_zgcCount_zgc_fr with no log;

!echo 'ZGC genes have added expression patterns from Thisse FR (and image numbers)'
select count(distinct zgc_genes) from tmp_zgcCount_zgc_fr;
select count(distinct images) from tmp_zgcCount_zgc_fr;



!echo '---------------------------------------------------------------------------'
-- 'FR xpats explicitly from MGC clones'

select xpatex_zdb_id as xpats,
       img_zdb_id as images
  from expression_experiment join marker
       on xpatex_probe_feature_zdb_id = mrkr_zdb_id
       join expression_result
       on xpatex_zdb_id = xpatres_xpatex_zdb_id
       join expression_pattern_figure
       on xpatres_zdb_id = xpatfig_xpatres_zdb_id
       join image
       on xpatfig_fig_zdb_id = img_fig_zdb_id
 where  xpatex_source_zdb_id in ("ZDB-PUB-040907-1","ZDB-PUB-051025-1")
  and   mrkr_abbrev[1,4] = "MGC:"
into temp tmp_zgcCount_mgc_fr with no log;

!echo 'FR xpats explicitly from MGC clones'
select count(distinct xpats) from tmp_zgcCount_mgc_fr;
select count(distinct images) from tmp_zgcCount_mgc_fr;



!echo '----------------------------------------------------------------------------'
!echo 'non zgc:, non im: temporary name genes that have expression pattern with images'

select count(distinct xpatex_gene_zdb_id) genes
  from expression_experiment join marker
       on xpatex_gene_zdb_id = mrkr_zdb_id
       join expression_result
       on xpatex_zdb_id = xpatres_xpatex_zdb_id
       join expression_pattern_figure
       on xpatres_zdb_id = xpatfig_xpatres_zdb_id
       join image
       on xpatfig_fig_zdb_id = img_fig_zdb_id

 where mrkr_abbrev like "%:%"
   and mrkr_abbrev[1,3] <> "im:"
   and mrkr_abbrev[1,4] <> "zgc:";

!echo '----------------------------------------------------------------------------'
!echo 'ensembl genes have expression pattern with images'
select count(distinct xpatex_gene_zdb_id) genes
  from expression_experiment join marker
       on xpatex_gene_zdb_id = mrkr_zdb_id
       join expression_result
       on xpatex_zdb_id = xpatres_xpatex_zdb_id
       join expression_pattern_figure
       on xpatres_zdb_id = xpatfig_xpatres_zdb_id
       join image
       on xpatfig_fig_zdb_id = img_fig_zdb_id
 where (mrkr_abbrev[1,3] = "si:"
        or exists ( select 't'
	              from data_alias
                     where dalias_data_zdb_id = mrkr_zdb_id
                       and dalias_alias[1,3] = "si:")
        );

!echo '----------------------------------------------------------------------------'
!echo 'total figures from literature'
select count(distinct fig_zdb_id)
  from figure
       join publication on fig_source_zdb_id = zdb_id
 where jtype not in ("Unpublished", "Curation");


!echo '----------------------------------------------------------------------------'
!echo 'figures from literature that have images'
select count(distinct fig_zdb_id)
  from figure
       join publication on fig_source_zdb_id = zdb_id
 where jtype not in ("Unpublished", "Curation")
   and exists ( select 't'
                  from image
                 where fig_zdb_id = img_fig_zdb_id);

!echo '----------------------------------------------------------------------------'
!echo 'literature curated for expression '
select count(distinct xpatex_source_zdb_id)
  from expression_experiment
       join publication on xpatex_source_zdb_id = zdb_id
 where jtype not in ("Unpublished", "Curation");



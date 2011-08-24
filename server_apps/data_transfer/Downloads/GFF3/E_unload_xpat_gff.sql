! echo "E_unload_xpat_gff.sql -> E_unload expression.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_expression.gff3' DELIMITER "	"
select
	zeg_seqname,
	"ZFIN_Expression" src,
	zeg_feature,
	min(zeg_start) gstart,
	max(zeg_end)   gend,
	zeg_score,
	zeg_strand,
	zeg_frame,
	zeg_ID_Name ||';Alias='||  zeg_Alias attribute
 from zfin_ensembl_gene,
 expression_experiment, expression_result, expression_pattern_figure
 where xpatex_gene_zdb_id is not NULL
   and xpatex_gene_zdb_id == zeg_Alias
   and xpatex_zdb_id = xpatres_xpatex_zdb_id
   and xpatres_expression_found = 't'
   and xpatres_zdb_id = xpatfig_xpatres_zdb_id

 group by 1,3,6,7,8,9
 order by 1,4,5,9
;


-- to be valid the gff3 requires a header
! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_expression.gff3


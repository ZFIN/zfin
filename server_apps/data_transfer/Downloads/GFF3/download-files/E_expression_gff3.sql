--! echo 'E_unload_xpat_gff.sql -> E_unload expression.gff3'

copy (
select
	zeg_seqname,
	'ZFIN_Expression' src,
	zeg_feature,
	min(zeg_start) gstart,
	max(zeg_end)   gend,
	zeg_score,
	zeg_strand,
	zeg_frame,
	zeg_ID_Name ||';Alias='||  zeg_Alias as attribute
 from zfin_ensembl_gene,
 expression_experiment2, expression_result2, expression_figure_stage
 where xpatex_gene_zdb_id is not NULL
   and xpatex_gene_zdb_id = zeg_Alias
   and xpatex_zdb_id = efs_xpatex_zdb_id
   and xpatres_expression_found = 't'
   and xpatres_efs_id = efs_pk_id
 group by 1,3,6,7,8,9
 order by 1,4,5,9 ) to 'E_expression.gff3' DELIMITER '	'
;


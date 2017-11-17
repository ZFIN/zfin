--! echo 'E_unload_antibody_gff.sql -> E_antibody.gff3'

copy (
select
	zeg_seqname,
	'ZFIN_Antibody' src,
	zeg_feature,
	min(zeg_start) gstart,
	max(zeg_end)   gend,
	zeg_score,
	zeg_strand,
	zeg_frame,
	zeg_ID_Name ||';Alias='|| zeg_Alias as attribute
 from zfin_ensembl_gene, marker_relationship
 where mrel_type = 'gene product recognized by antibody'
   and mrel_mrkr_1_zdb_id = zeg_Alias
 group by 1,3,6,7,8,9
 order by 1,4,5,9 )  to 'E_antibody.gff3' DELIMITER '	'


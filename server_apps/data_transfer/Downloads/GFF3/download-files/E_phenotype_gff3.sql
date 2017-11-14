--! echo 'unload_pheno_gff.sql -> E_phenotype.gff3'

copy (
select
	zeg_seqname,
	'ZFIN_Phenotype' src,
	zeg_feature,
	min(zeg_start) gstart,
	max(zeg_end)   gend,
	zeg_score,
	zeg_strand,
	zeg_frame,
	zeg_ID_Name ||';Alias='|| zeg_Alias as attribute
 from zfin_ensembl_gene
      join mutant_fast_search on zeg_Alias = mfs_mrkr_zdb_id
      join phenotype_source_generated on pg_genox_zdb_id = mfs_genox_zdb_id
 where exists (Select 'x' from phenotype_observation_generated
       	      	         where pg_id = psg_pg_id
		                       and psg_tag != 'normal')
 group by 1,3,6,7,8,9
 order by 1,4,5,9 )  to 'E_phenotype.gff3' DELIMITER '	'


! echo "unload_pheno_gff.sql -> E_phenotype.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_phenotype.gff3' DELIMITER "	"
select
	zeg_seqname,
	"ZFIN_Phenotype" src,
	zeg_feature,
	min(zeg_start) gstart,
	max(zeg_end)   gend,
	zeg_score,
	zeg_strand,
	zeg_frame,
	zeg_ID_Name ||';Alias='|| zeg_Alias attribute
 from zfin_ensembl_gene, phenotype_experiment, mutant_fast_search
 where phenox_genox_zdb_id == mfs_genox_zdb_id
   and mfs_mrkr_zdb_id     == zeg_Alias
 group by 1,3,6,7,8,9
 order by 1,4,5,9
;
-- to be valid the gff3 requires a header
! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_phenotype.gff3


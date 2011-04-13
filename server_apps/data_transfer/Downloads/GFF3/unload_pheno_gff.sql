! echo "unload_pheno_gff.sql -> phenotype.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/phenotype.gff3' DELIMITER "	"
select vg.seqname,
           "ZFIN_Phenotype" source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene'else 'gene' end  feature,
           min(vg.start) start,
           max(vg.end)   end,
           "." score,
           vg.strand,
           "." frame,
       'gene_id=' || gene.mrkr_zdb_id ||';Name='
       || gene.mrkr_abbrev  ||';Alias='|| gene.mrkr_zdb_id attribute

from phenotype_experiment, mutant_fast_search,
	db_link, marker_relationship, marker gene,
	gff3 vg, gff3 vt

where phenox_genox_zdb_id  = mfs_genox_zdb_id    --  idx-idx
  and mfs_mrkr_zdb_id      = gene.mrkr_zdb_id    --  idx-idx
  and mrkr_zdb_id[1,8]      = 'ZDB-GENE'

  and gene.mrkr_zdb_id      = mrel_mrkr_1_zdb_id --  idx-idx
  and mrel_mrkr_2_zdb_id    = dblink_linked_recid--  idx-idx
  and vt.source = 'vega' and vt.feature = 'transcript'
  and vg.source = 'vega' and vg.feature = 'gene'
  and dblink_acc_num = vt.id
  and vt.parent = vg.id
group by 1,3,7,9;

-- to be valid the gff3 requires a header
! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/phenotype.gff3


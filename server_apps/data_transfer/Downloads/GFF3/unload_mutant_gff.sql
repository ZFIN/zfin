! echo "unload_mutant_gff.sql -> mutant.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mutant.gff3' DELIMITER "	"
select  vg.seqname,
           "ZFIN_Mutant" source,
           'genotype'  feature,
           min(vg.start) start,
           max(vg.end)   end,
           "." score,
           vg.strand,
           "." frame,
       'geno_id=' || geno_zdb_id ||';Name='
       || geno_display_name ||';Alias='||  geno_zdb_id attribute

from mutant_fast_search,genotype_experiment,genotype,
 gff3 vt, gff3 vg, db_link,marker_relationship

 where mfs_genox_zdb_id  = genox_zdb_id
   and genox_geno_zdb_id = geno_zdb_id
   and mfs_mrkr_zdb_id   =  mrel_mrkr_1_zdb_id --  idx-idx
   and mrel_mrkr_2_zdb_id   = dblink_linked_recid--  idx-idx
   and vt.source = 'vega' and vt.feature = 'transcript'
   and vg.source = 'vega' and vg.feature = 'gene'
   and dblink_acc_num = vt.id
   and vt.parent = vg.id

 group by 1,3,7,9;

-- to be valid the gff3 requires a header
! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/mutant.gff3


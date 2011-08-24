! echo "unload_mutant_gff.sql -> mutant.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mutant.gff3' DELIMITER "	"
select  vg.gff_seqname,
           "ZFIN_Mutant" gff_source,
           'genotype'  gff_feature,
           min(vg.gff_start) gff_start,
           max(vg.gff_end)   gff_end,
           "." gff_score,
           vg.gff_strand,
           "." gff_frame,
       'geno_id=' || geno_zdb_id ||';Name='
       || geno_display_name ||';Alias='||  geno_zdb_id attribute

from mutant_fast_search,genotype_experiment,genotype,
 gff3 vt, gff3 vg, db_link,marker_relationship

 where mfs_genox_zdb_id  = genox_zdb_id
   and genox_geno_zdb_id = geno_zdb_id
   and mfs_mrkr_zdb_id   =  mrel_mrkr_1_zdb_id --  idx-idx
   and mrel_mrkr_2_zdb_id   = dblink_linked_recid--  idx-idx
   and vt.gff_source = 'vega' and vt.gff_feature = 'transcript'
   and vg.gff_source = 'vega' and vg.gff_feature = 'gene'
   and dblink_acc_num = vt.gff_ID
   and vt.gff_Parent = vg.gff_ID

 group by vg.gff_seqname,gstart,vg.gff_strand,attribute;

-- to be valid the gff3 requires a header
! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/mutant.gff3


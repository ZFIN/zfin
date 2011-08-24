! echo "unload_antibody_gff.sql -> antibody.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibody.gff3' DELIMITER "	"
select vg.gff_seqname,
           "ZFIN_Antibody" gff_source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene'else 'gene' end  feature,
           min(vg.gff_start) gstart,
           max(vg.gff_end)   gend,
           "." gff_score,
           vg.gff_strand,
           "." gff_frame,
       'gene_id=' || gene.mrkr_zdb_id ||';Name='
       || gene.mrkr_abbrev  ||';Alias='|| gene.mrkr_zdb_id attribute

from db_link, marker_relationship, marker gene, gff3 vg, gff3 vt

where mrel_type = 'gene product recognized by antibody'
  and gene.mrkr_zdb_id   = mrel_mrkr_1_zdb_id --  idx-idx
  and gene.mrkr_zdb_id   = dblink_linked_recid--  idx-idx
  and vt.gff_source = 'vega' and vt.gff_feature = 'transcript'
  and vg.gff_source = 'vega' and vg.gff_feature = 'gene'
  and dblink_acc_num = vg.gff_ID
  and vt.gff_Parent = vg.gff_ID
group by 1,3,7,9;

-- to be valid the gff3 requires a header
! awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibody.gff3


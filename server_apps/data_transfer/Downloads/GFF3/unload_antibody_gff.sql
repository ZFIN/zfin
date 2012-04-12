! echo "unload_antibody_gff.sql -> antibody.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibody.gff3' DELIMITER "	"
select gff_seqname,
           "ZFIN_Antibody" gff_source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene'else 'gene' end feature,
           min(gff_start) gstart,
           max(gff_end)   gend,
           "." gff_score,
           gff_strand,
           "." gff_frame,
       'gene_id=' || gene.mrkr_zdb_id ||';Name='
       || gene.mrkr_abbrev  ||';Alias='|| gene.mrkr_zdb_id attribute
from marker gene 
 join marker_relationship ab on gene.mrkr_zdb_id == ab.mrel_mrkr_1_zdb_id
 join marker_relationship gt on ab.mrel_mrkr_1_zdb_id == gt.mrel_mrkr_1_zdb_id
 join db_link on gt.mrel_mrkr_2_zdb_id == dblink_linked_recid
 join gff3 on gff_ID = dblink_acc_num

where ab.mrel_type = 'gene product recognized by antibody'
  and gt.mrel_type = 'gene produces transcript'
  and gff_source = 'vega' and gff_feature = 'transcript' 
group by 1,3,7,9;

-- to be valid the gff3 requires a header
! awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibody.gff3


! echo ""
! echo "                ENSEMBL         "
! echo ""


! echo "Copy the constant feature on the Ensembl backbone "
! echo "FROM: '/research/zprodmore/gff3/E_drerio_constant.gff3'"
! echo "TO: <!--|ROOT_PATH|-->/home/data_transfer/Downloads/"

! cp /research/zprodmore/gff3/E_drerio_constant.gff3 <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_drerio_constant.gff3

! echo "E_unload_transcript.sql -> E_drerio_transcript.gff3"
unload to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_drerio_transcript.gff3'  DELIMITER "	"
select
   gff_seqname,
   case dblink_acc_num when NULL then gff_source else 'vega' end,
   gff_feature,gff_start,gff_end,gff_score,gff_strand,gff_frame,
    "ID="       || gff_ID      ||
    ";Name="    || case gff_Name when NULL then "" else gff_Name end ||
    ";Parent="  || case gff_Parent when NULL then "" else gff_Parent end || 
    case dblink_linked_recid when NULL then "" else (";zdb_id=" || dblink_linked_recid) end ||
    ";Alias="   || gff_ID
 from  gff3, outer db_link
 where gff_source[1,8] == 'Ensembl_'
   and gff_feature in ('mRNA','transcript')
   and gff_ID  == dblink_acc_num   
order by 1,4,3
;

! cat /research/zprodmore/gff3/E_drerio_constant.gff3  <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_drerio_transcript.gff3 > <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_drerio_backbone.gff3 

-- gets its header fron the constant file
-- to be valid the gff3 requires a header
--! awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_drerio_transcript.gff3




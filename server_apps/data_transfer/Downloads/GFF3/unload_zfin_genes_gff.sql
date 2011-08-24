! echo "unload_zfin_genes_gff.sql -> zfin_gene.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene.gff3' DELIMITER "	"
select vg.gff_seqname,
           "ZFIN" gff_source,
           case mrkr_type when 'GENEP' then 'pseudogene' else  'gene' end  feature,
           min(vg.gff_start) gstart,
           max(vg.gff_end)   gend,
           "." gff_score,
           vg.gff_strand,
           "." gff_frame,
       'ID=' || mrkr_zdb_id    --- probably will not be unique
       ||';Name=' || mrkr_abbrev
       ||';Alias='|| mrkr_zdb_id  ||';'  attribute
 from  gff3 vg, gff3 vt, db_link tscript ,marker_relationship, marker

 where mrkr_type[1,4] = "GENE"  -- & GENEP
   and mrel_mrkr_1_zdb_id = mrkr_zdb_id
   and dblink_linked_recid = mrel_mrkr_2_zdb_id
   and vt.gff_source = 'vega' and vt.gff_feature = 'transcript'
   and vg.gff_source = 'vega' and vg.gff_feature = 'gene'
   and dblink_acc_num = vt.gff_ID
   and vt.gff_Parent  = vg.gff_ID
 group by vg.gff_seqname, vg.gff_strand, 3,9
 order by 1,4,5,9
 ;

-- to be valid the gff3 requires a header
! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene.gff3


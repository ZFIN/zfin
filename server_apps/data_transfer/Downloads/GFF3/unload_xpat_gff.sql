! echo "unload_xpat_gff.sql -> unload expression.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/expression.gff3' DELIMITER "	"
select vg.gff_seqname,
           "ZFIN_Expression" gff_source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene' else 'gene' end  feature,
           min(vg.gff_start) gstart,
           max(vg.gff_end)   gend,
           "." gff_score,
           vg.gff_strand,
           "." gff_frame,
       'gene_id=' || xpatex_gene_zdb_id ||';Name=' || gene.mrkr_abbrev
       --';Alias=' || clone_abbrev?
       attribute
from expression_experiment, marker gene,
  expression_result,expression_pattern_figure,
  gff3 vt, gff3 vg,  db_link, marker_relationship
 where xpatex_gene_zdb_id is not NULL
   and mrel_mrkr_1_zdb_id = xpatex_gene_zdb_id
   and dblink_linked_recid = mrel_mrkr_2_zdb_id
   and mrkr_zdb_id = xpatex_gene_zdb_id
   and xpatex_zdb_id = xpatres_xpatex_zdb_id
   and xpatres_expression_found = 't'
   and xpatres_zdb_id = xpatfig_xpatres_zdb_id
   and vt.gff_source = 'vega' and vt.gff_feature = 'transcript'
   and vg.gff_source = 'vega' and vg.gff_feature = 'gene'
   and dblink_acc_num = vt.gff_ID
   and vt.gff_Parent = vg.gff_ID

 group by 1,3,7,9;

-- to be valid the gff3 requires a header

! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/expression.gff3


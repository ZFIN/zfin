! echo "unload_xpat_gff.sql -> unload expression.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/expression.gff3' DELIMITER "	"
select vg.seqname,
           "ZFIN_Expression" source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene' else 'gene' end  feature,
           min(vg.start) start,
           max(vg.end)   end,
           "." score,
           vg.strand,
           "." frame,
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
   and vt.source = 'vega' and vt.feature = 'transcript'
   and vg.source = 'vega' and vg.feature = 'gene'
   and dblink_acc_num = vt.id
   and vt.parent = vg.id

 group by 1,3,7,9;

-- to be valid the gff3 requires a header

! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/expression.gff3


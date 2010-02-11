! echo "unload zfin_gene_gff -> zfin_gene.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene.gff3' DELIMITER "	"
select vg.seqname,
           "ZFIN" source,
           case mrkr_type when 'GENEP' then 'pseudogene' else  'gene' end  feature,
           min(vg.start) start,
           max(vg.end)   end,
           "." score,
           vg.strand,
           "." frame,
       'ID=' || mrkr_zdb_id    --- probably will not be unique
       ||';Name=' || mrkr_abbrev
       ||';Alias='|| mrkr_zdb_id  ||';'  attribute
 from  gff3 vg, gff3 vt, db_link tscript ,marker_relationship, marker

 where mrkr_type[1,4] = "GENE"  -- & GENEP
   and mrel_mrkr_1_zdb_id = mrkr_zdb_id
   and dblink_linked_recid = mrel_mrkr_2_zdb_id
   and vt.source = 'vega' and vt.feature = 'transcript'
   and vg.source = 'vega' and vg.feature = 'gene'
   and dblink_acc_num = vt.id
   and vt.parent = vg.id
 group by 1,3,7,9
 order by 1,4,5,9
 ;

-- to be valid the gff3 requires a header
! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene.gff3


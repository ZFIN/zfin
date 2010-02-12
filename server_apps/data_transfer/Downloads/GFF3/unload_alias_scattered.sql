! echo "unload_alias_scattered.sql -> zfin_gene_alias_scattered.tmp(deleted)"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias_scattered.tmp' DELIMITER "	"

select vg.seqname,
           "ZFIN" source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene' else  'gene' end  feature,
           min(vg.start) start,
           max(vg.end)   end,
           "." score,
           vg.strand,
           "." frame,
       'gene_id=' || gene.mrkr_zdb_id
       ||';Name=' || gene.mrkr_abbrev
       ||';Alias='|| gene.mrkr_zdb_id  attribute
 from  gff3 vt, gff3 vg, db_link tscript ,marker_relationship, marker gene
 where gene.mrkr_type[1,4] = "GENE"  -- & GENEP
   and mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and tscript.dblink_linked_recid = mrel_mrkr_2_zdb_id
   and vt.source = 'vega' and vt.feature = 'transcript'
   and vg.source = 'vega' and vg.feature = 'gene'
   and tscript.dblink_acc_num = vt.id
   and vt.parent = vg.id
 group by 1,3,7,9
---------------------------------------------------------
-- RefSeq
union
select vg.seqname,
           "ZFIN" source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene' else  'gene' end  feature,
           min(vg.start) start,
           max(vg.end)   end,
           '.' score,
           vg.strand,
           '.' frame,
       'gene_id=' || gene.mrkr_zdb_id
       ||';Name=' || gene.mrkr_abbrev
       ||';Alias='|| refseq.dblink_acc_num
       attribute
 from  gff3 vt, gff3 vg, db_link tscript ,marker_relationship, marker gene , db_link refseq
 where gene.mrkr_type[1,4] = "GENE"
   and mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and tscript.dblink_linked_recid = mrel_mrkr_2_zdb_id
   and refseq.dblink_fdbcont_zdb_id  in ('ZDB-FDBCONT-040412-38','ZDB-FDBCONT-040412-39') -- protein as well?
   and refseq.dblink_linked_recid =  gene.mrkr_zdb_id
   and vt.source = 'vega' and vt.feature = 'transcript'
   and vg.source = 'vega' and vg.feature = 'gene'
   and tscript.dblink_acc_num = vt.id
   and vt.parent = vg.id
group by 1,3,7,9
---------------------------------------------------------
union -- data alias
select vg.seqname,
           "ZFIN" source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene' else 'gene' end  feature,
           min(vg.start) start,
           max(vg.end)   end,
           "." score,
           vg.strand,
           "." frame,
       'gene_id='  || gene.mrkr_zdb_id
       ||';Name='  || gene.mrkr_abbrev
       ||';Alias=' || dalias_alias_lower
       attribute
 from  gff3 vt, gff3 vg,  db_link, marker_relationship,marker gene, data_alias
 where mrkr_type[1,4] = "GENE"
   and mrel_mrkr_1_zdb_id = mrkr_zdb_id
   and dblink_linked_recid = mrel_mrkr_2_zdb_id
   and dalias_data_zdb_id = mrkr_zdb_id
   and dalias_group_id = 1 --'alias'
   and dalias_alias not like "% %"
   and dalias_alias not like "%;%"
   and vt.source = 'vega' and vt.feature = 'transcript'
   and vg.source = 'vega' and vg.feature = 'gene'
   and dblink_acc_num = vt.id
   and vt.parent = vg.id
 group by 1,3,7,9
 order by 1,4,5,9
;


--! <!--|ROOT_PATH|-->/home/data_transfer/Downloads/GFF3/
! /usr/bin/rm -f <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias.gff3
! gather_alias.awk <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias_scattered.tmp >  <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias.gff3

! echo "gather_alias.awk -> zfin_gene_alias.gff3"
! /usr/bin/wc -l <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias.gff3
! /usr/bin/rm -f <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias_scattered.tmp
! echo ""

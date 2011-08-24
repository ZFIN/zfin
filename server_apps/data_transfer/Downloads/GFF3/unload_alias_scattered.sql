! echo "unload_alias_scattered.sql -> zfin_gene_alias_scattered.tmp(deleted)"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias_scattered.tmp' DELIMITER "	"

select  -----------------------------ZDBID ---------------------------    
			vg.gff_seqname,
           "ZFIN" source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene' else  'gene' end  feature,
           min(vg.gff_start) gstart,
           max(vg.gff_end)   gend,
           "." score,
           vg.gff_strand,
           "." gff_frame,
       'gene_id=' || gene.mrkr_zdb_id
       ||';Name=' || gene.mrkr_abbrev
       ||';Alias='|| gene.mrkr_zdb_id  attribute
 from  gff3 vt, gff3 vg, db_link tscript ,marker_relationship, marker gene
 where gene.mrkr_type[1,4] = "GENE"  -- & GENEP
   and mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and tscript.dblink_linked_recid = mrel_mrkr_2_zdb_id
   and vt.gff_source = 'vega' and vt.gff_feature = 'transcript'
   and vg.gff_source = 'vega' and vg.gff_feature = 'gene'
   and tscript.dblink_acc_num = vt.gff_ID
   and vt.gff_Parent = vg.gff_ID
 group by 1,3,7,9
-- RefSeq
union
select    vg.gff_seqname,
           "ZFIN" gff_source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene' else  'gene' end  feature,
           min(vg.gff_start) gstart,
           max(vg.gff_end)   gend,
           '.' gff_score,
           vg.gff_strand,
           '.' gff_frame,
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
   and vt.gff_source = 'vega' and vt.gff_feature = 'transcript'
   and vg.gff_source = 'vega' and vg.gff_feature = 'gene'
   and tscript.dblink_acc_num = vt.gff_ID
   and vt.gff_Parent = vg.gff_ID
group by 1,3,7,9
---------------------------------------------------------
union -- ottdarG
select vg.gff_seqname,
           "ZFIN" gff_source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene' else 'gene' end  feature,
           min(vg.gff_start) gstart,
           max(vg.gff_end)   gend,
           "." gff_score,
           vg.gff_strand,
           "." gff_frame,
       'gene_id='  || gene.mrkr_zdb_id
       ||';Name='  || gene.mrkr_abbrev
       ||';Alias=' || vg.gff_ID
       attribute
 from  gff3 vt, gff3 vg, db_link, marker_relationship, marker gene
 where mrkr_type[1,4] = "GENE"
   and mrel_mrkr_1_zdb_id = mrkr_zdb_id
   and dblink_linked_recid = mrel_mrkr_2_zdb_id
   and vt.gff_source = 'vega'
   and vg.gff_source = 'vega'
   and vt.gff_feature = 'transcript'
   and vg.gff_feature = 'gene'
   and dblink_acc_num = vt.gff_ID
   and vt.gff_Parent = vg.gff_ID
 group by 1,3,7,9
---------------------------------------------------------
union -- data alias
select vg.gff_seqname,
           "ZFIN" gff_source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene' else 'gene' end  feature,
           min(vg.gff_start) gstart,
           max(vg.gff_end)   gend,
           "." gff_score,
           vg.gff_strand,
           "." gff_frame,
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
   and vt.gff_source = 'vega'
   and vg.gff_source = 'vega'
   and vt.gff_feature = 'transcript'
   and vg.gff_feature = 'gene'
   and dblink_acc_num = vt.gff_ID
   and vt.gff_Parent = vg.gff_ID
 group by 1,3,7,9
 order by 1,3,5,9
;


--! <!--|ROOT_PATH|-->/home/data_transfer/Downloads/GFF3/
! /usr/bin/rm -f <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias.gff3
! gather_alias.awk <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias_scattered.tmp >  <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias.gff3

! echo "gather_alias.awk -> zfin_gene_alias.gff3"
! /usr/bin/wc -l <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias.gff3
! /usr/bin/rm -f <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_gene_alias_scattered.tmp
! echo ""

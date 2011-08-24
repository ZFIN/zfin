! echo "E_unload_alias_scattered.sql -> E_zfin_gene_alias_scattered.tmp(deleted)"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_zfin_gene_alias_scattered.tmp' DELIMITER "	"
select ------------------------------ ZDBID----------------------------
	zeg_seqname,
	zeg_source,
	zeg_feature,
	min(zeg_start) gstart,
	max(zeg_end)   gend,
	zeg_score,
	zeg_strand,
	zeg_frame,
	zeg_ID_Name ||';Alias='|| zeg_Alias  attribute
 from zfin_ensembl_gene
 group by 1,2,3,6,7,8,9
union ------------------------------ RefSeq----------------------------
select 
	zeg_seqname,
	zeg_source,
	zeg_feature,
	min(zeg_start) gstart,
	max(zeg_end)   gend,
	zeg_score,
	zeg_strand,
	zeg_frame,
	zeg_ID_Name ||';Alias='|| refseq.dblink_acc_num attribute
 from  zfin_ensembl_gene, db_link refseq
 where refseq.dblink_fdbcont_zdb_id  in ('ZDB-FDBCONT-040412-38','ZDB-FDBCONT-040412-39') -- protein as well
   and refseq.dblink_linked_recid ==  zeg_Alias
group by 1,2,3,6,7,8,9
union ------------------------------ensdarG---------------------------
select
	zeg_seqname,
	zeg_source,
	zeg_feature,
	min(zeg_start) gstart,
	max(zeg_end)   gend,
	zeg_score,
	zeg_strand,
	zeg_frame,
	zeg_ID_Name ||';Alias='|| vGdbl.dblink_acc_num attribute
 from zfin_ensembl_gene, db_link vGdbl
 where vGdbl.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-14'
   and vGdbl.dblink_linked_recid = zeg_alias
 group by 1,2,3,6,7,8,9
union --------------------------------ottdarT--------------------------------
select
	zeg_seqname,
	zeg_source,
	zeg_feature,
	min(zeg_start) gstart,
	max(zeg_end)   gend,
	zeg_score,
	zeg_strand,
	zeg_frame,
	zeg_ID_Name ||';Alias='|| tscript_load_id attribute
 from zfin_ensembl_gene, transcript, marker_relationship
 where mrel_mrkr_1_zdb_id = zeg_alias
   and tscript_mrkr_zdb_id == mrel_mrkr_2_zdb_id
   and mrel_type == 'gene produces transcript'
 group by 1,2,3,6,7,8,9
union -------------------------------- data alias -----------------------------
select
	zeg_seqname,
	zeg_source,
	zeg_feature,
	min(zeg_start) gstart,
	max(zeg_end)   gend,
	zeg_score,
	zeg_strand,
	zeg_frame,
	zeg_ID_Name ||';Alias='|| dalias_alias_lower
       attribute
 from  zfin_ensembl_gene, data_alias
 where dalias_data_zdb_id = zeg_alias
   and dalias_group_id == 1 --'alias'
   and dalias_alias not like "% %"
   and dalias_alias not like "%;%"
 group by 1,2,3,6,7,8,9

 order by 1,4,5,9
;

--! <!--|ROOT_PATH|-->/home/data_transfer/Downloads/GFF3/
! /usr/bin/rm -f <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_zfin_gene_alias.gff3
! echo "gather_alias.awk -> E_zfin_gene_alias.gff3"
! gather_alias.awk <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_zfin_gene_alias_scattered.tmp > <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_zfin_gene_alias.gff3
! /usr/bin/rm -f <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_zfin_gene_alias_scattered.tmp
! /usr/bin/wc -l <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_zfin_gene_alias.gff3
! echo ""


{
--select ---------------------------- zdbid--------------------------------
--	zeg_seqname,
--	zeg_source,
--	zeg_feature,
--	min(zeg_start) gstart,
--	max(zeg_end)   gend,
--	zeg_score,
--	zeg_strand,
--	zeg_frame,
--	zeg_ID_Name ||';Alias='|| zeg_Alias  attribute
-- from zfin_ensembl_gene
-- group by 1,2,3,6,7,8,9
--union 
}

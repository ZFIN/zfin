-- Script to create data files for Sanger to download.
--

--
-- Vega Expression
--	gene id, gene symbol, probe id ,probe symbol,probe genbank accession,expression type, expression pattern zfin id

-- generate a file with genes and associated expression patterns

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Sanger/VegaXpat.txt'
 DELIMITER "	"  
 select gene.mrkr_zdb_id[1,26]	gene_zdb,
	   gene.mrkr_abbrev[1,20]	gene_sym,
	   probe.mrkr_zdb_id[1,26]	probe_zdb,
	   probe.mrkr_abbrev[1,20]	probe_sym,
	   dblink_acc_num[1,10]		genbank_acc,
	   xpat_assay_name[1,20]	assay_type, 
	   xpat_zdb_id[1,26]		xpad_zdb_id, 
	   recattrib_source_zdb_id[1,26] pub_zdb
 from marker gene, marker probe, 
	  expression_pattern_assay, 
	  expression_pattern, 
	  marker_relationship,
	  db_link,
	  record_attribution,
	  foreign_db_contains

 where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id 
 and mrel_mrkr_2_zdb_id = xpat_probe_zdb_id 
 and xpat_assay_name = xpatassay_name
 and probe.mrkr_zdb_id = xpat_probe_zdb_id
 and probe.mrkr_zdb_id = dblink_linked_recid
 and dblink_fdbcont_zdb_id = fdbcont_zdb_id
 and fdbcont_fdb_db_name in ('Genbank','RefSeq')
 and fdbcont_fdbdt_data_type = 'cDNA'
 and xpat_zdb_id = recattrib_data_zdb_id
 union
  select gene.mrkr_zdb_id[1,26]	gene_zdb,
	   gene.mrkr_abbrev[1,20]	gene_sym,
	   probe.mrkr_zdb_id[1,26]	probe_zdb,
	   probe.mrkr_abbrev[1,20]	probe_sym,
	   dblink_acc_num[1,10]		genbank_acc,
	   xpat_assay_name[1,20]	assay_type, 
	   xpat_zdb_id[1,26]		xpad_zdb_id, 
	   recattrib_source_zdb_id[1,26] pub_zdb
 from marker gene, marker probe, 
 	  expression_pattern_assay, 
 	  expression_pattern, 
	  marker_relationship,
	  db_link,
	  record_attribution,
	  foreign_db_contains
	   
 where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id 
 and mrel_mrkr_2_zdb_id = xpat_probe_zdb_id 
 and xpat_assay_name = xpatassay_name
 and probe.mrkr_zdb_id = xpat_probe_zdb_id
 and gene.mrkr_zdb_id = dblink_linked_recid
 and dblink_fdbcont_zdb_id = fdbcont_zdb_id
 and fdbcont_fdb_db_name in ('Genbank','RefSeq')
 and fdbcont_fdbdt_data_type = 'cDNA'
 and xpat_zdb_id = recattrib_data_zdb_id
 order by 1,3,7;
----------------------------------------------------
-- sequence links for genes 
-- chose RefSeqs if they exist
select gene.mrkr_zdb_id[1,26]	gene_zdb,
	   gene.mrkr_abbrev[1,20]	gene_sym,
	   dblink_acc_num[1,10]			genbank_acc
from marker gene, db_link, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and fdbcont_fdb_db_name = 'RefSeq'
and gene.mrkr_zdb_id = dblink_linked_recid
into temp tmp_veg with no log
;

-- try and find associated est accessions for
-- genes with out refseq links

select gene.mrkr_zdb_id[1,26]	gene_zdb,
	   gene.mrkr_abbrev[1,20]	gene_sym,
	   dblink_acc_num[1,10]			genbank_acc
from marker gene, marker est, db_link, marker_relationship, foreign_db_contains
where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id 
and   est.mrkr_zdb_id  = mrel_mrkr_2_zdb_id 
and  mrel_type = 'gene encodes small segment'
and est.mrkr_zdb_id = dblink_linked_recid
and est.mrkr_type  = 'EST'
and gene.mrkr_type = 'GENE'
and dblink_fdbcont_zdb_id = fdbcont_zdb_id
and fdbcont_fdb_db_name ='Genbank'
and gene.mrkr_abbrev[3] <> ':'
and gene.mrkr_zdb_id not in(
	select gene_zdb from tmp_veg
)
into temp tmp_veg_est with no log;

insert into tmp_veg select * from tmp_veg_est;
drop table tmp_veg_est;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Sanger/VegaGeneSeq.txt'
DELIMITER "	"  
select * from tmp_veg
order by 3;

drop table tmp_veg;


-- FPC Alias and Mapping

select dalias_alias,mrkr_zdb_id,mrkr_abbrev 
from data_alias, marker, record_attribution 
where recattrib_source_zdb_id = 'ZDB-PUB-030703-2'
and   recattrib_data_zdb_id   = dalias_zdb_id
and   dalias_data_zdb_id      = mrkr_zdb_id
order by 1,2,3
into temp tmp_fpc_alias with no log;

-- Sanger id, symbol_panel, LG, location, metric
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Sanger/FPC_mapping.unl'
select dalias_alias, mrkr_abbrev || '_' || p.abbrev , or_lg, lg_location, mm.metric
from tmp_fpc_alias, mapped_marker mm ,panels p
where marker_id = mrkr_zdb_id
and   p.zdb_id = refcross_id;

-- Sanger id, ZFIN id, zfin symbol
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Sanger/FPC_alias.unl'
select * from tmp_fpc_alias;

drop table tmp_fpc_alias;


-- CV and XPAT associations for VEGA

unload to 'vega_xpat_stage.unl'
select distinct stg_zdb_id,
           stg_name,
           stg_hours_start,
           stg_hours_end
from stage
where stg_zdb_id not in (
          select stgcon_containeR_zdb_id from stage_contains
)  
order by 3
;

unload to 'vega_xpat_anatomy_item.unl'
select 
    anatitem_zdb_id,
    anatitem_name,
    anathier_name,
    anatitem_start_stg_zdb_id,
    anatitem_end_stg_zdb_id
from anatomy_item, anatomy_hierarchy
where anatitem_type_code = anathier_code
;

unload to 'vega_xpat_anatomy_contains.unl'
select * from anatomy_contains;

--
unload to 'vega_xpat_probe_gene.unl'
select 
    xpat_zdb_id, 
    xpat_probe_zdb_id,
    mrel_mrkr_1_zdb_id,
    xpat_assay_name
from expression_pattern,marker_relationship
where mrel_mrkr_2_zdb_id = xpat_probe_zdb_id
and mrel_type = 'gene encodes small segment'
;

--
unload to 'vega_xpat_stage_anatomy.unl'
select 
    xpatstg_xpat_zdb_id,
    xpatstg_start_stg_zdb_id,
    xpatstg_end_stg_zdb_id,
    xpatanat_anat_item_zdb_id  -- or null 
from expression_pattern_stage, outer expression_pattern_anatomy
where xpatstg_xpat_zdb_id      = xpatanat_xpat_zdb_id
and   xpatstg_start_stg_zdb_id = xpatanat_xpat_start_stg_zdb_id
and   xpatstg_end_stg_zdb_id   = xpatanat_xpat_end_stg_zdb_id
;


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
	   acc_num[1,10]			genbank_acc,
	   xpat_assay_name[1,20]	assay_type, 
	   xpat_zdb_id[1,26]		xpad_zdb_id, 
	   recattrib_source_zdb_id[1,26] pub_zdb
 from marker gene, marker probe, 
	  expression_pattern_assay, 
	  expression_pattern, 
	  marker_relationship,
	  db_link,
	  record_attribution

 where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id 
 and mrel_mrkr_2_zdb_id = xpat_probe_zdb_id 
 and xpat_assay_name = xpatassay_name
 and probe.mrkr_zdb_id = xpat_probe_zdb_id
 and probe.mrkr_zdb_id = linked_recid
 and db_name = 'Genbank'
 and xpat_zdb_id = recattrib_data_zdb_id
 { have decided against including gene accessions till asked
 union
  select gene.mrkr_zdb_id[1,26]	gene_zdb,
	   gene.mrkr_abbrev[1,20]	gene_sym,
	   probe.mrkr_zdb_id[1,26]	probe_zdb,
	   probe.mrkr_abbrev[1,20]	probe_sym,
	   acc_num[1,10]			genbank_acc,
	   xpat_assay_name[1,20]	assay_type, 
	   xpat_zdb_id[1,26]		xpad_zdb_id, 
	   recattrib_source_zdb_id[1,26] pub_zdb
 from marker gene, marker probe, 
 	  expression_pattern_assay, 
 	  expression_pattern, 
	  marker_relationship,
	  db_link,
	  record_attribution
	   
 where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id 
 and mrel_mrkr_2_zdb_id = xpat_probe_zdb_id 
 and xpat_assay_name = xpatassay_name
 and probe.mrkr_zdb_id = xpat_probe_zdb_id
 and gene.mrkr_zdb_id = linked_recid
 and db_name in ('Genbank','RefSeq')
 and xpat_zdb_id = recattrib_data_zdb_id
 }
 order by 7,1,3;

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
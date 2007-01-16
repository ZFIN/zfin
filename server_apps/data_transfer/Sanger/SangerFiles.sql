-- Script to create data files for Sanger to download.
--

--
-- Vega Expression
--	gene id, gene symbol, probe id,probe symbol,expression type, expression pattern zfin id, pub zfin id, fish line zfin id, experiment zfin id

-- generate a file with genes and associated expression patterns

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Sanger/VegaXpat.txt'
 DELIMITER "	"  
 select gene.mrkr_zdb_id gene_zdb, gene.mrkr_abbrev,
        probe.mrkr_zdb_id probe_zdb, probe.mrkr_abbrev,
        xpatex_assay_name, xpatex_zdb_id xpat_zdb, 
        xpatex_source_zdb_id, 
        genox_geno_zdb_id, genox_exp_zdb_id 	
 from expression_experiment
      join genotype_experiment 
	  on genox_zdb_id = xpatex_genox_zdb_id
      join marker gene
	  on gene.mrkr_zdb_id = xpatex_gene_zdb_id
      left join marker probe
	  on probe.mrkr_zdb_id = xpatex_probe_feature_zdb_id
 order by gene_zdb, xpat_zdb, probe_zdb;

----------------------------------------------------
-- sequence links for genes 
-- chose RefSeqs if they exist
select gene.mrkr_zdb_id		gene_zdb,
	   gene.mrkr_abbrev	gene_sym,
	   dblink_acc_num	genbank_acc
from marker gene, db_link, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and fdbcont_fdb_db_name = 'RefSeq'
and gene.mrkr_zdb_id = dblink_linked_recid
into temp tmp_veg with no log
;

-- try and find associated est accessions for
-- genes with out refseq links

select gene.mrkr_zdb_id		gene_zdb,
	   gene.mrkr_abbrev	gene_sym,
	   dblink_acc_num	genbank_acc
from marker gene, marker est, db_link, marker_relationship, foreign_db_contains
where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id 
and   est.mrkr_zdb_id  = mrel_mrkr_2_zdb_id 
and  mrel_type = 'gene encodes small segment'
and est.mrkr_zdb_id = dblink_linked_recid
and est.mrkr_type  = 'EST'
and gene.mrkr_type = 'GENE'
and dblink_fdbcont_zdb_id = fdbcont_zdb_id
and fdbcont_fdb_db_name ='GenBank'
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

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Sanger/vega_xpat_stage.unl'
  select stg_zdb_id,
           stg_name,
           stg_hours_start,
           stg_hours_end
  from stage
  order by stg_hours_start, stg_hours_end desc
;

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Sanger/vega_xpat_anatomy_item.unl'
select 
    anatitem_zdb_id,
    anatitem_name,
    anatitem_start_stg_zdb_id,
    anatitem_end_stg_zdb_id
from anatomy_item
;

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Sanger/vega_xpat_anatomy_relationship.unl'
select anatrel_anatitem_1_zdb_id, anatrel_anatitem_2_zdb_id 
  from anatomy_relationship;


unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Sanger/vega_xpat_stage_anatomy.unl'
select 
    xpatres_xpatex_zdb_id,
    xpatres_start_stg_zdb_id,
    xpatres_end_stg_zdb_id,
    xpatres_anat_item_zdb_id   
from expression_result;


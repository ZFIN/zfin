-- Script to create data files for public download.
--
-- We extract several different kinds of information:
-- 
-- All genetic markers (includes genes, ests, sslps, etc.)
--	zfin id, name, symbol, type, OR_lg
--	
-- Synonyms  (for any item in all genetic markers file) There may be multiple lines 	
--   per zfin id
--	zfin id, synonym 
--
-- Orthology - separate files for: D	
--   zebrafish - human
--	zfin id , zebrafish symbol, human symbol, OMIM id, LocusLink id
--   zebrafish - mouse
--	zfin id , zebrafish symbol, mouse symbol, MGI id, LocusLink id
--   zebrafish - fly
--	zfin id,  zebrafish symbol, fly symbol,  Flybase id
--   zebrafish - yeast
--	zfin id,  zebrafish symbol, yeast symbol,  SGD id
--
-- Gene Onotology-
--	A copy of the file we send to GO.
--
-- Gene Expression
--	gene zfin id, gene symbol, probe zfin id ,probe symbol,probe genbank accession,expression type, expression pattern zfin id



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
 {
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

-- clean up

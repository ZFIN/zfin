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
--	zfin id , zebrafish symbol, human symbol, OMIM id, Entrez Gene id
--   zebrafish - mouse
--	zfin id , zebrafish symbol, mouse symbol, MGI id, Entrez Gene id
--   zebrafish - fly
--	zfin id,  zebrafish symbol, fly symbol,  Flybase id
--   zebrafish - yeast
--	zfin id,  zebrafish symbol, yeast symbol,  SGD id
--
-- Gene Onotology-
--	A copy of the file we send to GO.
--
-- Gene Expression
--	gene zfin id , gene symbol, probe zfin id, probe name, expression type,
--      expression pattern zfin id, pub zfin id, fish line zfin id, 
--      experiment zfin id
--
-- Mapping data
--	zfin id, symbol, panel symbol, LG, loc, metric
--
-- Sequence data - separate files for GenBank, RefSeq, Entrez Gene, Unigene, 
-- SWISS-PROT, Interpro, GenPept and Vega (genes and transcripts)
-- as well as sequences indirectly associated with genes
--	zfin id, symbol, accession number
--	
-- Alleles
--	zfin id, allele, locus abbrev,locus name, pheno_keywords, locus id corresponding zfin gene id, gene symbol
--
-- create genetic markers file
--
-- Morpholino data
--      zfin id of gene, gene symbol, zfin id of MO, MO symbol, public note

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genetic_markers.txt' 
  DELIMITER "	" 
  select mrkr_zdb_id, mrkr_abbrev, mrkr_name, mrkr_type
    from marker order by 1;

-- create other names file 

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/marker_alias.txt' 
  DELIMITER "	" select distinct mrkr_zdb_id, dalias_alias from marker, data_alias where dalias_data_zdb_id = mrkr_zdb_id order by 1;


-- Create the orthologues files - mouse, human, fly and yeast

create table ortho_exp (
  gene_id varchar(50),
  ortho_id varchar(50),
  zfish_name varchar(120),
  zfish_abbrev varchar(40),
  organism varchar(30),
  ortho_name varchar(120),
  ortho_abbrev varchar(15), 
  flybase varchar(50),
  entrez varchar(50),
  mgi varchar(50),
  omim varchar(50),
  sgd varchar(50)   
);

insert into ortho_exp 
  select distinct c_gene_id, zdb_id, mrkr_name, mrkr_abbrev, organism, ortho_name, 
         ortho_abbrev, 
         NULL::varchar(50), 
         NULL::varchar(50), 
         NULL::varchar(50),
         NULL::varchar(50),
         NULL::varchar(50)
    from orthologue,marker
	where c_gene_id = mrkr_zdb_id;

update ortho_exp
	set flybase = (select distinct dblink_acc_num from db_link, orthologue o, foreign_db_contains
	   where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	        and fdbcont_fdb_db_name = 'FLYBASE'
	        and fdbcont_organism_common_name = o.organism
		and o.zdb_id = dblink_linked_recid
		and ortho_id = o.zdb_id);

update ortho_exp
	set Entrez = (select dblink_acc_num from db_link, orthologue o, foreign_db_contains
	   where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	        and fdbcont_fdb_db_name = 'Entrez Gene'
	        and fdbcont_organism_common_name = o.organism
		and o.zdb_id = dblink_linked_recid
		and ortho_id = o.zdb_id);
		

update ortho_exp
	set mgi = (select dblink_acc_num from db_link , orthologue o, foreign_db_contains
	   where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	        and fdbcont_fdb_db_name = 'MGI'
	        and fdbcont_organism_common_name = o.organism
		and o.zdb_id = dblink_linked_recid
		and ortho_id = o.zdb_id);	
		
	
update ortho_exp
	set omim = (select distinct dblink_acc_num from db_link, orthologue o, foreign_db_contains
	   where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	        and fdbcont_fdb_db_name = 'OMIM'
	        and fdbcont_organism_common_name = o.organism
		and o.zdb_id = dblink_linked_recid
		and ortho_id = o.zdb_id);

 
update ortho_exp
	set sgd = (select dblink_acc_num from db_link, orthologue o, foreign_db_contains
	   where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	        and fdbcont_fdb_db_name = 'SGD'
	        and fdbcont_organism_common_name = o.organism
		and o.zdb_id = dblink_linked_recid
		and ortho_id = o.zdb_id);	


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/fly_orthos.txt' 
  DELIMITER "	"  
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, flybase 
    from ortho_exp where organism = 'Fly' order by 1;


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/human_orthos.txt' 
  DELIMITER "	" 
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, omim, entrez
    from ortho_exp where organism = 'Human' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mouse_orthos.txt' 
  DELIMITER "	" 
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, mgi, entrez  
    from ortho_exp where organism = 'Mouse' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/yeast_orthos.txt' 
  DELIMITER "	" 
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, sgd 
    from ortho_exp where organism = 'Yeast' order by 1;

-- generate a file with genes and associated expression experiment

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat.txt'
 DELIMITER "	"  
 select gene.mrkr_zdb_id gene_zdb, gene.mrkr_abbrev,
        probe.mrkr_zdb_id probe_zdb, probe.mrkr_abbrev,
        xpatex_assay_name, xpatex_zdb_id xpat_zdb, 
        xpatex_source_zdb_id, 
        featexp_genome_feature_zdb_id, featexp_exp_zdb_id 	
 from expression_experiment
      join feature_experiment 
	  on featexp_zdb_id = xpatex_featexp_zdb_id
      join marker gene
	  on gene.mrkr_zdb_id = xpatex_gene_zdb_id
      left join marker probe
	  on probe.mrkr_zdb_id = xpatex_probe_feature_zdb_id
 order by gene_zdb, xpat_zdb, probe_zdb;



-- Create mapping data file
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mappings.txt'
 DELIMITER "	" select marker_id, mrkr_abbrev, p.abbrev,or_lg, lg_location, p.metric 
 from mapped_marker, panels p, marker m 
 where refcross_id = p.zdb_id and marker_id = mrkr_zdb_id 
 order by 1;

-- Generate sequence data files for GenBank, RefSeq, Entrez, UniGene, SWISS-PROT, Interpro and GenPept

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genbank.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev, dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'GenBank' order by 1;


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/refseq.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'RefSeq' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/entrezgene.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'Entrez Gene' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/unigene.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'UniGene' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/swissprot.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'SWISS-PROT' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/interpro.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'InterPro' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pfam.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'Pfam' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genpept.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and fdbcont_zdb_id = dblink_fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'GenPept' order by 1;
      
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and fdbcont_zdb_id = dblink_fdbcont_zdb_id
	  and fdbcont_fdb_db_name in ('VEGA','PREVEGA') order by 1;      

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega_transcript.txt'
 DELIMITER "    " select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, 
foreign_db_contains
        where mrkr_zdb_id = dblink_linked_recid
          and fdbcont_zdb_id = dblink_fdbcont_zdb_id
          and fdbcont_fdb_db_name = 'Vega_Trans' order by 1;

-- Generate alleles file

create table alleles_exp (
  fish_id varchar(50),
  allele varchar(40),
  abbrev varchar(30),
  locus_name varchar(120),
  locus_id varchar(50),
  pheno_keywords	lvarchar,
  gene_id varchar(50),
  gene_abbrev varchar (40) 
);

insert into alleles_exp
  select f.zdb_id, allele, 
		 case when l.abbrev = "" then NULL else l.abbrev end, 
		 l.locus_name, 
		 l.zdb_id, substr(pheno_keywords,2), '', ''
    from fish f, locus l 
   where line_type = 'mutant'
	 and f.locus = l.zdb_id;


update alleles_exp set (gene_id, gene_abbrev) = 
	    (( select mrkr_zdb_id, mrkr_abbrev 
		     from marker, locus l
		    where alleles_exp.locus_id=l.zdb_id 
		      and l.cloned_gene = mrkr_zdb_id ));

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/alleles.txt'
 DELIMITER "	" select fish_id, allele, abbrev, locus_name, locus_id, pheno_keywords, gene_abbrev, gene_id from alleles_exp order by 1;


-- generate a file with zdb history data

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zdb_history.txt'
 DELIMITER "	" select zrepld_old_zdb_id, zrepld_new_zdb_id from zdb_replaced_data;
 
-- clean up
drop table ortho_exp;
drop table alleles_exp;


-- indirect sequence links for genes

select distinct gene.mrkr_zdb_id gene_zdb,
       gene.mrkr_abbrev gene_sym,
       dblink_acc_num genbank_acc
from marker gene, marker est, db_link, marker_relationship, foreign_db_contains
where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id 
and   est.mrkr_zdb_id  = mrel_mrkr_2_zdb_id 
and  mrel_type = 'gene encodes small segment'
and est.mrkr_zdb_id = dblink_linked_recid
and est.mrkr_type  in ('EST','CDNA')
and gene.mrkr_type = 'GENE'
and dblink_fdbcont_zdb_id = fdbcont_zdb_id
and fdbcont_fdb_db_name = 'GenBank'
into temp tmp_veg with no log;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/gene_seq.txt'
 DELIMITER "	"  
select * from tmp_veg
order by 1,3;

drop table tmp_veg; 


-- Anatomical Ontologies

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_ontology.txt'
DELIMITER "	" 
select anatrel_anatitem_1_zdb_id, anatrel_anatitem_2_zdb_id 
  from anatomy_relationship;

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/stage_ontology.txt'
 DELIMITER "	" 
  select stg_zdb_id,
           stg_name,
           stg_hours_start,
           stg_hours_end
  from stage
  order by stg_hours_start, stg_hours_end desc
;

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_item.txt'
 DELIMITER "	" 
select 
    anatitem_zdb_id,
    anatitem_name,
    anatitem_start_stg_zdb_id,
    anatitem_end_stg_zdb_id
from anatomy_item
;

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_relationship.txt'
DELIMITER "	" 
select anatrel_anatitem_1_zdb_id, anatrel_anatitem_2_zdb_id, anatrel_dagedit_id
  from anatomy_relationship;


unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat_stage_anatomy.txt'
 DELIMITER "	" 
select xpatres_xpatex_zdb_id,
       xpatres_start_stg_zdb_id,
       xpatres_end_stg_zdb_id,
       xpatres_anat_item_zdb_id 
  from expression_result;


-- Morpholino data
-- unloaded Morpholino data would have HTML tags in public note column,
-- which will be removed by Perl script
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/Morpholinos2.txt'
 DELIMITER "	"
select gn.mrkr_zdb_id, gn.mrkr_abbrev, mo.mrkr_abbrev, mrkrseq_sequence, mo.mrkr_comments
  from marker gn, marker mo, marker_sequence, marker_relationship
  where gn.mrkr_zdb_id = mrel_mrkr_2_zdb_id
    and mo.mrkr_zdb_id = mrel_mrkr_1_zdb_id
    and mrel_mrkr_2_zdb_id like "ZDB-GENE-%" 
    and mrel_mrkr_1_zdb_id like "ZDB-MRPHLNO-%"
    and mrel_type = "knockdown reagent targets gene"
    and mo.mrkr_zdb_id = mrkrseq_mrkr_zdb_id
    order by gn.mrkr_abbrev;
  
  


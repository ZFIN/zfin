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
-- Orthology - separate files for: 
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
--	gene zfin id , gene symbol, expression type, expression pattern zfin id
--
-- Mapping data
--	zfin id, symbol, panel symbol, LG, loc, metric
--
-- Sequence data - separate files for Genbank, RefSeq, LocusLink, Unigene, 
-- SWISS-PROT, Interpro
--	zfin id, symbol, accession number
--	
-- Alleles
--	zfin id, allele, locus abbrev,locus name, locus id corresponding zfin gene id, gene symbol

-- create genetic markers file

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
  zfish_name varchar(120),
  zfish_abbrev varchar(20),
  organism varchar(30),
  ortho_name varchar(120),
  ortho_abbrev varchar(15), 
  flybase varchar(50),
  locuslink varchar(50),
  mgi varchar(50),
  omim varchar(50),
  sgd varchar(50)   
);

insert into ortho_exp 
  select c_gene_id, mrkr_name, mrkr_abbrev, organism, ortho_name, ortho_abbrev,NULL::varchar(50),NULL::varchar(50),NULL::varchar(50),NULL::varchar(50),NULL::varchar(50)
    from orthologue,marker
	where c_gene_id = mrkr_zdb_id;

update ortho_exp
	set flybase = (select acc_num from db_link, orthologue o
	   where db_name = 'FLYBASE'
		and o.zdb_id = linked_recid
		and gene_id = c_gene_id);

update ortho_exp
	set locuslink = (select acc_num from db_link
	   where db_name = 'LocusLink'
		and gene_id = linked_recid);
		

update ortho_exp
	set mgi = (select acc_num from db_link , orthologue o
	   where db_name = 'MGI'
		and o.zdb_id = linked_recid
		and gene_id = c_gene_id);	
		
	
update ortho_exp
	set omim = (select acc_num from db_link, orthologue o
	   where db_name = 'OMIM'
		and gene_id = linked_recid);

 
update ortho_exp
	set sgd = (select acc_num from db_link, orthologue o 
	   where db_name = 'SGD'
		and o.zdb_id = linked_recid
		and gene_id = c_gene_id);	


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/fly_orthos.txt' 
  DELIMITER "	"  
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, flybase 
    from ortho_exp where organism = 'Fly' order by 1;


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/human_orthos.txt' 
  DELIMITER "	" 
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, omim, locuslink
    from ortho_exp where organism = 'Human' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mouse_orthos.txt' 
  DELIMITER "	" 
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, mgi, locuslink  
    from ortho_exp where organism = 'Mouse' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/yeast_orthos.txt' 
  DELIMITER "	" 
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, sgd 
    from ortho_exp where organism = 'Yeast' order by 1;

-- generate a file with genes and associated expression patterns


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat.txt'
 DELIMITER "	"  select mrkr_zdb_id, mrkr_abbrev, xpat_assay_name, xpat_zdb_id from marker, expression_pattern_assay, expression_pattern, marker_relationship where mrkr_zdb_id = mrel_mrkr_1_zdb_id and mrel_mrkr_2_zdb_id = xpat_probe_zdb_id and xpat_assay_name = xpatassay_name order by 1;


-- Create mapping data file
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mappings.txt'
 DELIMITER "	" select marker_id, mrkr_abbrev, p.abbrev,or_lg, lg_location, p.metric from mapped_marker, panels p, marker m where refcross_id = p.zdb_id and marker_id = mrkr_zdb_id order by 1;

-- Generate sequence data files for Genbank, RefSeq, LocusLink, UniGene, SWISS-PROT, Interpro

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genbank.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev, acc_num from marker, db_link
	where mrkr_zdb_id = linked_recid
	  and db_name = 'Genbank' order by 1;


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/refseq.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,acc_num from marker, db_link
	where mrkr_zdb_id = linked_recid
	  and db_name = 'RefSeq' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/locuslink.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,acc_num from marker, db_link
	where mrkr_zdb_id = linked_recid
	  and db_name = 'LocusLink' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/unigene.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,acc_num from marker, db_link
	where mrkr_zdb_id = linked_recid
	  and db_name = 'UniGene' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/swissprot.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,acc_num from marker, db_link
	where mrkr_zdb_id = linked_recid
	  and db_name = 'SWISS-PROT' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/interpro.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,acc_num from marker, db_link
	where mrkr_zdb_id = linked_recid
	  and db_name = 'InterPro' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pfam.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,acc_num from marker, db_link
	where mrkr_zdb_id = linked_recid
	  and db_name = 'Pfam' order by 1;

-- Generate alleles file

create table alleles_exp (
  fish_id varchar(50),
  allele varchar(20),
  abbrev varchar(20),
  locus_name varchar(80),
  locus_id varchar(50),
  gene_id varchar(50),
  gene_abbrev varchar (20) 
);

insert into alleles_exp 
  select f.zdb_id, allele, l.abbrev, l.locus_name, l.zdb_id, NULL::varchar(50),NULL::varchar(20)
    from fish f, locus l where line_type = 'mutant'
	and f.locus = l.zdb_id;


update alleles_exp
    set (gene_id, gene_abbrev) = 
	    (( select g.zdb_id, g.abbrev 
		 from gene g, locus l
		 where alleles_exp.locus_id=l.zdb_id 
		   and l.cloned_gene=g.zdb_id ))
      where exists 
	      ( select 'x' 
		  from gene, locus 
		  where alleles_exp.locus_id = locus.zdb_id 
		    and locus.cloned_gene = gene.zdb_id );

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/alleles.txt'
 DELIMITER "	" select fish_id, allele, abbrev, locus_name, locus_id, gene_abbrev, gene_id from alleles_exp order by 1;


-- generate a file with zdb history data

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zdb_history.txt'
 DELIMITER "	" select zrepld_old_zdb_id, zrepld_new_zdb_id from zdb_replaced_data;

-- clean up
drop table ortho_exp;
drop table alleles_exp;





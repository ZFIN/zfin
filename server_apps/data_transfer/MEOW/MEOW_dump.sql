-- Command file to dump out GENE information for the MEOW server. Produces
-- 5 files in the FTP pub/transfer/MEOW directory.  This is currently (2001/02)
-- run as a cron job every week.  Don Gilbert at MEOW is then responsible for
-- picking up the files.  MEOW *probably* picks up the files through FTP, but
-- they could also pick them up through HTTP via the home/transfer directory,
-- which is a symbolic link to the FTP directory.

-- Here are the files:
--   zfin_genes.txt -- the main file with all ZFIN genes.(mapped and unmapped)
--   zfin_genes_locuslink.txt  -- same as above file except includes mapping info for LocusLink 
--   zfin_genes_mutants.txt - this file list known correspondences between genes and mutants
--   zfin_orthos.txt -- all known orthologues, indexed by gene_id
--   zfin_refs.txt -- all publications linked to genes, indexed by gene id
--   zfin_dblinks -- all links from genes to sequence DBs.
--   zfin_ortholinks -- similar to zfin_dblinks but is links from ortho to
--       their species DB files.

-- Create the main zfin_genes file

create table meow_exp1 (
  zdb_id varchar(50),
  mname varchar(80),
  abbrev varchar(20),
  OR_lg integer
);

-- get panel mappings

insert into meow_exp1 
  select distinct zdb_id,gene_name,abbrev,OR_lg
    from all_genes
   where private = 'f'
     and exists (select 'x' from panels
	 where panel_id = panels.zdb_id)
     and zdb_id like '%GENE%';

-- include gene names from private mappings when gene does not have any public mappings
insert into meow_exp1 
  select distinct zdb_id,gene_name,abbrev,0
    from all_genes a 
   where private = 't' 
     and not exists 
       ( select 'x' 
	   from all_genes b 
	  where a.zdb_id = b.zdb_id 
	    and private = 'f') 
     and zdb_id like '%GENE%';

-- get independent linkages

insert into meow_exp1 (zdb_id,mname,abbrev,OR_lg) 
  select distinct zdb_id,gene_name,abbrev,OR_lg 
    from all_genes a
   where private = 'f'
     and panel_id like '%LINK%'
     and zdb_id like '%GENE%'
      and not exists 
       ( select 'x' 
	   from all_genes b 
	  where a.zdb_id = b.zdb_id 
	    and panel_id  like '%REFCROSS%') ;


--  Add in  unmapped genes
insert into meow_exp1 (zdb_id,mname,abbrev,OR_lg) 
  select zdb_id,gene_name,abbrev,0 
    from all_genes 
   where panel_id = 'na'
      and zdb_id like '%GENE%';


-- Sanity check to see that there are no anomalies. These two queries 
-- should return same result. If not, there are redundant GENE records, 
-- or the same  gene mapped to different LGs (rare) by two experiments.
-- The solution in former case is to blast redundant records and rerun. In
-- latter case, be sure to find the redundant record (eg looking for redundant
-- abbrevs for instance) and delete one of them from output file.
-- At this time, eg, wnt4 is mapped MOP(LG15),GAT(LG11). I asked John P. and 
-- he says the GAT result is the one to use. So I delete the wnt4,LG15 
-- record from output file. In fact, since I know of this problem, I'll just
-- build the delete into the script here!
-- removed 11/13/00  this mapping inconsistency has been resolved.  delete from meow_exp1 where abbrev='wnt4' and OR_lg='15';

select count(*) 
  from meow_exp1;

select count(distinct abbrev) 
  from meow_exp1;

--  Okay, now write it to a file
UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_genes.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp1;


-- Create the main zfin_genes file for Locus Link - they would like mapping info also

create table meow_expll (
  zdb_id varchar(50),
  gene_name varchar(80),
  abbrev varchar(20),
  OR_lg integer,
  location numeric(6,2),
  panel_id varchar(50),
  panel_abbrev varchar(10),
  metric varchar(5)
);

-- get panel mappings

insert into meow_expll 
  select distinct zdb_id,gene_name,abbrev,OR_lg,lg_location,panel_id,panel_abbrev,metric 
    from all_genes
   where private = 'f'
     and exists (select 'x' from panels
	 where panel_id = panels.zdb_id)
     and zdb_id like '%GENE%';

-- include gene names from private mappings when gene does not have any public mappings
insert into meow_expll 
  select distinct zdb_id,gene_name,abbrev,0,0,null::varchar(50),null::varchar(10),null::varchar(5) 
    from all_genes a 
   where private = 't' 
     and not exists 
       ( select 'x' 
	   from all_genes b 
	  where a.zdb_id = b.zdb_id 
	    and private = 'f') 
     and zdb_id like '%GENE%';


-- get independent linkages

insert into meow_expll (zdb_id,gene_name,abbrev,OR_lg) 
  select distinct zdb_id,gene_name,abbrev,OR_lg 
    from all_genes a
   where private = 'f'
     and panel_id like '%LINK%'
     and zdb_id like '%GENE%'
      and not exists 
       ( select 'x' 
	   from all_genes b 
	  where a.zdb_id = b.zdb_id 
	    and panel_id  like '%REFCROSS%') ;


--  Add in  unmapped genes
insert into meow_expll (zdb_id,gene_name,abbrev,OR_lg) 
  select zdb_id,gene_name,abbrev,0 
    from all_genes 
   where panel_id = 'na'
      and zdb_id like '%GENE%';


-- Sanity check to see that there are no anomalies. These two queries 
-- should return same result. If not, there are redundant GENE records, 
-- or the same  gene mapped to different LGs (rare) by two experiments.
-- The solution in former case is to blast redundant records and rerun. In
-- latter case, be sure to find the redundant record (eg looking for redundant
-- abbrevs for instance) and delete one of them from output file.
-- At this time, eg, wnt4 is mapped MOP(LG15),GAT(LG11). I asked John P. and 
-- he says the GAT result is the one to use. So I delete the wnt4,LG15 
-- record from output file. In fact, since I know of this problem, I'll just
-- build the delete into the script here!
-- removed 11/13/00  this mapping inconsistency has been resolved.  delete from meow_exp1 where abbrev='wnt4' and OR_lg='15';

select count(*) 
  from meow_expll;

select count(distinct abbrev) 
  from meow_expll;

--  Okay, now write it to a file
UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_genes_locuslink.txt' 
  DELIMITER "	" 
  select * 
    from meow_expll;


-- Create the file of known correspondences
create table meow_mutant (
  gene_id varchar(50),
  gene_abbrev varchar(20),
  locus_id varchar(50),
  locus_abbrev varchar(10)
);  

insert into meow_mutant (gene_id,gene_abbrev,locus_id,locus_abbrev)
   select a.zdb_id, a.abbrev, a.cloned_gene, b.mrkr_abbrev from locus a, marker b
   where a.cloned_gene is not null 
   and a.cloned_gene = b.mrkr_zdb_id;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_genes_mutants.txt' 
  DELIMITER "	" 
  select * 
    from meow_mutant;


-- NOW let's create the table of pubs associated with these genes. Easy!
create table meow_exp2 (
  gene_id varchar(50),
  zdb_id varchar(50),
  title lvarchar,
  authors lvarchar ,
  pub_date date ,
  source lvarchar ,
  accession_no varchar(80)
);

insert into meow_exp2 
  select b.source_id, a.zdb_id, a.title, a.authors, a.pub_date, a.source,
         a.accession_no 
    from publication a, int_data_pub b 
   where a.zdb_id=b.target_id 
     and b.source_id in 
       ( select zdb_id 
           from meow_exp1 );

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_pubs.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp2;

-- Now the orthologues!
create table meow_exp3 (
  gene_id varchar(50),
  organism varchar(30),
  ortho_name varchar(50),
  ortho_abbrev varchar(15), 
  ortho_id varchar(50)
);

insert into meow_exp3 
  select c_gene_id, organism, ortho_name, ortho_abbrev, zdb_id
    from orthologue 
   where c_gene_id in 
       ( select zdb_id 
           from meow_exp1 );

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_orthos.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp3;

-- And now the links to sequence DBs
create table meow_exp4 (
  gene_id varchar(50),
  DB_name varchar(50),
  acc_num varchar(50)
);

insert into meow_exp4 
  select linked_recid, DB_name, acc_num 
    from db_link 
   where linked_recid in 
       ( select zdb_id 
           from meow_exp1 );

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_dblinks.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp4;

--  generate the ortho_links file with DB_links to other species DBs
create table meow_exp5 (
  linked_recid varchar(50), 
  DB_name varchar(50), 
  acc_num varchar(50)
);

insert into meow_exp5
  select linked_recid, DB_name, acc_num
    from db_link
   where linked_recid in 
       ( select ortho_id 
         from meow_exp3 )
     and acc_num <> 'DUMMY';

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_ortholinks.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp5;


-- generate a file of cDNAs and assoc GENBANK accession numbers

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/SC.txt'
  DELIMITER "	" select distinct zdb_id, abbrev, acc_num  from all_markers, OUTER db_link  where (mtype = 'EST')  and linked_recid = zdb_id and db_name = 'Genbank' order by 1; 

-- Clean up
drop table meow_exp1;
drop table meow_exp2;
drop table meow_exp3;
drop table meow_exp4;
drop table meow_exp5;
drop table meow_expll;
drop table meow_mutant;

 
-- NOW just notify Don Gilbert that the updated files are there and he
-- can download them!



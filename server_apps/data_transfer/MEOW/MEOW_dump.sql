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
  mname varchar(120),
  abbrev varchar(20),
  OR_lg varchar(2)
);

-- get panel mappings

insert into meow_exp1 
  select distinct zdb_id,gene_name,abbrev,OR_lg
    from all_genes
   where 
         exists (select 'x' from panels
	 where panel_id = panels.zdb_id)
     and zdb_id like '%GENE%';

-- get independent linkages

insert into meow_exp1 (zdb_id,mname,abbrev,OR_lg) 
  select distinct zdb_id,gene_name,abbrev,OR_lg 
    from all_genes a
   where 
         panel_id like '%LINK%'
     and zdb_id like '%GENE%'
      and not exists 
       ( select 'x' 
	   from all_genes b 
	  where a.zdb_id = b.zdb_id 
	    and panel_id  like '%REFCROSS%'
         ) ;


--  Add in  unmapped genes
insert into meow_exp1 (zdb_id,mname,abbrev,OR_lg) 
  select zdb_id,gene_name,abbrev,'0' 
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
-- removed 11/13/00  this mapping inconsistency has been resolved.
--  delete from meow_exp1 where abbrev='wnt4' and OR_lg='15';

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
  gene_name varchar(120),
  abbrev varchar(20),
  OR_lg varchar(2),
  location numeric(6,2),
  panel_id varchar(50),
  panel_abbrev varchar(10),
  metric varchar(5)
);

-- get panel mappings

insert into meow_expll 
  select distinct zdb_id,gene_name,abbrev,OR_lg,lg_location,panel_id,panel_abbrev,metric 
    from all_genes
   where 
         exists (select 'x' from panels
	 where panel_id = panels.zdb_id)
     and zdb_id like '%GENE%';


-- get independent linkages

insert into meow_expll (zdb_id,gene_name,abbrev,OR_lg) 
  select distinct zdb_id,gene_name,abbrev,OR_lg 
    from all_genes a
   where 
         panel_id like '%LINK%'
     and zdb_id like '%GENE%'
      and not exists 
       ( select 'x' 
	   from all_genes b 
	  where a.zdb_id = b.zdb_id 
	    and panel_id  like '%REFCROSS%') ;


--  Add in  unmapped genes
insert into meow_expll (zdb_id,gene_name,abbrev,OR_lg) 
  select zdb_id,gene_name,abbrev,'0'
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
-- removed 11/13/00  this mapping inconsistency has been resolved.
--  delete from meow_exp1 where abbrev='wnt4' and OR_lg='15';

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


-- NOW let's create the table of pubs associated with these genes.
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
  select recattrib_data_zdb_id, zdb_id, title, authors, pub_date, source,
         accession_no 
    from publication, record_attribution
   where zdb_id = recattrib_source_zdb_id
     and recattrib_data_zdb_id in
       ( select zdb_id 
           from meow_exp1 ) ;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_pubs.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp2;

-- Now the orthologues!
create table meow_exp3 (
  gene_id varchar(50),
  organism varchar(30),
  ortho_name varchar(120),
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

insert into meow_exp4 
  select  mrel_mrkr_1_zdb_id, DB_name, acc_num 
    from db_link , marker_relationship
   where   mrel_mrkr_2_zdb_id = linked_recid
        and db_name = 'Genbank'
	and mrel_mrkr_1_zdb_id in 
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
         from meow_exp3 );

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_ortholinks.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp5;


-- generate a file of cDNAs and assoc GENBANK accession numbers

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/SC.txt'
  DELIMITER "	" select distinct mrkr_zdb_id, mrkr_abbrev, acc_num  from marker, OUTER db_link  where (mrkr_type = 'EST')  and linked_recid = mrkr_zdb_id and db_name = 'Genbank' order by 1; 

-- generate a file of anonymous markers  and assoc GENBANK accession numbers
UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/SC_sts.txt'
  DELIMITER "	" select distinct mrkr_zdb_id, mrkr_abbrev, acc_num  from marker, db_link  where mrkr_type in ('STS', 'SSLP','RAPD', 'SSR') and linked_recid = mrkr_zdb_id and db_name = 'Genbank' order by 1; 

-- generate a file with zdb history data

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zdb_history.txt'
 DELIMITER "	" select zrepld_old_zdb_id, zrepld_new_zdb_id from zdb_replaced_data;

-- generate a file with genes and associated expression patterns

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/xpat.txt'
 DELIMITER "	"  select mrkr_zdb_id, xpat_zdb_id from marker, expression_pattern_assay, expression_pattern, marker_relationship where mrkr_zdb_id = mrel_mrkr_1_zdb_id and mrel_mrkr_2_zdb_id = xpat_probe_zdb_id and xpat_assay_name = xpatassay_name;

--- generate mapping data for LocusLink

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/panels.txt' 
  DELIMITER "	" select zdb_id, abbrev, metric from panels; 

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/mappings.txt' 
  DELIMITER "	" select distinct target_id, zdb_id, abbrev, OR_lg, lg_location from paneled_markers  where (zdb_id not like '%FISH%') and (zdb_id not like '%LOCUS%') order by 1;

-- wait to see what to do with mutants  union select distinct a.target_id, b.locus, c.abbrev, a.OR_lg, a.lg_location from paneled_markers a, fish b, locus c where a.zdb_id like '%FISH%' and a.zdb_id = b.zdb_id and b.locus = c.zdb_id

-- comment out selection of zmap makers temporarily   union select target_id, zdb_id, abbrev||'_'||panel_abbrev, OR_lg, lg_location from zmap_pub_pan_mark

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/markers.txt' 
  DELIMITER "	" select distinct zdb_id, abbrev from paneled_markers;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/marker_alias.txt' 

  DELIMITER "	" select distinct mrkr_zdb_id, dalias_alias from marker , data_alias where mrkr_zdb_id = dalias_data_zdb_id order by 1;



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



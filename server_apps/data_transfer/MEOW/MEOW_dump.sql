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
--   zfin_genes_relationships.txt - this file lists genes and 'related' markers

-- In the population of meow_exp1, and meow_expll tables, the all_genes tables is replaced by base tables. The modification is based on the all_genes table generations and the old select SQLs used in this scripts. Two more temporary tables--meow_exp1_dup and meow_expll_dup are added. 
 
-- Create the main zfin_genes file

create temp table meow_exp1_dup (
  zdb_id varchar(50),
  mname varchar(120),
  abbrev varchar(20),
  OR_lg varchar(2)
) with no log;

-- get panel mappings
insert into meow_exp1_dup 
  select distinct mrkr_zdb_id, mrkr_name, mrkr_abbrev, or_lg
    from marker, mapped_marker, panels p
   where mrkr_type like 'GENE%'
     and mrkr_zdb_id = marker_id
     and marker_type <> 'SNP'
     and refcross_id = p.zdb_id;

insert into meow_exp1_dup 
  select distinct a.mrkr_zdb_id, a.mrkr_name, a.mrkr_abbrev, or_lg
    from marker a, marker b, mapped_marker, marker_relationship, panels p
   where a.mrkr_type like 'GENE%'
     and b.mrkr_zdb_id = marker_id
     and a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
     and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
     and refcross_id = p.zdb_id;

create temp table meow_exp1 (
  zdb_id varchar(50),
  mname varchar(120),
  abbrev varchar(20),
  OR_lg varchar(2)
) with no log;

insert into  meow_exp1 
  select distinct * 
    from meow_exp1_dup;

-- get independent linkages

insert into meow_exp1 (zdb_id,mname,abbrev,OR_lg) 
  select distinct mrkr_zdb_id, mrkr_name, mrkr_abbrev, lnkg_or_lg
    from marker, linkage_member, linkage
   where mrkr_zdb_id = lnkgmem_member_zdb_id 
     and lnkgmem_linkage_zdb_id = lnkg_zdb_id 
     and mrkr_type like 'GENE%'
     and mrkr_zdb_id not in (
	 	select zdb_id 
		  from meow_exp1_dup
		);

--  Add in  unmapped genes
insert into meow_exp1 (zdb_id,mname,abbrev,OR_lg) 
  select mrkr_zdb_id,mrkr_name,mrkr_abbrev,'0' 
    from marker
   where mrkr_type like 'GENE%'
     and mrkr_zdb_id not in (
		select lnkgmem_member_zdb_id
		  from linkage_member
		 )
     and mrkr_zdb_id not in (
		select marker_id
		  from mapped_marker
		 )
     and mrkr_zdb_id not in (
		select mrel_mrkr_1_zdb_id
                  from mapped_marker, marker_relationship	
		 where mrel_mrkr_2_zdb_id = marker_id
		);


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
  select distinct *
    from meow_exp1;


-- Create the main zfin_genes file for Locus Link - they would like mapping info also

create temp table meow_expll_dup (
  zdb_id varchar(50),
  gene_name varchar(120),
  abbrev varchar(20),
  OR_lg varchar(2),
  location numeric(6,2),
  panel_id varchar(50),
  panel_abbrev varchar(10),
  metric varchar(5)
) with no log;

-- get panel mappings

insert into meow_expll_dup 
  select distinct mrkr_zdb_id, mrkr_name, mrkr_abbrev, or_lg,lg_location,p.zdb_id,p.abbrev,mm.metric 
    from marker, mapped_marker mm, panels p
   where mrkr_type like 'GENE%'
     and mrkr_zdb_id = marker_id
     and marker_type <> 'SNP'
     and refcross_id = p.zdb_id;

insert into meow_expll_dup
  select distinct a.mrkr_zdb_id, a.mrkr_name, a.mrkr_abbrev, or_lg,lg_location,p.zdb_id,p.abbrev,mm.metric
    from marker a, marker b, mapped_marker mm, marker_relationship, panels p
   where a.mrkr_type like 'GENE%'
     and b.mrkr_zdb_id = marker_id
     and a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
     and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
     and refcross_id = p.zdb_id;

create temp table meow_expll (
  zdb_id varchar(50),
  gene_name varchar(120),
  abbrev varchar(20),
  OR_lg varchar(2),
  location numeric(6,2),
  panel_id varchar(50),
  panel_abbrev varchar(10),
  metric varchar(5)
) with no log;

insert into meow_expll
  select distinct * 
    from meow_expll_dup;

-- get independent linkages

insert into meow_expll (zdb_id,gene_name,abbrev,OR_lg) 
  select distinct mrkr_zdb_id, mrkr_name, mrkr_abbrev, lnkg_or_lg
    from marker, linkage_member, linkage
   where mrkr_zdb_id = lnkgmem_member_zdb_id 
     and lnkgmem_linkage_zdb_id = lnkg_zdb_id 
     and mrkr_type like 'GENE%'
     and mrkr_zdb_id not in (
		select zdb_id
		  from meow_expll_dup
		);


--  Add in  unmapped genes
insert into meow_expll (zdb_id,gene_name,abbrev,OR_lg) 
  select mrkr_zdb_id,mrkr_name,mrkr_abbrev,'0' 
    from marker
   where mrkr_type like 'GENE%'
     and mrkr_zdb_id not in (
		select lnkgmem_member_zdb_id
		  from linkage_member
		  )
     and mrkr_zdb_id not in (
		select marker_id
		  from mapped_marker
		 )
     and mrkr_zdb_id not in (
		select mrel_mrkr_1_zdb_id
                  from mapped_marker, marker_relationship	
		 where mrel_mrkr_2_zdb_id = marker_id
		);


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
  select distinct *
    from meow_expll;


-- Create the file of known correspondences
create temp table meow_mutant (
  gene_id varchar(50),
  gene_abbrev varchar(20),
  locus_id varchar(50),
  locus_name   varchar(50),
  locus_abbrev varchar(10)
) with no log;  


insert into meow_mutant (gene_id,gene_abbrev,locus_id,locus_name,locus_abbrev)
   select a.cloned_gene, b.mrkr_abbrev, a.zdb_id, a.locus_name, a.abbrev 
   from locus a, marker b
   where a.cloned_gene is not null 
   and a.cloned_gene = b.mrkr_zdb_id;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_genes_mutants.txt' 
  DELIMITER "	" 
  select * 
    from meow_mutant;


-- NOW let's create the table of pubs associated with these genes.
create temp table meow_exp2 (
  gene_id varchar(50),
  zdb_id varchar(50),
  title lvarchar,
  authors lvarchar ,
  pub_date date ,
  source lvarchar ,
  accession_no varchar(80)
) with no log;

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
create temp table meow_exp3 (
  gene_id varchar(50),
  organism varchar(30),
  ortho_name varchar(120),
  ortho_abbrev varchar(15), 
  ortho_id varchar(50)
) with no log;

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
create temp table meow_exp4 (
  gene_id varchar(50),
  DB_name varchar(50),
  acc_num varchar(50)
) with no log;

insert into meow_exp4 
  select dblink_linked_recid, fdbcont_fdb_DB_name, dblink_acc_num 
    from db_link, foreign_db_contains
   where dblink_linked_recid in 
       ( select zdb_id 
           from meow_exp1 )
     and dblink_fdbcont_zdb_id = fdbcont_zdb_id;

insert into meow_exp4 
  select  mrel_mrkr_1_zdb_id, fdbcont_fdb_DB_name, dblink_acc_num 
    from db_link , marker_relationship, foreign_db_contains
   where   mrel_mrkr_2_zdb_id = dblink_linked_recid
        and dblink_fdbcont_zdb_id = fdbcont_zdb_id
        and fdbcont_fdb_db_name = 'Genbank'
	and mrel_mrkr_1_zdb_id in 
       ( select zdb_id 
           from meow_exp1 );

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_dblinks.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp4;

--  generate the ortho_links file with DB_links to other species DBs
create temp table meow_exp5 (
  linked_recid varchar(50), 
  DB_name varchar(50), 
  acc_num varchar(50)
) with no log;

insert into meow_exp5
  select dblink_linked_recid, fdbcont_fdb_DB_name, dblink_acc_num
    from db_link, foreign_db_contains
   where dblink_linked_recid in 
       ( select ortho_id 
         from meow_exp3 )
     and dblink_fdbcont_zdb_id = fdbcont_zdb_id;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_ortholinks.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp5;


-- generate a file of cDNAs and assoc GENBANK accession numbers

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/SC.txt'
  DELIMITER "	" select distinct mrkr_zdb_id, mrkr_abbrev, dblink_acc_num  from marker, OUTER (db_link, foreign_db_contains)  where (mrkr_type = 'EST')  and dblink_linked_recid = mrkr_zdb_id and dblink_fdbcont_zdb_id = fdbcont_zdb_id and fdbcont_fdb_db_name = 'Genbank' order by 1; 

-- generate a file of anonymous markers  and assoc GENBANK accession numbers
UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/SC_sts.txt'
  DELIMITER "	" select distinct mrkr_zdb_id, mrkr_abbrev, dblink_acc_num  from marker, db_link, foreign_db_contains  where mrkr_type in ('STS', 'SSLP','RAPD') and dblink_linked_recid = mrkr_zdb_id and dblink_fdbcont_zdb_id = fdbcont_zdb_id and fdbcont_fdb_db_name = 'Genbank' order by 1; 

-- generate a file with zdb history data

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zdb_history.txt'
 DELIMITER "	" select zrepld_old_zdb_id, zrepld_new_zdb_id from zdb_replaced_data;

-- generate a file with genes and associated expression patterns

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/xpat.txt'
 DELIMITER "	"  select xpat_gene_zdb_id, xpat_zdb_id from expression_pattern;

--- generate mapping data for LocusLink
 
UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/panels.txt' 
  DELIMITER "	" select zdb_id, abbrev, metric from panels; 

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/sanger_mappings.txt' 
  DELIMITER "	" select distinct pm.target_id, pm.zdb_id, pm.abbrev, pm.OR_lg, pm.lg_location,
        case
            when target_id in('ZDB-REFCROSS-980521-11','ZDB-REFCROSS-000320-1')
                then 1
            when target_id = 'ZDB-REFCROSS-990426-6'
                then 2
            when target_id = 'ZDB-REFCROSS-990707-1' 
                and owner in ('ZDB-PERS-971016-22','ZDB-PERS-971205-2')
                then 2
            else 3
        end
        from paneled_markers pm, outer mapped_marker mm  
        where pm.zdb_id[1,8] not in ('ZDB-FISH', 'ZDB-LOCU') 
        and mm.marker_id   == pm.zdb_id
        and mm.refcross_id == pm.target_id
        and mm.or_lg       == pm.or_lg
        and mm.lg_location == pm.lg_location
        and mm.map_name    == pm.map_name
        and mm.metric      == pm.metric
        order by 1;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/mappings.txt' 
  DELIMITER "	" select distinct target_id, zdb_id, abbrev, OR_lg, lg_location from paneled_markers  where (zdb_id not like '%FISH%') and (zdb_id not like '%LOCUS%') order by 1;

-- wait to see what to do with mutants  union select distinct a.target_id, b.locus, c.abbrev, a.OR_lg, a.lg_location from paneled_markers a, fish b, locus c where a.zdb_id like '%FISH%' and a.zdb_id = b.zdb_id and b.locus = c.zdb_id

--- generate file with zmap mapping data
UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zmap_mappings.txt' 
 DELIMITER "	"  select zdb_id, abbrev, abbrevp, panel_id, or_lg, lg_location from zmap_pub_pan_mark;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/markers.txt' 
  DELIMITER "	" select distinct zdb_id, abbrev from paneled_markers;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/marker_alias.txt' 

  DELIMITER "	" select distinct mrkr_zdb_id, dalias_alias from marker , data_alias where mrkr_zdb_id = dalias_data_zdb_id order by 1;

--- generate gene marker relationship data for LocusLink

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/gene_relationships.txt'
 
  DELIMITER "	" select distinct mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id from marker_relationship, meow_exp1 where mrel_type like 'gene%' and mrel_mrkr_1_zdb_id = meow_exp1.zdb_id order by 1;


-- NOW just notify Don Gilbert that the updated files are there and he
-- can download them!


